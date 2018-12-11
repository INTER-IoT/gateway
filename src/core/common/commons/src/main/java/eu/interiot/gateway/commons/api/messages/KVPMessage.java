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
package eu.interiot.gateway.commons.api.messages;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public class KVPMessage extends Message{
	
	private Map<String, Serializable> kvp;
	
	public KVPMessage() {
		this.kvp = new HashMap<>();
	}
	
	public KVPMessage(Serializable ...kvps) {
		this.kvp = new HashMap<>();
		for(int i = 0; i < kvps.length; i++) {
			if (i >= kvps.length) break;
			String key = (String) kvps[i];
			i++;
			if (i >= kvps.length) break;
			Serializable value = kvps[i];
			this.kvp.put(key, value);
		}
	}
	
	public Map<String, Serializable> getKVP(){
		return kvp;
	}
	
	public void setKVP(Map<String, Serializable> kvp) {
		this.kvp = kvp;
	}
	
	public Object getValue(String key) {
		return kvp.get(key);
	}
	
	public void setValue(String key, Serializable value) {
		this.kvp.put(key, value);
	}
	
}
