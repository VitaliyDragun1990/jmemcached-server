package com.revenat.jmemcached.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revenat.jmemcached.server.domain.Server;
import com.revenat.jmemcached.server.domain.ServerFactory;
import com.revenat.jmemcached.server.domain.impl.JMemcachedServerFactory;

/**
 * Special class responsible for starting and stopping {@code JMemcached} Server
 * if it's running as {@code MS Windows service}
 * 
 * @author Vitaly Dragun
 *
 */
public class ServiceWrapper {
	private static final Logger LOGGER = LoggerFactory.getLogger(ServiceWrapper.class);
	private static final Server SERVER = createServer();

	public static void main(String[] args) {
		if ("start".equalsIgnoreCase(args[0])) {
			start(args);
		} else if ("stop".equalsIgnoreCase(args[0])) {
			stop(args);
		}
	}

	public static void start(String[] args) {
		SERVER.start();
	}

	public static void stop(String[] args) {
		SERVER.start();
	}

	private static Server createServer() {
		ServerFactory serverFactory = new JMemcachedServerFactory();
		return serverFactory.buildNewServer(getServerProperties());
	}

	private static Properties getServerProperties() {
		Properties props = new Properties();
		String pathToServerProperties = System.getProperty("server-prop");
		try (InputStream in = new FileInputStream(pathToServerProperties)) {
			props.load(in);
		} catch (IOException e) {
			LOGGER.warn("Error while reading server config from " + pathToServerProperties, e);
		}

		return props;
	}
}
