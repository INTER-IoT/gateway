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
package eu.interiot.gateway.mwcontroller.api;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import eu.interiot.gateway.commons.api.Utils;
import freemarker.template.Template;
import freemarker.template.TemplateException;

public class MWMessage {
	
	private Map<String, Object> keyValues;
	private Template template;
	private MessageDigest md;
	private String messageId;
	
	public MWMessage(Template template) throws Exception{
		this.keyValues = new HashMap<>();
		this.template = template;
		this.md = MessageDigest.getInstance("MD5");
		this.messageId = null;
	}
	
	public String getMessageId(){
		if(this.messageId!=null) return messageId;
		this.md.reset();
		for(String key : keyValues.keySet()){
			this.md.update(key.getBytes());
			this.md.update(keyValues.get(key).toString().getBytes());
		}
		this.messageId = Utils.Converter.bytesToHex(this.md.digest());
		return this.messageId;
	}
	
	public void set(String key, Object value){
		this.keyValues.put(key, value);
		this.messageId = null;
	}
	
	public String process() throws TemplateException, IOException{
		Writer writer = new StringWriter();
		Map<String, Object> copy = new HashMap<String, Object>(keyValues);
		copy.put("messageId", this.getMessageId());
		template.process(copy, writer);
		return writer.toString();
	}
	
	public void process(Writer writer) throws TemplateException, IOException{
		Map<String, Object> copy = new HashMap<String, Object>(keyValues);
		copy.put("messageId", this.getMessageId());
		template.process(copy, writer);
	}
}