package com.revenat.jmemcached.server.domain.impl;

import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.revenat.jmemcached.exception.JMemcachedConfigException;
import com.revenat.jmemcached.protocol.RequestReader;
import com.revenat.jmemcached.protocol.ResponseWriter;
import com.revenat.jmemcached.protocol.impl.RequestConverter;
import com.revenat.jmemcached.protocol.impl.ResponseConverter;
import com.revenat.jmemcached.server.domain.ClientConnectionHandler;
import com.revenat.jmemcached.server.domain.CommandHandler;
import com.revenat.jmemcached.server.domain.DateTimeProvider;
import com.revenat.jmemcached.server.domain.RequestProcessor;
import com.revenat.jmemcached.server.domain.ResourceLoader;
import com.revenat.jmemcached.server.domain.ServerConfig;
import com.revenat.jmemcached.server.domain.ServerConnectionManager;
import com.revenat.jmemcached.server.domain.Storage;

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
	private final RequestReader requestReader;
	private final ResponseWriter responseWriter;
	private final Storage storage;
	private final CommandHandler commandHandler;
	private final RequestProcessor requestProcessor;
	
	DefaultServerConfig(Properties overrideProperties, DateTimeProvider dateTimeProvider,
			ResourceLoader resourceLoader) {
		this.applicationProperties = resourceLoader.loadProperties(SERVER_PROPERTIES);
		if (overrideProperties != null) {
			applicationProperties.putAll(overrideProperties);
		}
		this.requestReader = new RequestConverter();
		this.responseWriter = new ResponseConverter();
		this.storage = createStorage(dateTimeProvider);
		this.commandHandler = buildHandlersChain();
		this.requestProcessor = new DefaultRequestProcessor(requestReader, responseWriter, commandHandler);
	}

	private CommandHandler buildHandlersChain() {
		AbstractCommandHandler handler = new GetCommandHandler(storage);

		handler.add(new PutCommandHandler(storage));
		handler.add(new RemoveCommandHandler(storage));
		handler.add(new ClearCommandHandler(storage));
		handler.add(new UnsupportedCommandHandler(storage));
		
		return handler;
	}

	Storage createStorage(DateTimeProvider dateTimeProvider) {
		return new DefaultStorage(dateTimeProvider, getClearDataInterval());
	}

	@Override
	public ThreadFactory getWorkerThreadFactory() {
		return new ThreadFactory() {
			private int threadCount = 0;
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r, "Worker-"+threadCount++);
				t.setDaemon(true);
				return t;
			}
		};
	}
	
	ExecutorService createWorkerThreadPool() {
		return new ThreadPoolExecutor(getInitThreadCount(),
				getMaxThreadCount(),
				60L,
				TimeUnit.SECONDS,
				new SynchronousQueue<>(),
				getWorkerThreadFactory(),
				new ThreadPoolExecutor.AbortPolicy());
	}
	
	public ServerConnectionManager buildServerConnectionManager() {
		return new DefaultServerConnectionManager(createWorkerThreadPool());
	}

	@Override
	public int getClearDataInterval() {
		return getProperty(CLEAR_DATA_INTERVAL_PROPERTY, interval -> {
			if (interval < 1000) {
				throw new JMemcachedConfigException(
						CLEAR_DATA_INTERVAL_PROPERTY + " should be >= 1000 millis: " + interval);
			}
		});
	}

	@Override
	public int getServerPort() {
		return getProperty(SERVER_PORT_PROPERTY, port -> {
			if (port < 0 || port > 65535) {
				throw new JMemcachedConfigException(SERVER_PORT_PROPERTY +" should be between 0 and 65535: " + port);
			}
		});
	}

	@Override
	public int getInitThreadCount() {
		return getProperty(INIT_THREAD_COUNT_PROPERTY, threadCount -> {
			if (threadCount < 1) {
				throw new JMemcachedConfigException(INIT_THREAD_COUNT_PROPERTY + " should be >= 1: " + threadCount);
			}
		});
	}

	@Override
	public int getMaxThreadCount() {
		return getProperty(MAX_THREAD_COUNT_PROPERTY, threadCount -> {
			if (threadCount < 1) {
				throw new JMemcachedConfigException(MAX_THREAD_COUNT_PROPERTY + " should be >= 1: " + threadCount);
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
	public ClientConnectionHandler buildNewClientConnectionHandler(Socket clientSocket) {
		return new DefaultClientConnectionHandler(clientSocket, requestProcessor);
	}
	
	@Override
	public void close() throws Exception {
		storage.close();
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
