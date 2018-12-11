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

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import eu.interiot.gateway.framework.api.GatewayFrameworkService;
import picocli.CommandLine;

public class Main {
	
	public static Properties frameworkProperties = new Properties();
	public static GatewayFrameworkService gfs = new GatewayFrameworkService();
	
	private static DateTimeFormatter isoTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;
    private static DateTimeFormatter df = DateTimeFormatter.ofPattern(FWConstants.BUILDTIME_FORMAT);
	
	static {
		try {
			//LOAD FRAMEWORK PROPERTIES
			frameworkProperties.load(Main.class.getResourceAsStream(FWConstants.FWProperties.DEFAULT_FILE));
			frameworkProperties.load(Main.class.getResourceAsStream(FWConstants.FWProperties.FILE));
			
			//LOAD GATEWAY PROPERTIES
			Path uuidFile = Paths.get(frameworkProperties.getProperty(FWConstants.GW_UUID_FILE_KEY));
			gfs.getGatewayProperties().setProperty(FWConstants.GWProperties.UUID, Main.getUuid(uuidFile));
			gfs.getGatewayProperties().setProperty(FWConstants.GWProperties.TYPE, frameworkProperties.getProperty(FWConstants.FWProperties.Keys.GATEWAY_TYPE));
			Main.loadManifestProperties(Main.getManifest(), gfs.getGatewayProperties());
		}catch(Exception ex) {
			ex.printStackTrace();
			System.exit(-1);
		}
	}
	
	public static void main(String [] args) throws Exception{
		
		FWCmdOptions options = new FWCmdOptions();
		CommandLine.populateCommand(options, args);
		
		if(options.isInstall()) {
			InstallExecution.execute(options);
			System.exit(0);
		}
		
		MainExecution.execute(options);	
		
	}
	
	private static Manifest getManifest() throws IOException {
		Manifest manifest = null;
		Enumeration<URL> resources = Main.class.getClassLoader().getResources(FWConstants.MANIFEST_LOCATION);
		while (resources.hasMoreElements()) {
			Manifest mfest = new Manifest(resources.nextElement().openStream());
			String specVendor = mfest.getMainAttributes().getValue(FWConstants.ManifestEntries.SPECIFICATION_VENDOR);
			if(specVendor != null && specVendor.equals(FWConstants.INTER_IOT_VENDOR_ID)){
				manifest = mfest;
				break;
			}
			
		}
		return manifest;
	}
    
	private static void loadManifestProperties(Manifest manifest, Properties properties) {
		Attributes attributes = manifest.getMainAttributes();
		String buildTimeEntry = attributes.getValue(FWConstants.ManifestEntries.BUILD_TIME);
		TemporalAccessor accessor = isoTimeFormatter.parse(buildTimeEntry);
        ZonedDateTime zdt = ZonedDateTime.from(accessor);
        ZonedDateTime utctime = zdt.withZoneSameInstant(ZoneOffset.UTC);
        
        properties.setProperty(FWConstants.GWProperties.BUILD, df.format(utctime));
		properties.setProperty(FWConstants.GWProperties.VERSION, attributes.getValue(FWConstants.ManifestEntries.IMPLEMENTATION_VERSION));
		properties.setProperty(FWConstants.GWProperties.VENDOR, attributes.getValue(FWConstants.ManifestEntries.IMPLEMENTATION_VENDOR));
		properties.setProperty(FWConstants.GWProperties.SPEC_VERSION, attributes.getValue(FWConstants.ManifestEntries.SPECIFICATION_VERSION));
		properties.setProperty(FWConstants.GWProperties.SPEC_VENDOR, attributes.getValue(FWConstants.ManifestEntries.SPECIFICATION_VENDOR));
		
	}
	
	private static String getUuid(Path uuidFile) throws Exception{
		String uuid;
		if(Files.exists(uuidFile) && Files.isReadable(uuidFile)) {
			List<String> lines = FrameworkUtils.readLines(Files.newInputStream(uuidFile));
			if(lines.size() != 1) throw new Exception("Corrupted UUID File");
			uuid = lines.get(0);
		} else {
			uuid = UUID.randomUUID().toString();
			Files.createFile(uuidFile);
			if(Files.isWritable(uuidFile)) {
				PrintWriter pw = new PrintWriter(Files.newOutputStream(uuidFile));
				pw.println("#Gateway UUID File");
				pw.println(uuid);
				pw.close();
			} else throw new Exception("Cannot write UUID File");	
		}
		return uuid;
	}
}
