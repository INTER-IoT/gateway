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
package eu.interiot.gateway.dmanager.api.impl;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.interiot.gateway.commons.api.connector.RemoteConnector;
import eu.interiot.gateway.commons.api.device.Action;
import eu.interiot.gateway.commons.api.device.Action.ActionData;
import eu.interiot.gateway.commons.api.device.DeviceDefinition;
import eu.interiot.gateway.commons.api.device.Measurement;
import eu.interiot.gateway.commons.api.device.Measurement.MeasurementData;
import eu.interiot.gateway.commons.api.dispatcher.MessageHandler;
import eu.interiot.gateway.commons.api.messages.ActionMessage;
import eu.interiot.gateway.commons.api.messages.DeviceListMessage;
import eu.interiot.gateway.commons.api.messages.DeviceRequestMessage;
import eu.interiot.gateway.commons.api.messages.ErrorMessage;
import eu.interiot.gateway.commons.api.messages.GenericMessage;
import eu.interiot.gateway.commons.api.messages.KVPMessage;
import eu.interiot.gateway.commons.api.messages.MeasurementMessage;
import eu.interiot.gateway.commons.api.messages.Message;
import eu.interiot.gateway.commons.physical.api.dmanager.Device;
import eu.interiot.gateway.commons.physical.api.dmanager.Device.State;
import eu.interiot.gateway.commons.physical.api.dmanager.DeviceManager;

public class DeviceRequestEventHandler implements MessageHandler{
	
	private static Logger log = LogManager.getLogger(DeviceRequestEventHandler.class);
	
	private final RemoteConnector connector;
	private final DeviceManager devManager;
	
	public DeviceRequestEventHandler(DeviceManager devManager, RemoteConnector connector) {
		this.connector = connector;
		this.devManager = devManager;
	}
	
	@Override
	public void messageReceived(Message message) {
		switch(message.getMessageType()) {
		case DeviceRequest: this.handleDeviceRequestMessage((DeviceRequestMessage) message); break;
		case Action: this.handleActionMessage((ActionMessage) message); break;
		default:
			break;
		}
	}
	
	public void handleActionMessage(ActionMessage message) {
		Message response = null;
		try {
			String deviceId = message.getDeviceId();
			Action action = message.getAction();
			Device device = devManager.getDevice(deviceId);
			device.pushAction(action);
			Measurement measurement = device.getLastMeasurement();
			for(MeasurementData data : measurement.getData()) {
				Optional<ActionData> opt = action.getData().stream().filter(d -> d.getAttribute().getName().equals(data.getAttribute().getName())).findFirst();
				if(opt.isPresent()) {
					measurement.setValue(data.getAttribute(), opt.get().getValue());
				}
			}
			response = new MeasurementMessage(deviceId, measurement);
		}catch(Exception ex) {
			response = new ErrorMessage(ex);
		}
		if(message.awaitsResponse()) response.stampAsResponseOf(message);
		try {
			connector.send(response);
		} catch (Exception e) {
			log.error(e);
		}
	}
	public void handleDeviceRequestMessage(DeviceRequestMessage message) {
		Message response;
		try {
			switch(message.getRequest()) {
				case "list-devices": {
					response = new DeviceListMessage(devManager.getDevices().stream().map(device -> device.getDeviceDefinition()).collect(Collectors.toSet()));
					break;
				}
				case "get-device": {
					String deviceUid = (String) message.getParameter("deviceUid");
					Device device = devManager.getDevice(deviceUid);
					if (device == null) response = new ErrorMessage(new Exception("Device not found"));
					else response = new DeviceListMessage(Arrays.asList(new DeviceDefinition[] {device.getDeviceDefinition()}));
					break;
				}
				case "device-state": {
					String deviceUid = (String) message.getParameter("deviceUid");
					Device device = devManager.getDevice(deviceUid);
					response = new GenericMessage<State>(device.getState());
					break;
				}
				case "device-start": {
					String deviceUid = (String) message.getParameter("deviceUid");
					Device device = devManager.getDevice(deviceUid);
					device.connect();
					State state = device.getState();
					boolean successful = state == State.CONNECTED;
					response = new KVPMessage("success", successful, "state", state);
					break;
				}
				case "device-stop":{
					String deviceUid = (String) message.getParameter("deviceUid");
					Device device = devManager.getDevice(deviceUid);
					device.disconnect();
					State state = device.getState();
					boolean successful = state != State.CONNECTED;
					response = new KVPMessage("success", successful, "state", state);
					break;
				}
				case "read-device": {
					String deviceUid = (String) message.getParameter("deviceUid");
					Device device = devManager.getDevice(deviceUid);
					response = new MeasurementMessage(deviceUid, device.getLastMeasurement());
					break;
				}
				default: {
					response = new ErrorMessage(new Exception("Request: " + message.getRequest()+" not found."));
				}
			}
		}catch(Exception ex) {
			response = new ErrorMessage(ex);
		}
		response.stampAsResponseOf(message);
		try {
			connector.send(response);
		} catch (Exception e) {
			log.error(e);
		}
	}
}
