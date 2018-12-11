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
package eu.interiot.gateway.commons.api.dispatcher;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import eu.interiot.gateway.commons.api.messages.Message;

public class MessageFilter {
	
	private Map<Message.Type, Map<String, String>> messageTypeProps;
	
	private MessageFilter() {
		
	}
	
	public Message.Type [] getTypes() {
		return this.messageTypeProps.keySet().toArray(new Message.Type[] {});
	}
	
	public Map<String, String> getProperties(Message.Type type){
		return this.messageTypeProps.get(type);
	}

	public static class Builder {
		
		private Map<Message.Type, Map<String, String>> messageTypeProps;
		
		public Builder() {
			this.messageTypeProps = new HashMap<>();
		}
		
		public Builder types(Message.Type... types) {
			for(Message.Type type : types) 
				if(!this.messageTypeProps.containsKey(type))
					this.messageTypeProps.put(type, new HashMap<>());
			return this;
		}
		
		public Builder type(Message.Type type, String... properties) {
			Map<String, String> props;
			if(this.messageTypeProps.containsKey(type)) props = this.messageTypeProps.get(type);
			else props = new HashMap<>();
			Stream.iterate(0, i -> i + 2).limit(properties.length / 2).forEach(i -> props.put(properties[i], properties[i + 1]));
			this.messageTypeProps.put(type, props);
			return this;
		}
		
		public MessageFilter build() {
			MessageFilter filter = new MessageFilter();
			filter.messageTypeProps = new HashMap<>();
			this.messageTypeProps.forEach((k, v) -> {
				Map<String, String> props = new HashMap<>();
				props.putAll(v);
				filter.messageTypeProps.put(k, v);
			}); 
			return filter;
		}
		
		public static Builder all() {
			Builder builder = new Builder();
			return builder;
		}
		
	}
	
}
