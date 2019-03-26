package com.revenat.jmemcached.server.domain.impl;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.revenat.jmemcached.protocol.model.Request;
import com.revenat.jmemcached.protocol.model.Response;
import com.revenat.jmemcached.server.domain.Storage;

@RunWith(MockitoJUnitRunner.Silent.class)
public class AbstractCommandHandlerTest {
	
	@Mock
	private AbstractCommandHandler nextInChain;
	@Mock
	private Storage storage;
	
	private AbstractCommandHandler handler;
	
	@Test(expected = NullPointerException.class)
	public void shouldNotAllowToCreateWithNullStorage() throws Exception {
		handler = new TestCommandHandler(null);
	}
	
	@Test
	public void shouldAllowToSetNextInChainHandler() throws Exception {
		handler = new TestCommandHandler(storage);
		
		handler.add(nextInChain);
		
		assertThat(handler.getNextInChain(), notNullValue());
	}
	
	@Test
	public void shouldAllowToPassNextInChainHandlerAlongTheChain() throws Exception {
		handler = new TestCommandHandler(storage);
		handler.add(nextInChain);
		
		handler.add(mock(AbstractCommandHandler.class));
		
		verify(nextInChain, times(1)).add(Mockito.any(AbstractCommandHandler.class));
	}
	
	@Test
	public void shouldAllowToGetStorage() throws Exception {
		handler = new TestCommandHandler(storage);
		
		assertThat(handler.getStorage(), notNullValue());
	}

	public static class TestCommandHandler extends AbstractCommandHandler {
		TestCommandHandler(Storage storage) {
			super(storage);
		}
		@Override
		public Response handle(Request request) {
			// do nothing
			return null;
		}
	}
}
