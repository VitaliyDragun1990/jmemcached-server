package com.revenat.jmemcached.server.domain.impl;

import static java.util.Objects.requireNonNull;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revenat.jmemcached.server.domain.ClientConnectionHandler;
import com.revenat.jmemcached.server.domain.RequestProcessor;

/**
 * Default implementation of the {@link ClientConnectionHandler} interface.
 * Uses {@link Socket} to represents connection with a client.
 * 
 * @author Vitaly Dragun
 *
 */
class DefaultClientConnectionHandler implements ClientConnectionHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultClientConnectionHandler.class);
	
	private final Socket clientSocket;
	private final RequestProcessor requestProcessor;

	DefaultClientConnectionHandler(Socket clientSocket, RequestProcessor requestProcessor) {
		this.clientSocket = requireNonNull(clientSocket, "clientSocket can not be null");
		this.requestProcessor =  requireNonNull(requestProcessor, "requestProcessor can not be null");
	}

	@Override
	public void run() {
		try {
			InputStream clientInput = clientSocket.getInputStream();
			OutputStream clientOutput = clientSocket.getOutputStream();
			
			while (shouldContinue()) {
				requestProcessor.process(clientInput, clientOutput);
			}
		} catch (EOFException | SocketException e) {
			LOGGER.info("Remote client connection closed: {}: {}", clientSocket.getRemoteSocketAddress(), e.getMessage());
		} catch (IOException e) {
			if (!clientSocket.isClosed()) {
				LOGGER.error("IO Error: " + e.getMessage(), e);
			}
		} finally {
			closeClientSocket();
		}
	}
	
	private boolean shouldContinue() {
		return !Thread.interrupted();
	}
	
	private void closeClientSocket() {
		try {
			clientSocket.close();
		} catch (IOException e) {
			LOGGER.error("Close socket failed: " + e.getMessage(), e);
		}
	}
}
