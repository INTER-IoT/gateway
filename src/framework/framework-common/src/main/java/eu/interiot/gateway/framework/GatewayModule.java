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
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

public class GatewayModule {
	
	private static Logger log = LogManager.getLogger("Framework");
	
	public static enum Type {
		FRAGMENT, CORE, EXTENSION, LIB;
	}
	
	public static DirectoryStream.Filter<Path> fileFilter = new DirectoryStream.Filter<Path>() {
	    public boolean accept(Path entry) throws IOException {
	       return !Files.isDirectory(entry) && entry.toString().toLowerCase().endsWith(".jar");
	    }
	};
	
	public static Comparator<GatewayModule> runLevelComparator = new Comparator<GatewayModule>() {
		@Override
		public int compare(GatewayModule m1, GatewayModule m2) {
			int r1 = m1.runLevel == -1 ? Integer.MAX_VALUE : m1.runLevel;
			int r2 = m2.runLevel == -1 ? Integer.MAX_VALUE : m2.runLevel;
			return Integer.compare(r1, r2);
		}
	};
	
	private Collection<String> systemExtraPackages;
	private List<String> libLoadOrder;
	private Properties moduleProperties;
	private int runLevel;
	private String name;
	private Bundle bundle;
	private final Path filePath;
	private File file;
	private final Type moduleType;
	private boolean started;
	
	public GatewayModule(Path filePath, Type moduleType) {
		this(filePath, moduleType, -1);
	}
	
	public GatewayModule(Path filePath, Type moduleType, int runLevel) {
		this.moduleType = moduleType;
		this.runLevel = runLevel;
		this.filePath = filePath;
		this.started = false;
		this.moduleProperties = new Properties();
	}
	
	public GatewayModule inspectFile() throws ModuleException {
		try {
			this.file = filePath.toFile();
			JarFile jarFile = new JarFile(this.file);
			this.name = jarFile.getManifest().getMainAttributes().getValue("Bundle-SymbolicName"); 
			if(this.name == null) { 
				jarFile.close();
			    throw new Exception("Invalid bundle manifest entries"); 
			 } 
			ZipEntry sepEntry = jarFile.getEntry(FWConstants.SYSTEM_EXTRA_PACKAGES_FILENAME);
			if(sepEntry != null && !sepEntry.isDirectory()) this.systemExtraPackages = FrameworkUtils.readLines(jarFile.getInputStream(sepEntry));
			else this.systemExtraPackages = new ArrayList<>();
			ZipEntry libOrderEntry = jarFile.getEntry(FWConstants.LIB_LOAD_FILENAME);
			if(libOrderEntry != null && !libOrderEntry.isDirectory()) this.libLoadOrder = FrameworkUtils.getLoadOrderFromStream(jarFile.getInputStream(libOrderEntry));
			else this.libLoadOrder = new ArrayList<>();
			ZipEntry modulePropEntry = jarFile.getEntry(FWConstants.MODULE_PROPERTIES_FILENAME);
			if(modulePropEntry != null && !modulePropEntry.isDirectory()) this.moduleProperties.load(jarFile.getInputStream(modulePropEntry));
			jarFile.close();
			log.info("Inspecting file: " + this.moduleType.name().toLowerCase() + "/" + this.filePath.getFileName().toString() + "... ok");
			return this;
		} catch (Exception e) {
			log.warn("Inspecting file: " + this.moduleType.name().toLowerCase() + "/" + this.filePath.getFileName().toString() + "... fail");
			throw ModuleException.fileNotValid(this, e);
		}
		
	}
	
	public void install(BundleContext context) throws ModuleException {
		try {
			FileInputStream fis = new FileInputStream(this.file);
			this.bundle = context.installBundle(this.filePath.toUri().toString(), fis);
			fis.close();
			if(this.bundle == null) throw new Exception();
			if(this.name == null) {
				this.bundle.uninstall();
				this.bundle = null;
				throw new Exception();
			}
			log.info("Installing file: " + this.moduleType.name().toLowerCase() + "/" + this.filePath.getFileName().toString() + "... ok");
		} catch (Exception e) {
			log.warn("Installing file: " + this.moduleType.name().toLowerCase() + "/" + this.filePath.getFileName().toString() + "... fail");
			throw ModuleException.installFailed(this, e);
		}
	}
	
	public void start() throws ModuleException {
		try {
			if(this.bundle == null) throw new Exception();
			this.bundle.start();
			log.info("Starting module: " + this.moduleType.name().toLowerCase() + "/" + this.name + "... ok");
			this.started = true;
		} catch (Exception e) {
			e.printStackTrace();
			log.warn("Starting module: " + this.moduleType.name().toLowerCase() + "/" + this.name + "... fail");
			throw ModuleException.startFailed(this, e);
		}
	}
	
	public void setRunLevel(int runLevel) {
		this.runLevel = runLevel;
	}
	
	public int getRunLevel() {
		return this.runLevel;
	}
	
	public Type getType() {
		return this.moduleType;
	}
	
	public Bundle getBundle() {
		return bundle;
	}
	
	public String getName(){
		return name;
	}
	
	public boolean isStarted() {
		return started;
	}
	
	public Collection<String> getSystemExtraPackages() {
		return this.systemExtraPackages;
	}
	
	public List<String> getLibLoadOrder() {
		return this.libLoadOrder;
	}
	
	public static Consumer<GatewayModule> runLevelConsumer(List<String> loadProperties) {
		return new Consumer<GatewayModule>() {
			public void accept(GatewayModule module) {
				String targetModule = module.getName();
				if(module.moduleProperties.containsKey(FWConstants.TARGET_MODULE_KEY))
					targetModule = module.moduleProperties.getProperty(FWConstants.TARGET_MODULE_KEY);
				if(loadProperties != null && loadProperties.contains(targetModule))
					module.runLevel = loadProperties.indexOf(targetModule);
			};
		};
	}
	
	public static Consumer<GatewayModule> runLevelConsumer2(Map<String, Integer> loadProperties) {
		return new Consumer<GatewayModule>() {
			public void accept(GatewayModule module) {
				if(loadProperties != null && loadProperties.containsKey(module.getName()))
					module.runLevel = loadProperties.get(module.getName());
			};
		};
	}
	
	@SuppressWarnings("serial")
	public static class ModuleException extends RuntimeException {
		
		private final GatewayModule module;
		
		private ModuleException(GatewayModule module, String message, Throwable cause) {
			super(message, cause);
			this.module = module;
		}
		
		public GatewayModule getGatewayModule() {
			return module;
		}
		
		public static ModuleException fileNotValid(GatewayModule module, Throwable cause) {
			return new ModuleException(module, "Invalid file: " + module.moduleType.name().toLowerCase() + "/" + module.filePath.getFileName().toString(), cause);
		}
		
		public static ModuleException installFailed(GatewayModule module, Throwable cause) {
			return new ModuleException(module, "Failed to install: " + module.moduleType.name().toLowerCase() + "/" + module.filePath.getFileName().toString(), cause);
		}
		
		public static ModuleException startFailed(GatewayModule module, Throwable cause) {
			return new ModuleException(module, "Failed to start: " + module.moduleType.name().toLowerCase() + "/" + module.name, cause);
		}
		
	}
	
}