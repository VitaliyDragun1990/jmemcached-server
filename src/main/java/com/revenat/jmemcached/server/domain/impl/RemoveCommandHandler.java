package com.revenat.jmemcached.server.domain.impl;

import com.revenat.jmemcached.protocol.model.Command;
import com.revenat.jmemcached.protocol.model.Request;
import com.revenat.jmemcached.protocol.model.Response;
import com.revenat.jmemcached.protocol.model.Status;
import com.revenat.jmemcached.server.domain.Storage;

/**
 * This implementation of the {@link CommandHandler} interface responsible for
 * handling {@code REMOVE} {@link Request}s
 * 
 * @author Vitaly Dragun
 *
 */
class RemoveCommandHandler extends AbstractCommandHandler {

	RemoveCommandHandler(Storage storage) {
		super(storage);
	}

	@Override
	public Response handle(Request request) {
		if (request.getCommand() == Command.REMOVE) {
			Status status = getStorage().remove(request.getKey());
			return Response.empty(status);
		} else {
			return getNextInChain().handle(request);
		}
	}

}
