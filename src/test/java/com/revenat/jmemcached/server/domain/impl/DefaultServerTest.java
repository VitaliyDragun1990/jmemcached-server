package com.revenat.jmemcached.server.domain.impl;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.revenat.jmemcached.server.domain.ServerConfig;

@RunWith(MockitoJUnitRunner.Silent.class)
public class DefaultServerTest {
	
	@Mock
	private ServerConfig serverConfigStub;
	
	private DefaultServer server;

	@Test
	public void test() {
		fail("Not yet implemented");
	}

}
