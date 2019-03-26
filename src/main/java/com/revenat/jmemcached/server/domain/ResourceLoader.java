package com.revenat.jmemcached.server.domain;

import java.util.Properties;

/**
 * This interface represents component responsible for loading resource that can
 * be useful when configuring JMemcached server.
 * 
 * @author Vitaly Dragun
 *
 */
public interface ResourceLoader {
	
	/**
	 * Loads properties from resource with specified {@code resourceName}
	 * 
	 * @param resourceName name of the resource to load properties from.
	 * @return {@link Properties} instance with properties loaded from specified resource
	 */
	Properties loadProperties(String resourceName);
}
