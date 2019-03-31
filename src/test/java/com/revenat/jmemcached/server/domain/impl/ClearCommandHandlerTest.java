package com.revenat.jmemcached.server.domain.impl;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
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
import com.revenat.jmemcached.server.domain.ServerStorage;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ClearCommandHandlerTest {
	
	@Mock
	private ServerStorage storage;
	@Mock
	private AbstractCommandHandler nextInChain;
	
	private ClearCommandHandler handler;
	
	@Before
	public void setUp() {
		handler = new ClearCommandHandler(storage);
		handler.add(nextInChain);
		when(storage.clear()).thenReturn(Status.CLEARED);
	}
	
	@Test
	public void shouldAllowToHandleClearRequest() throws Exception {
		Request clearRequest = Request.empty(Command.CLEAR);
		
		Response response = handler.handle(clearRequest);
		
		assertThat(response.getStatus(), equalTo(Status.CLEARED));
		verify(storage, times(1)).clear();
		verifyZeroInteractions(nextInChain);
	}
	
	@Test
	public void shouldCallNextInChainIfHandleNotClearRequest() throws Exception {
		Request getRequest = Request.withKey(Command.GET, "any key");
		
		handler.handle(getRequest);
		
		verifyZeroInteractions(storage);
		verify(nextInChain, times(1)).handle(getRequest);
	}
}
