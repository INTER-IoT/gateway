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

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.osgi.framework.BundleContext;

public class GatewayModuleSet {
	
	private static Logger log = LogManager.getLogger("Framework");
	
	private List<String> loadOrder;
	private Path dir;
	private boolean ignoreExceptions;
	private List<GatewayModule> moduleList;
	private GatewayModule.Type moduleType;
	private Map<String, String> setSubProperties; 
	
	public GatewayModuleSet(Path dir, GatewayModule.Type type, Properties properties) {
		this.moduleType = type;
		this.loadOrder = null;
		this.dir = dir;
		this.setSubProperties = FrameworkUtils.getSubProperties(properties, type.toString().toLowerCase());
		this.ignoreExceptions = true;
		this.moduleList = getModules();
		this.loadOrder = null;
		File loadPropsFile = dir.resolve(FWConstants.LOAD_ORDER_FILENAME).toFile();
		if(loadPropsFile.exists()) try{
			this.loadOrder = FrameworkUtils.getLoadOrderFromStream(new FileInputStream(loadPropsFile));
		}catch(Exception ex) {}
		
	}
	
	private List<GatewayModule> getModules() {
		try {
			if(!Files.exists(this.dir) || !Files.isDirectory(this.dir)) throw new Exception();
			DirectoryStream<Path> dstream = Files.newDirectoryStream(this.dir, GatewayModule.fileFilter);
			return StreamSupport.stream(dstream.spliterator(), false).map(path -> new GatewayModule(path, moduleType)).collect(Collectors.toList());
		}catch(Exception ex) {
			log.warn(dir.toString()+" is not a valid directory, modules won't be loaded");
			return new ArrayList<GatewayModule>();
		}
	}
	
	public List<GatewayModule> listModules() {
		return this.moduleList;
	}
	
	public void inspect() {
		if(ignoreExceptions) this.moduleList = this.moduleList.stream()
			.filter(module -> FrameworkUtils.supplierExceptionFilter(module::inspectFile))
			.collect(Collectors.toList());
		else this.moduleList.stream().forEach(GatewayModule::inspectFile);
	}
	
	public void filterEnabled() { 
		this.moduleList = this.moduleList.stream() 
			.filter(module -> Boolean.valueOf(setSubProperties.get(module.getName()+".enabled"))) 
			.collect(Collectors.toList()); 
	 } 
	
	public void setLoadOrder(List<String> loadOrder) {
		this.loadOrder = loadOrder;
	}
	
	public void setLoadOrderFromResource(String resourcePath) {
		this.loadOrder = FrameworkUtils.getLoadOrderFromStream(this.getClass().getResourceAsStream(resourcePath));
	}
	
	public void sortByLoadOrder() {
		this.moduleList = this.moduleList.stream()
			.peek(GatewayModule.runLevelConsumer(this.loadOrder))
			.sorted(GatewayModule.runLevelComparator)
			.collect(Collectors.toList());
	}
	
	public void install(BundleContext context) {
		if(ignoreExceptions) this.moduleList = this.moduleList.stream()
			.filter(module -> FrameworkUtils.consumerExceptionFilter(module::install, context))
			.collect(Collectors.toList());
		else this.moduleList.stream().forEach(module -> module.install(context));
	}
	
	public void start() {
		if(ignoreExceptions) this.moduleList.stream().forEach(FrameworkUtils.safeConsumer(GatewayModule::start));
		else this.moduleList.stream().forEach(GatewayModule::start);
	}
	
	public void ignoreExceptions(boolean ignoreExceptions) {
		this.ignoreExceptions = ignoreExceptions;
	}
}
