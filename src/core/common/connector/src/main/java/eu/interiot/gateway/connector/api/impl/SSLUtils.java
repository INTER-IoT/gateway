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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.xml.bind.DatatypeConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SSLUtils {
	
	private static Logger log = LogManager.getLogger("SSLUtils");
	
	public static class Server {
		public static SSLContext getSSLContextFromPEM(Path pathToPEMFile, Path pathToKeyFile, String keyPassword) {
		    SSLContext context;
		    try {
		        context = SSLContext.getInstance("TLS");
		        byte[] certBytes = parseDERFromPEM(Files.readAllBytes(pathToPEMFile), "-----BEGIN CERTIFICATE-----", "-----END CERTIFICATE-----");
		        byte[] keyBytes = parseDERFromPEM(Files.readAllBytes(pathToKeyFile), "-----BEGIN PRIVATE KEY-----", "-----END PRIVATE KEY-----");

		        X509Certificate cert = generateCertificateFromDER(certBytes);
		        RSAPrivateKey key = generatePrivateKeyFromDER(keyBytes);

		        KeyStore keystore = KeyStore.getInstance("JKS");
		        keystore.load(null);
		        keystore.setCertificateEntry("cert-alias", cert);
		        keystore.setKeyEntry("key-alias", key, keyPassword.toCharArray(), new Certificate[]{cert});

		        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
		        kmf.init(keystore, keyPassword.toCharArray());

		        KeyManager[] km = kmf.getKeyManagers();

		        context.init(km, null, null);
		    } catch (IOException | KeyManagementException | KeyStoreException | InvalidKeySpecException | UnrecoverableKeyException | NoSuchAlgorithmException | CertificateException e) {
		        log.error(String.format("Unable to load certificate or key file"));
		    	log.error(e);
		    	throw new IllegalArgumentException();
		    }        
		    return context;
		}

	}
	
	public static class Client {
		
		public static SSLContext getUntrustedSSLContext() throws NoSuchAlgorithmException, KeyManagementException {
			TrustManager tm = new X509TrustManager() {
	            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	            }

	            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
	            }

	            public X509Certificate[] getAcceptedIssuers() {
	                return null;
	            }
	        };

			SSLContext sslContext = null;
			sslContext = SSLContext.getInstance( "TLS" );
			sslContext.init(null, new TrustManager[] { tm }, null);
			return sslContext;
		}
		
		public static SSLContext getTrustedSSLContext(Path pathToCertFolder) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
			ks.load(null); 
			Files.list(pathToCertFolder).forEach(filePath -> {
				try {
					byte[] certBytes = parseDERFromPEM(Files.readAllBytes(filePath), "-----BEGIN CERTIFICATE-----", "-----END CERTIFICATE-----");
					X509Certificate caCert = generateCertificateFromDER(certBytes);
					ks.setCertificateEntry(filePath.getFileName().toString(), caCert);
				}catch(KeyStoreException | IOException | CertificateException e) {
					log.error(String.format("Unable to load certificate file: %s", filePath.getFileName().toString()));
			    	log.error(e);
				}
			});
			tmf.init(ks);
			SSLContext sslContext = SSLContext.getInstance("TLS");
			sslContext.init(null, tmf.getTrustManagers(), null);
			return sslContext;
		}
		
	}
	
	private static byte[] parseDERFromPEM(byte[] pem, String beginDelimiter, String endDelimiter) {
	    String data = new String(pem);
	    String[] tokens = data.split(beginDelimiter);
	    tokens = tokens[1].split(endDelimiter);
	    return DatatypeConverter.parseBase64Binary(tokens[0]);
	}

	private static RSAPrivateKey generatePrivateKeyFromDER(byte[] keyBytes) throws InvalidKeySpecException, NoSuchAlgorithmException {
	    PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
	    KeyFactory factory = KeyFactory.getInstance("RSA");
	    return (RSAPrivateKey) factory.generatePrivate(spec);
	}

	private static X509Certificate generateCertificateFromDER(byte[] certBytes) throws CertificateException {
	    CertificateFactory factory = CertificateFactory.getInstance("X.509");

	    return (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(certBytes));
	}
	
}
