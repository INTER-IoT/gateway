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
package eu.interiot.gateway.commons.api.messages;

import java.util.Arrays;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import eu.interiot.gateway.commons.api.gateway.GatewayInfo;

@SuppressWarnings("serial")
public class GatewayInfoMessage extends Message implements GatewayInfo{
	private String UUID;
	private String [] extensions;
	private String version;
	private String specVersion;
	private String build;
	private GatewayInfo.Type type;
	private String vendor;
	private String specVendor;
	
	public GatewayInfoMessage() {
		
	}
	
	public GatewayInfoMessage(GatewayInfo info) {
		this.UUID = info.getUUID();
		this.extensions = Arrays.copyOf(info.getExtensions(), info.getExtensions().length);
		this.version = info.getVersion();
		this.build = info.getBuild();
		this.vendor = info.getVendor();
		this.specVersion = info.getSpecificationVersion();
		this.specVendor = info.getSpecificationVendor();
		this.type = info.getType();
		
	}
	
	@Override
	public String getUUID() {
		return UUID;
	}

	public void setUUID(String UUID) {
		this.UUID = UUID;
	}

	@Override
	public String[] getExtensions() {
		return extensions;
	}

	public void setExtensions(String[] extensions) {
		this.extensions = extensions;
	}
	
	@Override
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	@Override
	public String getBuild() {
		return build;
	}

	public void setBuild(String build) {
		this.build = build;
	}
	
	@Override
	public GatewayInfo.Type getType() {
		return type;
	}

	public void setType(GatewayInfo.Type type) {
		this.type = type;
	}

	@Override
	public String getVendor() {
		return vendor;
	}

	public void setVendor(String vendor) {
		this.vendor = vendor;
	}

	@Override
	public String toString() {
		StringJoiner sj = new StringJoiner("\n");
		sj.add("uuid: " + this.UUID);
		sj.add("version: " + this.version);
		sj.add("build: " + this.build);
		sj.add("vendor: " + this.vendor);
		sj.add("specification version: " + this.specVersion);
		sj.add("specification vendor: " + this.specVendor);
		sj.add("type: " + this.type);
		sj.add("extensions: " + Stream.of(this.extensions).collect(Collectors.joining(",")));
		return sj.toString();
	}

	@Override
	public String getSpecificationVersion() {
		return specVersion;
	}
	
	public void setSpecificationVersion(String specVersion) {
		this.specVersion = specVersion;
	}

	@Override
	public String getSpecificationVendor() {
		return specVendor;
	}	
	
	public void setSpecificationVendor(String specVendor) {
		this.specVendor = specVendor;
	}
}
