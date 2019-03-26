
package com.revenat.jmemcached.server.domain.impl;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
public class PutCommandHandlerTest {
	private static final long ANY_TTL = 10000;
	private static final byte[] ANY_DATA = new byte[] {1, 2, 3};
	private static final String ANY_KEY = "any key";
	
	@Mock
	private Storage storage;
	@Mock
	private AbstractCommandHandler nextInChain;
	
	private PutCommandHandler handler;
	
	@Before
	public void setUp() {
		handler = new PutCommandHandler(storage);
		handler.add(nextInChain);
		when(storage.put(anyString(), any(byte[].class))).thenReturn(Status.ADDED);
		when(storage.put(anyString(), anyLong(), any(byte[].class))).thenReturn(Status.ADDED);
	}
	
	@Test
	public void shouldAllowToHandlePutRequestWithoutTtl() throws Exception {
		Request putRequest = Request.withKeyAndData(Command.PUT, ANY_KEY, ANY_DATA, null);
		
		Response response = handler.handle(putRequest);
		
		assertThat(response.getStatus(), equalTo(Status.ADDED));
		verify(storage, times(1)).put(anyString(), any(byte[].class));
		verifyZeroInteractions(nextInChain);
	}
	
	@Test
	public void shouldAllowToHandlePutRequestWithTtl() throws Exception {
		Request putRequest = Request.withKeyAndData(Command.PUT, ANY_KEY, ANY_DATA, ANY_TTL);
		
		Response response = handler.handle(putRequest);
		
		assertThat(response.getStatus(), equalTo(Status.ADDED));
		verify(storage, times(1)).put(anyString(), anyLong(), any(byte[].class));
		verifyZeroInteractions(nextInChain);
	}

	@Test
	public void shouldCallNextInChainIfHandleNotPutRequest() throws Exception {
		Request getRequest = Request.withKey(Command.GET, "any key");
		
		handler.handle(getRequest);
		
		verifyZeroInteractions(storage);
		verify(nextInChain, times(1)).handle(getRequest);
	}
}
