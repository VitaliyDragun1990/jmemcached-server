package com.revenat.jmemcached.server.domain.impl;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.concurrent.TimeUnit;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.revenat.jmemcached.exception.JMemcachedException;

@RunWith(MockitoJUnitRunner.Silent.class)
public class DefaultServerTest {
	
	@Rule
	public ExpectedException expected = ExpectedException.none();
	
	@Mock
	private ServerTask serverTask;
	
	private DefaultServer server;

	@Test(expected = NullPointerException.class)
	public void shouldNotAllowToCreateWithNullServerTask() throws Exception {
		server = new DefaultServer(null);
	}
	
	@Test
	public void shouldAddItselfToServerTaskWhenCreated() throws Exception {
		server = new DefaultServer(serverTask);
		
		verify(serverTask, times(1)).setServer(server);
	}
	
	@Test
	public void shouldStartServerTaskWhenStarted() throws Exception {
		server = new DefaultServer(serverTask);
		
		server.start();
		TimeUnit.MILLISECONDS.sleep(200);
		
		verify(serverTask, times(1)).run();
	}
	
	@Test
	public void shouldNowAllowToStartSameInstanceSeveralTimes() throws Exception {
		server = new DefaultServer(serverTask);
		expected.expect(JMemcachedException.class);
		expected.expectMessage(containsString("Current server instance has been already started or stopped!"));
		
		server.start();
		server.start();
	}

	@Test
	public void shouldShutdownServerTaskWhenStopped() throws Exception {
		server = new DefaultServer(serverTask);
		
		server.start();
		server.stop();
		
		verify(serverTask, times(1)).shutdown();
	}
}
