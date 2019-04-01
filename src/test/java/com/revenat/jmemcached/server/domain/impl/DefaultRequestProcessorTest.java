package com.revenat.jmemcached.server.domain.impl;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.revenat.jmemcached.exception.JMemcachedException;
import com.revenat.jmemcached.protocol.RequestReader;
import com.revenat.jmemcached.protocol.ResponseWriter;
import com.revenat.jmemcached.protocol.model.Command;
import com.revenat.jmemcached.protocol.model.Request;
import com.revenat.jmemcached.protocol.model.Response;
import com.revenat.jmemcached.protocol.model.Status;
import com.revenat.jmemcached.server.domain.CommandHandler;

@RunWith(MockitoJUnitRunner.Silent.class)
public class DefaultRequestProcessorTest {
	private static final Request ANY_REQUEST = Request.empty(Command.CLEAR);
	private static final Response ANY_RESPONSE = Response.empty(Status.CLEARED);
	
	private ByteArrayInputStream clientInput = new ByteArrayInputStream(new byte[0]);
	private ByteArrayOutputStream clientOutput = new ByteArrayOutputStream();
	
	@Mock
	private RequestReader reader;
	@Mock
	private ResponseWriter writer;
	@Mock
	private CommandHandler handler;
	
	private DefaultRequestProcessor processor;
	
	@Before
	public void setUp() throws IOException {
		processor = new DefaultRequestProcessor(reader, writer, handler);
		setupMocks();
	}
	
	private void setupMocks() throws IOException {
		when(reader.readFrom(any(InputStream.class))).thenReturn(ANY_REQUEST);
		when(handler.handle(any(Request.class))).thenReturn(ANY_RESPONSE);
		doAnswer(new Answer<Void>() {
			@Override
			public Void answer(InvocationOnMock invocation) throws Throwable {
				OutputStream out = invocation.getArgument(0);
				out.write(1);
				out.flush();
				return null;
			}
		}).when(writer).writeTo(any(OutputStream.class), any(Response.class));
	}

	@Test(expected = NullPointerException.class)
	public void shouldNotAllowToCreateWithNullRequestReader() throws Exception {
		processor = new DefaultRequestProcessor(null, writer, handler);
	}
	
	@Test(expected = NullPointerException.class)
	public void shouldNowAllowToCreateWithNullResponseWriter() throws Exception {
		processor = new DefaultRequestProcessor(reader, null, handler);
	}
	
	@Test(expected = NullPointerException.class)
	public void shouldNotAllowToCreateWithNullCommandHandler() throws Exception {
		processor = new DefaultRequestProcessor(reader, writer, null);
	}
	
	@Test
	public void shouldProcessClientRequest() throws Exception {
		processor.process(clientInput, clientOutput);
		
		assertThat(clientOutput.toByteArray().length, greaterThan(0));
		verify(reader, atLeast(1)).readFrom(any(InputStream.class));
		verify(handler, atLeast(1)).handle(any(Request.class));
		verify(writer, atLeast(1)).writeTo(any(OutputStream.class), any(Response.class));
	}
	
	@Test(expected = JMemcachedException.class)
	public void shouldThrowJMemcachedExceptionIfRequestProcessingFailed() throws Exception {
		when(handler.handle(any(Request.class))).thenThrow(RuntimeException.class);
		
		processor.process(clientInput, clientOutput);
	}
}
