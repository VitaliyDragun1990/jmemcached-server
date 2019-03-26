package com.revenat.jmemcached.server;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

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
	private static final Server httpServer = createServer();

	public static void main(String[] args) {
		if ("start".equalsIgnoreCase(args[0])) {
			start();
		} else if ("stop".equalsIgnoreCase(args[0])) {
			stop();
		}
	}

	private static void start() {
		httpServer.start();
	}

	private static void stop() {
		httpServer.start();
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
			e.printStackTrace();
		}

		return props;
	}
}
