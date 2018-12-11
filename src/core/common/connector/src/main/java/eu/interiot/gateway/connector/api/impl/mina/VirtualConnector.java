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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.service.IoServiceListener;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

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

public class VirtualConnector implements PersistentConnector, WebSocketHandler.WebSocketDataListener, IoServiceListener{
	
	private static Logger log = LogManager.getLogger("Connector");
	
	private final String hostname;
	private final int port;
	private final JsonParser jsonParser;
	private final MessageDispatcher dispatcher;
	private WebSocketHandler handler;
	private IoSession session;
	private final FutureMessageDispatcher futureMessageDispatcher;
	private final Set<ConnectionListener> connectionListeners;
	
	public VirtualConnector(ConfigurationService configService, MessageDispatcher dispatcher, FutureMessageDispatcher futureMessageDispatcher, CommandService commandService){
		this.hostname = configService.get("host", "0.0.0.0");
		this.port = configService.getInt("port", 8829);
		this.handler = new WebSocketHandler(this);
		this.jsonParser = new JsonParser();
		this.dispatcher = dispatcher;
		this.futureMessageDispatcher = futureMessageDispatcher;
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
		WebSocketHandler.send(message.toJson().toString(), session);
	}
	
	@Override
	public FutureMessage sendSync(Message message) throws Exception {
		message.stamp();
		message.awaitResponse(true);
		FutureMessage future = futureMessageDispatcher.newFuture(message.getMessageUuid());
		System.out.println(message.toJson().toString());
		WebSocketHandler.send(message.toJson().toString(), this.session);
		return future;
	}

	@Override
	public void connect() throws Exception {
		IoAcceptor acceptor = new NioSocketAcceptor();
		acceptor.getFilterChain().addLast("code", new ProtocolCodecFilter(new WebSocketCodecFactory()));
		acceptor.addListener(this);
		acceptor.setHandler(this.handler);
		acceptor.getSessionConfig().setReadBufferSize( 2048 );
		acceptor.getSessionConfig().setIdleTime( IdleStatus.BOTH_IDLE, 10 );
		acceptor.bind( new InetSocketAddress(this.hostname, this.port) );
		log.info(String.format("STARTING CONNECTOR AT %s:%d", this.hostname, this.port));
	}

	@Override
	public void disconnect() throws Exception {
		VirtualConnector.this.session.closeNow().awaitUninterruptibly();
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
				VirtualConnector.this.disconnect();
			}else if(messages != null) {
				StringJoiner sj = new StringJoiner(" ");
				for(String arg : messages) sj.add(arg);
				VirtualConnector.this.send(new ControlMessage(sj.toString()));
			}
		}
		
	}
	
	@Override
	public void onTextFrame(String text) {
		try{
			JsonObject jsonMessage = jsonParser.parse(text).getAsJsonObject();
			Message message = Message.parseJsonMessage(jsonMessage);
			dispatcher.messageEvent(message);
		}catch(Exception ex){
			log.error(ex);
		}
	}

	@Override
	public void serviceActivated(IoService service) throws Exception {
		log.info("SERVICE ACTIVATED");
	}

	@Override
	public void serviceIdle(IoService service, IdleStatus idleStatus) throws Exception {
		log.info("SERVICE IDLE");
	}

	@Override
	public void serviceDeactivated(IoService service) throws Exception {
		log.info("SERVICE DEACTIVATED");
	}

	@Override
	public void sessionCreated(IoSession session) throws Exception {
		if(this.session != null) throw new Exception("Client already connected");
		this.session = session;
		VirtualConnector.this.connectionListeners.forEach(listener -> listener.onConnect());
	}

	@Override
	public void sessionClosed(IoSession session) throws Exception {
		this.session = null;
		VirtualConnector.this.connectionListeners.forEach(listener -> listener.onDisconnect());
	}

	@Override
	public void sessionDestroyed(IoSession session) throws Exception {
		this.session = null;
	}

	@Override
	public boolean isConnected() {
		return this.session != null;
	}

	@Override
	public InetSocketAddress getRemoteSocketInfo() {
		return (InetSocketAddress) this.session.getRemoteAddress();
	}

	@Override
	public void addConnectionListener(ConnectionListener listener) {
		this.connectionListeners.add(listener);
	}
	
}
