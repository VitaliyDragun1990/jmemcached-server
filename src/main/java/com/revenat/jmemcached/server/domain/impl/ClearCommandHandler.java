package com.revenat.jmemcached.server.domain.impl;

import com.revenat.jmemcached.protocol.model.Command;
import com.revenat.jmemcached.protocol.model.Request;
import com.revenat.jmemcached.protocol.model.Response;
import com.revenat.jmemcached.protocol.model.Status;
import com.revenat.jmemcached.server.domain.ServerStorage;

/**
 * This implementation of the {@link CommandHandler} interface responsible for
 * handling {@code CLEAR} {@link Request}s
 * 
 * @author Vitaly Dragun
 *
 */
class ClearCommandHandler extends AbstractCommandHandler {

	ClearCommandHandler(ServerStorage storage) {
		super(storage);
	}

	@Override
	public Response handle(Request request) {
		if (request.getCommand() == Command.CLEAR) {
			Status status = getStorage().clear();
			return Response.empty(status);
		} else {
			return getNextInChain().handle(request);
		}
	}

}
