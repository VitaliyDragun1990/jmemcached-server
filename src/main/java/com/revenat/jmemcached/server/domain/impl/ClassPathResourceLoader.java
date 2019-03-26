package com.revenat.jmemcached.server.domain.impl;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revenat.jmemcached.exception.JMemcachedConfigException;
import com.revenat.jmemcached.server.domain.ResourceLoader;

/**
 * Implementation of the {@link ResourceLoader} that loads required resources
 * from server's class path.
 * 
 * @author Vitaly Dragun
 *
 */
class ClassPathResourceLoader implements ResourceLoader {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClassPathResourceLoader.class);

	@Override
	public Properties loadProperties(String resourceName) {
		validateResourceName(resourceName);
		
		try (InputStream input = getClassPathResourceInputStream(resourceName)) {
			Properties properties = new Properties();
			if (input != null) {
				properties.load(input);
				LOGGER.debug("Successfully loaded proeprties from classpath resource: {}", resourceName);
				return properties;
			} else {
				throw new JMemcachedConfigException("Class path resource is not found: " + resourceName);
			}
		} catch (IOException e) {
			throw new JMemcachedConfigException("Can not load properties from class path resource: " + resourceName, e);
		}
	}

	private static void validateResourceName(String resourceName) {
		requireNonNull(resourceName, "Resource name can not be null");
		if (resourceName.trim().length() == 0) {
			throw new IllegalArgumentException("Resource name can not be empty");
		}
	}

	private InputStream getClassPathResourceInputStream(String classPathResource) {
		return getClass().getClassLoader().getResourceAsStream(classPathResource);
	}
}
