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

public final class FWConstants {
	
	public static final String LOGO = "" 
	+ "   _____   ____  _____   _________   ________   _______                   _____            _________  " + "\n"
	+ "  |_   _| |_   \\|_   _| |  _   _  | |_   __  | |_   __ \\                 |_   _|          |  _   _  | " + "\n"
	+ "    | |     |   \\ | |   |_/ | | \\_|   | |_ \\_|   | |__) |      ______      | |     .--.   |_/ | | \\_| " + "\n"
	+ "    | |     | |\\ \\| |       | |       |  _| _    |  __ /      |______|     | |   / .'`\\ \\     | |     " + "\n"
	+ "   _| |_   _| |_\\   |_     _| |_     _| |__/ |  _| |  \\ \\_                _| |_  | \\__. |    _| |_    " + "\n"
	+ "  |_____| |_____|\\____|   |_____|   |________| |____| |___|              |_____|  '.__.'    |_____|   " + "\n"
	+ "";                          
	public static final String INTER_IOT_VENDOR_ID = "INTER-IoT";
	
	public static final class FWProperties {
		public static final String FILE = "/framework.properties";
		public static final String DEFAULT_FILE = "/framework.default.properties";
		public static final String EXTRA_PACKAGES_FILE = "/system.extra.packages";
		public static final String DEFAULT_EXTRA_PACKAGES_FILE = "/system.extra.packages.default";
		
		public static final String OSGI_FRAMEWORK_SUBKEY = "osgi.framework";
		
		public static final class Keys {
			public static final String LIB_FOLDER = "gateway.lib.bundles.path";
			public static final String CORE_FOLDER = "gateway.core.bundles.path";
			public static final String EXTENSION_FOLDER = "gateway.extension.bundles.path";
			public static final String FRAGMENT_FOLDER = "gateway.fragment.bundles.path";
			public static final String GATEWAY_CORE_CLASS = "gateway.core.class";
			public static final String GATEWAY_TYPE = "gateway.type";
			public static final String GATEWAY_EXTENSION_VERSION_RANGE = "gateway.extension.version.range";
			public static final String GATEWAY_EXTENSION_DEFAULT_REPO = "gateway.extension.default.repo";
		}
	}
	
	public static final String MANIFEST_LOCATION = "META-INF/MANIFEST.MF";
	
	public static class ManifestEntries {
		public static final String BUILD_TIME = "Build-Time";
		public static final String IMPLEMENTATION_VERSION = "Implementation-Version";
		public static final String SPECIFICATION_VERSION = "Specification-Version";
		public static final String IMPLEMENTATION_VENDOR = "Implementation-Vendor";
		public static final String SPECIFICATION_VENDOR = "Specification-Vendor";
	}
	
	
	public static final String FRAMEWORK_CORE_LOAD_FILE = "/core.load.properties";
	
	public static final String GATEWAY_EXTENSION_SCHEMA = "/extension-0.3.0.xsd";
	
	public static final String LOAD_ORDER_FILENAME = "load.properties";
	public static final String LIB_LOAD_FILENAME = "lib.load.properties";
	public static final String MODULE_PROPERTIES_FILENAME = "gateway-module.properties";
	public static final String TARGET_MODULE_KEY = "target.module";
	public static final String SYSTEM_EXTRA_PACKAGES_FILENAME = "system.extra.packages";
	public static final String GW_CONF_DIR_KEY = "gateway.configuration.dir";
	public static final String GW_UUID_FILE_KEY = "gateway.uuid.file";
	
	public static class GWProperties {
		public static final String VERSION = "version";
		public static final String SPEC_VERSION = "specVersion";
		public static final String BUILD = "build";
		public static final String UUID = "uuid";
		public static final String EXTENSIONS = "extensions";
		public static final String TYPE = "type";
		public static final String VENDOR = "vendor";
		public static final String SPEC_VENDOR = "specVendor";
	}
		
	public static final String BUILDTIME_FORMAT = "yyyyMMddHHmmss";
}
