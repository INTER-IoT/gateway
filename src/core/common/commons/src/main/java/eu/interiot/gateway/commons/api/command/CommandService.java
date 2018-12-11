/*
 * Copyright 2016-2018 Universitat Politècnica de València
 * Copyright 2016-2018 Università della Calabria
 * Copyright 2016-2018 Prodevelop, SL
 * Copyright 2016-2018 Technische Universiteit Eindhoven
 * Copyright 2016-2018 Fundación de la Comunidad Valenciana para la
 * Investigación, Promoción y Estudios Comerciales de Valenciaport
 * Copyright 2016-2018 Rinicom Ltd
 * Copyright 2016-2018 Association pour le développement de la formation
 * professionnelle dans le transport
 * Copyright 2016-2018 Noatum Ports Valenciana, S.A.U.
 * Copyright 2016-2018 XLAB razvoj programske opreme in svetovanje d.o.o.
 * Copyright 2016-2018 Systems Research Institute Polish Academy of Sciences
 * Copyright 2016-2018 Azienda Sanitaria Locale TO5
 * Copyright 2016-2018 Alessandro Bassi Consulting SARL
 * Copyright 2016-2018 Neways Technologies B.V.
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.interiot.gateway.commons.api.command;

import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import eu.interiot.gateway.commons.api.command.CommandLine.Command;

public final class CommandService {
	private final Map<Class<?>, CommandFactory<? extends ExecutableCommand>> commandFactories;
	private final Map<String, Class<? extends ExecutableCommand>> commandClasses;
	
	public CommandService() {
		this.commandFactories = new HashMap<>();
		this.commandClasses = new HashMap<>();
	}
	
	public synchronized final <T extends ExecutableCommand> void registerCommand(Class<T> clazz) {
		this.registerCommand(clazz, new DefaultFactory<T>(clazz));
	}
	
	@SuppressWarnings("unchecked")
	public synchronized final <T extends ExecutableCommand> void registerCommand(Class<T> clazz, CommandFactory<T> factory){
		Command cmd = clazz.getAnnotation(Command.class);
		if(cmd == null || cmd.name() == null) return;
		this.commandFactories.put(clazz, factory);
		this.commandClasses.put(cmd.name(), clazz);
		for(Class<?> clss : clazz.getAnnotation(Command.class).subcommands()) {
			if(clss.isAnnotationPresent(Command.class)) {
				this.registerCommand((Class<? extends ExecutableCommand>) clss);
			}
		}
	}
	
	public synchronized ExecutableCommand getExecutableCommand(String commandName, PrintWriter out) throws Exception {
		if(!commandClasses.containsKey(commandName)) return null;
		Class<?> commandClass = this.commandClasses.get(commandName);
		ExecutableCommand executableCommand = commandFactories.get(commandClass).createInstance();
		Field outField = ExecutableCommand.class.getDeclaredField("out");
		outField.setAccessible(true);
		outField.set(executableCommand, out);
		return executableCommand;
	}
	
	private synchronized ExecutableCommand getExecutableCommand(Class<?> commandClass, PrintWriter out) throws Exception {
		if(!commandFactories.containsKey(commandClass)) return null;
		ExecutableCommand executableCommand = commandFactories.get(commandClass).createInstance();
		Field outField = ExecutableCommand.class.getDeclaredField("out");
		outField.setAccessible(true);
		outField.set(executableCommand, out);
		return executableCommand;
	}

	
	public synchronized CommandLine getLinkedCommand(String commandName, PrintWriter out) throws Exception {
		ExecutableCommand executable = getExecutableCommand(commandName, out);
		if(executable == null) return null;
		CommandLine cmd = new CommandLine(executable);
		for(Class<?> clss : commandClasses.get(commandName).getAnnotation(Command.class).subcommands()) {
			if(clss.isAnnotationPresent(Command.class)) {
				String subCommand = clss.getAnnotation(Command.class).name();
				CommandLine subCmd = getLinkedCommand(clss, out);
				if(subCmd != null) cmd.addSubcommand(subCommand, subCmd);
			}
		}
		return cmd;
	}
	
	private synchronized CommandLine getLinkedCommand(Class<?> commandClass, PrintWriter out) throws Exception {
		ExecutableCommand executable = getExecutableCommand(commandClass, out);
		if(executable == null) return null;
		CommandLine cmd = new CommandLine(executable);
		for(Class<?> clss : commandClass.getAnnotation(Command.class).subcommands()) {
			if(clss.isAnnotationPresent(Command.class)) {
				String subCommand = clss.getAnnotation(Command.class).name();
				CommandLine subCmd = getLinkedCommand(clss, out);
				if(subCmd != null) cmd.addSubcommand(subCommand, subCmd);
			}
		}
		return cmd;
	}
	
	public synchronized Map<String, Class<? extends ExecutableCommand>> listCommands() {
		return this.commandClasses
			.entrySet()
			.stream()
			.collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
	}
	
	private static final class DefaultFactory<T extends ExecutableCommand> implements CommandFactory<T>{
		
		private Class<T> clazz;
		
		private DefaultFactory(Class<T> clazz) {
			this.clazz = clazz;
		}
		
		@Override
		public T createInstance() throws Exception{
			return this.clazz.newInstance();
		}
		
	}
	
	
}
