package com.revenat.jmemcached.server.domain.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;

import com.revenat.jmemcached.server.domain.ClientConnectionHandler;
import com.revenat.jmemcached.server.domain.ServerConnectionManager;
import com.revenat.jmemcached.server.domain.exception.ConnectionRejectedException;

/**
 * Default implementation of the {@link ServerConnectionManager} interface,
 * which encapsulates worker thread pool which in turn responsible for
 * allocating worker thread for each new connection between server and its
 * client.
 * 
 * @author Vitaly Dragun
 *
 */
class DefaultServerConnectionManager implements ServerConnectionManager {
	private final ExecutorService threadPool;

	DefaultServerConnectionManager(ExecutorService workerThreadPool) {
		this.threadPool = workerThreadPool;
	}

	@Override
	public void establishConnection(ClientConnectionHandler connectionHandler) {
		try {
			threadPool.submit(connectionHandler);
		} catch (RejectedExecutionException e) {
			throw new ConnectionRejectedException("All connection slots are occupied: new connection attempt has been rejected.", e);
		}
	}

	@Override
	public void shutdown() {
		threadPool.shutdownNow();
	}
}
