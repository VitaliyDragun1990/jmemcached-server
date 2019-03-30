package com.revenat.jmemcached.server.domain.impl;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

class ServerSocketStub extends ServerSocket {
	private Socket clientSocket;
	private boolean isClosed;
	private int attemptCount;
	private int attemptWhenException = -1;

	public ServerSocketStub(Socket clientSocket) throws IOException {
		super();
		this.clientSocket = clientSocket;
		this.isClosed = false;
		this.attemptCount = 0;
	}

	public void throwExceptionOnAttempt(int attemptNumber) {
		this.attemptWhenException = attemptNumber;
	}

	@Override
	public Socket accept() throws IOException {
		attemptCount++;
		if (attemptCount == attemptWhenException) {
			throw new IOException("Error during establishing connection with client socket.");
		}
		return clientSocket;
	}

	@Override
	public void close() throws IOException {
		this.isClosed = true;
	}

	@Override
	public boolean isClosed() {
		return isClosed;
	}
}