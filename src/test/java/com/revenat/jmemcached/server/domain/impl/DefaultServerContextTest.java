package com.revenat.jmemcached.server.domain.impl;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.revenat.jmemcached.server.domain.Storage;

@RunWith(MockitoJUnitRunner.Silent.class)
public class DefaultServerContextTest {
	
	@Mock
	private Storage storage;
	
	private DefaultServerContext context;
	
	@Before
	public void setup() {
		context = new DefaultServerContext(storage);
	}

	@Test(expected = NullPointerException.class)
	public void shouldNotAllowToCreateWithNullStorage() throws Exception {
		context = new DefaultServerContext(null);
	}
	
	@Test
	public void shouldAllowToGetRequestReader() throws Exception {
		assertThat(context.getRequestReader(), notNullValue());
	}
	
	@Test
	public void shouldAllowToGetResponseWriter() throws Exception {
		assertThat(context.getResponseWriter(), notNullValue());
	}
	
	@Test
	public void shouldAllowToGetCommandHandler() throws Exception {
		assertThat(context.getCommandHandler(), notNullValue());
	}
}
