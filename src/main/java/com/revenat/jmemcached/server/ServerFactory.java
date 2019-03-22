package com.revenat.jmemcached.server;

import java.util.Properties;

/**
 * Factory which is responsible for building {@link Server} instances.
 * 
 * @author Vitaly Dragun
 *
 */
public interface ServerFactory {

	/**
	 * Builds new {@link Server} instance, using specified
	 * {@code overrideServerProperties} {@link Properties} parameter to override
	 * server configuration.
	 * 
	 * @param overrideServerProperties {@link Properties} with configuration
	 *                                 parameters to override server's
	 *                                 configurations.
	 * @return fully configured {@link Server} instance ready to use.
	 */
	Server buildNewServer(Properties overrideServerProperties);

	/**
	 * Builds new {@link Server} instance.
	 * 
	 * @return fully configured {@link Server} instance ready to use.
	 */
	Server buildNewServer();
}
