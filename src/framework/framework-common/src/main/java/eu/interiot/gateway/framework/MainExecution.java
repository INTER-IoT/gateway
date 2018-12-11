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
package eu.interiot.gateway.framework;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.launch.Framework;
import org.osgi.framework.launch.FrameworkFactory;

import eu.interiot.gateway.framework.api.GatewayFrameworkService;

public class MainExecution {
	
	private static Logger log = LogManager.getLogger("Framework");
	
	public static void execute(FWCmdOptions options) throws Exception{	
		
		System.out.println(FWConstants.LOGO);
		
		//LOAD CONFIG PROPERTIES
		Path gwConfDirPath = Paths.get(Main.frameworkProperties.getProperty(FWConstants.GW_CONF_DIR_KEY));
		MainExecution.loadConfigProperties(gwConfDirPath, Main.gfs.getConfigurationProperties());
		
		//GET GATEWAY MODULE FOLDERS
		Path fragmentsFolderPath = Paths.get(Main.frameworkProperties.getProperty(FWConstants.FWProperties.Keys.FRAGMENT_FOLDER));
		Path librariesFolderPath = Paths.get(Main.frameworkProperties.getProperty(FWConstants.FWProperties.Keys.LIB_FOLDER));
		Path extensionsFolderPath = Paths.get(Main.frameworkProperties.getProperty(FWConstants.FWProperties.Keys.EXTENSION_FOLDER));
		Path coreModulesFolderPath = Paths.get(Main.frameworkProperties.getProperty(FWConstants.FWProperties.Keys.CORE_FOLDER));
		
		//CREATE GATEWAY MODULE SETS
		GatewayModuleSet fragments = new GatewayModuleSet(fragmentsFolderPath, GatewayModule.Type.FRAGMENT, Main.gfs.getConfigurationProperties());
		GatewayModuleSet libraries = new GatewayModuleSet(librariesFolderPath, GatewayModule.Type.LIB, Main.gfs.getConfigurationProperties());
		GatewayModuleSet extnsions = new GatewayModuleSet(extensionsFolderPath, GatewayModule.Type.EXTENSION, Main.gfs.getConfigurationProperties());
		GatewayModuleSet coreModls = new GatewayModuleSet(coreModulesFolderPath, GatewayModule.Type.CORE, Main.gfs.getConfigurationProperties());
		coreModls.ignoreExceptions(false);
		coreModls.setLoadOrderFromResource(FWConstants.FRAMEWORK_CORE_LOAD_FILE);
		
		//INSPECT MODULES
		Stream.of(fragments, libraries, extnsions, coreModls)
		.forEach(GatewayModuleSet::inspect);
		
		//FILTER ENABLED
		extnsions.filterEnabled();
		
		//SET ENABLED EXTENSIONS PROPERTY
		Main.gfs.getGatewayProperties().put(FWConstants.GWProperties.EXTENSIONS, 
				extnsions.listModules().stream()
				.map(GatewayModule::getName)
				.collect(Collectors.toList())
				.toArray(new String[] {})
		);
		
		//GET LIB LOAD ORDER
		List<String> libLoadOrder = Stream.of(coreModls.listModules().stream(), extnsions.listModules().stream())
		.reduce(Stream::concat)
		.orElseGet(Stream::empty)
		.map(mod -> mod.getLibLoadOrder().stream())
		.reduce(Stream::concat)
		.orElseGet(Stream::empty)
		.distinct()
		.collect(Collectors.toList());
		
		libraries.setLoadOrder(libLoadOrder);
		
		//GET EXTRA PACKAGES TO LOAD FROM JVM
		String systemExtraPackages =  Stream.of(
			FrameworkUtils.readLines(Main.class.getResourceAsStream(FWConstants.FWProperties.DEFAULT_EXTRA_PACKAGES_FILE)).stream(),
			FrameworkUtils.readLines(Main.class.getResourceAsStream(FWConstants.FWProperties.EXTRA_PACKAGES_FILE)).stream(),
			Stream.of(
				fragments.listModules().stream(),
				libraries.listModules().stream(),
				coreModls.listModules().stream(),
				extnsions.listModules().stream()
			)
			.reduce(Stream::concat)
			.orElseGet(Stream::empty)
			.map(GatewayModule::getSystemExtraPackages)
			.map(Collection::stream)
			.reduce(Stream::concat)
			.orElseGet(Stream::empty)
		)
		.reduce(Stream::concat)
		.orElseGet(Stream::empty)
		.filter(s -> !s.trim().equals(""))
		.map(s -> s.trim())
		.distinct()
		.collect(Collectors.joining(","));
		
		log.info("System Packages: " + systemExtraPackages);
		
		//CREATE FRAMEWORK CONFIGURATION
		Map<String, String> frameworkConfiguration = new HashMap<String, String>();
		frameworkConfiguration.putAll(FrameworkUtils.getSubProperties(Main.frameworkProperties, FWConstants.FWProperties.OSGI_FRAMEWORK_SUBKEY));
		frameworkConfiguration.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, systemExtraPackages);
		
		//CREATE OSGI FRAMEWORK
		FrameworkFactory frameworkFactory = ServiceLoader.load(FrameworkFactory.class).iterator().next();	
		Framework concierge = frameworkFactory.newFramework(frameworkConfiguration);

		//START OSGI FRAMEWORK
		concierge.start();
		
		BundleContext context = concierge.getBundleContext();
		
		context.registerService(GatewayFrameworkService.class, Main.gfs, null);
		
		//INSTALL MODULES AND SORT
		Stream.of(fragments, libraries, extnsions, coreModls)
		.peek(gmset -> gmset.install(context))
		.forEach(GatewayModuleSet::sortByLoadOrder);
		
		//START MODULES (EXCEPT FRAGMENTS)
		Stream.of(libraries, coreModls, extnsions)
		.forEach(GatewayModuleSet::start);
	
		//GET CORE CLASS AND RUN IT
		String coreClassName = Main.frameworkProperties.getProperty(FWConstants.FWProperties.Keys.GATEWAY_CORE_CLASS);
		Runnable core = (Runnable) context.getService(context.getServiceReference(coreClassName));
		
		core.run();
		
		try {
		    concierge.waitForStop(0);
		} finally {
		    System.exit(0);
		}
	}
	
	private static void loadConfigProperties(Path gwConfDirPath, Properties properties) {
		try {
			Files.list(gwConfDirPath)
				.sorted((p1, p2) -> p1.compareTo(p2))
				.map(path -> path.toFile())
				.forEach(file -> {
					try{
						properties.load(new FileInputStream(file));
					}catch(Exception ex) {
					}
				});
		}catch(Exception ex) {
		}
	}
	
}
