package com.revenat.jmemcached.server;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revenat.jmemcached.server.domain.Server;
import com.revenat.jmemcached.server.domain.ServerFactory;
import com.revenat.jmemcached.server.domain.impl.JMemcachedServerFactory;

/**
 * This component represents command line interface (CLI) to start and stop
 * {@link Server} instance.
 * 
 * @author Vitaly Dragun
 *
 */
public class CLI {
	private static final Logger LOGGER = LoggerFactory.getLogger(CLI.class);
	private static final List<String> QUIT_CMDS = Collections.unmodifiableList(Arrays.asList("q", "quit", "exit"));

	public static void main(String[] args) {
		Thread.currentThread().setName("CLI-main thread");
		
		try {
			ServerFactory serverFactory = new JMemcachedServerFactory();
			Server server = serverFactory.buildNewServer();
			server.start();
			
			waitForStopCommand(server);
		} catch(Exception e) {
			LOGGER.error("Can't execute cmd: " + e.getMessage(), e);
		}
	}

	private static void waitForStopCommand(Server server) {
		try(Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8.name())) {
			while (true) {
				String cmd = scanner.nextLine();
				if (QUIT_CMDS.contains(cmd.toLowerCase())) {
					server.stop();
					break;
				} else {
					LOGGER.warn("Unsupported cmd: {}. To shutdown server please type: q", cmd);
				}
			}
		} finally {
			System.exit(0);
		}
	}
}
