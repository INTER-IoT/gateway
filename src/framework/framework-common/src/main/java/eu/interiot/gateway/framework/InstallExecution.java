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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipFile;

public class InstallExecution {
		
	public static void execute(FWCmdOptions options) throws Exception{	
		//VersionRange validExtensionSpecVersionRange = new VersionRange(Main.frameworkProperties.getProperty(FWConstants.FWProperties.Keys.GATEWAY_EXTENSION_VERSION_RANGE));
		//for(String url : options.getArgs()) InstallExecution.install(url, validExtensionSpecVersionRange);
		for(String filename : options.getArgs()) InstallExecution.install(Paths.get(filename), Paths.get("."));
	}

	/*private static void install(String url, VersionRange validExtensionSpecVersionRange) throws Exception{
		System.out.println(String.format("\nINSTALLING %s", url));
		System.out.println("----------\n");
		URL extensionUrl = null;
		try {
			extensionUrl = new URL(url);
		}catch(MalformedURLException ex) {
			extensionUrl = new URL(Main.frameworkProperties.getProperty(FWConstants.FWProperties.Keys.GATEWAY_EXTENSION_DEFAULT_REPO) + url + ".xml");
		}
		Extension extension = Extension.parseExtension(extensionUrl.openStream());
		if(!validExtensionSpecVersionRange.includes(extension.getSpecVersion())) throw new Exception("Invalid spec version. Valid versions are within range: " + validExtensionSpecVersionRange.toString());
		extension.install(Paths.get("."), Paths.get("extensions"));
		System.out.println(String.format("\nExtension %s successfully installed\n", extension.getName()));
	}*/
	
	private static void install(Path filePath, Path gatewayPath) throws Exception{
		//ZipInputStream zis = new ZipInputStream(new FileInputStream(filePath.toFile()));
		ZipFile zipFile = new ZipFile(filePath.toString());
		zipFile.stream().filter(ze -> !ze.isDirectory()).forEach(ze -> {
			try {
				ReadableByteChannel rbc = Channels.newChannel(zipFile.getInputStream(ze));
				File decompressedFile = gatewayPath.resolve(Paths.get(ze.getName())).toFile();
				decompressedFile.getParentFile().mkdirs();
				FileOutputStream fos2 = new FileOutputStream(decompressedFile);
				System.out.print(" > DECOMPRESSING " + decompressedFile.getPath() + ".....");
				fos2.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
				fos2.close();
				System.out.println("ok");
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		zipFile.close();
	}
	
	/*private static void install(URL url, Path gatewayPath, Path installationPath) throws IOException {
		ReadableByteChannel rbc = Channels.newChannel(url.openStream());
		String urlString = url.toString();
		File extensionFile = installationPath.resolve(urlString.substring(urlString.lastIndexOf('/')+1, urlString.length())).toFile();
		extensionFile.getParentFile().mkdirs();
		FileOutputStream fos = new FileOutputStream(extensionFile);
		System.out.print(" > DOWNLOADING ./" + extensionFile.getPath() + ".....");
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		fos.close();
		System.out.println("ok");
		JarFile jarFile = new JarFile(extensionFile);
		ZipEntry deployEntry = jarFile.getEntry("deploy/");
		if(deployEntry == null) {
			jarFile.close();
			return;
		};
		Path deployPath = Paths.get(deployEntry.getName());
		if(deployEntry.isDirectory()) {
			jarFile.stream().filter(ze -> !ze.isDirectory()).filter(ze -> Paths.get(ze.getName()).startsWith(deployPath)).forEach(ze -> {
				try {
					ReadableByteChannel rbc2 = Channels.newChannel(jarFile.getInputStream(ze));
					File decompressedFile = gatewayPath.resolve(deployPath.relativize(Paths.get(ze.getName()))).toFile();
					decompressedFile.getParentFile().mkdirs();
					FileOutputStream fos2 = new FileOutputStream(decompressedFile);
					System.out.print(" > DECOMPRESSING " + decompressedFile.getPath() + ".....");
					fos2.getChannel().transferFrom(rbc2, 0, Long.MAX_VALUE);
					fos2.close();
					System.out.println("ok");
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		}
		jarFile.close();
	}*/
	
	
}
