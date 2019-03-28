package com.revenat.jmemcached.server.domain;

import com.revenat.jmemcached.protocol.RequestReader;
import com.revenat.jmemcached.protocol.ResponseWriter;
import com.revenat.jmemcached.protocol.model.Request;
import com.revenat.jmemcached.protocol.model.Response;

/**
 * This interface represents high-level abstraction responsible for creating
 * components whose primary jobs are reading client request, preparing server
 * response, and passing this response back to the client.
 * 
 * @author Vitaly Dragun
 *
 */
public interface ServerContext {

	/**
	 * Returns {@link RequestReader} instance, whose primary job is reading
	 * {@link Request}s.
	 */
	RequestReader getRequestReader();

	/**
	 * Returns {@link ResponseWriter} instance, whose primary job is writing
	 * {@link Response}s.
	 */
	ResponseWriter getResponseWriter();

	/**
	 * Returns {@link CommandHandler} instance, whose primary job is preparing
	 * appropriate {@link Response} based on {@link Request}.
	 */
	CommandHandler getCommandHandler();
}
