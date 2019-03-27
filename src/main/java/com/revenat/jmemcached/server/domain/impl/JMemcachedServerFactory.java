package com.revenat.jmemcached.server.domain.impl;

import java.time.Clock;
import java.util.Properties;

import com.revenat.jmemcached.server.domain.Server;
import com.revenat.jmemcached.server.domain.ServerConfig;
import com.revenat.jmemcached.server.domain.ServerFactory;

/**
 * Default implementation of the {@link ServerFactory}.
 * 
 * @author Vitaly Dragun
 *
 */
public class JMemcachedServerFactory implements ServerFactory {

	@Override
	public Server buildNewServer(Properties overrideServerProperties) {
		ServerConfig config = new DefaultServerConfig(
				overrideServerProperties,
				new DefaultDateTimeProvider(Clock.systemDefaultZone()),
				new ClassPathResourceLoader());
		return new DefaultServer(config);
	}

	@Override
	public Server buildNewServer() {
		ServerConfig config = new DefaultServerConfig(
				null,
				new DefaultDateTimeProvider(Clock.systemDefaultZone()),
				new ClassPathResourceLoader());
		return new DefaultServer(config);
	}
}
