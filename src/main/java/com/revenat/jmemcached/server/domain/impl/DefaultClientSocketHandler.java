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

import com.revenat.jmemcached.protocol.RequestReader;
import com.revenat.jmemcached.protocol.ResponseWriter;
import com.revenat.jmemcached.protocol.model.Request;
import com.revenat.jmemcached.protocol.model.Response;
import com.revenat.jmemcached.server.domain.ClientSocketHandler;
import com.revenat.jmemcached.server.domain.CommandHandler;

/**
 * Default implementation of the {@link ClientSocketHandler} interface.
 * 
 * @author Vitaly Dragun
 *
 */
class DefaultClientSocketHandler implements ClientSocketHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultClientSocketHandler.class);
	
	private final Socket clientSocket;
	private final RequestReader requestReader;
	private final ResponseWriter responseWriter;
	private final CommandHandler commandHandler;
	
	DefaultClientSocketHandler(Socket clientSocket, RequestReader reader, ResponseWriter writer, CommandHandler handler) {
		this.clientSocket = requireNonNull(clientSocket, "clientSocket can not be null");
		this.requestReader = requireNonNull(reader, "requestReader can not be null");
		this.responseWriter = requireNonNull(writer, "responseWriter can not be null");
		this.commandHandler = requireNonNull(handler, "commandHandler can not be null");
	}

	@Override
	public void run() {
		try {
			InputStream clientInput = clientSocket.getInputStream();
			OutputStream clientOutput = clientSocket.getOutputStream();
			
			while (shouldContinue()) {
				handleClientRequest(clientInput, clientOutput);
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

	private void handleClientRequest(InputStream clientInput, OutputStream clientOutput) throws IOException {
		try {
			Request request = requestReader.readFrom(clientInput);
			Response response = commandHandler.handle(request);
			responseWriter.writeTo(clientOutput, response);
			LOGGER.debug("Command {} -> {}", request, response);
		} catch (RuntimeException e) {
			LOGGER.error("Handle request failed: " + e.getMessage(), e);
		}
	}
	
	private void closeClientSocket() {
		try {
			clientSocket.close();
		} catch (IOException e) {
			LOGGER.error("Close socket failed: " + e.getMessage(), e);
		}
	}
}
