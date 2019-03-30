package com.revenat.jmemcached.server.domain.impl;

import static java.util.Objects.requireNonNull;

import java.net.Socket;

import com.revenat.jmemcached.exception.JMemcachedException;
import com.revenat.jmemcached.protocol.RequestReader;
import com.revenat.jmemcached.protocol.ResponseWriter;
import com.revenat.jmemcached.protocol.impl.RequestConverter;
import com.revenat.jmemcached.protocol.impl.ResponseConverter;
import com.revenat.jmemcached.server.domain.ClientConnectionHandler;
import com.revenat.jmemcached.server.domain.CommandHandler;
import com.revenat.jmemcached.server.domain.RequestProcessor;
import com.revenat.jmemcached.server.domain.Storage;

/**
 * Factory class responsible for building new instances of the
 * {@link ClientConnectionHandler} component. After finishing using this factory
 * and {@link ClientConnectionHandler} instances it produces, factory should be
 * closed by calling {@link #close()} method, thus appropriately closing all all
 * other resources it holds.
 * 
 * @author Vitaly Dragun
 *
 */
class ClientConnectionHandlerFactory implements AutoCloseable {
	private final Storage storage;
	private final RequestReader requestReader;
	private final ResponseWriter responseWriter;
	private final CommandHandler commandHandler;
	private final RequestProcessor requestProcessor;
	
	private boolean isClosed;

	ClientConnectionHandlerFactory(Storage storage) {
		this.storage = requireNonNull(storage);
		this.isClosed = false;
		this.requestReader = new RequestConverter();
		this.responseWriter = new ResponseConverter();
		this.commandHandler = buildHandlersChain(storage);
		this.requestProcessor = new DefaultRequestProcessor(requestReader, responseWriter, commandHandler);
	}

	private CommandHandler buildHandlersChain(Storage stor) {
		AbstractCommandHandler handler = new GetCommandHandler(stor);

		handler.add(new PutCommandHandler(stor));
		handler.add(new RemoveCommandHandler(stor));
		handler.add(new ClearCommandHandler(stor));
		handler.add(new UnsupportedCommandHandler(stor));

		return handler;
	}

	/**
	 * Builds new {@link ClientConnectionHandler} instance for handling client's
	 * {@link Socket} connection
	 * 
	 * @param clientSocket {@link Socket} representing client's connection with the
	 *                     server.
	 * @throws JMemcachedException if try to create {@link ClientConnectionHandler}
	 *                             after the factory has been closed.
	 */
	ClientConnectionHandler createClientConnectionHandler(Socket clientSocket) {
		if (isClosed) {
			throw new JMemcachedException("Can not create ClientConnectionHandler: factory has been closed!");
		}
		return new DefaultClientConnectionHandler(clientSocket, requestProcessor);
	}

	@Override
	public void close() throws Exception {
		isClosed = true;
		storage.close();
	}
}
