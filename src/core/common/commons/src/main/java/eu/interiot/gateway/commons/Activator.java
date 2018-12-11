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
package eu.interiot.gateway.commons;

import java.util.Properties;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import eu.interiot.gateway.commons.api.command.CommandService;
import eu.interiot.gateway.commons.api.gateway.GatewayInfo;
import eu.interiot.gateway.commons.api.gateway.GatewayInfo.Type;
import eu.interiot.gateway.commons.api.messages.GatewayInfoMessage;
import eu.interiot.gateway.framework.api.GatewayFrameworkService;


public class Activator implements BundleActivator{
	
	private static GatewayInfoMessage localInstance = null;

	@Override
	public void start(BundleContext context) throws Exception {
		GatewayFrameworkService gfs = context.getService(context.getServiceReference(GatewayFrameworkService.class));
		Properties gwProperties = gfs.getGatewayProperties();
		localInstance = new GatewayInfoMessage();
		localInstance.setExtensions((String[]) gwProperties.get("extensions")); 
		localInstance.setUUID(gwProperties.getProperty("uuid")); 
		localInstance.setVersion(gwProperties.getProperty("version")); 
		localInstance.setSpecificationVersion(gwProperties.getProperty("specVersion"));
		localInstance.setBuild(gwProperties.getProperty("build")); 
		localInstance.setType(Type.valueOf(gwProperties.getProperty("type").toUpperCase())); 
		localInstance.setVendor(gwProperties.getProperty("vendor"));
		localInstance.setSpecificationVendor(gwProperties.getProperty("specVendor"));
		context.registerService(GatewayInfo.class, Activator.localInstance, null);
		context.registerService(CommandService.class, new CommandService(), null);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	public static GatewayInfo getGatewayLocalInstance() {
		return localInstance;
	}

}
