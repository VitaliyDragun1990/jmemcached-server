package com.revenat.jmemcached.server.domain.impl;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Properties;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import com.revenat.jmemcached.exception.JMemcachedConfigException;


public class ClassPathResourceLoaderTest {
	
	@Rule
	public ExpectedException expected = ExpectedException.none();
	
	private ClassPathResourceLoader loader;
	
	@Before
	public void setUp() {
		loader = new ClassPathResourceLoader();
	}

	@Test
	public void shouldNotAllowToLoadPropertiesUsingNullResourceName() throws Exception {
		expected.expect(NullPointerException.class);
		expected.expectMessage(containsString("Resource name can not be null"));
		
		loader.loadProperties(null);
	}
	
	@Test
	public void shouldNotAllowToLoadPropertiesUsingEmptyResourceName() throws Exception {
		expected.expect(IllegalArgumentException.class);
		expected.expectMessage(containsString("Resource name can not be empty"));
		
		loader.loadProperties("");
	}
	
	@Test
	public void shouldThrowExceptionIfCanNotFindClassPathResource() throws Exception {
		expected.expect(JMemcachedConfigException.class);
		expected.expectMessage(containsString("Class path resource is not found"));
		
		loader.loadProperties("nonexistent.properties");
	}

	@Test
	public void shouldAllowToLoadPropertiesFromClassPath() throws Exception {
		Properties properties = loader.loadProperties("test.properties");
		
		assertThat(properties.size(), equalTo(2));
		assertThat(properties.getProperty("java"), equalTo("test"));
		assertThat(properties.getProperty("ide"), equalTo("eclipse"));
	}
}
