package com.revenat.jmemcached.server.domain.impl;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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

import com.revenat.jmemcached.protocol.RequestReader;
import com.revenat.jmemcached.protocol.ResponseWriter;
import com.revenat.jmemcached.protocol.model.Command;
import com.revenat.jmemcached.protocol.model.Request;
import com.revenat.jmemcached.protocol.model.Response;
import com.revenat.jmemcached.protocol.model.Status;
import com.revenat.jmemcached.server.domain.CommandHandler;

@RunWith(MockitoJUnitRunner.Silent.class)
public class DefaultClientSocketHandlerTest {
	private static final Request ANY_REQUEST = Request.empty(Command.CLEAR);
	private static final Response ANY_RESPONSE = Response.empty(Status.CLEARED);
	
	private ByteArrayInputStream clientInput = new ByteArrayInputStream(new byte[0]);
	private ByteArrayOutputStream clientOutput = new ByteArrayOutputStream();
	
	@Mock
	private RequestReader requestReader;
	@Mock
	private ResponseWriter responseWriter;
	@Mock
	private CommandHandler commandHandler;
	
	private Socket clientSocket;
	
	private DefaultClientSocketHandler socketHandler;
	
	@Before
	public void setUp() throws IOException {
		clientSocket = new SocketStub(clientInput, clientOutput);
		socketHandler = new DefaultClientSocketHandler(clientSocket, requestReader, responseWriter, commandHandler);
		setupMocks();
	}

	private void setupMocks() throws IOException {
		when(requestReader.readFrom(any(InputStream.class))).thenReturn(ANY_REQUEST).thenThrow(IOException.class);
		when(commandHandler.handle(any(Request.class))).thenReturn(ANY_RESPONSE);
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				OutputStream out = invocation.getArgument(0);
				out.write(1);
				out.flush();
				return null;
			}
		}).when(responseWriter).writeTo(any(OutputStream.class), any(Response.class));
	}

	@Test(expected = NullPointerException.class)
	public void shouldNotAllowToConstructWithNullClientSocket() throws Exception {
		socketHandler = new DefaultClientSocketHandler(null, requestReader, responseWriter, commandHandler);
	}
	
	@Test(expected = NullPointerException.class)
	public void shouldNotAllowToConstructWithNullRequestReader() throws Exception {
		socketHandler = new DefaultClientSocketHandler(clientSocket, null, responseWriter, commandHandler);
	}
	
	@Test(expected = NullPointerException.class)
	public void shouldNotAllowToConstructWithNullResponseWriter() throws Exception {
		socketHandler = new DefaultClientSocketHandler(clientSocket, requestReader, null, commandHandler);
	}
	
	@Test(expected = NullPointerException.class)
	public void shouldNotAllowToConstructWithNullCommandHandler() throws Exception {
		socketHandler = new DefaultClientSocketHandler(clientSocket, requestReader, responseWriter, null);
	}
	
	@Test
	public void shouldHandleClientRequest() throws Exception {
		Thread t = createSeparateThread(socketHandler);
		t.start();
		
		TimeUnit.SECONDS.sleep(1);
		
		assertThat(clientOutput.toByteArray().length, greaterThan(0));
		verify(requestReader, atLeast(1)).readFrom(any(InputStream.class));
		verify(commandHandler, atLeast(1)).handle(any(Request.class));
		verify(responseWriter, atLeast(1)).writeTo(any(OutputStream.class), any(Response.class));
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
		
		assertThat(clientOutput.toByteArray().length, equalTo(0));
		verifyZeroInteractions(requestReader);
		verifyZeroInteractions(commandHandler);
		verifyZeroInteractions(responseWriter);
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