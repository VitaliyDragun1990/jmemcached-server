package com.revenat.jmemcached.server;

/**
 * This interface represents JMemcached Server instance.
 * 
 * @author Vitaly Dragun
 *
 */
public interface Server {

	/**
	 * Starts JMemcached Server.
	 */
	void start();
	
	/**
	 * Stops JMemcached Server.
	 */
	void stop();
}
