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
package eu.interiot.gateway.commons.api.device;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Action {
	
	private long timestamp;
	private Set<ActionData> data;
	
	public Action(){
		this.data = new HashSet<>();
	}
	
	public Action(long timestamp) {
		this();
		this.timestamp = timestamp;
	}
	
	public void addData(ActionData data){
		this.data.add(data);
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public Set<ActionData> getData() {
		return data;
	}

	public void setData(Collection<ActionData> data) {
		this.data = new HashSet<>(data);
	}
	
	public void setValue(Attribute attribute, Object value) {
		removeValue(attribute);
		data.add(new ActionData(attribute, value));
	}
	
	public void removeValue(Attribute attribute) {
		Set<ActionData> toRemove = new HashSet<>();
		for(ActionData item : data) {
			if(item.getAttribute().getName().equals(attribute.getName())) toRemove.add(item); //data.remove(item);
		}
		data.removeAll(toRemove);
	}
	
	public static class ActionData {
		private Attribute attribute;
		private String value;
		
		public ActionData() {
			
		}
		
		public ActionData(Attribute attribute, Object value){
			this.attribute = attribute;
			this.value = value.toString();
		}
		
		public ActionData(String attributeName, Attribute.Type type, Object value){
			this(new Attribute(attributeName, type), value);
		}

		public Attribute getAttribute() {
			return attribute;
		}

		public void setAttribute(Attribute attribute) {
			this.attribute = attribute;
		}

		public String getValue() {
			return value;
		}

		public void setValue(Object value) {
			this.value = value.toString();
		}
	}
	
}
