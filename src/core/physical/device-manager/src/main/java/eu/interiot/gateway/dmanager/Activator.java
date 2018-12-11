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
package eu.interiot.gateway.dmanager;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import eu.interiot.gateway.commons.api.command.CommandFactory;
import eu.interiot.gateway.commons.api.command.CommandService;
import eu.interiot.gateway.commons.api.connector.RemoteConnector;
import eu.interiot.gateway.commons.api.dispatcher.MessageDispatcher;
import eu.interiot.gateway.commons.api.dispatcher.MessageFilter;
import eu.interiot.gateway.commons.api.messages.Message;
import eu.interiot.gateway.commons.physical.api.dmanager.DeviceManager;
import eu.interiot.gateway.dmanager.api.impl.DeviceCommands;
import eu.interiot.gateway.dmanager.api.impl.DeviceManagerImpl;
import eu.interiot.gateway.dmanager.api.impl.DeviceRequestEventHandler;

public class Activator implements BundleActivator{

	@Override
	public void start(BundleContext context) throws Exception {
		RemoteConnector connector = context.getService(context.getServiceReference(RemoteConnector.class));
		DeviceManager deviceManager = new DeviceManagerImpl(context);
		//DeviceCommands commands = new DeviceCommands(deviceManager, connector);
		context.registerService(DeviceManager.class.getName(), deviceManager, null);
		//context.registerService(CommandService_old.class, commands.getCommandService(), null);
		
		context.getService(context.getServiceReference(CommandService.class)).registerCommand(DeviceCommands.class, new CommandFactory<DeviceCommands>(){

			@Override
			public DeviceCommands createInstance() throws Exception {
				return new DeviceCommands(deviceManager, context);
			}
			
		});
		
		DeviceRequestEventHandler deviceRequestEventHandler = new DeviceRequestEventHandler(deviceManager, connector);
		MessageFilter filter = 
			new MessageFilter.Builder()
			.types(Message.Type.DeviceRequest, Message.Type.Action)
			.build();
		
		MessageDispatcher dispatcher = context.getService(context.getServiceReference(MessageDispatcher.class));
		dispatcher.subscribe(filter, deviceRequestEventHandler);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
