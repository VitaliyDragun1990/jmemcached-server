package com.revenat.jmemcached.server.domain.impl;

import java.time.Clock;
import java.util.Properties;

import com.revenat.jmemcached.server.domain.Server;
import com.revenat.jmemcached.server.domain.ServerConfig;
import com.revenat.jmemcached.server.domain.ServerContext;
import com.revenat.jmemcached.server.domain.ServerFactory;
import com.revenat.jmemcached.server.domain.Storage;

/**
 * Default implementation of the {@link ServerFactory}.
 * 
 * @author Vitaly Dragun
 *
 */
public class JMemcachedServerFactory implements ServerFactory {

	@Override
	public Server buildNewServer(Properties overrideServerProperties) {
		
		
		return buildServerInstance(overrideServerProperties);
	}
	
	@Override
	public Server buildNewServer() {
		return buildServerInstance(null);
	}

	public Server buildServerInstance(Properties overrideServerProperties) {
		ServerConfig config = new DefaultServerConfig(
				overrideServerProperties,
				new ClassPathResourceLoader());
		Storage storage = new DefaultStorage(new DefaultDateTimeProvider(Clock.systemDefaultZone()),
				config.getClearDataInterval());
		ServerContext serverContext = new DefaultServerContext(config,
															   new ServerSocketFactory(),
															   new ServerConnectionManagerFactory(),
															   new ClientConnectionHandlerFactory(storage));
		ServerTask serverTask = new ServerTask(serverContext);
				
		return new DefaultServer(serverTask);
	}
}
