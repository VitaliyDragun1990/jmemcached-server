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
import com.revenat.jmemcached.server.domain.ServerStorage;

@RunWith(MockitoJUnitRunner.Silent.class)
public class RemoveCommandHandlerTest {
	
	@Mock
	private ServerStorage storage;
	@Mock
	private AbstractCommandHandler nextInChain;
	
	private RemoveCommandHandler handler;
	
	@Before
	public void setUp() {
		handler = new RemoveCommandHandler(storage);
		handler.add(nextInChain);
		when(storage.remove(anyString())).thenReturn(Status.REMOVED);
	}
	
	@Test
	public void shouldAllowToHandleRemoveRequest() throws Exception {
		Request removeRequest = Request.withKey(Command.REMOVE, "any key");
		
		Response response = handler.handle(removeRequest);
		
		assertThat(response.getStatus(), equalTo(Status.REMOVED));
		verify(storage, times(1)).remove(anyString());
		verifyZeroInteractions(nextInChain);
	}
	
	@Test
	public void shouldCallNextInChainIfHandleNotRemoveRequest() throws Exception {
		Request getRequest = Request.withKey(Command.GET, "any key");
		
		handler.handle(getRequest);
		
		verifyZeroInteractions(storage);
		verify(nextInChain, times(1)).handle(getRequest);
	}
}
