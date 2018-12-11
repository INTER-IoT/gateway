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
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.osgi.framework.Version;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

//Spec Version 0.3.0
public class Extension {
	
	public static enum Category {
		CONTROLLER, VIRTUAL, PHYSICAL, GENERIC;
	}
	
	private static Schema schema;
	static {
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Source schemaSource = new StreamSource(Extension.class.getResourceAsStream(FWConstants.GATEWAY_EXTENSION_SCHEMA));
		try {
			Extension.schema = schemaFactory.newSchema(schemaSource);
		} catch (SAXException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	private String description, name, vendor;
	
	private Category category;
	
	private URL url;
	
	private Version version;
	
	private Version specVersion;
	
	private Extension() {
		
	}

	public String getDescription() {
		return description;
	}

	public String getName() {
		return name;
	}

	public String getVendor() {
		return vendor;
	}

	public Category getCategory() {
		return category;
	}

	public URL getUrl() {
		return url;
	}

	public Version getVersion() {
		return version;
	}

	public Version getSpecVersion() {
		return specVersion;
	}
	
	public void install(Path gatewayPath, Path extensionPath) throws IOException {
		ReadableByteChannel rbc = Channels.newChannel(this.url.openStream());
		String urlString = this.url.toString();
		File extensionFile = extensionPath.resolve(urlString.substring(urlString.lastIndexOf('/')+1, urlString.length())).toFile();
		extensionFile.getParentFile().mkdirs();
		FileOutputStream fos = new FileOutputStream(extensionFile);
		System.out.print(" > DOWNLOADING ./" + extensionFile.getPath() + ".....");
		fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
		fos.close();
		System.out.println("ok");
		JarFile jarFile = new JarFile(extensionFile);
		ZipEntry deployEntry = jarFile.getEntry("deploy");
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
	}
	
	public static Extension parseExtension(InputStream extensionXml) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setNamespaceAware(true);
		factory.setSchema(Extension.schema);
		
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new InputSource(extensionXml));
		Element root = document.getDocumentElement();
		
		Extension extension = new Extension();
		
		Function<String, String> extract = new Function<String, String>() {

			@Override
			public String apply(String t) {
				return root.getElementsByTagName(t).item(0).getTextContent();
			}
			
		};
		
		extension.description = extract.apply("description");
		extension.name = extract.apply("name");
		extension.vendor = extract.apply("vendor");
		extension.category = Category.valueOf(extract.apply("category").toUpperCase());
		extension.url = new URL(extract.apply("url"));
		extension.version = Version.parseVersion(extract.apply("version"));
		extension.specVersion = Version.parseVersion(extract.apply("specVersion"));
		
		return extension;
	}
	
	
}
