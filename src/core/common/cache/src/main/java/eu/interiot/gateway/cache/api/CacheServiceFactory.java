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

import java.io.Serializable;
import java.nio.file.Path;

import javax.cache.Cache;
import javax.cache.integration.CacheLoader;

import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;

import eu.interiot.gateway.commons.api.storage.CacheService;

public class CacheServiceFactory implements ServiceFactory<CacheService>{

	private final CacheServiceImpl cacheService;
	
	public CacheServiceFactory(Path cacheStorePath) {
		this.cacheService = new CacheServiceImpl(cacheStorePath);
	}
	
	@Override
	public CacheService getService(Bundle bundle, ServiceRegistration<CacheService> registration) {
		return new BundleCacheServiceWrapper(bundle.getSymbolicName());
	}

	@Override
	public void ungetService(Bundle bundle, ServiceRegistration<CacheService> registration, CacheService service) {
		// TODO Auto-generated method stub
		
	}
	
	public CacheServiceImpl getInnerCacheServiceImpl() {
		return this.cacheService;
	}
	
	public void close() {
		cacheService.close();
	}
	
	private class BundleCacheServiceWrapper implements CacheService {
		private final String bundleName;
		public BundleCacheServiceWrapper(String bundleName) {
			this.bundleName = bundleName;
		}

		@Override
		public <K extends Serializable, V extends Serializable> Cache<K, V> getCache(String name, Class<K> keyClass, Class<V> valueClass, CacheLoader<K, V> loader) throws Exception {
			return CacheServiceFactory.this.cacheService.getCache(bundleName + "." + name, keyClass, valueClass, loader);
		}

		@Override
		public void commitAll() throws Exception {
			CacheServiceFactory.this.cacheService.commitAll();
		}

		@Override
		public <K extends Serializable, V extends Serializable> void commit(Cache<K, V> cache) throws Exception {
			CacheServiceFactory.this.cacheService.commit(cache);
		}

		@Override
		public <K extends Serializable, V extends Serializable> Cache<K, V> getCache(String name, Class<K> keyClass, Class<V> valueClass) throws Exception {
			return CacheServiceFactory.this.cacheService.getCache(bundleName + "." + name, keyClass, valueClass);
		}
		
	}

}
