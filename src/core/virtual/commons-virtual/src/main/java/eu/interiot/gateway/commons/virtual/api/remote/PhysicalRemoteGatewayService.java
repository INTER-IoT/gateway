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
package eu.interiot.gateway.commons.virtual.api.remote;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import eu.interiot.gateway.commons.api.device.Action;
import eu.interiot.gateway.commons.api.device.DeviceDefinition;
import eu.interiot.gateway.commons.api.device.Measurement;
import eu.interiot.gateway.commons.api.gateway.RemoteGatewayService;
import eu.interiot.gateway.commons.virtual.Activator;

public interface PhysicalRemoteGatewayService extends RemoteGatewayService{
	public static PhysicalRemoteGatewayService instance() {
		return Activator.remoteGatewayInstance;
	}
	
	public boolean isConnected() throws Exception;
	public Collection<DeviceDefinition> getDeviceList() throws Exception;
	public DeviceDefinition getDevice(String devUid) throws Exception;
	public String getDeviceState(String devUid) throws Exception;
	public Measurement readDevice(String devUid) throws Exception;
	public Measurement writeDevice(String devUid, Action action) throws Exception;
	public Map<String, Serializable> startDevice(String devUid) throws Exception;
	public Map<String, Serializable> stopDevice(String devUid) throws Exception;
}
