package com.revenat.jmemcached.server.domain.impl;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.revenat.jmemcached.exception.JMemcachedException;
import com.revenat.jmemcached.server.domain.ServerConfig;

@RunWith(MockitoJUnitRunner.Silent.class)
public class DefaultServerTest {
	
	@Rule
	public ExpectedException expected = ExpectedException.none();
	
	@Mock
	private ServerTask serverTask;
	private ServerConfig serverConfigStub = new ServerConfigStub();
	
	private DefaultServer server;
	
	@Before
	public void setUp() {
		server = new DefaultServer(serverTask, serverConfigStub);
	}

	@Test(expected = NullPointerException.class)
	public void shouldNotAllowToCreateWithNullServerTask() throws Exception {
		server = new DefaultServer(null, serverConfigStub);
	}
	
	@Test(expected = NullPointerException.class)
	public void shouldNotAllowToCreateWithNullServerConfig() throws Exception {
		server = new DefaultServer(serverTask, null);
	}
	
	@Test
	public void shouldAddItselfToServerTaskWhenCreated() throws Exception {
		verify(serverTask, times(1)).setServer(server);
	}
	
	@Test
	public void shouldStartServerTaskWhenStarted() throws Exception {
		server.start();
		TimeUnit.MILLISECONDS.sleep(200);
		
		verify(serverTask, times(1)).run();
	}
	
	@Test
	public void shouldNowAllowToStartSameInstanceSeveralTimes() throws Exception {
		expected.expect(JMemcachedException.class);
		expected.expectMessage(containsString("Current server instance has been already started or stopped!"));
		
		server.start();
		server.start();
	}

	@Test
	public void shouldShutdownServerTaskWhenStopped() throws Exception {
		server.start();
		server.stop();
		
		verify(serverTask, times(1)).shutdown();
	}
	
	private static class ServerConfigStub implements ServerConfig {
		@Override
		public int getClearDataInterval() {
			return 0;
		}

		@Override
		public int getServerPort() {
			return 0;
		}

		@Override
		public int getInitThreadCount() {
			return 0;
		}

		@Override
		public int getMaxThreadCount() {
			return 0;
		}
	}
}
