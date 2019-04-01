package com.revenat.jmemcached.server.domain.impl;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.revenat.jmemcached.exception.JMemcachedException;
import com.revenat.jmemcached.server.domain.ClientConnectionHandler;
import com.revenat.jmemcached.server.domain.Server;
import com.revenat.jmemcached.server.domain.ServerConnectionManager;
import com.revenat.jmemcached.server.domain.exception.ConnectionRejectedException;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ServerTaskTest {
	
	@Mock
	private ServerConnectionManager connectionManager;
	@Mock
	private Server server;
	
	private ServerContextStub serverContext;
	
	private ServerSocketStub serverSocketStub;
	private Socket clientSocketStub;
	
	private ServerTask serverTask;
	
	@Before
	public void setUp() throws IOException {
		clientSocketStub = new ClientSocketStub();
		serverSocketStub = new ServerSocketStub(clientSocketStub);
		serverContext = new ServerContextStub(serverSocketStub, connectionManager);
	}

	@Test(expected = NullPointerException.class)
	public void shouldNotAllowToCreateWithNullSocketContext() throws Exception {
		serverTask = new ServerTask(null);
	}
	
	@Test(expected = NullPointerException.class)
	public void shouldNotAllowToSetNullServer() throws Exception {
		serverTask = new ServerTask(serverContext);
		
		serverTask.setServer(null);
	}
	
	@Test(expected = JMemcachedException.class)
	public void shouldNotAllowToStartWithoutServerReference() throws Exception {
		serverTask = new ServerTask(serverContext);
		
		serverTask.run();
	}
	
	@Test
	public void shouldHandleClientConnection() throws Exception {
		serverTask = new ServerTask(serverContext);
		serverTask.setServer(server);
		serverSocketStub.throwExceptionOnAttempt(2); // to interrupt task, and make it handle only one client
		
		serverTask.run();
		
		verify(connectionManager, times(1)).establishConnection(any(ClientConnectionHandler.class));
	}
	
	@Test
	public void shouldCloseClientSocketIfConnectionHasBeenRejectedByConnectionManager() throws Exception {
		serverTask = new ServerTask(serverContext);
		serverTask.setServer(server);
		serverSocketStub.throwExceptionOnAttempt(2); // to interrupt task, and make it handle only one client
		doThrow(ConnectionRejectedException.class).when(connectionManager).establishConnection(any(ClientConnectionHandler.class));
		
		serverTask.run();
		
		assertTrue("Client socket should be closed", clientSocketStub.isClosed());
	}
	
	@Test
	public void shouldStopServerIfIOExceptionWhileAcceptingClientSocket() throws Exception {
		serverTask = new ServerTask(serverContext);
		serverTask.setServer(server);
		serverSocketStub.throwExceptionOnAttempt(1); // make serverSocket to throw IOException of first accept attempt
		
		serverTask.run();
		
		verify(server, times(1)).stop();
	}
	
	@Test
	public void shouldCloseServerContextWhenTheTaskIsDone() throws Exception {
		serverTask = new ServerTask(serverContext);
		serverTask.setServer(server);
		serverSocketStub.throwExceptionOnAttempt(2); // to interrupt task, and make it handle only one client
		
		serverTask.run();
		serverTask.shutdown();
		
		assertTrue("ServerContext should be closed", serverContext.isClosed());
	}
	
	private static class ServerContextStub implements ServerContext {
		private boolean isClosed = false;
		private ServerSocket serverSocket;
		private ServerConnectionManager connectionManager;
		private ClientConnectionHandler connectionHandler;

		ServerContextStub(ServerSocket serverSocket, ServerConnectionManager connectionManager) {
			this.serverSocket = serverSocket;
			this.connectionManager = connectionManager;
			this.connectionHandler = () -> {};
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
			return connectionHandler;
		}

		@Override
		public void close() {
			this.isClosed = true;
		}
		
		public boolean isClosed() {
			return isClosed;
		}
	}
	
	private static class ClientSocketStub extends Socket {
		private boolean isClosed;
		
		ClientSocketStub() {
			super();
			isClosed = false;
		}

		@Override
		public synchronized void close() throws IOException {
			this.isClosed = true;
		}

		@Override
		public boolean isClosed() {
			return isClosed;
		}
	}

}
