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
package eu.interiot.gateway.commons.api.core;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import eu.interiot.gateway.commons.api.Utils;
import eu.interiot.gateway.commons.api.command.CommandFactory;
import eu.interiot.gateway.commons.api.command.CommandLine;
import eu.interiot.gateway.commons.api.command.CommandLine.Command;
import eu.interiot.gateway.commons.api.command.CommandLine.Option;
import eu.interiot.gateway.commons.api.command.CommandLine.Parameters;
import eu.interiot.gateway.commons.api.command.CommandService;
import eu.interiot.gateway.commons.api.command.ExecutableCommand;
import eu.interiot.gateway.commons.api.services.CoreService;

public abstract class Core implements Runnable, ServiceListener{
	
	private static Logger log = LogManager.getLogger("Core");
	
	protected final BundleContext context;
	private final Map<String, Thread> runnableServices;
	
	public Core(BundleContext context) throws Exception{
		this.context = context;
		this.context.addServiceListener(this, "(objectClass=eu.interiot.gateway.commons.api.services.CoreService)");
		this.runnableServices = new HashMap<>();
		
		CommandService commandService = this.context.getService(this.context.getServiceReference(CommandService.class));
		commandService.registerCommand(ExitCommand.class, new CommandFactory<ExitCommand>() {

			@Override
			public ExitCommand createInstance() throws Exception {
				return new ExitCommand(Core.this);
			}
			
		});
		
		commandService.registerCommand(ThreadCommand.class, new CommandFactory<ThreadCommand>() {

			@Override
			public ThreadCommand createInstance() throws Exception {
				return new ThreadCommand(Core.this);
			}
			
		});
		
		commandService.registerCommand(CheckSum.class);
		
		ServiceReference<?> [] serviceReferences = this.context.getAllServiceReferences(CoreService.class.getName(), null);
		if(serviceReferences != null) Stream.of(serviceReferences)
		.forEach(serviceReference -> {
			CoreService coreService = (CoreService) context.getService(serviceReference);
			this.registerService(serviceReference.getBundle().getSymbolicName() + ":" + coreService.getClass().getName(), coreService);
		});
	}
	
	@Override
	public final void run(){
		try{
			resolveComponents(context);
			this.start();
			this.runnableServices.entrySet().forEach(e -> {
				log.info("Starting " + e.getKey());
				e.getValue().start(); //TODO: PROVISIONAL, NOT SAFE
			});
			
		}catch(Exception ex){
			log.error(ex);
			ex.printStackTrace();
			terminate(ex.getMessage());
		}
	}
	
	protected abstract void start() throws Exception;
	protected abstract void finish() throws Exception;
	protected abstract void resolveComponents(BundleContext context) throws Exception;
	
	
	public void terminate(String reason) {
		try{
			this.finish();
		}catch(Exception ex){
			log.error(ex);
			System.exit(-1);
		}
		if (reason == null) System.exit(0);
		else System.err.println("Terminating..." + reason);
		System.exit(-1);
	}
	
	@Override
	public void serviceChanged(ServiceEvent event) {
		switch(event.getType()){
		case ServiceEvent.REGISTERED: 
			ServiceReference<?> serviceReference = event.getServiceReference();
			CoreService coreService = (CoreService) context.getService(serviceReference);
			this.registerService(serviceReference.getBundle().getSymbolicName() + ":" + coreService.getClass().getName(), coreService);
			break;
		}
	}
	
	public void registerService(String name, CoreService coreService){
		log.info("Registering Core Service : " + name);
		this.runnableServices.put(name, new Thread(coreService));
	}
	
	@CommandLine.Command(name = "exit", description = "Gracefully terminate the gateway")
	private static class ExitCommand extends ExecutableCommand{
		
		private final Core core;
		
		@Option(names = {"-h", "--help"}, usageHelp = true, description = "Show this help message and exit.")
	    private boolean helpRequested;
		
		private ExitCommand(Core core) {
			this.core = core;
		}
		
		@Override
		public void execute(PrintWriter out) throws Exception {
			out.println("Terminating...");
			out.flush();
			core.terminate(null);
		}
	}
	
	@Command(name="threads", description="Manage gateway process threads.")
	private static class ThreadCommand extends ExecutableCommand {
		
		private final Core core;
		
		@Option(names= {"-l", "--list"}, description="Show a list of active Core threads")
		private boolean showThreads;
		
		@Option(names = {"-h", "--help"}, usageHelp = true, description = "Show this help message and exit.")
	    private boolean helpRequested;
		
		private ThreadCommand(Core core) {
			this.core = core;
		}

		@Override
		public void execute(PrintWriter out) throws Exception {
			if(showThreads) {
				StringJoiner sj = new StringJoiner("\n");
				for(String name : core.runnableServices.keySet()){
					Thread thread = core.runnableServices.get(name);
					sj.add("\t" + name + "\t | " + thread.getName()+"("+thread.getId()+")" + "\t | " + thread.getState());
				}
				out.println("\n" + sj.toString());
				out.flush();
			}
		}
	}
	
	@Command(name = "checksum", description = "Prints the checksum (MD5 by default) of a file to STDOUT.")
	public static class CheckSum extends ExecutableCommand {

	    @Parameters(index = "0", description = "The file whose checksum to calculate.")
	    private File file;

	    @Option(names = {"-a", "--algorithm"}, description = "MD5, SHA-1, SHA-256, ...")
	    private String algorithm = "MD5";

	    @Option(names = {"-h", "--help"}, usageHelp = true, description = "Show this help message and exit.")
	    private boolean helpRequested;

		@Override
		public void execute(PrintWriter out) throws Exception{
			// your business logic goes here...
	        byte[] fileContents = Files.readAllBytes(file.toPath());
	        byte[] digest = MessageDigest.getInstance(algorithm).digest(fileContents);
	        out.println(Utils.Converter.bytesToHex(digest));
		}
	}
	
}
