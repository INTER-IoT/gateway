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
package eu.interiot.gateway.framework;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class FrameworkUtils {
	
	public static Map<String, Integer> getLoadOrderFromStream3(InputStream is) {
		try {
			Properties properties = new Properties();
			properties.load(is);
			is.close();
			return properties.entrySet().stream().collect(Collectors.toMap(e->e.getKey().toString(), e->Integer.parseInt(e.getValue().toString())));
		}catch(Exception ex) {
			sync(System.err::println, ex.getMessage());
			return new HashMap<String,Integer>();
		}
	}
	
	public static List<String> getLoadOrderFromStream2(InputStream is) {
		try {
			Properties properties = new Properties();
			properties.load(is);
			is.close();
			List<String> loadOrder = properties.entrySet().stream().map(e -> e.getKey().toString()).collect(Collectors.toList());
			loadOrder.sort((a, b) -> {
				int m1, m2;
				try {m1 = Integer.parseInt(properties.getProperty(a));}catch(Exception ex) {m1 = Integer.MAX_VALUE;}
				try {m2 = Integer.parseInt(properties.getProperty(b));}catch(Exception ex) {m2 = Integer.MAX_VALUE;}
				return Integer.compare(m1, m2);
			});
			return loadOrder;
		}catch(Exception ex) {
			sync(System.err::println, ex.getMessage());
			return new ArrayList<String>();
		}
	}
	
	public static List<String> getLoadOrderFromStream(InputStream is) {
		try {
			return readLines(is);
		}catch(Exception ex) {
			sync(System.err::println, ex.getMessage());
			return new ArrayList<String>();
		}
	}
	
	public static Map<String,String> getSubProperties(Properties properties, String subkey){
		Set<String> keys = properties.keySet().stream().map(o -> o.toString()).filter(s -> s.startsWith(subkey)).collect(Collectors.toSet());
		Map<String, String> subproperties = new HashMap<>();
		int length = subkey.length() + 1;
		keys.forEach(key -> subproperties.put(key.substring(length), properties.getProperty(key)));
		return subproperties;
	}
	
	public static List<String> readLines(InputStream is) throws IOException {
		List<String> lines = new ArrayList<>();
		Reader reader = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(reader);
		String line;
		while ((line = br.readLine()) != null) if(!line.startsWith("#") && !line.trim().equals("")) lines.add(line);
		reader.close();
		return lines;
	}
	
	public static boolean supplierExceptionFilter(Supplier<?> supplier) {
		try {
			supplier.get();
			return true;
		}catch(Exception ex) {
			sync(System.err::println, ex.getMessage());
			sync(ex::printStackTrace, System.err);
			return false;
		}
	}
	
	public static <T> boolean consumerExceptionFilter(Consumer<T> consumer, T t) {
		try {
			consumer.accept(t);
			return true;
		}catch(Exception ex) {
			sync(System.err::println, ex.getMessage());
			sync(ex::printStackTrace, System.err);
			return false;
		}
	}
	
	public static <T> Predicate<T> supplierExceptionPredicate(Supplier<T> supplier) {
		return new Predicate<T>() {
			@Override
			public boolean test(T t) {
				try {
					supplier.get();
					return true;
				}catch(Exception ex) {
					sync(System.err::println, ex.getMessage());
					return false;
				}
			}
		};	
	}
	
	public static <T> Consumer<T> safeConsumer(Consumer<T> consumer) {
		return new Consumer<T>() {
			@Override
			public void accept(T t) {
				try {
					consumer.accept(t);
				}catch(Exception ex) {
					sync(System.err::println, ex.getMessage());
				}
			}
		};
		
	}
	
	public static <T> void safeConsumerx(Consumer<T> consumer, T t) {
		try {
			consumer.accept(t);
		}catch(Exception ex) {
			sync(System.err::println, ex.getMessage());
		}
	}
	
	public static <T> Consumer<T> syncConsumer(Consumer<T> consumer) {
		return new Consumer<T>() {
			@Override
			public void accept(T t) {
				synchronized(consumer) {
					consumer.accept(t);
				}
			}
		};
	}
	
	public static <T> void sync(Consumer<T> consumer, T t) {
		synchronized(consumer) {
			consumer.accept(t);
		}
	}
}
