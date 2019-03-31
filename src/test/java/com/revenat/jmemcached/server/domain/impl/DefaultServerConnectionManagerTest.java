package com.revenat.jmemcached.server.domain.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.revenat.jmemcached.server.domain.ClientConnectionHandler;
import com.revenat.jmemcached.server.domain.exception.ConnectionRejectedException;

public class DefaultServerConnectionManagerTest {
	private static final int MAX_NUMBER_OF_WORKER_THREADS = 2;
	private static final int INIT_NUMBER_OF_WORKER_THREADS = 1;

	private ExecutorService threadPool;

	private DefaultServerConnectionManager manager;

	@Before
	public void setUp() {
		threadPool = createThreadPool();
		manager = new DefaultServerConnectionManager(threadPool);
	}

	private ExecutorService createThreadPool() {
		return new ThreadPoolExecutor(INIT_NUMBER_OF_WORKER_THREADS, MAX_NUMBER_OF_WORKER_THREADS, 10L,
				TimeUnit.SECONDS, new SynchronousQueue<>(), createThreadFactory(),
				new ThreadPoolExecutor.AbortPolicy());
	}

	private ThreadFactory createThreadFactory() {
		return new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setDaemon(true);
				return t;
			}
		};
	}

	@After
	public void tearDown() {
		manager.shutdown();
	}

	@Test
	public void shouldAllowToEstablishNewConnection() throws Exception {
		ClientConnectionHandlerStub connectionHandler = new ClientConnectionHandlerStub();
		assertFalse("Should not be handled before submitting", connectionHandler.isHandled());

		submitNewConnection(connectionHandler, 1);
		TimeUnit.MILLISECONDS.sleep(600);

		assertTrue("Should be handled in a new connection", connectionHandler.isHandled());
	}

	@Test(expected = ConnectionRejectedException.class)
	public void shouldNotAllowToEstablishNewConnectionIfNoFreeWrokerThreadAvailable() throws Exception {
		submitNewConnection(new ClientConnectionHandlerStub(), 3);
	}

	@Test
	public void shouldShutdownThreadPoolWhenShutdownItself() throws Exception {
		manager.shutdown();

		assertTrue("Thread pool should be shut down", threadPool.isShutdown());
	}

	private void submitNewConnection(ClientConnectionHandler connectionHandler, int times) {
		for (int i = 0; i < times; i++) {
			manager.establishConnection(connectionHandler);
		}
	}

	private static class ClientConnectionHandlerStub implements ClientConnectionHandler {
		private boolean isHandled = false;

		@Override
		public void run() {
			try {
				TimeUnit.MILLISECONDS.sleep(300);
			} catch (InterruptedException e) {
			}
			isHandled = true;
		}

		public boolean isHandled() {
			return isHandled;
		}
	}
}
