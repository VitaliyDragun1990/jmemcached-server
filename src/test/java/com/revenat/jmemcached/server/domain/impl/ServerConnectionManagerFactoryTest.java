package com.revenat.jmemcached.server.domain.impl;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import org.junit.Test;

import com.revenat.jmemcached.server.domain.ServerConnectionManager;

public class ServerConnectionManagerFactoryTest {

	@Test
	public void shouldAllowToCreateServerConnectionManager() throws Exception {
		ServerConnectionManagerFactory factory = new ServerConnectionManagerFactory();

		ServerConnectionManager manager = factory.createServerConnectionManager(1, 2);

		assertThat(manager, notNullValue());
	}

	@Test
	public void shouldCreateNewInstanceOfServerConnectionManager() throws Exception {
		ServerConnectionManagerFactory factory = new ServerConnectionManagerFactory();

		ServerConnectionManager managerA = factory.createServerConnectionManager(1, 2);
		ServerConnectionManager managerB = factory.createServerConnectionManager(1, 2);

		assertThat(managerA, not(sameInstance(managerB)));
	}

}
