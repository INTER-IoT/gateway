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

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import eu.interiot.gateway.commons.api.connector.PersistentConnector;
import eu.interiot.gateway.commons.api.connector.RemoteConnector;
import eu.interiot.gateway.commons.api.device.Action;
import eu.interiot.gateway.commons.api.device.DeviceDefinition;
import eu.interiot.gateway.commons.api.device.Measurement;
import eu.interiot.gateway.commons.api.gateway.DefaultRemoteGatewayService;
import eu.interiot.gateway.commons.api.gateway.GatewayInfo;
import eu.interiot.gateway.commons.api.messages.ActionMessage;
import eu.interiot.gateway.commons.api.messages.DeviceListMessage;
import eu.interiot.gateway.commons.api.messages.DeviceRequestMessage;
import eu.interiot.gateway.commons.api.messages.ErrorMessage;
import eu.interiot.gateway.commons.api.messages.GenericMessage;
import eu.interiot.gateway.commons.api.messages.KVPMessage;
import eu.interiot.gateway.commons.api.messages.MeasurementMessage;
import eu.interiot.gateway.commons.api.messages.Message;
import eu.interiot.gateway.commons.virtual.api.remote.PhysicalRemoteGatewayService;

public class PhysicalRemoteGatewayServiceImpl extends DefaultRemoteGatewayService implements PhysicalRemoteGatewayService{

	public PhysicalRemoteGatewayServiceImpl() {
		super();
	}
	
	public PhysicalRemoteGatewayServiceImpl(GatewayInfo remoteInfo, RemoteConnector connector) {
		super(remoteInfo, connector);
	}

	@Override
	public Collection<DeviceDefinition> getDeviceList() throws Exception {
		DeviceRequestMessage deviceRequestMessage = new DeviceRequestMessage();
		deviceRequestMessage.setRequest("list-devices");
		Message response = this.getConnector().sendSync(deviceRequestMessage).await();
		errorCheck(response);
		return ((DeviceListMessage) response).getDevices();
	}

	@Override
	public DeviceDefinition getDevice(String devUid) throws Exception {
		DeviceRequestMessage deviceRequestMessage = new DeviceRequestMessage();
		deviceRequestMessage.setRequest("get-device");
		deviceRequestMessage.setParameter("deviceUid", devUid);
		Message response = this.getConnector().sendSync(deviceRequestMessage).await();
		errorCheck(response);
		return ((DeviceListMessage) response).getDevices().toArray(new DeviceDefinition[] {})[0];
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getDeviceState(String devUid) throws Exception {
		DeviceRequestMessage deviceRequestMessage = new DeviceRequestMessage();
		deviceRequestMessage.setRequest("device-state");
		deviceRequestMessage.setParameter("deviceUid", devUid);
		Message response = this.getConnector().sendSync(deviceRequestMessage).await();
		errorCheck(response);
		return ((GenericMessage<String>) response).getEntity(); 
	}

	@Override
	public Measurement readDevice(String devUid) throws Exception {
		DeviceRequestMessage deviceRequestMessage = new DeviceRequestMessage();
		deviceRequestMessage.setRequest("read-device");
		deviceRequestMessage.setParameter("deviceUid", devUid);
		Message response = this.getConnector().sendSync(deviceRequestMessage).await();
		errorCheck(response);
		return ((MeasurementMessage) response).getMeasurement(); 
	}

	@Override
	public Measurement writeDevice(String devUid, Action action) throws Exception {
		ActionMessage actionMessage = new ActionMessage(devUid, action);
		Message response = this.getConnector().sendSync(actionMessage).await();
		errorCheck(response);
		return ((MeasurementMessage) response).getMeasurement(); 
	}

	@Override
	public Map<String, Serializable> startDevice(String devUid) throws Exception {
		DeviceRequestMessage deviceRequestMessage = new DeviceRequestMessage();
		deviceRequestMessage.setRequest("device-start");
		deviceRequestMessage.setParameter("deviceUid", devUid);
		Message response = this.getConnector().sendSync(deviceRequestMessage).await();
		return ((KVPMessage) response).getKVP();
	}

	@Override
	public Map<String, Serializable> stopDevice(String devUid) throws Exception {
		DeviceRequestMessage deviceRequestMessage = new DeviceRequestMessage();
		deviceRequestMessage.setRequest("device-stop");
		deviceRequestMessage.setParameter("deviceUid", devUid);
		Message response = this.getConnector().sendSync(deviceRequestMessage).await();
		errorCheck(response);
		return ((KVPMessage) response).getKVP();
	}
	
	private static void errorCheck(Message response) throws Exception {
		if (response instanceof ErrorMessage) {
			ErrorMessage errorMessage = (ErrorMessage) response;
			Throwable t = new Throwable(errorMessage.getError());
			t.setStackTrace(errorMessage.getStackTrace());
			throw new Exception(t);
		}
	}

	@Override
	public boolean isConnected() throws Exception {
		if(this.getConnector() instanceof PersistentConnector) {
			PersistentConnector persistentConnector = (PersistentConnector) this.getConnector();
			return persistentConnector.isConnected();
		}
		throw new Exception("Persistent connector is not implemented");
	}
}
