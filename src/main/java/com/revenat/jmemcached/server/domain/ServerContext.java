package com.revenat.jmemcached.server.domain;

import java.net.ServerSocket;
import java.net.Socket;

import com.revenat.jmemcached.exception.JMemcachedConfigException;

/**
 * This interface represents high-level abstraction responsible for holding and
 * providing access to server-specific components. It holds {@link ServerSocket}
 * and {@link ServerConnectionManager} instances and can provide access to them
 * for other server components that need them. It also responsible for creating
 * new instances of the {@link ClientConnectionHandler} component. After
 * finishing work with {@link ServerContext} it should be mandatory closed by
 * calling its {@link #close()} method, which in turn closes
 * {@link ServerSocket} and {@link ServerConnectionManager} instances
 * {@link ServerContext} provide access to.
 * 
 * @author Vitaly Dragun
 *
 */
public interface ServerContext extends AutoCloseable {

	/**
	 * Provide access to a server socket, bound to the port with number received by
	 * calling {@link ServerConfig#getServerPort()} method. Client that calls this
	 * method is responsible for closing created socket appropriately.
	 * 
	 * @throws JMemcachedConfigException if socket can not be created for some
	 *                                   reason.
	 */
	ServerSocket getServerSocket();

	/**
	 * Provide access to {@link ServerConnectionManager} instance for managing
	 * server's connections with its clients.
	 */
	ServerConnectionManager getServerConnectionManager();

	/**
	 * Builds new {@link ClientConnectionHandler} instance for handling client's
	 * {@link Socket} connection
	 * 
	 * @param clientSocket {@link Socket} representing client's connection with the
	 *                     server.
	 */
	ClientConnectionHandler buildNewClientConnectionHandler(Socket clientSocket);

	/**
	 * Closes {@link ServerContext}, appropriately releasing resources it holds,
	 * altogether closing {@link ServerSocket} and {@link ServerConnectionManager}
	 * instances {@link ServerContext} provide access to.
	 */
	@Override
	void close();
}
