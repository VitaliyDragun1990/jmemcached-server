package com.revenat.jmemcached.server.domain.impl;

import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.revenat.jmemcached.server.domain.ClientConnectionHandler;
import com.revenat.jmemcached.server.domain.ServerConfig;
import com.revenat.jmemcached.server.domain.ServerConnectionManager;

@RunWith(MockitoJUnitRunner.Silent.class)
public class DefaultServerContextTest {
	private static final Socket ANY_CLIENT_SOCKET = new Socket();

	@Mock
	private ServerConfig serverConfig;
	
	private ServerSocket serverSocketStub;
	private ServerConnectionManagerStub connectionManagerStub;
	private ClientConnectionHandler connectionHandlerStub;
	
	@Mock
	private ServerConnectionManagerFactory connectionManagerFactory;
	@Mock
	private ClientConnectionHandlerFactory handlerFactory;
	private ServerSocketFactoryStub serverSocketFactory;
	
	private DefaultServerContext context;
	
	@Before
	public void setup() throws IOException {
		serverSocketStub =  new ServerSocketStub(ANY_CLIENT_SOCKET);
		connectionManagerStub = new ServerConnectionManagerStub();
		connectionHandlerStub = () -> {};
		serverSocketFactory = new ServerSocketFactoryStub(serverSocketStub);
		when(connectionManagerFactory.createServerConnectionManager(anyInt(), anyInt())).thenReturn(connectionManagerStub);
		when(handlerFactory.createClientConnectionHandler(any(Socket.class))).thenReturn(connectionHandlerStub);
		
		context = new DefaultServerContext(serverConfig, serverSocketFactory, connectionManagerFactory, handlerFactory);
	}

	@Test(expected = NullPointerException.class)
	public void shouldNotAllowToCreateWithNullServerSocketFactory() throws Exception {
		context = new DefaultServerContext(serverConfig, null, connectionManagerFactory, handlerFactory);
	}
	
	@Test(expected = NullPointerException.class)
	public void shouldNotAllowToCreateWithNullServerConfig() throws Exception {
		context = new DefaultServerContext(null, serverSocketFactory, connectionManagerFactory, handlerFactory);
	}
	
	@Test(expected = NullPointerException.class)
	public void shouldNotAllowToCreateWithNullConnectionManagerFactory() throws Exception {
		context = new DefaultServerContext(serverConfig, serverSocketFactory, null, handlerFactory);
	}
	
	@Test(expected = NullPointerException.class)
	public void shouldNotAllowToCreateWithNullClientConnectionHandlerFactory() throws Exception {
		context = new DefaultServerContext(serverConfig, serverSocketFactory, connectionManagerFactory, null);
	}
	
	@Test
	public void shouldReturnServerSocketCreateByServerSocketFactory() throws Exception {
		ServerSocket serverSocket = context.getServerSocket();
		
		assertThat(serverSocket, sameInstance(serverSocketStub));
	}
	
	@Test
	public void shouldReturnServerConnectionManagerCreatedByServerConnectionmanagerFactory() throws Exception {
		ServerConnectionManager connectionManager = context.getServerConnectionManager();
		
		assertThat(connectionManager, sameInstance(connectionManagerStub));
		verify(connectionManagerFactory, times(1)).createServerConnectionManager(anyInt(), anyInt());
	}
	
	@Test
	public void shouldReturnClientConnectionHandlerCreatedByClientConnectionHandlerFactory() throws Exception {
		ClientConnectionHandler connectionHandler = context.buildNewClientConnectionHandler(ANY_CLIENT_SOCKET);
		
		assertThat(connectionHandler, sameInstance(connectionHandlerStub));
		verify(handlerFactory, times(1)).createClientConnectionHandler(any(Socket.class));
	}
	
	@Test
	public void shouldCloseServerSocketWhenClosed() throws Exception {
		assertFalse("ServerSocket should be opened", serverSocketStub.isClosed());
		
		context.close();
		
		assertTrue("ServerSocket should be closed", serverSocketStub.isClosed());
	}
	
	@Test
	public void shouldShutdownServerConnectionManagerWhenClosed() throws Exception {
		assertFalse("ServerConnectionmanager should be up", connectionManagerStub.isShutdown());
		
		context.close();
		
		assertTrue("ServerConnectionmanager should be stutdown", serverSocketStub.isClosed());
	}
	
	@Test
	public void shouldCloseClientConnectionHandlerFactoryWhenClosed() throws Exception {
		context.close();
		
		verify(handlerFactory, times(1)).close();
	}
	
	private static class ServerSocketFactoryStub extends ServerSocketFactory {
		private ServerSocket serverSocket;

		ServerSocketFactoryStub(ServerSocket serverSocket) {
			this.serverSocket = serverSocket;
		}

		@Override
		ServerSocket createServerSocket(int serverPort) {
			return serverSocket;
		}
	}
	
	private static class ServerConnectionManagerStub implements ServerConnectionManager {
		private boolean isClosed = false;
		
		@Override
		public void establishConnection(ClientConnectionHandler connectionHandler) {
		}

		@Override
		public void shutdown() {
			isClosed = true;
		}
		
		boolean isShutdown() {
			return isClosed;
		}
	}
}
