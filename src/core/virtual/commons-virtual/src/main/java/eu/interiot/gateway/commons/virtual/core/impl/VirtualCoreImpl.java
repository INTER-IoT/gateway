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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.osgi.framework.BundleContext;

import eu.interiot.gateway.commons.api.connector.PersistentConnector;
import eu.interiot.gateway.commons.api.connector.RemoteConnector;
import eu.interiot.gateway.commons.api.dispatcher.MessageDispatcher;
import eu.interiot.gateway.commons.api.dispatcher.MessageFilter;
import eu.interiot.gateway.commons.api.gateway.GatewayInfo;
import eu.interiot.gateway.commons.api.messages.GatewayInfoMessage;
import eu.interiot.gateway.commons.api.messages.Message;
import eu.interiot.gateway.commons.virtual.Activator;
import eu.interiot.gateway.commons.virtual.core.VirtualCore;

public class VirtualCoreImpl extends VirtualCore{
	
	private static Logger log = LogManager.getLogger(VirtualCoreImpl.class);
	
	private RemoteConnector connector;
	private MessageDispatcher dispatcher;
	//private VirtualStorageImpl virtualStorage;
	
	public VirtualCoreImpl(BundleContext context) throws Exception {
		super(context);
		//this.virtualStorage = new VirtualStorageImpl(context.getService(context.getServiceReference(CacheService.class)));
	}

	@Override
	protected void start() throws Exception {
		MessageFilter filter = new MessageFilter.Builder().types(Message.Type.GatewayInfo).build();
		dispatcher.subscribe(filter, this);
		
		/*MessageFilter filter2 = MessageFilter.Builder.all().build();
		dispatcher.subscribe(filter2, this.virtualStorage);*/
		if(connector instanceof PersistentConnector) {
			((PersistentConnector) this.connector).connect();
		}
		
	}

	@Override
	protected void finish() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void resolveComponents(BundleContext context) throws Exception {
		this.connector = this.context.getService(context.getServiceReference(RemoteConnector.class));
		this.dispatcher = this.context.getService(context.getServiceReference(MessageDispatcher.class));
	}

	@Override
	public void messageReceived(Message message) {
		try {
			switch(message.getMessageType()) {
			case GatewayInfo:
				GatewayInfo gateway = (GatewayInfoMessage) message;
				System.out.println("Physical Gateway Info:\n" + gateway);
				Activator.remoteGatewayInstance.setConnector(this.connector);
				Activator.remoteGatewayInstance.setRemoteInfo(gateway);
				if(message.awaitsResponse()) {
					GatewayInfoMessage response = new GatewayInfoMessage(GatewayInfo.localInstance());
					response.stampAsResponseOf(message);
					this.connector.send(response);
				}
				break;
			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e);
		}
	}
	/*
	public VirtualStorage getStorage() {
		return virtualStorage;
	}*/
}
