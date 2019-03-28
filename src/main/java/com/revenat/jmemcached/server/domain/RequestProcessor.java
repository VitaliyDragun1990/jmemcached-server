package com.revenat.jmemcached.server.domain;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.revenat.jmemcached.protocol.model.Response;

public interface RequestProcessor {

	/**
	 * Reads client's {@link Request} from provided {@link InputStream}, creates
	 * appropriate {@link Response} and writes it back to client via provided
	 * {@link OutputStream}.
	 * 
	 * @param clientInput  {@link InputStream} to read {@link Request} from.
	 * @param clientOutput {@link OutputStream} to write {@link Response} to.
	 * @throws IOException 
	 */
	void process(InputStream clientInput, OutputStream clientOutput) throws IOException;
}
