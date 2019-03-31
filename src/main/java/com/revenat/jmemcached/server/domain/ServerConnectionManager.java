package com.revenat.jmemcached.server.domain;

/**
 * This interface represents component responsible for managing server's
 * connections with its clients.
 * 
 * @author Vitaly Dragun
 *
 */
public interface ServerConnectionManager {

	/**
	 * Tries to establish new connection between server and its client, using provided
	 * {@link ClientConnectionHandler} {@code connectionHandler} parameter to
	 * handle it.
	 * 
	 * @param connectionHandler {@link ClientConnectionHandler} instance responsible
	 *                          for handling new connection between server and its
	 *                          client.
	 * @throws ConnectionRejectedException if new connection can not be established
	 *                                     due the fact that server might reached
	 *                                     it's capacity limit for total
	 *                                     simultaneously opened connections.
	 */
	void establishConnection(ClientConnectionHandler connectionHandler);

	/**
	 * Shutdowns current instance of the {@link ServerConnectionManager}, thus
	 * closing all currently active connections.
	 */
	void shutdown();
}
