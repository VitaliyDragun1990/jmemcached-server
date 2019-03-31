package com.revenat.jmemcached.server.domain.impl;

import com.revenat.jmemcached.exception.JMemcachedException;
import com.revenat.jmemcached.protocol.model.Request;
import com.revenat.jmemcached.protocol.model.Response;
import com.revenat.jmemcached.server.domain.CommandHandler;
import com.revenat.jmemcached.server.domain.ServerStorage;

/**
 * This implementation of {@link CommandHandler} interface should be last in chain
 * and always responds by throwing an exception by every request.
 * 
 * @author Vitaly Dragun
 *
 */
class UnsupportedCommandHandler extends AbstractCommandHandler {

	UnsupportedCommandHandler(ServerStorage storage) {
		super(storage);
	}

	@Override
	public Response handle(Request request) {
		throw new JMemcachedException("Unsupported command: " + request.getCommand());
	}
}
