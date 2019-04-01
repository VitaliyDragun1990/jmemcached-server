package com.revenat.jmemcached.server.domain.impl;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revenat.jmemcached.exception.JMemcachedException;
import com.revenat.jmemcached.protocol.RequestReader;
import com.revenat.jmemcached.protocol.ResponseWriter;
import com.revenat.jmemcached.protocol.model.Request;
import com.revenat.jmemcached.protocol.model.Response;
import com.revenat.jmemcached.server.domain.CommandHandler;
import com.revenat.jmemcached.server.domain.RequestProcessor;

/**
 * Default implementation of the {@link RequestProcessor} interface.
 * 
 * @author Vitaly Dragun
 *
 */
class DefaultRequestProcessor implements RequestProcessor {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRequestProcessor.class);
	
	private RequestReader requestReader;
	private ResponseWriter responseWriter;
	private CommandHandler commandHandler;
	
	DefaultRequestProcessor(RequestReader requestReader, ResponseWriter responseWriter, CommandHandler commandHandler) {
		this.requestReader = requireNonNull(requestReader, "requestReader can not be null");
		this.responseWriter = requireNonNull(responseWriter, "responseWriter can not be null");
		this.commandHandler = requireNonNull(commandHandler, "commandHandler can not be null");
	}

	@Override
	public void process(InputStream clientInput, OutputStream clientOutput) throws IOException {
		try {
			Request request = requestReader.readFrom(clientInput);
			Response response = commandHandler.handle(request);
			responseWriter.writeTo(clientOutput, response);
			LOGGER.debug("Command {} -> {}", request, response);
		} catch (RuntimeException e) {
			throw new JMemcachedException("Process request failed", e);
		}
	}
}
