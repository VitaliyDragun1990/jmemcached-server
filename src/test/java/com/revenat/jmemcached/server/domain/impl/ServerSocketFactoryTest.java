package com.revenat.jmemcached.server.domain.impl;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.IOException;
import java.net.ServerSocket;

import org.junit.After;
import org.junit.Test;

import com.revenat.jmemcached.exception.JMemcachedConfigException;

public class ServerSocketFactoryTest {
	
	private ServerSocketFactory factory = new ServerSocketFactory();
	ServerSocket socket;
	
	@After
	public void tearDown() throws IOException {
		if (socket != null) {
			socket.close();
		}
	}

	@Test
	public void allowToCreateServerSocketListeningOnSpecifiedPort() throws Exception {
		socket = factory.createServerSocket(9596);
		
		assertThat(socket.getLocalPort(), equalTo(9596));
	}
	
	@Test
	public void allowToCreateOpenedServerSocket() throws Exception {
		ServerSocket socket = factory.createServerSocket(9597);
		
		assertThat(socket.isClosed(), is(false));
	}
	
	@Test(expected = JMemcachedConfigException.class)
	public void shouldThrowExceptionIfCanNotCreateServerSocketListeningOnSpecifiedPort() throws Exception {
		socket = factory.createServerSocket(9595);
		factory.createServerSocket(9595);
	}

}
