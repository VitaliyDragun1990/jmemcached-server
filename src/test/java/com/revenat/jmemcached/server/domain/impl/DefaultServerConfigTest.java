package com.revenat.jmemcached.server.domain.impl;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.revenat.jmemcached.exception.JMemcachedConfigException;
import com.revenat.jmemcached.server.domain.ResourceLoader;

@RunWith(MockitoJUnitRunner.Silent.class)
public class DefaultServerConfigTest {
	private static final Properties VALID_PROPERTIES = createProperties(
				"jmemcached.server.port=5555", "jmemcached.server.init.thread.count=1",
				"jmemcached.server.max.thread.count=10", "jmemcached.storage.clear.data.interval=10000"
			);
	private static final Properties INVALID_CLEAR_DATA_PROPERTY = createProperties(
			"jmemcached.server.port=5555", "jmemcached.server.init.thread.count=1",
			"jmemcached.server.max.thread.count=10", "jmemcached.storage.clear.data.interval=vvv"
		);
	private static final Properties OUT_OF_RANGE_CLEAR_DATA_PROPERTY = createProperties(
			"jmemcached.server.port=5555", "jmemcached.server.init.thread.count=1",
			"jmemcached.server.max.thread.count=10", "jmemcached.storage.clear.data.interval=10"
			);
	
	private static final Properties INVALID_PORT_PROPERTY = createProperties(
			"jmemcached.server.port=kkk", "jmemcached.server.init.thread.count=1",
			"jmemcached.server.max.thread.count=10", "jmemcached.storage.clear.data.interval=10000"
		);
	
	private static final Properties OUT_OF_RANGE_PORT_PROPERTY = createProperties(
			"jmemcached.server.port=99999", "jmemcached.server.init.thread.count=1",
			"jmemcached.server.max.thread.count=10", "jmemcached.storage.clear.data.interval=10000"
		);
	
	private static final Properties INVALID_INIT_THREAD_PROPERTY = createProperties(
			"jmemcached.server.port=5555", "jmemcached.server.init.thread.count=kkk",
			"jmemcached.server.max.thread.count=10", "jmemcached.storage.clear.data.interval=10000"
		);
	
	private static final Properties OUT_OF_BOUND_INIT_THREAD_PROPERTY = createProperties(
			"jmemcached.server.port=5555", "jmemcached.server.init.thread.count=-1",
			"jmemcached.server.max.thread.count=-2", "jmemcached.storage.clear.data.interval=10000"
		);
	private static final Properties INVALID_MAX_THREAD_PROPERTY = createProperties(
			"jmemcached.server.port=5555", "jmemcached.server.init.thread.count=1",
			"jmemcached.server.max.thread.count=kkk", "jmemcached.storage.clear.data.interval=10000"
		);
	
	private static final Properties OUT_OF_BOUND_MAX_THREAD_PROPERTY = createProperties(
			"jmemcached.server.port=5555", "jmemcached.server.init.thread.count=2",
			"jmemcached.server.max.thread.count=1", "jmemcached.storage.clear.data.interval=10000"
		);
	
	@Rule
	public ExpectedException expected = ExpectedException.none();
	
	@Mock
	private ResourceLoader resourceLoader;
	
	private DefaultServerConfig serverConfig;
	
	@Before
	public void setUp() {
		when(resourceLoader.loadProperties(anyString())).thenReturn(new Properties(VALID_PROPERTIES));
		serverConfig = new DefaultServerConfig(null, resourceLoader);
	}
	
	@Test
	public void shouldAllowToGetClearDataIntervalValue() throws Exception {
		int clearDataInterval = serverConfig.getClearDataInterval();
		
		assertThat(clearDataInterval, equalTo(10000));
	}
	
	@Test
	public void shouldAllowToGetServerPortValue() throws Exception {
		int serverPort = serverConfig.getServerPort();
		
		assertThat(serverPort, equalTo(5555));
	}
	
	@Test
	public void shouldAllowToOverrideConfigPropertiesWhenCreating() throws Exception {
		Properties overrideProperties = new Properties();
		overrideProperties.setProperty("jmemcached.server.port", "10000");
		serverConfig = new DefaultServerConfig(overrideProperties, resourceLoader);
		
		assertThat(serverConfig.getServerPort(), equalTo(10000));
	}
	
	@Test
	public void shouldAllowToGetInitThreadCountValue() throws Exception {
		int initThreadCount = serverConfig.getInitThreadCount();
		
		assertThat(initThreadCount, equalTo(1));
	}
	
