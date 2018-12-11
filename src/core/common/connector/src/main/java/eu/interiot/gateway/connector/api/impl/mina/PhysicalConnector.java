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
package eu.interiot.gateway.connector.api.impl.mina;

import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

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
import eu.interiot.gateway.connector.api.impl.FutureMessageDispatcher;
import eu.interiot.gateway.connector.api.impl.mina.websocket.WebSocketCodecFactory;

public class PhysicalConnector implements PersistentConnector, WebSocketHandler.WebSocketDataListener {

	private static Logger log = LogManager.getLogger("Connector");

	private final InetSocketAddress remoteAddress;

	private final JsonParser jsonParser;
	private final MessageDispatcher dispatcher;
	private final WebSocketHandler handler;
	private IoSession session;
	private final FutureMessageDispatcher futureMessageDispatcher;
	private boolean connected;
	private final Set<ConnectionListener> connectionListeners;
	
	public PhysicalConnector(ConfigurationService configService, MessageDispatcher dispatcher, FutureMessageDispatcher futureMessageDispatcher, CommandService commandService) throws URISyntaxException {
		this.remoteAddress = new InetSocketAddress(configService.get("host", "localhost"), configService.getInt("port", 8829));
		this.handler = new WebSocketHandler(this);
		this.jsonParser = new JsonParser();
		this.dispatcher = dispatcher;
		this.futureMessageDispatcher = futureMessageDispatcher;
		this.connected = false;
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
		WebSocketHandler.send(message.toJson().toString(), this.session);
	}
	
	@Override
	public FutureMessage sendSync(Message message) throws Exception {
		message.stamp();
		message.awaitResponse(true);
		FutureMessage future = futureMessageDispatcher.newFuture(message.getMessageUuid());
		WebSocketHandler.send(message.toJson().toString(), this.session);
		return future;
	}

	@Override
	public void connect() throws Exception {
		IoConnector connector = new NioSocketConnector();
		connector.getSessionConfig().setReadBufferSize(2048);

		//connector.getFilterChain().addLast("logger", new LoggingFilter());
		connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new WebSocketCodecFactory()));

		connector.setHandler(this.handler);
		ConnectFuture future = connector.connect(this.remoteAddress);
		future.awaitUninterruptibly();

		if (!future.isConnected()) {
			return;
		}
		this.session = future.getSession();
		session.getConfig().setUseReadOperation(true);
		this.connected = true;
	}

	@Override
	public void disconnect() throws Exception {
		PhysicalConnector.this.session.closeNow().awaitUninterruptibly();
		this.connected = false;
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
				PhysicalConnector.this.disconnect();
			}else if(messages != null) {
				StringJoiner sj = new StringJoiner(" ");
				for(String arg : messages) sj.add(arg);
				PhysicalConnector.this.send(new ControlMessage(sj.toString()));
			}
		}
		
	}
	
	@Override
	public void onTextFrame(String text) {
		try {
			JsonObject jsonMessage = jsonParser.parse(text).getAsJsonObject();
			Message message = Message.parseJsonMessage(jsonMessage);
			dispatcher.messageEvent(message);
		} catch (Exception ex) {
			log.error(ex);
		}
	}

	@Override
	public boolean isConnected() {
		return connected;
	}

	@Override
	public InetSocketAddress getRemoteSocketInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addConnectionListener(ConnectionListener listener) {
		this.connectionListeners.add(listener);
	}

}
