package com.revenat.jmemcached.server.domain.impl;

import java.util.Properties;

import com.revenat.jmemcached.exception.JMemcachedConfigException;
import com.revenat.jmemcached.server.domain.ResourceLoader;
import com.revenat.jmemcached.server.domain.ServerConfig;

/**
 * Default implementation of the {@link ServerConfig}
 * 
 * @author Vitaly Dragun
 *
 */
class DefaultServerConfig implements ServerConfig {
	static final String MAX_THREAD_COUNT_PROPERTY = "jmemcached.server.max.thread.count";
	static final String INIT_THREAD_COUNT_PROPERTY = "jmemcached.server.init.thread.count";
	static final String SERVER_PORT_PROPERTY = "jmemcached.server.port";
	static final String CLEAR_DATA_INTERVAL_PROPERTY = "jmemcached.storage.clear.data.interval";
	static final String SERVER_PROPERTIES = "server.properties";
	
	private final Properties applicationProperties;
	
	private final int clearDataInterval;
	private final int serverPort;
	private final int initThreadCount;
	private final int maxThreadCount;
	
	DefaultServerConfig(Properties overrideProperties, ResourceLoader resourceLoader) {
		this.applicationProperties = resourceLoader.loadProperties(SERVER_PROPERTIES);
		if (overrideProperties != null) {
			applicationProperties.putAll(overrideProperties);
		}
		
		this.clearDataInterval = getProperty(CLEAR_DATA_INTERVAL_PROPERTY, interval -> {
			if (interval < 1000) {
				throw new JMemcachedConfigException(
						CLEAR_DATA_INTERVAL_PROPERTY + " should be >= 1000 millis: " + interval);
			}
		});
		this.serverPort = getProperty(SERVER_PORT_PROPERTY, port -> {
			if (port < 0 || port > 65535) {
				throw new JMemcachedConfigException(SERVER_PORT_PROPERTY +" should be between 0 and 65535: " + port);
			}
		});
		this.initThreadCount = getProperty(INIT_THREAD_COUNT_PROPERTY, threadCount -> {
			if (threadCount < 1) {
				throw new JMemcachedConfigException(INIT_THREAD_COUNT_PROPERTY + " should be >= 1: " + threadCount);
			}
		});
		this.maxThreadCount = getProperty(MAX_THREAD_COUNT_PROPERTY, threadCount -> {
			if (threadCount < initThreadCount) {
				throw new JMemcachedConfigException(MAX_THREAD_COUNT_PROPERTY + " should be >= " + initThreadCount + "("
			+ INIT_THREAD_COUNT_PROPERTY +"): " + threadCount);
			}
		});
	}
	
	private int getProperty(String propertyName, BoundaryChecker checker) {
		String propertyValue = applicationProperties.getProperty(propertyName);
		try {
			int value = Integer.parseInt(propertyValue);
			checker.check(value);
			return value;
		} catch (NumberFormatException | NullPointerException e) {
			throw new JMemcachedConfigException(propertyName + " should be a number: " + propertyValue);
		}
	}

	@Override
	public int getClearDataInterval() {
		return clearDataInterval;
	}

	@Override
	public int getServerPort() {
		return serverPort;
	}

	@Override
	public int getInitThreadCount() {
		return initThreadCount;
	}

	@Override
	public int getMaxThreadCount() {
		return maxThreadCount;
	}
	
	@Override
	public String toString() {
		return String.format("DefaultServerConfig: port=%d, initThreadCount=%d, maxThreadCount=%d,"
				+ " clearDataInterval=%d millis",
				getServerPort(), getInitThreadCount(), getMaxThreadCount(), getClearDataInterval());
	}
	
	@FunctionalInterface
	private interface BoundaryChecker {
		void check(int value);
	}
}
