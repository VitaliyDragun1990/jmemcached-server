package com.revenat.jmemcached.server.domain.impl;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revenat.jmemcached.exception.JMemcachedException;
import com.revenat.jmemcached.server.domain.Server;
import com.revenat.jmemcached.server.domain.ServerConfig;

/**
 * Default implementation of the {@link Server} interface.
 * 
 * @author Vitaly Dragun
 *
 */
class DefaultServer implements Server {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServer.class);
	
	private final ServerConfig config;
	private final ServerSocket serverSocket;
	private final ExecutorService executorService;
	private final Thread mainServerThread;
	private volatile boolean serverStopped;
	
	DefaultServer(ServerConfig serverConfig) {
		this.config = requireNonNull(serverConfig, "serverConfig can not be null");
		this.serverSocket = createServerSocket();
		this.executorService = createExecutorService();
		this.mainServerThread = createMainServerThread(createServerJob());
	}

	ServerSocket createServerSocket() {
		try {
			ServerSocket socket = new ServerSocket(config.getServerPort());
			socket.setReuseAddress(true);
			return socket;
		} catch (IOException e) {
			throw new JMemcachedException("Can not create server socket with port=" + config.getServerPort(), e);
		}
	}

	ExecutorService createExecutorService() {
		ThreadFactory factory = config.getWorkerThreadFactory();
		int initThreadCount = config.getInitThreadCount();
		int maxThreadCount = config.getMaxThreadCount();
		return new ThreadPoolExecutor(initThreadCount, maxThreadCount, 60L, TimeUnit.SECONDS,
				new SynchronousQueue<>(), factory, new ThreadPoolExecutor.AbortPolicy());
	}

	Runnable createServerJob() {
		return new ServerTask(serverSocket,
				client -> executorService.submit(config.buildNewClientConnectionHandler(client)),
				this::shutdownServer);
	}

	void shutdownServer() {
		try {
			config.close();
		} catch (Exception e) {
			LOGGER.error("ServerConfig close failed: " + e.getMessage(), e);
		}
		executorService.shutdownNow();
		closeServerSocket();
		LOGGER.info("Server stopped");
		serverStopped = true;
	}

	Thread createMainServerThread(Runnable serverJob) {
		Thread thread = new Thread(serverJob);
		thread.setName("Main Server Thread");
		thread.setPriority(Thread.MAX_PRIORITY);
		thread.setDaemon(false);
		return thread;
	}

	@Override
	public void start() {
		if (mainServerThread.getState() != Thread.State.NEW) {
			throw new JMemcachedException("Current JMemcached server instance has been already started or stopped!."
					+ "Please create a new server instance.");
		}
		Runtime.getRuntime().addShutdownHook(getShutdownHook());
		mainServerThread.start();
		LOGGER.info("Server started: {}", config);
	}
	
	Thread getShutdownHook() {
		return new Thread(() ->  {
			if (!serverStopped) {
				shutdownServer();
			}
		}, "ShutdownHook") ;
	}

	@Override
	public void stop() {
		LOGGER.info("Detected stop command");
		mainServerThread.interrupt();
		closeServerSocket();
	}

	void closeServerSocket() {
		try {
			if (!serverSocket.isClosed()) {
				serverSocket.close();
			}
		} catch (IOException e) {
			LOGGER.warn("Error while closing server socket: " + e.getMessage(), e);
		}
	}
}
