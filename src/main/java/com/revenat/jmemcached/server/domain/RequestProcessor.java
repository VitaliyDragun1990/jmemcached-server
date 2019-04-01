package com.revenat.jmemcached.server.domain;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.revenat.jmemcached.exception.JMemcachedException;
import com.revenat.jmemcached.protocol.model.Response;

/**
 * This interface represents component responsible for reading client's
 * {@link Request} from {@link InputStream}, processing that request, creating
 * appropriate {@link Response} object and sending it back to client via
 * {@link OutputStream}.
 * 
 * @author Vitaly Dragun
 *
 */
public interface RequestProcessor {

	/**
	 * Reads client's {@link Request}, processes it, creates appropriate
	 * {@link Response} and sends it back to client via {@link OutputStream}.
	 * 
	 * @param clientInput  {@link InputStream} to read {@link Request} from.
	 * @param clientOutput {@link OutputStream} to write {@link Response} to.
	 * @throws IOException         if Input/Output error occurred during
	 *                             reading/writing client request/server response
	 * @throws JMemcachedException if error occurred during handling client request.
	 */
	void process(InputStream clientInput, OutputStream clientOutput) throws IOException;
}
