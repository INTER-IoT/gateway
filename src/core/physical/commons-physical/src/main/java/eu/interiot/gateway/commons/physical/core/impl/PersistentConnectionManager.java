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
package eu.interiot.gateway.commons.physical.core.impl;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.interiot.gateway.commons.api.configuration.ConfigurationService;
import eu.interiot.gateway.commons.api.connector.PersistentConnector;
import eu.interiot.gateway.commons.api.connector.PersistentConnector.ConnectionListener;
import eu.interiot.gateway.commons.api.messages.Message;

public class PersistentConnectionManager {
	
	private Logger log = LogManager.getLogger(PersistentConnectionManager.class);
	
	private static final int incrementRate = 2;
	private final ScheduledExecutorService executorService;
	private final PersistentConnector connector;
	private final long initialDelay;
	private long currentDelay;
	private final long maxDelay;
	
	private final Runnable connectionTask = new Runnable() {

		@Override
		public void run() {
			PersistentConnectionManager.this.attemptConnection();
		}
		
	};
	
	private final ConnectionListener connectionListener = new ConnectionListener() {
		
		@Override
		public void onDisconnect() {
			System.out.println(PersistentConnectionManager.this.currentDelay);
			log.warn(String.format("Remote disconnected, attempt reconnection in %s milliseconds", PersistentConnectionManager.this.currentDelay));
			PersistentConnectionManager.this.scheduleConnection();
			PersistentConnectionManager.this.currentDelay = PersistentConnectionManager.incrementRate * PersistentConnectionManager.this.currentDelay;
			PersistentConnectionManager.this.currentDelay = Math.min(PersistentConnectionManager.this.currentDelay, maxDelay);
		}
		
		@Override
		public void onConnect() {
			PersistentConnectionManager.this.currentDelay = PersistentConnectionManager.this.initialDelay;
			log.info("Remote connection success");
		}

		@Override
		public void onSend(Message message, boolean sync) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onSent(Message message, boolean sync) {
			// TODO Auto-generated method stub
			
		}
	};
	
	private PersistentConnectionManager(PersistentConnector connector, long initialDelay, long maxDelay) {
		this.executorService = Executors.newSingleThreadScheduledExecutor();
		this.connector = connector;
		this.connector.addConnectionListener(this.connectionListener);
		this.initialDelay = initialDelay;
		this.currentDelay = initialDelay;
		this.maxDelay = maxDelay;
	}
	
	public static void init(PersistentConnector connector, ConfigurationService configService) {
		long reconnectMillis = configService.getLong("reconnect.delay", 2000L);
		long maxdelay = configService.getLong("reconnect.maxdelay", 2000L);
		PersistentConnectionManager manager = new PersistentConnectionManager(connector, reconnectMillis, maxdelay);
		manager.attemptConnection();
	}
	
	private void scheduleConnection() {
		this.executorService.schedule(this.connectionTask, this.currentDelay, TimeUnit.MILLISECONDS);
	}
	
	private void attemptConnection() {
		try {
			this.connector.connect();
		}catch(Exception ex) {
			log.error(ex);
		}
	}
	
	
}
