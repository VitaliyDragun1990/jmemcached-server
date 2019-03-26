package com.revenat.jmemcached.server.domain.impl;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.revenat.jmemcached.protocol.model.Command;
import com.revenat.jmemcached.protocol.model.Request;
import com.revenat.jmemcached.protocol.model.Response;
import com.revenat.jmemcached.protocol.model.Status;
import com.revenat.jmemcached.server.domain.Storage;

@RunWith(MockitoJUnitRunner.Silent.class)
public class GetCommandHandlerTest {

	private static final String UNKNOWN_KEY = "unknown key";
	private static final String STORED_KEY = "stored key";
	private static final byte[] NOT_EMPTY_DATA = new byte[] { 1, 2, 3 };
	private static final byte[] EMPTY_DATA = new byte[0];
	@Mock
	private Storage storage;
	@Mock
	private AbstractCommandHandler nextInChain;

	private GetCommandHandler handler;

	@Before
	public void setUp() {
		handler = new GetCommandHandler(storage);
		handler.add(nextInChain);
		when(storage.get(STORED_KEY)).thenReturn(NOT_EMPTY_DATA);
		when(storage.get(UNKNOWN_KEY)).thenReturn(EMPTY_DATA);
	}

	@Test
	public void shouldAllowToHandleGetRequestWithSuccessResult() throws Exception {
		Request getRequest = Request.withKey(Command.GET, STORED_KEY);

		Response response = handler.handle(getRequest);

		assertThat(response.getStatus(), equalTo(Status.GOTTEN));
		verify(storage, times(1)).get(anyString());
		verifyZeroInteractions(nextInChain);
	}

	@Test
	public void shouldAllowToHandleGetRequestWithNotFoundResult() throws Exception {
		Request getRequest = Request.withKey(Command.GET, UNKNOWN_KEY);

		Response response = handler.handle(getRequest);

		assertThat(response.getStatus(), equalTo(Status.NOT_FOUND));
		verify(storage, times(1)).get(anyString());
		verifyZeroInteractions(nextInChain);
	}

	@Test
	public void shouldCallNextInChainIfHandleNotGetRequest() throws Exception {
		Request removeRequest = Request.withKey(Command.REMOVE, STORED_KEY);

		handler.handle(removeRequest);

		verifyZeroInteractions(storage);
		verify(nextInChain, times(1)).handle(removeRequest);
	}
}
