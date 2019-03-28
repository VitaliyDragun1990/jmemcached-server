package com.revenat.jmemcached.server.domain;

import com.revenat.jmemcached.protocol.model.Command;
import com.revenat.jmemcached.protocol.model.Request;
import com.revenat.jmemcached.protocol.model.Response;

/**
 * This interface represents component responsible for handling {@link Request}
 * and creating appropriate {@link Response} depending on {@link Request}'s
 * {@link Command}.
 * 
 * @author Vitaly Dragun
 *
 */
public interface CommandHandler {

	/**
	 * Handles specified {@link Request} and returns appropriate {@link Response}.
	 * 
	 * @param request {@link Request} that should be handled.
	 * @return {@link Response} instance.
	 */
	Response handle(Request request);
}
