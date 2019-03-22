package com.revenat.jmemcached.server;

import com.revenat.jmemcached.protocol.model.Command;
import com.revenat.jmemcached.protocol.model.Request;
import com.revenat.jmemcached.protocol.model.Response;

/**
 * This interface represents component responsible for handling client's
 * {@link Request} depending on {@link Command} one contains.
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
