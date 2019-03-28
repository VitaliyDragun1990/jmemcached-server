package com.revenat.jmemcached.server.domain.impl;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revenat.jmemcached.exception.JMemcachedException;

/**
 * This {@link Runnable} implementation represents main server task of
 * receiving client's connections and appropriately handling them.
 * 
 * @author Vitaly Dragun
 *
 */
class ServerTask implements Runnable {
	private static final Logger LOGGER = LoggerFactory.getLogger(ServerTask.class);
	
	private final ServerSocket serverSocket;
	private final ClientSocketHandler clientSocketHandler;
	private final ServerShutdownHandler shutdownServerHandler;
	
	ServerTask(ServerSocket serverSocket, ClientSocketHandler clientSocketHandler,
			ServerShutdownHandler shutdownServerHandler) {
		this.serverSocket = requireNonNull(serverSocket, "serverSocket can not be null");
		this.clientSocketHandler = requireNonNull(clientSocketHandler, "clientSocketHandler can not be null");
		this.shutdownServerHandler = requireNonNull(shutdownServerHandler, "shutdownServerHandler can not be null");
	}

	@Override
	public void run() {
		while (!Thread.currentThread() .isInterrupted()) {
			try {
				Socket clientSocket = serverSocket.accept();
				handleClientSocket(clientSocket);
			} catch (IOException e) {
				if (!serverSocket.isClosed()) {
					LOGGER.error("Can't accept client connection: " + e.getMessage(), e);
				}
				shutdownServerHandler.shutdownServer();
				break;
			}
		}
	}

	void handleClientSocket(Socket clientSocket) throws IOException {
		try {
			clientSocketHandler.handle(clientSocket);
			LOGGER.info("A new client connection has been established: {}",
					clientSocket.getRemoteSocketAddress());
		} catch (JMemcachedException e) {
			LOGGER.error(e.getMessage());
			clientSocket.close();
		}
	}
	
	/**
	 * This interface represents function of handling client's
	 * {@link Socket} in some meaningful way.
	 * 
	 * @author Vitaly Dragun
	 *
	 */
	@FunctionalInterface
	static interface ClientSocketHandler {
		void handle(Socket clientSocket);
	}
	
	/**
	 * This interface represents function of
	 * shutdown server.
	 * 
	 * @author Vitaly Dragun
	 *
	 */
	@FunctionalInterface
	static interface ServerShutdownHandler {
		void shutdownServer();
	}
}