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

import java.io.IOException;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import eu.interiot.gateway.connector.api.impl.mina.websocket.WebSocketCodecPacket;

public class WebSocketHandler extends IoHandlerAdapter {
	
	private WebSocketDataListener listener;

	public WebSocketHandler(WebSocketDataListener listener) {
		this.listener = listener;
	}
	
    @Override
    public void exceptionCaught( IoSession session, Throwable cause ) throws Exception {
        cause.printStackTrace();
    }
    
    @Override
    public void messageReceived( IoSession session, Object message ) throws Exception {
    	IoBuffer data = (IoBuffer) message;
    	byte [] buf = new byte[data.limit()];
    	data.get(buf);
        String str = new String(buf, "UTF-8");
        this.listener.onTextFrame(str);
    }
    
    @Override
    public void sessionIdle( IoSession session, IdleStatus status ) throws Exception {
        System.out.println( "IDLE " + session.getIdleCount( status ));
    }
    
    public static void send(String message, IoSession session) throws IOException{
    	WebSocketCodecPacket packet = WebSocketCodecPacket.buildPacket(IoBuffer.wrap(message.getBytes("UTF-8")));
        session.write(packet);
    }
    
    public interface WebSocketDataListener {
    	public void onTextFrame(String text);
    }
    
}
