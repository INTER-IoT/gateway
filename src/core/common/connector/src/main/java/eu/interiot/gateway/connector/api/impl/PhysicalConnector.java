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
package eu.interiot.gateway.connector.api.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import javax.net.ssl.SSLContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import eu.interiot.gateway.commons.api.command.CommandFactory;
import eu.interiot.gateway.commons.api.command.CommandLine.Command;
import eu.interiot.gateway.commons.api.command.CommandLine.Option;
import eu.interiot.gateway.commons.api.command.CommandService;
import eu.interiot.gateway.commons.api.command.ExecutableCommand;
import eu.interiot.gateway.commons.api.configuration.ConfigurationService;
import eu.interiot.gateway.commons.api.connector.FutureMessage;
import eu.interiot.gateway.commons.api.connector.PersistentConnector;
import eu.interiot.gateway.commons.api.dispatcher.MessageDispatcher;
import eu.interiot.gateway.commons.api.messages.ControlMessage;
import eu.interiot.gateway.commons.api.messages.Message;

public class PhysicalConnector implements PersistentConnector{
	
	private static Logger log = LogManager.getLogger("Connector");
	
	private final InetSocketAddress remoteAddress;
	
	private final JsonParser jsonParser;
	private Client client;
	private final MessageDispatcher dispatcher;
	private final FutureMessageDispatcher futureMessageDispatcher;
	private final FileSniffer sniffer;
	private final Set<ConnectionListener> connectionListeners;
	private final SSLContext sslContext;
	
	public PhysicalConnector(ConfigurationService configService, MessageDispatcher dispatcher, FutureMessageDispatcher futureMessageDispatcher, CommandService commandService) throws URISyntaxException, KeyManagementException, NoSuchAlgorithmException, CertificateException, KeyStoreException, IOException{
		this.remoteAddress = new InetSocketAddress(configService.get("host", "localhost"), configService.getInt("port", 8829));
		boolean isSSL = configService.getBoolean("ssl", false);
		if(isSSL) {
			String caFile = configService.get("ssl.trustedCertDir");
			if(caFile != null) this.sslContext = SSLUtils.Client.getTrustedSSLContext(Paths.get(caFile));
			else this.sslContext = SSLUtils.Client.getUntrustedSSLContext();
		}else {
			this.sslContext = null;
		}
		this.jsonParser = new JsonParser();		
		this.dispatcher = dispatcher;
		this.futureMessageDispatcher = futureMessageDispatcher;
		this.sniffer = new FileSniffer();
		this.sniffer.enable(configService.getBoolean("log", false));
		commandService.registerCommand(RemoteCommand.class, new CommandFactory<RemoteCommand>() {

			@Override
			public RemoteCommand createInstance() throws Exception {
				return new RemoteCommand();
			}
			
		});
		this.connectionListeners = new HashSet<>();
	}

	@Override
	public void send(Message message) throws Exception {
		message.stamp();
		this.connectionListeners.forEach(listener -> listener.onSend(message, false));
		client.send(message.toJson().toString());
		sniffer.sent(message.toJson().toString());
		this.connectionListeners.forEach(listener -> listener.onSent(message, false));
	}
	
	@Override
	public FutureMessage sendSync(Message message) throws Exception {
		message.stamp();
		this.connectionListeners.forEach(listener -> listener.onSend(message, true));
		message.awaitResponse(true);
		FutureMessage future = futureMessageDispatcher.newFuture(message.getMessageUuid());
		client.send(message.toJson().toString());
		sniffer.sent(message.toJson().toString());
		this.connectionListeners.forEach(listener -> listener.onSent(message, true));
		return future;
	}
	
	@Override
	public void connect() throws Exception {
		if (client != null) throw new Exception("Client already connected!");
		if(this.sslContext == null) {
			this.client = new Client(remoteAddress.getHostName(), remoteAddress.getPort());
		} else {
			this.client = new Client(remoteAddress.getHostName(), remoteAddress.getPort(), this.sslContext);
		}
		
		boolean result = this.client.connectBlocking();
		if(!result) {
			this.client = null;
			throw new Exception("Connection failure");
		}
	}
	
	@Override
	public void disconnect() throws Exception {
		this.client.closeBlocking();
		this.client = null;
	}
	
	@Command(name="remote", description="Control remote connection.")
	public class RemoteCommand extends ExecutableCommand {

		@Option(names= {"-c", "--connect"}, description="Connect to remote endpoint")
		private boolean isConnect;
		
		@Option(names= {"-d", "--disconnect"}, description="Disconnect from remote endpoint")
		private boolean isDisconnect;
		
		@Option(names= {"-s", "--send"}, arity="1...*", description="Send message to remote endpoint")
		private List<String> messages;
		
		@Option(names = {"-h", "--help"}, usageHelp = true, description = "Show this help message and exit.")
	    private boolean helpRequested;
		
		@Override
		public void execute(PrintWriter out) throws Exception {
			if(isConnect) {
				PhysicalConnector.this.connect();
			}else if(isDisconnect) {
				client.close();
			}else if(messages != null) {
				StringJoiner sj = new StringJoiner(" ");
				for(String arg : messages) sj.add(arg);
				PhysicalConnector.this.send(new ControlMessage(sj.toString()));
			}
		}
		
	}
	
	
	public class Client extends WebSocketClient {
		
		public Client(String host, int port) throws URISyntaxException {
			super(new URI("ws://"+host+":"+port));
		}
		
		public Client(String host, int port, SSLContext sslContext) throws URISyntaxException, IOException {
			super(new URI("wss://" + host + ":" + port));
			Socket socket = sslContext.getSocketFactory().createSocket();
			super.setSocket(socket);
		}
		
		@Override
		public void onOpen(ServerHandshake handshakedata) {
			log.info("Connected to server!");
			PhysicalConnector.this.connectionListeners.forEach(listener -> listener.onConnect());
		}

		@Override
		public void onMessage(String text) {
			try{
				sniffer.received(text);
				JsonObject jsonMessage = jsonParser.parse(text).getAsJsonObject();
				Message message = Message.parseJsonMessage(jsonMessage);
				dispatcher.messageEvent(message);
			}catch(Exception ex){
				log.error(ex);
			}
		}

		@Override
		public void onClose(int code, String reason, boolean remote) {
			log.info("Disconnected from server!: " + code);
			PhysicalConnector.this.client = null;
			PhysicalConnector.this.connectionListeners.forEach(listener -> listener.onDisconnect());
		}

		@Override
		public void onError(Exception ex) {
			log.error(ex);
		}
	}

	@Override
	public boolean isConnected() {
		return this.client != null;
	}

	@Override
	public InetSocketAddress getRemoteSocketInfo() {
		return this.remoteAddress;
	}

	@Override
	public void addConnectionListener(ConnectionListener listener) {
		this.connectionListeners.add(listener);
	}
	
}
