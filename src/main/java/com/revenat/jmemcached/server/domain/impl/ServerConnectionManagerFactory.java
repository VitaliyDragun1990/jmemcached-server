package com.revenat.jmemcached.server.domain.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.revenat.jmemcached.server.domain.ServerConnectionManager;

/**
 * Factory class responsible for building new instances of the
 * {@link ServerConnectionManager} component.
 * 
 * @author Vitaly Dragun
 *
 */
class ServerConnectionManagerFactory {

	/**
	 * Creates new instance of the {@link ServerConnectionManager} component
	 * 
	 * @param initSynchronousConnection represents initial number of synchronous
	 *                                  connection server can establish with its
	 *                                  clients
	 * @param maxSynchronousConnection  represents maximal number of synchronous
	 *                                  connection server can establish with its
	 *                                  clients
	 */
	ServerConnectionManager createServerConnectionManager(int initSynchronousConnection, int maxSynchronousConnection) {
		ThreadFactory threadFactory = createWorkerThreadFactory();
		ExecutorService threadPool = createWorkerThreadPool(initSynchronousConnection,
															maxSynchronousConnection,
															threadFactory);
		return new DefaultServerConnectionManager(threadPool);
	}
	
	/**
	 * Returns specific {@link ThreadFactory} instance responsible for building new
	 * instances of the worker thread.
	 */
	private ThreadFactory createWorkerThreadFactory() {
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
	
	/**
	 * Creates {@link ExecutorService} instance which represents threads pool
	 * with worker threads.
	 */
	private ExecutorService createWorkerThreadPool(int initThreadCount, int maxThreadCount, ThreadFactory threadFactory) {
		return new ThreadPoolExecutor(
				initThreadCount,
				maxThreadCount,
				60L,
				TimeUnit.SECONDS,
				new SynchronousQueue<>(),
				threadFactory,
				new ThreadPoolExecutor.AbortPolicy());
	}
}
