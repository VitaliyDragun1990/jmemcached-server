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
import java.util.concurrent.RejectedExecutionException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.revenat.jmemcached.server.domain.impl.ServerTask.ServerShutdownHandler;
import com.revenat.jmemcached.server.domain.impl.ServerTask.ClientSocketHandler;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ServerTaskTest {
	
	@Mock
	private ClientSocketHandler socketHandler;
	@Mock
	private ServerShutdownHandler shutdownHandler;
	
	private ServerSocketStub serverSocketStub;
	private Socket clientSocketStub;
	
	private ServerTask serverTask;
	
	@Before
	public void setUp() throws IOException {
		clientSocketStub = new ClientSocketStub();
		serverSocketStub = new ServerSocketStub(clientSocketStub);
	}

	@Test(expected = NullPointerException.class)
	public void shouldNotAllowToCreateWithNullServerSocket() throws Exception {
		serverTask = new ServerTask(null, socketHandler, shutdownHandler);
	}
	
	@Test(expected = NullPointerException.class)
	public void shouldNotAllowToCreateWithNullSocketHandler() throws Exception {
		serverTask = new ServerTask(serverSocketStub, null, shutdownHandler);
	}
	
	@Test(expected = NullPointerException.class)
	public void shouldNotAllowToCreateWithNullServerShutdownHandler() throws Exception {
		serverTask = new ServerTask(serverSocketStub, socketHandler, null);
	}
	
	@Test
	public void shouldHandleClientSocket() throws Exception {
		serverTask = new ServerTask(serverSocketStub, socketHandler, shutdownHandler);
		
		serverTask.handleClientSocket(clientSocketStub);
		
		verify(socketHandler, times(1)).handle(clientSocketStub);
	}
	
	@Test
	public void shouldCloseClientSocketIfConnectionHasBeenRejectedByServer() throws Exception {
		serverTask = new ServerTask(serverSocketStub, socketHandler, shutdownHandler);
		doThrow(RejectedExecutionException.class).when(socketHandler).handle(any(Socket.class));
		
		serverTask.handleClientSocket(clientSocketStub);
		
		assertTrue("Client socket should be closed", clientSocketStub.isClosed());
	}
	
	@Test
	public void shouldShutdownServerIfErrorHappendDuringEstablishingConnectionWithClient() throws Exception {
		serverTask = new ServerTask(serverSocketStub, socketHandler, shutdownHandler);
		serverSocketStub.throwExceptionOnAttempt(2);
		
		serverTask.run();
		
		verify(shutdownHandler, times(1)).shutdownServer();
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
	
	private static class ServerSocketStub extends ServerSocket {
		private Socket clientSocket;
		private boolean isClosed;
		private int attemptCount;
		private int attemptWhenException = -1;

		public ServerSocketStub(Socket clientSocket) throws IOException {
			super();
			this.clientSocket = clientSocket;
			this.isClosed = false;
			this.attemptCount = 0;
		}

		public void throwExceptionOnAttempt(int attemptNumber) {
			this.attemptWhenException = attemptNumber;
		}

		@Override
		public Socket accept() throws IOException {
			attemptCount++;
			if (attemptCount == attemptWhenException) {
				throw new IOException("Error during establishing connection with client socket.");
			}
			return clientSocket;
		}

		@Override
		public void close() throws IOException {
			this.isClosed = true;
		}

		@Override
		public boolean isClosed() {
			return isClosed;
		}
	}

}
