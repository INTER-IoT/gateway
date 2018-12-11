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

import java.io.IOException;
import java.util.Properties;

import eu.interiot.gateway.commons.api.configuration.ConfigurationService;

public class ConfigurationServiceImpl implements ConfigurationService{
	
	private Properties properties;
	
	public ConfigurationServiceImpl(Properties properties) throws IOException{
		this.properties = properties;
	}

	@Override
	public String get(String key) {
		return this.get(key, null);
	}

	@Override
	public Integer getInt(String key) {
		return this.getInt(key, null);
	}

	@Override
	public Long getLong(String key) {
		return this.getLong(key, null);
	}

	@Override
	public Double getDouble(String key) {
		return this.getDouble(key, null);
	}

	@Override
	public Boolean getBoolean(String key) {
		return this.getBoolean(key, null);
	}

	@Override
	public String get(String key, String defValue) {
		String value = properties.getProperty(key);
		if(value == null) return defValue;
		return value;
	}

	@Override
	public Integer getInt(String key, Integer defValue) {
		String value = this.get(key);
		if(value == null) return defValue;
		return Integer.parseInt(value);
	}

	@Override
	public Long getLong(String key, Long defValue) {
		String value = this.get(key);
		if(value == null) return defValue;
		return Long.parseLong(value);
	}

	@Override
	public Double getDouble(String key, Double defValue) {
		String value = this.get(key);
		if(value == null) return defValue;
		return Double.parseDouble(value);
	}

	@Override
	public Boolean getBoolean(String key, Boolean defValue) {
		String value = this.get(key);
		if(value == null) return defValue;
		return Boolean.parseBoolean(value);
	}

}
