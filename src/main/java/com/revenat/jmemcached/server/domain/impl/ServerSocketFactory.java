package com.revenat.jmemcached.server.domain.impl;

import java.io.IOException;
import java.net.ServerSocket;

import com.revenat.jmemcached.exception.JMemcachedConfigException;

/**
 * Factory class responsible for building new instances of the {@link ServerSocket}
 * 
 * @author Vitaly Dragun
 *
 */
class ServerSocketFactory {

	/**
	 * Builds new instance of the {@link ServerSocket}, bound to the {@code serverPort} port number.
	 * @param serverPort number of the port to bound server socket to
	 */
	ServerSocket createServerSocket(int serverPort) {
		try {
			ServerSocket socket = new ServerSocket(serverPort);
			socket.setReuseAddress(true);
			return socket;
		} catch (IOException e) {
			throw new JMemcachedConfigException("Can not create server socket with port=" + serverPort, e);
		}
	}
}
