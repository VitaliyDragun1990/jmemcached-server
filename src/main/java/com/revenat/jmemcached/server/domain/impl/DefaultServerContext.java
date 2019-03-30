package com.revenat.jmemcached.server.domain.impl;

import static java.util.Objects.requireNonNull;

import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revenat.jmemcached.server.domain.ClientConnectionHandler;
import com.revenat.jmemcached.server.domain.ServerConfig;
import com.revenat.jmemcached.server.domain.ServerConnectionManager;
import com.revenat.jmemcached.server.domain.ServerContext;

/**
 * Default implementation of the {@link ServerContext} interface.
 * 
 * @author Vitaly Dragun
 *
 */
class DefaultServerContext implements ServerContext {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServerContext.class);
	
	private final ServerConnectionManager connectionManager;
	private final ServerSocket serverSocket;
	private final ClientConnectionHandlerFactory handlerFactory;
	
	DefaultServerContext(ServerConfig serverConfig,
						 ServerSocketFactory socketFactory,
						 ServerConnectionManagerFactory connectionManagerFactory,
						 ClientConnectionHandlerFactory connectionHandlerFactory
			) {
		requireNonNull(serverConfig, "serverConfig can not be null");
		requireNonNull(socketFactory, "socketFactory can not be null");
		requireNonNull(connectionManagerFactory, "connectionManagerFactory can not be null");
		requireNonNull(connectionHandlerFactory, "connectionHandlerFactory can not be null");
		
		int maxThreadCount = serverConfig.getMaxThreadCount();
		int initThreadCount = serverConfig.getInitThreadCount();
		int serverPort = serverConfig.getServerPort();
		
		this.handlerFactory = connectionHandlerFactory;
		this.connectionManager = connectionManagerFactory.createServerConnectionManager(initThreadCount, maxThreadCount);
		this.serverSocket = socketFactory.createServerSocket(serverPort);
	}

	@Override
	public ServerSocket getServerSocket() {
		return serverSocket;
	}

	@Override
	public ServerConnectionManager getServerConnectionManager() {
		return connectionManager;
	}

	@Override
	public ClientConnectionHandler buildNewClientConnectionHandler(Socket clientSocket) {
		return handlerFactory.createClientConnectionHandler(clientSocket);
	}
	
	@Override
	public void close() {
		closeResource(serverSocket, "Error while closing Server socket");
		connectionManager.shutdown();
		closeResource(handlerFactory, "Error while closing connectionHandlerFactory");
	}
	
	private void closeResource(AutoCloseable resource, String msg) {
		try {
			resource.close();
		} catch (Exception e) {
			LOGGER.error(msg, e);
		}
	}
}
