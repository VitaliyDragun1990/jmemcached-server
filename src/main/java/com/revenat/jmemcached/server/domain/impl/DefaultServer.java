package com.revenat.jmemcached.server.domain.impl;

import static java.util.Objects.requireNonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revenat.jmemcached.exception.JMemcachedException;
import com.revenat.jmemcached.server.domain.Server;

/**
 * Default implementation of the {@link Server} interface.
 * 
 * @author Vitaly Dragun
 *
 */
class DefaultServer implements Server {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServer.class);
	
	private final ServerTask serverTask;
	private final Thread mainServerThread;
	private volatile boolean serverStopped;
	
	DefaultServer(ServerTask serverTask) {
		this.serverTask = requireNonNull(serverTask, "serverTask can not be null");
		this.serverTask.setServer(this);
		this.mainServerThread = createMainServerThread(serverTask);
	}

	private Thread createMainServerThread(Runnable serverJob) {
		Thread thread = new Thread(serverJob);
		thread.setName("Main Server Thread");
		thread.setPriority(Thread.MAX_PRIORITY);
		thread.setDaemon(false);
		return thread;
	}

	@Override
	public void start() {
		if (mainServerThread.getState() != Thread.State.NEW) {
			throw new JMemcachedException("Current server instance has been already started or stopped!"
					+ "Please create a new server instance.");
		}
		Runtime.getRuntime().addShutdownHook(getShutdownHook());
		mainServerThread.start();
		LOGGER.info("Server started.");
	}
	
	private Thread getShutdownHook() {
		return new Thread(() ->  {
			if (!serverStopped) {
				shutdownServer();
			}
		}, "ShutdownHook") ;
	}

	@Override
	public void stop() {
		LOGGER.info("Detected stop condition");
		shutdownServer();
	}
	
	private void shutdownServer() {
		mainServerThread.interrupt();
		serverTask.shutdown();
		LOGGER.info("Server stopped");
		serverStopped = true;
	}
}
