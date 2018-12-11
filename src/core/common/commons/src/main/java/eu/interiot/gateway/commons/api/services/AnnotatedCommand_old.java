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
package eu.interiot.gateway.commons.api.services;

import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class AnnotatedCommand_old/* implements Command*/{
/*
	private String commandName;
	private Map<String, Method> methods;
	
	public AnnotatedCommand_old(){
		this.commandName = this.getClass().getAnnotation(AnnotatedCommand.Command.class).value();
		this.methods = new HashMap<>();
		for(Method method : this.getClass().getMethods()){
			method.setAccessible(true);
			if(method.isAnnotationPresent(AnnotatedCommand.SubCommand.class)){
				String subcommand = method.getAnnotation(AnnotatedCommand.SubCommand.class).value();
				if (subcommand.equals("")) subcommand = method.getName();
				methods.put(subcommand, method);
			}
				
		}
	}
	
	@Override
	public final String getCommandName() {
		return commandName;
	}

	@Override
	public void execute(String[] args, PrintWriter writer) throws Exception {
		String [] subargs = Arrays.copyOfRange(args, 1, args.length);
		methods.get(args[0]).invoke(this, subargs, writer);
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface Command{
		String value();
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	public static @interface SubCommand{
		String value() default "";
	}*/
}
