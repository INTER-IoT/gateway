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
package eu.interiot.gateway.commons.virtual.core.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.cache.Cache;

import eu.interiot.gateway.commons.api.device.DeviceDefinition;
import eu.interiot.gateway.commons.api.dispatcher.MessageHandler;
import eu.interiot.gateway.commons.api.messages.Message;
import eu.interiot.gateway.commons.api.messages.RegisterDeviceMessage;
import eu.interiot.gateway.commons.api.storage.CacheService;
import eu.interiot.gateway.commons.virtual.api.storage.VirtualStorage;

public class VirtualStorageImpl implements VirtualStorage, MessageHandler {
	
	private final CacheService cacheService;
	private final Cache<String, DeviceDefinition> deviceCache;
	
	public VirtualStorageImpl(CacheService cacheService) throws Exception {
		this.cacheService = cacheService;
		this.deviceCache = cacheService.getCache("device", String.class, DeviceDefinition.class);
	}
	
	@Override
	public DeviceDefinition getDeviceById(String id) {
		return this.deviceCache.get(id);
	}

	@Override
	public Collection<DeviceDefinition> getAllDevices() {
		Set<DeviceDefinition> deviceSet = new HashSet<>();
		this.deviceCache.forEach(e -> deviceSet.add(e.getValue()));
		return deviceSet;
	}

	@Override
	public void messageReceived(Message message) {
		switch(message.getMessageType()) {
		case RegisterDevice:
			RegisterDeviceMessage registerDeviceMessage = (RegisterDeviceMessage) message;
			DeviceDefinition device = registerDeviceMessage.getDevice();
			try {
				deviceCache.put(device.getId(), device);
			}catch(Exception ex) {
				ex.printStackTrace();
			}
			break;
		default:
			break;
		}
	}
	
}
