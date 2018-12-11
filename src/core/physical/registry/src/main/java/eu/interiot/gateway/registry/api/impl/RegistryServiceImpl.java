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
package eu.interiot.gateway.registry.api.impl;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eu.interiot.gateway.commons.api.configuration.ConfigurationService;
import eu.interiot.gateway.commons.api.device.Attribute;
import eu.interiot.gateway.commons.api.device.DeviceDefinition;
import eu.interiot.gateway.commons.api.device.DeviceIO;
import eu.interiot.gateway.commons.physical.api.dmanager.DeviceManager;
import eu.interiot.gateway.commons.physical.api.registry.RegistryService;

public class RegistryServiceImpl implements RegistryService {
	
	private DeviceManager deviceManager;

	public RegistryServiceImpl(ConfigurationService configService, DeviceManager deviceManager) throws IOException{
		this.deviceManager = deviceManager;
		
	}
		
	public void loadDeviceFromReader(Reader reader) throws Exception{	
		JsonElement parsed = new JsonParser().parse(reader);
		JsonObject jsonDef = checkRootIntegrity(parsed);
			
		String id = ensureString(jsonDef.get("id"), null);
		String type = ensureString(jsonDef.get("type"));
		String controller = ensureString(jsonDef.get("controller"));
		String description = ensureString(jsonDef.get("description"), "");
		
		Map<String, String> devConfig = getJsonConfigurationMap(jsonDef);
		
		List<DeviceIO> deviceIO = getDeviceIOs(jsonDef);
		
		DeviceDefinition device = new DeviceDefinition();
		device.setId(id);
		device.setType(type);
		device.setController(controller);
		device.setDescription(description);
		device.setConfig(devConfig);
		device.setDeviceIOs(deviceIO);
		
		this.deviceManager.addDevice(device);
	}
	
	public JsonObject checkRootIntegrity(JsonElement jsonDefElement) throws IOException {
		if(!jsonDefElement.isJsonObject()) throw new IOException("Wrong Device Definition file format");
		JsonObject jsonDef = jsonDefElement.getAsJsonObject();
		boolean memberCheck = 
			jsonDef.has("type")			&&
			jsonDef.has("controller")	&&
			jsonDef.has("device_io");
		if(!memberCheck) throw new IOException("Wrong Device Definition file format");
		return jsonDef;
	}
	
	public String ensureString(JsonElement element) throws IOException {
		if(element == null || element.isJsonNull()) throw new IOException("Wrong Device Definition file format");
		if(!element.isJsonPrimitive()) throw new IOException("Wrong Device Definition file format");
		return element.getAsString();
	}
	
	public String ensureString(JsonElement element, String def) throws IOException {
		if(element == null || element.isJsonNull()) {
			return def;
		}
		if(!element.isJsonPrimitive()) throw new IOException("Wrong Device Definition file format");
		return element.getAsString();
	}
	
	public Map<String, String> getJsonConfigurationMap(JsonObject jsonDef) throws IOException {
		Map<String, String> devConfig = new HashMap<>();
		if(!jsonDef.has("config")) return devConfig;
		JsonElement configJsonElement = jsonDef.get("config");
		if(!configJsonElement.isJsonObject()) throw new IOException("Wrong Device Definition file format");
		JsonObject jsonConfig = configJsonElement.getAsJsonObject();
		for(Entry<String, JsonElement> entry : jsonConfig.entrySet()) {
			JsonElement value = entry.getValue();
			if(value.isJsonNull() || !value.isJsonPrimitive()) throw new IOException("Wrong Device Definition file format");
			devConfig.put(entry.getKey(), value.getAsString());
		}
		return devConfig;
	}
	
	public List<DeviceIO> getDeviceIOs(JsonObject jsonDef) throws IOException{
		List<DeviceIO> devIOList = new ArrayList<>();
		JsonElement devIOJsonElement = jsonDef.get("device_io");
		if(!devIOJsonElement.isJsonArray()) throw new IOException("Wrong Device Definition file format");
		JsonArray devIOJsonArray = devIOJsonElement.getAsJsonArray();
		for(JsonElement jsonDevIOElement : devIOJsonArray) {
			JsonObject jsonDevIO = checkDevIOIntegrity(jsonDevIOElement);
			devIOList.add(getDevIO(jsonDevIO));
		}
		return devIOList;
	}
	
	public JsonObject checkDevIOIntegrity(JsonElement jsonDevIOElement) throws IOException {
		if(!jsonDevIOElement.isJsonObject()) throw new IOException("Wrong Device Definition file format");
		JsonObject jsonDevIO = jsonDevIOElement.getAsJsonObject();
		boolean memberCheck = 
			jsonDevIO.has("type")			&&
			jsonDevIO.has("attr_name")	&&
			jsonDevIO.has("attr_type");
		if(!memberCheck) throw new IOException("Wrong Device Definition file format");
		return jsonDevIO;
	}
	
	public DeviceIO getDevIO(JsonObject jsonDevIO) throws IOException {
		String typeString = ensureString(jsonDevIO.get("type"));
		String attrName = ensureString(jsonDevIO.get("attr_name"));
		String attrTypeString = ensureString(jsonDevIO.get("attr_type"));
		DeviceIO.Type type;
		Attribute.Type attrType;
		try {
			type = DeviceIO.Type.valueOf(typeString.toUpperCase());
			attrType = Attribute.Type.valueOf(attrTypeString.toUpperCase());
		}catch(IllegalArgumentException ex) {
			throw new IOException("Wrong Device Definition file format");
		}
		Map<String, String> config = getJsonConfigurationMap(jsonDevIO);
		DeviceIO deviceIO = new DeviceIO();
		deviceIO.setAttribute(new Attribute(attrName, attrType));
		deviceIO.setType(type);
		deviceIO.setConfig(config);
		return deviceIO;
	}
	
	
}
