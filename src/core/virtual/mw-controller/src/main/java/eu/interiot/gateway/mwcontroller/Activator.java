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
package eu.interiot.gateway.mwcontroller;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import eu.interiot.gateway.commons.api.dispatcher.MessageDispatcher;
import eu.interiot.gateway.commons.api.dispatcher.MessageFilter;
import eu.interiot.gateway.commons.api.messages.Message;
import eu.interiot.gateway.mwcontroller.api.MWRegistryService;
import eu.interiot.gateway.mwcontroller.api.impl.MWRegistryServiceImpl;
import jodd.http.HttpRequest;
import jodd.http.HttpResponse;

public class Activator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		MWRegistryService mwRegistry = new MWRegistryServiceImpl();
		context.registerService(MWRegistryService.class, mwRegistry, null);
		MessageDispatcher messageDispatcher = context.getService(context.getServiceReference(MessageDispatcher.class));
		MessageFilter filter = 
			new MessageFilter.Builder()
			.types(Message.Type.Measurement, Message.Type.RegisterDevice)
			.build();
		messageDispatcher.subscribe(filter, mwRegistry);
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// TODO Auto-generated method stub

	}
	
	public void testJodd() {
		HttpRequest request = new HttpRequest();
		request.method("GET").protocol("https").host("google.es").port(443).path("");
		HttpResponse response = request.send();
		System.out.println("\n" + response.bodyText());
	}
	
	public void testApache() throws Exception{
		/*
		String url = "http://www.google.com/search?q=httpClient";

		HttpClient client = HttpClientBuilder.create().build();
		HttpGet request = new HttpGet(url);

		// add request header
		//request.addHeader("User-Agent", USER_AGENT);
		HttpResponse response = client.execute(request);

		System.out.println("Response Code : "
		                + response.getStatusLine().getStatusCode());

		BufferedReader rd = new BufferedReader(
			new InputStreamReader(response.getEntity().getContent()));

		StringBuffer result = new StringBuffer();
		String line = "";
		while ((line = rd.readLine()) != null) {
			result.append(line);
		}
		System.out.println("\n" + result.toString() + "\n");*/
	}
	
}