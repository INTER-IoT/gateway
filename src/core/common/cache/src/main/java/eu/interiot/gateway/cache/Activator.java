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
package eu.interiot.gateway.cache;

import java.nio.file.Paths;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import eu.interiot.gateway.cache.api.CacheCommands;
import eu.interiot.gateway.cache.api.CacheServiceFactory;
import eu.interiot.gateway.commons.api.command.CommandFactory;
import eu.interiot.gateway.commons.api.command.CommandService;
import eu.interiot.gateway.commons.api.configuration.ConfigurationService;
import eu.interiot.gateway.commons.api.storage.CacheService;

public class Activator implements BundleActivator{

	@Override
	public void start(BundleContext context) throws Exception {
		//ApiRouter apiRouter = context.getService(context.getServiceReference(ApiRouter.class));
		//apiRouter.addClass(StorageRoute.class);
		ConfigurationService config = context.getService(context.getServiceReference(ConfigurationService.class));
		
		CacheServiceFactory cacheServiceFactory = new CacheServiceFactory(Paths.get(config.get("path")));
		context.registerService(CacheService.class.getName(), cacheServiceFactory, null);
		
		context.getService(context.getServiceReference(CommandService.class)).registerCommand(CacheCommands.class, new CommandFactory<CacheCommands>(){

			@Override
			public CacheCommands createInstance() throws Exception {
				return new CacheCommands(cacheServiceFactory, context);
			}
			
		});
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// TODO Auto-generated method stub
		
	}

	public void pff() {
		
	}
	
}