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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.interiot.gateway.connector.api.impl.mina.websocket;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Static utility class containing methods used for websocket encoding
 * and decoding.
 *
 * @author DHRUV CHOPRA
 */
class WebSocketUtils {
    
    static final String SessionAttribute = "isWEB";
    // Construct a successful websocket handshake response using the key param
    // (See RFC 6455).
    static WebSocketHandShakeResponse buildWSHandshakeResponse(String key){
        String response = "HTTP/1.1 101 Web Socket Protocol Handshake\r\n";
        response += "Upgrade: websocket\r\n";
        response += "Connection: Upgrade\r\n";
        response += "Sec-WebSocket-Accept: " + key + "\r\n";
        response += "\r\n";        
        return new WebSocketHandShakeResponse(response);
    }
    
    // Parse the string as a websocket request and return the value from
    // Sec-WebSocket-Key header (See RFC 6455). Return empty string if not found.
    static String getClientWSRequestKey(String WSRequest) {
        String[] headers = WSRequest.split("\r\n");
        String socketKey = "";
        for (int i = 0; i < headers.length; i++) {
            if (headers[i].contains("Sec-WebSocket-Key")) {
                socketKey = (headers[i].split(":")[1]).trim();
                break;
            }
        }
        return socketKey;
    }    
    
    // 
    // Builds the challenge response to be used in WebSocket handshake.
    // First append the challenge with "258EAFA5-E914-47DA-95CA-C5AB0DC85B11" and then
    // make a SHA1 hash and finally Base64 encode it. (See RFC 6455)
    static String getWebSocketKeyChallengeResponse(String challenge) throws NoSuchAlgorithmException, UnsupportedEncodingException {

        challenge += "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
        MessageDigest cript = MessageDigest.getInstance("SHA-1");
        cript.reset();
        cript.update(challenge.getBytes("utf8"));
        byte[] hashedVal = cript.digest();        
        return Base64.encodeBytes(hashedVal);
    }
}
