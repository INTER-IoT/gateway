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

import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import javax.net.ssl.SSLContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.DefaultSSLWebSocketServerFactory;
import org.java_websocket.server.WebSocketServer;

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
import eu.interiot.gateway.commons.api.gateway.GatewayInfo;
import eu.interiot.gateway.commons.api.messages.ControlMessage;
import eu.interiot.gateway.commons.api.messages.Message;

public class VirtualConnector implements PersistentConnector {
	
	private static Logger log = LogManager.getLogger("Connector");
	
	private final JsonParser jsonParser;
	private WebSocket client;
	private final Server server;
	private final MessageDispatcher dispatcher;
	private final FutureMessageDispatcher futureMessageDispatcher;
	private final FileSniffer sniffer;
	private final Set<ConnectionListener> connectionListeners;
	
	public VirtualConnector(ConfigurationService configService, MessageDispatcher dispatcher, FutureMessageDispatcher futureMessageDispatcher, CommandService commandService){
		String host = configService.get("host", "0.0.0.0");
		int port = configService.getInt("port", 8829);
		boolean isSSL = configService.getBoolean("ssl", false);
		if(isSSL) {
			Path pemFilePath = Paths.get(configService.get("ssl.pemCertFile"));
			Path keyFilePath = Paths.get(configService.get("ssl.pemKeyFile"));
			String keyPass = GatewayInfo.localInstance().getUUID();
			SSLContext sslContext = SSLUtils.Server.getSSLContextFromPEM(pemFilePath, keyFilePath, keyPass);
			this.server = new Server(host, port, sslContext);
		}else {
			this.server = new Server(host, port);
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
		client.send(message.toJson().toString());
		sniffer.sent(message.toJson().toString());
	}
	
	@Override
	public FutureMessage sendSync(Message message) throws Exception {
		message.stamp();
		message.awaitResponse(true);
		FutureMessage future = futureMessageDispatcher.newFuture(message.getMessageUuid());
		client.send(message.toJson().toString());
		sniffer.sent(message.toJson().toString());
		return future;
	}
	
	@Override
	public void connect() throws Exception {
		this.server.start();
	}

	@Override
	public void disconnect() throws Exception {
		this.server.stop();
	}
	
	@Command(name="remote", description="Control remote connection.")
	public class RemoteCommand extends ExecutableCommand {

		@Option(names= {"-d", "--disconnect"}, description="Disconnect from remote endpoint")
		private boolean isDisconnect;
		
		@Option(names= {"-s", "--send"}, arity="1...*", description="Send message to remote endpoint")
		private List<String> messages;
		
		@Option(names = {"-h", "--help"}, usageHelp = true, description = "Show this help message and exit.")
	    private boolean helpRequested;
		
		@Override
		public void execute(PrintWriter out) throws Exception {
			if(isDisconnect) {
				client.close();
			}else if(messages != null) {
				StringJoiner sj = new StringJoiner(" ");
				for(String arg : messages) sj.add(arg);
				VirtualConnector.this.send(new ControlMessage(sj.toString()));
			}
		}
		
	}

	public class Server extends WebSocketServer{
		
		public Server(String host, int port){
			super(new InetSocketAddress(host, port));
		}
		
		public Server(String host, int port, SSLContext sslContext) {
			this(host, port);
			super.setWebSocketFactory(new DefaultSSLWebSocketServerFactory(sslContext));
		}

		@Override
		public void onOpen(WebSocket conn, ClientHandshake handshake) {
			log.info("Client trying to connect");
			if (client != null) {
				conn.close(4000, "Only one remote client supported.");
			}
			client = conn;
			log.info("Client connected!");
			VirtualConnector.this.connectionListeners.forEach(listener -> listener.onConnect());
		}

		@Override
		public void onClose(WebSocket conn, int code, String reason, boolean remote) {
			client = null;
			log.info("WebSocket remote client disconnected");
			VirtualConnector.this.connectionListeners.forEach(listener -> listener.onDisconnect());
		}

		@Override
		public void onMessage(WebSocket conn, String text) {
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
		public void onError(WebSocket conn, Exception ex) {
			log.error(ex);
		}

		@Override
		public void onStart() {
			log.info("Connector WebSocket server started");
		}
		
	}

	@Override
	public boolean isConnected() {
		return this.client != null;
	}

	@Override
	public InetSocketAddress getRemoteSocketInfo() {
		return this.client.getRemoteSocketAddress();
	}

	@Override
	public void addConnectionListener(ConnectionListener listener) {
		this.connectionListeners.add(listener);
	}

}