	@Test
	public void shouldAllowToGetMaxThreadCountValue() throws Exception {
		int maxThreadCount = serverConfig.getMaxThreadCount();
		
		assertThat(maxThreadCount, equalTo(10));
	}
	
	@Test
	public void shouldGenerateStringWithPortNumberInIt() throws Exception {
		String s = serverConfig.toString();
		
		assertThat(s, containsString("port=5555"));
	}
	
	@Test
	public void shouldGenerateStringWithClearDataIntervalInIt() throws Exception {
		String s = serverConfig.toString();

		assertThat(s, containsString("clearDataInterval=10000"));
	}
	
	@Test
	public void shouldGenerateStringWithInitThreadCountInIt() throws Exception {
		String s = serverConfig.toString();

		assertThat(s, containsString("initThreadCount=1"));
	}
	
	@Test
	public void shouldGenerateStringWithMaxThreadCountInIt() throws Exception {
		String s = serverConfig.toString();

		assertThat(s, containsString("maxThreadCount=10"));
	}
	
	@Test
	public void shouldThrowExceptionIfClearDataIntervalValueCanNotBeObtain() throws Exception {
		expected.expect(JMemcachedConfigException.class);
		expected.expectMessage(containsString("should be a number"));
		
		serverConfig = createServerConfigWith(INVALID_CLEAR_DATA_PROPERTY);
		
		serverConfig.getClearDataInterval();
	}
	
	@Test
	public void shouldNotAllowToGetClearDataIntervalValueIfItOutOfBound() throws Exception {
		expected.expect(JMemcachedConfigException.class);
		expected.expectMessage(containsString("should be >= 1000 millis"));
		
		serverConfig = createServerConfigWith(OUT_OF_RANGE_CLEAR_DATA_PROPERTY);
		
		serverConfig.getClearDataInterval();
	}
	
	@Test
	public void shouldThrowExceptionIfServerPortValueCanNotBeObtain() throws Exception {
		expected.expect(JMemcachedConfigException.class);
		expected.expectMessage(containsString("should be a number"));
		
		serverConfig = createServerConfigWith(INVALID_PORT_PROPERTY);
		
		serverConfig.getServerPort();
	}
	
	@Test
	public void shouldNotAllowToGetServerPortValueIfItOutOfBound() throws Exception {
		expected.expect(JMemcachedConfigException.class);
		expected.expectMessage(containsString("should be between 0 and 65535"));
		
		serverConfig = createServerConfigWith(OUT_OF_RANGE_PORT_PROPERTY);
		
		serverConfig.getServerPort();
	}
	
	@Test
	public void shouldThrowExceptionIfInitThreadCountValueCanNotBeObtain() throws Exception {
		expected.expect(JMemcachedConfigException.class);
		expected.expectMessage(containsString("should be a number"));
		
		serverConfig = createServerConfigWith(INVALID_INIT_THREAD_PROPERTY);
		
		serverConfig.getInitThreadCount();
	}
	
	@Test
	public void shouldNotAllowToGetInitThreadCountValueIfItOutOfBound() throws Exception {
		expected.expect(JMemcachedConfigException.class);
		expected.expectMessage(containsString("should be >= 1"));
		
		serverConfig = createServerConfigWith(OUT_OF_BOUND_INIT_THREAD_PROPERTY);
		
		serverConfig.getInitThreadCount();
	}
	
	@Test
	public void shouldThrowExceptionIfMaxThreadCountValueCanNotBeObtain() throws Exception {
		expected.expect(JMemcachedConfigException.class);
		expected.expectMessage(containsString("should be a number"));
		
		serverConfig = createServerConfigWith(INVALID_MAX_THREAD_PROPERTY);
		
		serverConfig.getMaxThreadCount();
	}
	
	@Test
	public void shouldNotAllowToGetMaxThreadCountValueIfItOutOfBound() throws Exception {
		expected.expect(JMemcachedConfigException.class);
		expected.expectMessage(containsString("should be >= 2"));
		
		serverConfig = createServerConfigWith(OUT_OF_BOUND_MAX_THREAD_PROPERTY);
		
		serverConfig.getMaxThreadCount();
	}
	
	private DefaultServerConfig createServerConfigWith(Properties properties) {
		when(resourceLoader.loadProperties(anyString())).thenReturn(properties);
		return new DefaultServerConfig(null, resourceLoader);
	}

	private static Properties createProperties(String...propertyStrings) {
		Properties props = new Properties();
		for (String propertyString : propertyStrings) {
			String[] nameValuePair = propertyString.split("=");
			String name = nameValuePair[0].trim();
			String value = nameValuePair[1].trim();
			props.setProperty(name, value);
		}
		return props;
	}
}
