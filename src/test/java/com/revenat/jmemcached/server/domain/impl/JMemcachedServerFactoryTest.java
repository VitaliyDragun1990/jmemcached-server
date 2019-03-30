package com.revenat.jmemcached.server.domain.impl;

import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.util.Properties;

import org.junit.Test;

import com.revenat.jmemcached.server.domain.Server;

public class JMemcachedServerFactoryTest {
	
	private JMemcachedServerFactory serverFactory = new JMemcachedServerFactory();

	@Test
	public void shouldAllowToCreateNewServerInstance() throws Exception {
		Server server = serverFactory.buildNewServer();
		
		assertThat(server, notNullValue());
	}
	
	@Test
	public void shouldAllowToCreateNewServerInstanceWithOverridenConfigurations() throws Exception {
		Properties overridenConfifuration = new Properties();
		overridenConfifuration.setProperty("jmemcached.server.port", "9095");
		
		Server server = serverFactory.buildNewServer(overridenConfifuration);
		
		assertThat(server, notNullValue());
	}

}
