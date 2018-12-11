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
package eu.interiot.gateway.cache.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.Factory;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.integration.CacheLoader;
import javax.cache.spi.CachingProvider;

import eu.interiot.gateway.commons.api.storage.CacheService;

public class CacheServiceImpl implements CacheService{
	
	private final CachingProvider cachingProvider;
	private final CacheManager cacheManager;
	private final Path cacheStorePath;
	private final Map<String, Commiter<?,?>> commiters;
	
	
	public CacheServiceImpl(Path cacheStorePath) {
		this.commiters = new HashMap<>();
		this.cacheStorePath = cacheStorePath;
		cacheStorePath.toFile().mkdirs();
		ClassLoader contextClassloader = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
		this.cachingProvider = Caching.getCachingProvider();
		this.cacheManager = cachingProvider.getCacheManager();
		Thread.currentThread().setContextClassLoader(contextClassloader);
	}
	
	@Override
	public <K extends Serializable,V extends Serializable> Cache<K,V> getCache(String name, Class<K> keyClass, Class<V> valueClass, CacheLoader<K,V> loader) throws ClassNotFoundException, IOException{
		
		Cache<K, V> cache = cacheManager.getCache(name);
		
		if(cache != null) return cache;
		
		MutableConfiguration<K, V> config = new MutableConfiguration<>();
		
		//config.setWriteThrough(true);
		//config.setCacheWriterFactory(ItemCacheFactory.writerFactory);
		
		config.setWriteThrough(false);
		config.setReadThrough(true);
		config.setCacheLoaderFactory(new SingletonFactory<K,V>(loader));
		
		cache = cacheManager.createCache(name, config);
		
		Path cacheFilePath = cacheStorePath.resolve(name);
				
		if(Files.exists(cacheFilePath)) loadFromFile(cache, cacheFilePath);
		
		this.commiters.put(name, new Commiter<K,V>(cache));
		
		return cache;
		
	}
	
	@Override
	public <K extends Serializable, V extends Serializable> Cache<K, V> getCache(String name, Class<K> keyClass, Class<V> valueClass) throws Exception {
		Cache<K, V> cache = cacheManager.getCache(name);
		
		if(cache != null) return cache;
		
		MutableConfiguration<K, V> config = new MutableConfiguration<>();
		
		config.setWriteThrough(false);
		config.setReadThrough(false);
		
		cache = cacheManager.createCache(name, config);
		
		Path cacheFilePath = cacheStorePath.resolve(name);
				
		if(Files.exists(cacheFilePath)) loadFromFile(cache, cacheFilePath);
		
		this.commiters.put(name, new Commiter<K,V>(cache));
		
		return cache;
	}
	
	public Collection<Cache<?, ?>> getAllCaches() {
		return this.commiters.values().stream().map(commiter -> commiter.cache).collect(Collectors.toSet());
	}
	
	public void commit(String cacheName) throws IOException {
		if(!this.commiters.containsKey(cacheName)) throw new NoSuchElementException();
		this.commiters.get(cacheName).commit(this);
	}
	
	@Override
	public void commitAll() throws IOException {
		for(Commiter<?,?> commiter : commiters.values()) commiter.commit(this);
	}
	
	public void close() {
		this.cacheManager.close();
	}
	
	@Override
	public <K extends Serializable, V extends Serializable> void commit(Cache<K,V> cache) throws IOException {
		Path cacheFilePath = cacheStorePath.resolve(cache.getName());
		persist(cache, cacheFilePath);
	}
	
	
	private static class Commiter<K extends Serializable,V extends Serializable> {
		private final Cache<K,V> cache;
		public Commiter(Cache<K,V> cache) {
			this.cache = cache;
		}
		public void commit(CacheServiceImpl cacheService) throws IOException {
			cacheService.commit(cache);
		}
	}
	
	@SuppressWarnings("serial")
	private static class SingletonFactory<K,V> implements Factory<CacheLoader<K,V>>{
		
		private final CacheLoader<K,V> loader;
		
		private SingletonFactory(CacheLoader<K,V> loader) {
			this.loader = loader;
		}
		
		@Override
		public CacheLoader<K,V> create() {
			return loader;
		}
		
	}
	
	private static <K extends Serializable,V extends Serializable> void persist(Cache<K, V> cache, Path filePath) throws IOException {
		HashMap<K,V> map = new HashMap<K,V>();
		cache.forEach(entry -> map.put(entry.getKey(), entry.getValue()));
		serialize(map, filePath.toFile());
	}
	
	@SuppressWarnings("unchecked")
	private static <K extends Serializable,V extends Serializable> void loadFromFile(Cache<K, V> cache, Path filePath) throws ClassNotFoundException, IOException {
		HashMap<K,V> map = (HashMap<K,V>) deserialize(filePath.toFile());
		cache.clear();
		cache.putAll(map);
	}
	
	private static void serialize(Object object, File file) throws IOException {
		ObjectOutputStream oos = null;
		FileOutputStream fout = null;
		try{
		    fout = new FileOutputStream(file);
		    oos = new ObjectOutputStream(fout);
		    oos.writeObject(object);
		} finally {
		    if(oos != null){
		        oos.close();
		    } 
		}
	}
	
	private static Object deserialize(File file) throws IOException, ClassNotFoundException {
		ObjectInputStream objectinputstream = null;
		try {
		    FileInputStream streamIn = new FileInputStream(file);
		    objectinputstream = new ObjectInputStream(streamIn);
		    return objectinputstream.readObject();
		} finally {
		    if(objectinputstream != null){
		        objectinputstream .close();
		    } 
		}
	}
	
}
