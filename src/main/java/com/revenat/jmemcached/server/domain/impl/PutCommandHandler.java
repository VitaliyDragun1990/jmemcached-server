package com.revenat.jmemcached.server.domain.impl;

import com.revenat.jmemcached.protocol.model.Command;
import com.revenat.jmemcached.protocol.model.Request;
import com.revenat.jmemcached.protocol.model.Response;
import com.revenat.jmemcached.protocol.model.Status;
import com.revenat.jmemcached.server.domain.Storage;

/**
 * This implementation of the {@link CommandHandler} interface responsible for
 * handling {@code PUT} {@link Request}s
 * 
 * @author Vitaly Dragun
 *
 */
class PutCommandHandler extends AbstractCommandHandler {

	PutCommandHandler(Storage storage) {
		super(storage);
	}

	@Override
	public Response handle(Request request) {
		if (request.getCommand() == Command.PUT) {
			Status status;
			if (request.hasTtl()) {
				status = getStorage().put(request.getKey(), request.getTtl(), request.getData());
			} else {
				status = getStorage().put(request.getKey(), request.getData());
			}
			return Response.empty(status);
		} else {
			return getNextInChain().handle(request);
		}
	}

}
