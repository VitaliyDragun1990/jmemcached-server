package com.revenat.jmemcached.server.domain.exception;

import com.revenat.jmemcached.exception.JMemcachedException;

/**
 * This exception represents the fact that the server can not establish
 * new connection with its client due to problem of some sort.
 * 
 * @author Vitaly Dragun
 *
 */
public class ConnectionRejectedException extends JMemcachedException {
	private static final long serialVersionUID = -6792889694539346876L;

	public ConnectionRejectedException(String message, Throwable cause) {
		super(message, cause);
	}
}
