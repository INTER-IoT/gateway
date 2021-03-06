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
package \${package};

import java.util.Random;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.interiot.gateway.commons.api.device.Attribute;
import eu.interiot.gateway.commons.api.device.DeviceDefinition;
import eu.interiot.gateway.commons.physical.api.dmanager.Device.State;
import eu.interiot.gateway.commons.physical.api.dmanager.DeviceController;
import eu.interiot.gateway.commons.physical.api.dmanager.DeviceState;

public class ExampleDeviceController extends DeviceController {
	
	private static Logger log = LogManager.getLogger(ExampleDeviceController.class);
	
	private Random random;
	
	public ExampleDeviceController(DeviceDefinition deviceDefinition, DeviceState deviceState) {
		super(deviceDefinition, deviceState);
		this.random = new Random();
	}

	@Override
	public void connect() throws Exception {
		// connect to device, once it's state changes trigger the update
		super.deviceState.updateState(State.CONNECTED);
	}

	@Override
	public void disconnect() throws Exception {
		// disconnect to device, once it's state changes trigger the update
		super.deviceState.updateState(State.DISCONNECTED);
	}

	@Override
	public void update() throws Exception {
		switch(super.getDeviceDefinition().getType()) {
		case "\${artifactId}_temperature_and_light": {
			Attribute attr = super.deviceDefinition.getDeviceIOByAttributeName("temperature").getAttribute();
			int randValue = (int) Math.floor(100 * random.nextDouble() - 50);
			super.deviceState.updateValue(attr, randValue);
		}
		default:
		}
	}

	@Override
	public void action(Attribute attribute, String value) throws Exception {
		log.info(String.format("Received Action for %s: %s=%s[%s]", super.deviceDefinition.getId(), attribute.getName(), value, attribute.getType()));
	}
	
}
