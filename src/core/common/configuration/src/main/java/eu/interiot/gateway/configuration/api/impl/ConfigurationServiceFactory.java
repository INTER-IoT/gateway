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
package eu.interiot.gateway.configuration.api.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

import eu.interiot.gateway.commons.api.configuration.ConfigurationService;

public class ConfigurationServiceFactory implements ServiceFactory<ConfigurationService>{

	private Properties defaultProperties;
	
	public ConfigurationServiceFactory(Properties defaultProperties) {
		this.defaultProperties = defaultProperties;
	}
	
	@Override
	public ConfigurationService getService(Bundle bundle, ServiceRegistration<ConfigurationService> registration) {
		ConfigurationService configurationService = null;
		try{
			configurationService = new ConfigurationServiceImpl(getSubProperties(bundle.getSymbolicName()));
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return configurationService;
	}

	@Override
	public void ungetService(Bundle bundle, ServiceRegistration<ConfigurationService> registration,
			ConfigurationService service) {
		// TODO Auto-generated method stub
	}
	
	public Properties getSubProperties(String subkey){
		Set<String> keys = this.defaultProperties.keySet().stream().map(o -> o.toString()).filter(s -> s.startsWith(subkey)).collect(Collectors.toSet());
		Map<String, String> subpropertiesMap = new HashMap<>();
		int length = subkey.length() + 1;
		keys.forEach(key -> subpropertiesMap.put(key.substring(length), this.defaultProperties.getProperty(key)));
		Properties subproperties = new Properties();
		subproperties.putAll(subpropertiesMap);
		return subproperties;
	}

}
