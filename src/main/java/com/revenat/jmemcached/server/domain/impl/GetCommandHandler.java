package com.revenat.jmemcached.server.domain.impl;

import com.revenat.jmemcached.protocol.model.Command;
import com.revenat.jmemcached.protocol.model.Request;
import com.revenat.jmemcached.protocol.model.Response;
import com.revenat.jmemcached.protocol.model.Status;
import com.revenat.jmemcached.server.domain.Storage;

/**
 * This implementation of the {@link CommandHandler} interface responsible for
 * handling {@code GET} {@link Request}s
 * 
 * @author Vitaly Dragun
 *
 */
class GetCommandHandler extends AbstractCommandHandler {

	GetCommandHandler(Storage storage) {
		super(storage);
	}

	@Override
	public Response handle(Request request) {
		if (request.getCommand() == Command.GET) {
			byte[] data = getStorage().get(request.getKey());
			if (data.length == 0) {
				return Response.empty(Status.NOT_FOUND);
			} else {
				return Response.withData(Status.GOTTEN, data);
			}
		} else {
			return getNextInChain().handle(request);
		}
	}

}
