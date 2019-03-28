package com.revenat.jmemcached.server.domain.impl;

import java.util.Objects;

import com.revenat.jmemcached.protocol.RequestReader;
import com.revenat.jmemcached.protocol.ResponseWriter;
import com.revenat.jmemcached.protocol.impl.RequestConverter;
import com.revenat.jmemcached.protocol.impl.ResponseConverter;
import com.revenat.jmemcached.server.domain.CommandHandler;
import com.revenat.jmemcached.server.domain.ServerContext;
import com.revenat.jmemcached.server.domain.Storage;

/**
 * Default implementation of the {@link ServerContext} interface.
 * 
 * @author Vitaly Dragun
 *
 */
class DefaultServerContext implements ServerContext {
	private final Storage storage;
	
	DefaultServerContext(Storage storage) {
		this.storage = Objects.requireNonNull(storage, "storage can not be null");
	}

	@Override
	public RequestReader getRequestReader() {
		return new RequestConverter() ;
	}

	@Override
	public ResponseWriter getResponseWriter() {
		return new ResponseConverter();
	}

	@Override
	public CommandHandler getCommandHandler() {
		AbstractCommandHandler handler = new GetCommandHandler(storage);

		handler.add(new PutCommandHandler(storage));
		handler.add(new RemoveCommandHandler(storage));
		handler.add(new ClearCommandHandler(storage));
		handler.add(new UnsupportedCommandHandler(storage));
		
		return handler;
	}
}
