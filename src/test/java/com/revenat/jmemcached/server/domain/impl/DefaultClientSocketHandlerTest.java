package com.revenat.jmemcached.server.domain.impl;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.revenat.jmemcached.server.domain.RequestProcessor;

@RunWith(MockitoJUnitRunner.Silent.class)
public class DefaultClientSocketHandlerTest {
	private ByteArrayInputStream clientInput = new ByteArrayInputStream(new byte[0]);
	private ByteArrayOutputStream clientOutput = new ByteArrayOutputStream();
	
	@Mock
	private RequestProcessor requestProcessor;
	
	private Socket clientSocket;
	
	private DefaultClientSocketHandler socketHandler;
	
	@Before
	public void setUp() throws IOException {
		clientSocket = new SocketStub(clientInput, clientOutput);
		socketHandler = new DefaultClientSocketHandler(clientSocket, requestProcessor);
		clientDisconnectAfterFirstRequest();
	}

	private void clientDisconnectAfterFirstRequest() throws IOException {
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				return null;
			}
		}).doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				throw new EOFException();
			}
		}).when(requestProcessor).process(any(InputStream.class), any(OutputStream.class));
	}

	@Test(expected = NullPointerException.class)
	public void shouldNotAllowToConstructWithNullClientSocket() throws Exception {
		socketHandler = new DefaultClientSocketHandler(null, requestProcessor);
	}
	
	@Test(expected = NullPointerException.class)
	public void shouldNotAllowToConstructWithNullRequestProcessor() throws Exception {
		socketHandler = new DefaultClientSocketHandler(clientSocket, null);
	}
	
	@Test
	public void shouldHandleClientRequest() throws Exception {
		Thread t = createSeparateThread(socketHandler);
		t.start();
		
		TimeUnit.SECONDS.sleep(1);
		
		verify(requestProcessor, atLeast(1)).process(any(InputStream.class), any(OutputStream.class));
	}
	
	@Test
	public void shouldCloseClientSocketAfterFinishingWithIt() throws Exception {
		Thread t = createSeparateThread(socketHandler);
		t.start();
		
		TimeUnit.SECONDS.sleep(1);
		
		assertTrue("Client socket should be closed", clientSocket.isClosed());
	}
	
	@Test
	public void shouldNotHandleClientRequestIfWorkingThreadWasInterrupted() throws Exception {
		Thread t = createInterruptedThread(socketHandler);
		t.start();
		
		TimeUnit.SECONDS.sleep(1);
		
		verifyZeroInteractions(requestProcessor);
	}

	private static Thread createSeparateThread(DefaultClientSocketHandler socketHandler) {
		Thread t = new Thread(socketHandler);
		t.setDaemon(true);
		return t;
	}
	
	private static Thread createInterruptedThread(final Runnable job) {
		Thread t = new Thread() {
			@Override
			public void run() {
				interrupt();
				job.run();
			}
		};
		t.setDaemon(true);
		return t;
	}

	private static class SocketStub extends Socket {
		private InputStream input;
		private OutputStream output;
		
		SocketStub(InputStream input, OutputStream out) {
			this.input = input;
			this.output = out;
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return input;
		}

		@Override
		public OutputStream getOutputStream() throws IOException {
			return output;
		}
	}
}
