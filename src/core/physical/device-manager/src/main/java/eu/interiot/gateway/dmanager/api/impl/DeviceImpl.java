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

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.interiot.gateway.commons.api.connector.RemoteConnector;
import eu.interiot.gateway.commons.api.device.Action;
import eu.interiot.gateway.commons.api.device.Attribute;
import eu.interiot.gateway.commons.api.device.DeviceDefinition;
import eu.interiot.gateway.commons.api.device.DeviceIO;
import eu.interiot.gateway.commons.api.device.Measurement;
import eu.interiot.gateway.commons.api.messages.MeasurementMessage;
import eu.interiot.gateway.commons.physical.api.dmanager.Device;
import eu.interiot.gateway.commons.physical.api.dmanager.DeviceController;
import eu.interiot.gateway.commons.physical.api.dmanager.DeviceControllerFactory;
import eu.interiot.gateway.commons.physical.api.dmanager.DeviceState;

public class DeviceImpl implements Device, Runnable {
	
	private static Logger log = LogManager.getLogger(Device.class);
	
	private final RemoteConnector connector;
	private final ScheduledExecutorService scheduledExecutorService;

	private final DeviceStateImpl deviceState;
	private final DeviceDefinition deviceDef;
	private final Collection<Attribute> validAttributes;
	private final Collection<Attribute> validActuatorAttributes;
	private final DeviceController  controller;
	
	private ScheduledFuture<?> runningTask;
	private final long rate;
	private final TimeUnit rateUnit;
	
	public DeviceImpl(DeviceDefinition deviceDef, DeviceControllerFactory controllerFactory, RemoteConnector connector, ScheduledExecutorService scheduledExecutorService) throws Exception {
		this.deviceState = new DeviceStateImpl();
		this.deviceDef = deviceDef;
		this.validAttributes = deviceDef.getDeviceIOs().stream()
			.map(io -> io.getAttribute())
			.collect(Collectors.toSet());
		this.validActuatorAttributes = deviceDef.getDeviceIOs().stream()
			.filter(io -> io.getType().equals(DeviceIO.Type.ACTUATOR))
			.map(io -> io.getAttribute())
			.collect(Collectors.toSet());
		this.controller = controllerFactory.getDeviceController(this.deviceDef, this.deviceState);
		this.connector = connector;
		this.scheduledExecutorService = scheduledExecutorService;
		Map<String, String> deviceConfig = this.deviceDef.getConfig();
		this.rate = deviceConfig.containsKey("rate") ? Long.parseLong(deviceConfig.get("rate")) : 5000;
		String rateUnitString = deviceConfig.containsKey("rateUnit") ? deviceConfig.get("rateUnit") : "MILLISECONDS";
		if(TimeUnit.valueOf(rateUnitString) == null) rateUnitString = TimeUnit.MILLISECONDS.toString();
		this.rateUnit = TimeUnit.valueOf(rateUnitString);
		log.info(String.format("Added device: name=%s, type=%s, rate=%s %s", this.deviceDef.getId(), this.deviceDef.getType(), this.rate, this.rateUnit.toString()));
	}
	
	public DeviceController getController() {
		return this.controller;
	}
	
	@Override
	public void connect() throws Exception {
		this.controller.connect();
	}

	@Override
	public void disconnect() throws Exception {
		this.controller.disconnect();
	}

	@Override
	public DeviceDefinition getDeviceDefinition() {
		return this.deviceDef;
	}

	@Override
	public State getState() {
		return this.deviceState.getState();
	}

	@Override
	public Measurement getLastMeasurement() {
		return this.deviceState.getMeasurement();
	}

	@Override
	public void pushAction(Action action) {
		action.getData().stream()
			.filter(data -> this.validActuatorAttributes.contains(data.getAttribute()))
			.forEach(data -> {
				try {
					this.controller.action(data.getAttribute(), data.getValue());
				}catch(Exception ex) {
					log.error(String.format(
						"Error sending action to Device - Type: %s, ID: %s, Attribute: %s [%s] = %s",
						DeviceImpl.this.deviceDef.getType(),
						DeviceImpl.this.deviceDef.getId(),
						data.getAttribute().getName(),
						data.getAttribute().getType(),
						data.getValue()
					));
				}
			});
	}
	
	@Override
	public void run() {
		try {
			this.controller.update();
			Measurement measurement = this.deviceState.getMeasurement();
			DeviceImpl.this.connector.send(new MeasurementMessage(DeviceImpl.this.deviceDef.getId(), measurement));
		} catch (Exception e) {
			log.error(e);
		}
	}
	
	private void stateUpdated(State state) {
		if(state != State.CONNECTED && runningTask != null) {
			runningTask.cancel(true);
			runningTask = null;
		}
		switch(state) {
		case CONNECTED: {
			runningTask = this.scheduledExecutorService.scheduleWithFixedDelay(this, 0, this.rate, this.rateUnit);
		}
		case CONNECTING: {
			
		}
		case DISCONNECTED: {
			
		}
		case FAILURE: {
			
		}
		case READY: {
			
		}
		}
		
	}
	
	private class DeviceStateImpl implements DeviceState {
		
		private State state;
		private Measurement measurement;
		
		private DeviceStateImpl() {
			this.state = State.DISCONNECTED;
			this.measurement = new Measurement();
		}
		
		public synchronized Measurement getMeasurement() {
			return this.measurement;
		}
		
		public synchronized State getState() {
			return this.state;
		}
		
		@Override
		public synchronized void updateState(State state) {
			if(this.state == state) return;
			this.state = state;
			DeviceImpl.this.stateUpdated(state);
		}

		@Override
		public synchronized void updateValue(Attribute attribute, Object value){
			if(!DeviceImpl.this.validAttributes.contains(attribute)) {
				log.error(String.format(
					"Attempting to update invalid attribute - Device Type: %s, Device ID: %s, Attribute: %s [%s]",
					DeviceImpl.this.deviceDef.getType(),
					DeviceImpl.this.deviceDef.getId(),
					attribute.getName(),
					attribute.getType()
				));
			}
			long timestamp = System.currentTimeMillis();
			this.measurement.setTimestamp(timestamp);
			this.measurement.setValue(attribute, value, timestamp);
		}

		@Override
		public void triggerUpdate() {
			try {
				DeviceImpl.this.connector.send(new MeasurementMessage(DeviceImpl.this.deviceDef.getId(), this.measurement));
			} catch (Exception e) {
				log.error(e);
			}
		}

	}

}
