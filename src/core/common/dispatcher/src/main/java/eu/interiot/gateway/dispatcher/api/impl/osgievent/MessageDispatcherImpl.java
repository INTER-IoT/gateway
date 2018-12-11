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
package eu.interiot.gateway.dispatcher.api.impl.osgievent;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.osgi.framework.BundleContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import eu.interiot.gateway.commons.api.dispatcher.MessageDispatcher;
import eu.interiot.gateway.commons.api.dispatcher.MessageFilter;
import eu.interiot.gateway.commons.api.dispatcher.MessageHandler;
import eu.interiot.gateway.commons.api.messages.KVPMessage;
import eu.interiot.gateway.commons.api.messages.Message;

public class MessageDispatcherImpl implements MessageDispatcher{
	
	public static final String MESSAGE_EVENT_TOPIC = "eu/interiot/gateway/message";
	public static final String REMOTE_EVENT_TOPIC = "eu/interiot/gateway/remote";
	public static final String ALL_EVENT_TOPIC = "eu/interiot/gateway/*";
	
	private final EventAdmin eventAdmin;
	private final BundleContext context;
	
	public MessageDispatcherImpl(BundleContext context) {
		this.context = context;
		this.eventAdmin = context.getService(context.getServiceReference(EventAdmin.class));
	}

	@Override
	public void messageEvent(Message message) {
		Map<String, Object> properties = new HashMap<>();
		properties.put("message", message);
		properties.put("type", message.getMessageType().toString());
		/*if(message.getMessageType() == Message.Type.Generic) {
			properties.put("entityClass", ((GenericMessage<?>) message).getEntityClass());
		}*/
		if(message.getMessageType() == Message.Type.KVP) {
			KVPMessage kvp = (KVPMessage) message;
			properties.putAll(kvp.getKVP());
		}

		Event messageEvent = new Event(MESSAGE_EVENT_TOPIC, properties);
		eventAdmin.postEvent(messageEvent);
	}

	@Override
	public void subscribe(MessageFilter filter, MessageHandler handler) {
		EventHandler eventHandler = new MessageHandlerOSGiEventWrapper(handler);
		Dictionary<String, String> properties = new Hashtable<>();
		properties.put(EventConstants.EVENT_TOPIC, MESSAGE_EVENT_TOPIC);
		if(filter.getTypes()!=null && filter.getTypes().length > 0) {
			String messageTypes = "(|" + Stream.of(filter.getTypes()).map(t -> {
				Map<String, String> props = filter.getProperties(t);
				String typefilter = "(type=" + t + ")";
				if(props.isEmpty()) return typefilter;
				else {
					return "(&" + typefilter + 
					props.entrySet().stream().map(e -> "(" + e.getKey() + "=" + e.getValue() + ")").collect(Collectors.joining())
					+ ")";
				}
			}).collect(Collectors.joining()) + ")";
			properties.put(EventConstants.EVENT_FILTER, messageTypes);
		}
		context.registerService(EventHandler.class, eventHandler, properties);
	}

	@Override
	public void unsubscribe(MessageHandler handler) {
		// TODO Auto-generated method stub
		
	}
	
}
