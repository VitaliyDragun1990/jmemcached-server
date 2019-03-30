package com.revenat.jmemcached.server.domain.impl;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revenat.jmemcached.exception.JMemcachedException;
import com.revenat.jmemcached.server.domain.Server;
import com.revenat.jmemcached.server.domain.ServerConnectionManager;
import com.revenat.jmemcached.server.domain.ServerContext;

/**
 * This {@link Runnable} implementation represents main server task of receiving
 * client's connections and appropriately handling them.
 * 
 * @author Vitaly Dragun
 *
 */
class ServerTask implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(ServerTask.class);

	private Server server;
	private final ServerContext serverContext;

	ServerTask(ServerContext serverContext) {
		this.serverContext = requireNonNull(serverContext, "serverContext can not be null");
	}
	
	public void setServer(Server server) {
		this.server = requireNonNull(server, "server can not be null");
	}

	@Override
	public void run() {
		if (server != null) {
			handleServerTask();
		} else {
			throw new JMemcachedException("Can not start server task without server refrerence.");
		}
	}

	/**
	 * Shutdowns this server task, appropriately closing all resources. This method
	 * must be called in order to for sure close {@link ServerTask}
	 */
	public void shutdown() {
		serverContext.close();
	}

	private void handleServerTask() {
		ServerSocket serverSocket = serverContext.getServerSocket();
		while (!Thread.currentThread().isInterrupted()) {
			try {
				Socket clientSocket = serverSocket.accept();
				handleClientSocket(clientSocket);
			} catch (IOException e) {
				if (!serverSocket.isClosed()) { // this means it's not a server called shutdown on this
					LOGGER.error("Can't accept client connection: " + e.getMessage(), e);
					server.stop();
				}
				break;
			}
		}
	}

	private void handleClientSocket(Socket clientSocket) throws IOException {
		ServerConnectionManager connectionManager = serverContext.getServerConnectionManager();
		try {
			connectionManager.submit(serverContext.buildNewClientConnectionHandler(clientSocket));
			LOGGER.info("A new client connection has been established: {}", clientSocket.getRemoteSocketAddress());
		} catch (JMemcachedException e) {
			LOGGER.error(e.getMessage());
			clientSocket.close();
		}
	}
}