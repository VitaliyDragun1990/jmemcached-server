package com.revenat.jmemcached.server;

import com.revenat.jmemcached.protocol.model.Status;

/**
 * This interface represents component responsible for storing data in the form
 * of {@code Hashtable}
 * 
 * @author Vitaly Dragun
 *
 */
public interface Storage extends AutoCloseable {

	/**
	 * Adds data into this storage using specified {@code key} and {@code ttl}
	 * parameters
	 * 
	 * @param key  string key associated with specified {@code data}
	 * @param ttl  time-to-live parameter in milliseconds representing for how long
	 *             specified key-data pair should be stored.
	 * @param data data in form of byte array to be stored in this storage
	 * @return {@link Status} which represents result of this operation
	 */
	Status put(String key, Long ttl, byte[] data);

	/**
	 * Returns data associated with a specified {@code key}.
	 * 
	 * @param key key to get data associated with.
	 * @return data associated with a specified {@code key} or null if no data with
	 *         a such key in the storage.
	 */
	byte[] get(String key);

	/**
	 * Removes data associated with a specified {@code key} if any.
	 * 
	 * @param key key to remove data associated with.
	 * @return {@link Status} which represents result of this operation.
	 */
	Status remove(String key);

	/**
	 * Clears the storage, removing all data in it.
	 * 
	 * @return {@link Status} which represents result of this operation.
	 */
	Status clear();
}
