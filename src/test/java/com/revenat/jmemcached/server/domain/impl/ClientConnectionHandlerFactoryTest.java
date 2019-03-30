package com.revenat.jmemcached.server.domain.impl;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.net.Socket;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.revenat.jmemcached.exception.JMemcachedException;
import com.revenat.jmemcached.server.domain.ClientConnectionHandler;
import com.revenat.jmemcached.server.domain.Storage;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ClientConnectionHandlerFactoryTest {

	@Mock
	private Storage storage;
	
	private ClientConnectionHandlerFactory factory;
	
	@Before
	public void setUp() {
		factory = new ClientConnectionHandlerFactory(storage);
	}
	
	@Test(expected = NullPointerException.class)
	public void shouldNotAllowToCreateWithNullStorage() throws Exception {
		factory = new ClientConnectionHandlerFactory(null);
	}
	
	@Test
	public void shouldAllowToCreateClientConnectionHandler() throws Exception {
		ClientConnectionHandler handler = factory.createClientConnectionHandler(new Socket());
		
		assertThat(handler, notNullValue());
	}

	@Test
	public void shouldAllowToCreateNewInstanceOfClientConnectionHandler() throws Exception {
		ClientConnectionHandler handlerA = factory.createClientConnectionHandler(new Socket());
		ClientConnectionHandler handlerB = factory.createClientConnectionHandler(new Socket());
		
		assertThat(handlerA, not(sameInstance(handlerB)));
	}
	
	@Test
	public void shouldCloseStorageWhenClosing() throws Exception {
		factory.close();
		
		verify(storage, times(1)).close();
	}
	
	@Test(expected = JMemcachedException.class)
	public void shouldNotAllowToCreateNewInstanceOfClientConnectionHandlerIfHasBeenClosed() throws Exception {
		factory.close();
		
		factory.createClientConnectionHandler(new Socket());
	}
}
