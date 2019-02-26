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
package eu.interiot.gateway.commons.physical.core.impl;

import java.io.File;
import java.io.FileReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.osgi.framework.BundleContext;

import eu.interiot.gateway.commons.api.configuration.ConfigurationService;
import eu.interiot.gateway.commons.api.connector.PersistentConnector;
import eu.interiot.gateway.commons.api.connector.PersistentConnector.ConnectionListener;
import eu.interiot.gateway.commons.api.connector.RemoteConnector;
import eu.interiot.gateway.commons.api.gateway.GatewayInfo;
import eu.interiot.gateway.commons.api.messages.GatewayInfoMessage;
import eu.interiot.gateway.commons.api.messages.Message;
import eu.interiot.gateway.commons.physical.Activator;
import eu.interiot.gateway.commons.physical.api.registry.RegistryService;
import eu.interiot.gateway.commons.physical.core.PhysicalCore;

public class PhysicalCoreImpl extends PhysicalCore implements ConnectionListener {
	
	private static final Logger log = LogManager.getLogger(PhysicalCoreImpl.class);
	
	private ConfigurationService configService;
	//private DeviceManager devManager;
	private RegistryService registryService;
	private RemoteConnector connector;
	
	public PhysicalCoreImpl(BundleContext context) throws Exception{
		super(context);
	}

	@Override
	protected final void start() throws Exception {
		if(this.connector instanceof PersistentConnector) {
			PersistentConnector persistentConnector = (PersistentConnector) this.connector;
			persistentConnector.addConnectionListener(this);
			PersistentConnectionManager.init(persistentConnector, this.configService);
		}else {
			this.onConnect();
		}
		if(this.configService.getBoolean("autodeploy", false)) {
			this.readFolderAndRegisterDevices(this.configService.get("autodeploy.folder"));
		}
	}
	
	private void readFolderAndRegisterDevices(String folderPath) { //TODO: PROVISIONAL
		log.info("Device Configuration folder: " + folderPath);
		//Open the different files		
		File folder = new File(folderPath);
		File[] listOfFiles = folder.listFiles();		
		for (File file : listOfFiles) {
		    if (file.isFile() && file.getName().endsWith(".json")) {
		    	log.info("Register device: "  + folderPath + '/' + file.getName());		    
		    	try {
					registryService.loadDeviceFromReader(new FileReader(file));
				} catch (Exception e) {
					log.error(e);
					e.printStackTrace();
				}	
		    }
		}		
	}
	
	@Override
	protected final void resolveComponents(BundleContext context) throws Exception{
		this.connector = this.context.getService(context.getServiceReference(RemoteConnector.class));
		this.configService = this.context.getService(context.getServiceReference(ConfigurationService.class));
		//this.devManager = this.context.getService(context.getServiceReference(DeviceManager.class));
		this.registryService = this.context.getService(context.getServiceReference(RegistryService.class));
	}
	
	@Override
	protected void finish() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onConnect() {
		try {
			connector.sendSync(new GatewayInfoMessage(GatewayInfo.localInstance())).setListener(message -> {
				Activator.remoteGatewayInstance.setRemoteInfo((GatewayInfoMessage) message);
				Activator.remoteGatewayInstance.setConnector(PhysicalCoreImpl.this.connector);
				System.out.println("Virtual Gateway Info:\n" + Activator.remoteGatewayInstance.getRemoteInfo());
			});
		}catch(Exception ex) {
			log.error(ex);
		}
	}

	@Override
	public void onDisconnect() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSend(Message message, boolean sync) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSent(Message message, boolean sync) {
		// TODO Auto-generated method stub
		
	}

}
