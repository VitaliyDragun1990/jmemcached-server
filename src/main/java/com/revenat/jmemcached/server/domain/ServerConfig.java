package com.revenat.jmemcached.server.domain;

import java.net.Socket;
import java.util.concurrent.ThreadFactory;

/**
 * Component responsible for storing all the server-specific configurations for
 * the server module of the {@code JMemcached} application.
 * 
 * @author Vitaly Dragun
 *
 */
public interface ServerConfig extends AutoCloseable {

	/**
	 * Returns interval in milliseconds which designates how often server should
	 * provide check for whether outdated data is present and clear it if needed.
	 * 
	 */
	int getClearDataInterval();

	/**
	 * Returns specific {@link ThreadFactory} instance responsible for building new
	 * instances of the Server's worker {@link Thread}s.
	 */
	ThreadFactory getWorkerThreadFactory();

	/**
	 * Returns port number server is listening on.
	 */
	int getServerPort();

	/**
	 * Returns initial count of worker threads to be created in the server's worker
	 * thread pool.
	 */
	int getInitThreadCount();

	/**
	 * Returns maximum count of worker threads to be created in the server's worker
	 * thread pool. Effectively designate maximum number of clients to be served by
	 * the server at the same time.
	 */
	int getMaxThreadCount();

	/**
	 * Builds new {@link ClientSocketHandler} instance for handling client's
	 * {@link Socket} connection
	 * 
	 * @param clientSocket {@link Socket} representing client's connection with the
	 *                     server.
	 */
	ClientSocketHandler buildNewClientSocketHandler(Socket clientSocket);
}
