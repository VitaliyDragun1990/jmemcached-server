package com.revenat.jmemcached.server.domain.impl;

import static org.hamcrest.Matchers.containsString;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.revenat.jmemcached.exception.JMemcachedException;
import com.revenat.jmemcached.protocol.model.Command;
import com.revenat.jmemcached.protocol.model.Request;
import com.revenat.jmemcached.server.domain.ServerStorage;

@RunWith(MockitoJUnitRunner.Silent.class)
public class UnsupportedCommandHandlerTest {

	@Rule
	public ExpectedException expected = ExpectedException.none();
	
	@Mock
	private ServerStorage storage;
	
	@Test
	public void shouldThrowExceptionWhenHandleAnyRequest() throws Exception {
		UnsupportedCommandHandler handler = new UnsupportedCommandHandler(storage);
		Request anyRequest = Request.empty(Command.CLEAR);
		expected.expect(JMemcachedException.class);
		expected.expectMessage(containsString(String.format("Unsupported command: %s", anyRequest.getCommand())));
		
		handler.handle(anyRequest);
	}

}
