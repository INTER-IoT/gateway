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
import java.time.Instant;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

@SuppressWarnings("serial")
public abstract class Message implements Serializable{
	private static final Gson gson = new Gson();
	
	public static enum Type{
		Measurement("measurement", MeasurementMessage.class),
		Control("control", ControlMessage.class),
		RegisterDevice("register-device", RegisterDeviceMessage.class),
		Action("action", ActionMessage.class),
		DeviceRequest("device-request", DeviceRequestMessage.class),
		DeviceList("device-list", DeviceListMessage.class),
		Error("error-message", ErrorMessage.class),
		Generic("generic-message", GenericMessage.class),
		KVP("kvp-message", KVPMessage.class),
		GatewayInfo("gateway-info", GatewayInfoMessage.class);
		private String type;
		private Class<? extends Message> clazz;
		private Type(final String type, final Class<? extends Message> clazz){
			this.type = type;
			this.clazz = clazz;
		}
		private static Type getByTypeString(String typeString){
			for(Type type : Type.values()) if(type.type.equals(typeString)) return type;
			return null;
		}
		private static Type getByTypeClass(Class<? extends Message> clazz){
			for(Type type : Type.values()) if(type.clazz.equals(clazz)) return type;
			return null;
		}
	}
	
	private transient String timestamp = null;
	private transient String uuid = null;
	private transient String responseUuid = null;
	private transient boolean awaitResponse = false;
	
	protected Message(){
		
	}
	
	public final Type getMessageType(){
		return Type.getByTypeClass(this.getClass());
	}
	
	private JsonElement getAsJsonElement(){
		return gson.toJsonTree(this);
	}
	
	public final JsonObject toJson(){
		JsonObject jobj = new JsonObject();
		jobj.addProperty("type", this.getMessageType().type);
		jobj.add("payload", this.getAsJsonElement());
		jobj.addProperty("timestamp", timestamp);
		jobj.addProperty("uuid", this.uuid);
		jobj.addProperty("awaitResponse", this.awaitResponse);
		if (responseUuid != null) jobj.addProperty("responseUuid", responseUuid);
		return jobj;
	}
	
	public final void stamp(){
		if (this.timestamp == null) this.timestamp = Instant.now().toString();
		if (this.uuid == null) uuid = UUID.randomUUID().toString();
	}
	
	public final <T extends Message> T generateResponse(Class<T> messageClass) {
		try {
			T message = messageClass.newInstance();
			((Message)message).responseUuid = this.uuid;
			return message;
		}catch(Exception ex) {
			return null;
		}
	}
	
	public final void stampAsResponseOf(Message parent) {
		this.responseUuid = parent.uuid;
	}
	
	public String getMessageUuid() {
		return this.uuid;
	}
	
	public String getTimestamp() {
		return this.timestamp;
	}
	
	public final String getResponseMessageUuid() {
		return this.responseUuid;
	}
	
	public boolean awaitsResponse() {
		return awaitResponse;
	}
	
	public void awaitResponse(boolean awaitResponse) {
		this.awaitResponse = awaitResponse;
	}
	
		
	public static Message parseJsonMessage(JsonObject message) throws ParseException{
		try{
			String type = message.get("type").getAsString();
			Class<? extends Message> messageClass = Type.getByTypeString(type).clazz;
			Message msg = gson.fromJson(message.get("payload"), messageClass);
			msg.timestamp = message.get("timestamp").getAsString();
			msg.uuid = message.get("uuid").getAsString();
			if(message.has("responseUuid")) msg.responseUuid = message.get("responseUuid").getAsString();
			if(message.has("awaitResponse")) msg.awaitResponse = message.get("awaitResponse").getAsBoolean();
			return msg;
		}catch(Exception ex){
			throw new ParseException(ex);
		}
	}
	
	public static class ParseException extends Exception{
		private Exception wrappedException;
		public ParseException(Exception ex){
			this.wrappedException = ex;
		}
		@Override
		public String getMessage(){
			return "Message parse exception: " + wrappedException.getMessage();
		}
	}
}
