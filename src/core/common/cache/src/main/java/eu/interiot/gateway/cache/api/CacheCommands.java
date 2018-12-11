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

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import javax.cache.Cache;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import eu.interiot.gateway.cache.api.CacheCommands.CacheCommit;
import eu.interiot.gateway.cache.api.CacheCommands.CacheDumper;
import eu.interiot.gateway.cache.api.CacheCommands.CacheList;
import eu.interiot.gateway.commons.api.command.CommandLine.Command;
import eu.interiot.gateway.commons.api.command.CommandLine.Option;
import eu.interiot.gateway.commons.api.command.CommandLine.Parameters;
import eu.interiot.gateway.commons.api.command.CommandLine.ParentCommand;
import eu.interiot.gateway.commons.api.command.ExecutableCommand;
import eu.interiot.gateway.commons.api.storage.CacheService;

@Command(name = "cache", description = "Manage stored caches", subcommands = {
	CacheList.class,
	CacheCommit.class,
	CacheDumper.class
})
public class CacheCommands extends ExecutableCommand{

	@Option(names = {"-h", "--help"}, usageHelp = true, description = "Show this help message and exit.")
    private boolean helpRequested;
	
	private BundleContext context;
	private CacheServiceFactory cacheServiceFactory;
	
	public CacheCommands(CacheServiceFactory cacheServiceFactory, BundleContext context) {
		this.context = context;
		this.cacheServiceFactory = cacheServiceFactory;
	}
	
	@Override
	public void execute(PrintWriter out) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	@Command(name="list", description="List stored caches")
	public static class CacheList extends ExecutableCommand{

		@Option(names = {"-h", "--help"}, usageHelp = true, description = "Show this help message and exit.")
	    private boolean helpRequested;
		
		@Option(names = {"-b", "--bundles"}, description = "List bundles using the cache service")
		private boolean listBundles;
		
		@Parameters(arity = "0...1", description = "List cache for a given bundle (list all if omited)")
		private String bundleName;
		
		@ParentCommand
		private CacheCommands parent;

		@Override
		public void execute(PrintWriter out) throws Exception {
			ServiceReference<CacheService> sr = this.parent.context.getServiceReference(CacheService.class);
			Bundle [] bundles = sr.getUsingBundles();
			if(bundles == null) return;
			List<String> bundleNames = Arrays.asList(bundles).stream().map(bundle -> bundle.getSymbolicName()).collect(Collectors.toList());
			if(listBundles) {
				out.println("Bundles using cache service:");
				bundleNames.stream().map(name -> "\t" + name).forEach(out::println);
				return;
			}
			Collection<String> cacheNames = this.parent.cacheServiceFactory.getInnerCacheServiceImpl().getAllCaches()
				.stream()
				.map(cache -> cache.getName())
				.collect(Collectors.toSet());
			if(bundleName == null) {
				cacheNames.forEach(out::println);
				return;
			}
			if(bundleNames.contains(bundleName)) cacheNames.stream().filter(name -> name.startsWith(bundleName+".")).forEach(out::println);
		}
	}
	
	@Command(name="commit", description="Commit cache to filesystem")
	public static class CacheCommit extends ExecutableCommand{

		@Option(names = {"-h", "--help"}, usageHelp = true, description = "Show this help message and exit.")
	    private boolean helpRequested;
		
		@Parameters(arity = "0...1", description = "Commit specified cache to filesystem (commit all if omited)")
		private String cacheName;
		
		@ParentCommand
		private CacheCommands parent;

		@Override
		public void execute(PrintWriter out) throws Exception {
			CacheServiceImpl cacheService = this.parent.cacheServiceFactory.getInnerCacheServiceImpl();
			if(cacheName != null) {
				try {
					cacheService.commit(cacheName);
				}catch(NoSuchElementException e) {
					out.println("No cache found with name: " + cacheName);
				}
				return;
			}
			cacheService.commitAll();
		}
	}
	
	@Command(name="dump", description="Print cache")
	public static class CacheDumper extends ExecutableCommand{

		@Option(names = {"-h", "--help"}, usageHelp = true, description = "Show this help message and exit.")
	    private boolean helpRequested;
		
		@Parameters(arity = "0...1", description = "Dump specified cache (dump all if omited)")
		private String cacheName;
		
		@ParentCommand
		private CacheCommands parent;

		@Override
		public void execute(PrintWriter out) throws Exception {
			CacheServiceImpl cacheService = this.parent.cacheServiceFactory.getInnerCacheServiceImpl();
			Map<String, Cache<?, ?>> cacheMap = cacheService.getAllCaches().stream()
				.collect(Collectors.toMap(cache -> cache.getName(), cache -> cache));
			if(cacheName != null) {
				if(!cacheMap.containsKey(cacheName)) {
					out.println("No cache found with name: " + cacheName);
					return;
				}
				cacheMap.get(cacheName).forEach(e -> out.println(e.getKey().toString()+": "+e.getValue().toString()));
				return;
			}
			cacheMap.forEach((name, cache) -> {
				out.println(name);
				cache.forEach(e -> out.println("\t" + e.getKey().toString()+": "+e.getValue().toString()));
			}); 
		}
	}

}