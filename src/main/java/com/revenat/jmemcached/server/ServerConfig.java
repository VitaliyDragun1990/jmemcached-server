package com.revenat.jmemcached.server;

import java.net.Socket;
import java.util.concurrent.ThreadFactory;

import com.revenat.jmemcached.protocol.RequestReader;
import com.revenat.jmemcached.protocol.ResponseWriter;
import com.revenat.jmemcached.protocol.model.Command;
import com.revenat.jmemcached.protocol.model.Request;
import com.revenat.jmemcached.protocol.model.Response;

/**
 * Component responsible for storing all the server-specific configurations for
 * the server module of the {@code JMemcached} application.
 * 
 * @author Vitaly Dragun
 *
 */
public interface ServerConfig extends AutoCloseable {

	/**
	 * Returns {@link RequestReader} instance to read client's {@link Request}s.
	 */
	RequestReader getRequestReader();

	/**
	 * Returns {@link ResponseWriter} instance to write server's {@link Response}s.
	 */
	ResponseWriter getResponseWriter();

	/**
	 * Returns current server's data storage.
	 */
	Storage getStorage();

	/**
	 * Returns {@link CommandHandler} instance for handling client's
	 * {@link Command}'s.
	 */
	CommandHandler getCommandHandler();

	/**
	 * Returns specific {@link ThreadFactory} instance responsible for building new
	 * instances of the Server's worker {@link Thread}s.
	 */
	ThreadFactory getWorkerThreadFactory();

	/**
	 * Returns interval in milliseconds which designates how often server should
	 * provide check for whether outdated data is present and clear it if needed.
	 * 
	 * @return interval in milliseconds
	 */
	int getClearDataInterval();

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
	 * @param clientSocket {@link Socket} representing client's connection with the server.
	 */
	ClientSocketHandler buildNewClientSocketHandler(Socket clientSocket);
}
