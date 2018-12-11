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

import java.security.SecureRandom;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

import eu.interiot.gateway.commons.api.connector.RemoteConnector;
import eu.interiot.gateway.commons.api.device.DeviceDefinition;
import eu.interiot.gateway.commons.api.messages.RegisterDeviceMessage;
import eu.interiot.gateway.commons.physical.api.dmanager.Device;
import eu.interiot.gateway.commons.physical.api.dmanager.DeviceControllerFactory;
import eu.interiot.gateway.commons.physical.api.dmanager.DeviceManager;

public class DeviceManagerImpl implements DeviceManager, ServiceListener{
	
	private Logger log = LogManager.getLogger(DeviceManagerImpl.class);
	private final Map<String, DeviceControllerFactory> controllerServices;
	
	private final Map<Integer, Device> devices;
	
	private final IDGenerator idGenerator;

	private final BundleContext context;
	private final RemoteConnector connector;
	
	private final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(5);
	
	public DeviceManagerImpl(BundleContext context) throws InvalidSyntaxException {
		this.controllerServices = new HashMap<>();
		this.devices = new HashMap<>();
		this.context = context;
		this.context.addServiceListener(this, String.format("(objectClass=%s)", DeviceControllerFactory.class.getName()));
		this.connector = context.getService(context.getServiceReference(RemoteConnector.class));
		this.idGenerator = new IDGenerator();
	}
	
	@Override
	public String addDevice(DeviceDefinition deviceDef) throws Exception {
		String controllerKey = deviceDef.getController();
		if(!this.controllerServices.containsKey(controllerKey)) throw new Exception(String.format("Specified device controller %s not registered", controllerKey));
		DeviceControllerFactory controllerService = this.controllerServices.get(controllerKey);
		
		String b32Id = deviceDef.getId();
		int id;
	
		if(b32Id == null) {
			id = idGenerator.next(devices.keySet());
			b32Id = new String(IDGenerator.toB32(id));
			deviceDef.setId(b32Id);
		} else {
			id = IDGenerator.fromB32(b32Id.toCharArray());
			if(devices.keySet().contains(id)) throw new IllegalArgumentException(b32Id);
		}
		
		Device device = new DeviceImpl(deviceDef, controllerService, this.connector, this.scheduledExecutorService);
		devices.put(id, device);
		
		this.connector.send(new RegisterDeviceMessage(deviceDef));
		
		if(deviceDef.getConfig().containsKey("autostart")) {
			String autostartUnitString = deviceDef.getConfig().containsKey("autostartUnit") ? deviceDef.getConfig().get("autostartUnit") : "MILLISECONDS";
			if(TimeUnit.valueOf(autostartUnitString) == null) autostartUnitString = TimeUnit.MILLISECONDS.toString();
			int autostart = Integer.parseInt(deviceDef.getConfig().get("autostart"));
			if(autostart < 0) autostart = 0;
			if(autostart == 0) {
				device.connect();
			} else {
				this.scheduledExecutorService.schedule(() -> {
					try {
						device.connect();
					} catch (Exception e) {
						log.error(e);
					}
				}, autostart, TimeUnit.valueOf(autostartUnitString));
			}
		}
		
		return b32Id;
	}

	@Override
	public void removeDevice(String id) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Collection<Device> getDevices() {
		return this.devices.values();
	}

	@Override
	public Device getDevice(String id) {
		return this.devices.get(IDGenerator.fromB32(id.toCharArray()));
	}

	@Override
	public void serviceChanged(ServiceEvent event) {
		ServiceReference<?> serviceReference = event.getServiceReference();
		switch(event.getType()) {
			case ServiceEvent.REGISTERED: {
				DeviceControllerFactory controllerService = (DeviceControllerFactory) context.getService(serviceReference);
				String key = serviceReference.getProperty("controller-key").toString();
				log.info(String.format("Registering device controller %s", key));
				this.controllerServices.put(key, controllerService);
				break;
			}
			case ServiceEvent.UNREGISTERING: {
				String key = serviceReference.getProperty("controller-key").toString();
				log.info(String.format("Unregistering device controller %s ",key));
				this.controllerServices.remove(key);
				break;
			}
		}
	}
	
	private static class IDGenerator {
		private static final int max = 1073741824; //2^30
		
		private final SecureRandom r;
		
		private IDGenerator() {
			this.r = new SecureRandom();
		}
		
		public int next(Collection<Integer> invalidIds) {
			int id;
			do {
				id = this.next();
			}while(invalidIds.contains(id));
			return id;
		}
		
		public int next() {
			return this.r.nextInt(IDGenerator.max);
		}
		
		private static final char [] b32 = new char[] {
			'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H',
			'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P',
			'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
			'Y', 'Z', '2', '3', '4', '5', '6', '7'
		};
		
		public static char [] toB32(int id) {
			char [] b32id = new char[6];
			for(int i = 5; i >= 0; i--) {
				b32id[i] = b32[id & 0x1F];
				id = id >>> 5;
			}
			return b32id;
		}
		
		public static int fromB32(char [] b32id) {
			if(b32id.length != 6) throw new IllegalArgumentException(new String(b32id));
			int id = 0;
			for(int i = 0; i < 6; i++) {
				int val = b32id[i] - 65;
				if(val >= -15 && val <= -10) val = val + 41;
				if(val < 0 || val >= 32) throw new IllegalArgumentException(new String(b32id));
				id = id + val;
				if(i < 5) id = id << 5;
			}
			return id;
		}
		
	}

}
