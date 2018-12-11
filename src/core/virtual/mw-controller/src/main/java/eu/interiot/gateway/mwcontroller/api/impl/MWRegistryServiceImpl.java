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
package eu.interiot.gateway.mwcontroller.api.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.interiot.gateway.commons.api.messages.MeasurementMessage;
import eu.interiot.gateway.commons.api.messages.Message;
import eu.interiot.gateway.commons.api.messages.RegisterDeviceMessage;
import eu.interiot.gateway.mwcontroller.api.MWModule;
import eu.interiot.gateway.mwcontroller.api.MWRegistryService;

public class MWRegistryServiceImpl implements MWRegistryService{
	
	private static Logger log = LogManager.getLogger(MWRegistryServiceImpl.class);
	
	private MWModule activeMWModule;
	
	public MWRegistryServiceImpl() {

	}
	
	@Override
	public void setMWModule(MWModule mwmodule) throws Exception {
		if (this.activeMWModule != null) throw new Exception("Active MW already configured.");
		this.activeMWModule = mwmodule;
	}
	
	@Override
	public MWModule getMWModule() {
		return activeMWModule;
	}

	@Override
	public void messageReceived(Message message) {
		if(this.activeMWModule != null) {
			try {
				switch(message.getMessageType()) {
				case Measurement:
					this.handleMeasurementMessage((MeasurementMessage) message);
					break;
				case RegisterDevice:
					this.handleRegisterMessage((RegisterDeviceMessage) message);
					break;
				default:
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
				log.error(e);
			}
		}
	}
	
	public void handleMeasurementMessage(MeasurementMessage message) throws Exception {
		this.activeMWModule.pushMeasurement(message.getDeviceId(), message.getMeasurement());
	}
	
	public void handleRegisterMessage(RegisterDeviceMessage message) throws Exception {
		this.activeMWModule.registerDevice(message.getDevice());
	}
	
}
