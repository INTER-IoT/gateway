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
package eu.interiot.gateway.commons.api.device;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class DeviceDefinition implements Serializable{
	private String id;
	private String type;
	private String description;
	private Map<String, DeviceIO> deviceIOs; 
	private String controller;
	private Map<String, String> config;
	public DeviceDefinition () {
		this.deviceIOs = new HashMap<>();
		this.config = new HashMap<>();
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	public void addDeviceIO(DeviceIO deviceIO){
		this.deviceIOs.put(deviceIO.getAttribute().getName(), deviceIO);
	}

	public Collection<DeviceIO> getDeviceIOs() {
		return Collections.unmodifiableCollection(deviceIOs.values());
	}
	
	public void setDeviceIOs(Collection<DeviceIO> deviceIOs){
		this.deviceIOs = new HashMap<>();
		for(DeviceIO deviceIO : deviceIOs) this.addDeviceIO(deviceIO);
	}

	public DeviceIO getDeviceIOByAttributeName(String attributeName){
		return this.deviceIOs.get(attributeName);
	}

	public String getController() {
		return controller;
	}

	public void setController(String controller) {
		this.controller = controller;
	}

	public Map<String, String> getConfig() {
		return Collections.unmodifiableMap(this.config);
	}
	
	public void setConfig(Map<String, String> config){
		this.config = new HashMap<>();
		config.forEach((k, v) -> this.config.put(k, v));
	}
	
}
