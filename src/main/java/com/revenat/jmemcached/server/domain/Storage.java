package com.revenat.jmemcached.server.domain;

import com.revenat.jmemcached.protocol.model.Status;

/**
 * This interface represents component responsible for storing data.
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
	 *             specified key-data pair should be stored. {@code ttl} value <
	 *             {@code 0} would be treated as given {@code data} should be
	 *             removed immediately after it has been added to the store.
	 * @param data data in form of byte array to be stored in this storage
	 * @return {@link Status} which represents result of this operation
	 * @throws NullPointerException     if whether {@code key} or {@code data} is
	 *                                  null.
	 * @throws IllegalArgumentException if {@code data} is empty.
	 */
	Status put(String key, long ttl, byte[] data);

	/**
	 * Adds data into this storage using specified {@code key} and {@code ttl}
	 * parameters
	 * 
	 * @param key  string key associated with specified {@code data}
	 * @param data data in form of byte array to be stored in this storage
	 * @return {@link Status} which represents result of this operation
	 * @throws NullPointerException     if either {@code key} or {@code data} is
	 *                                  null.
	 * @throws IllegalArgumentException if {@code data} is empty.
	 */
	Status put(String key, byte[] data);

	/**
	 * Returns data associated with a specified {@code key}.
	 * 
	 * @param key key to get data associated with.
	 * @return data associated with a specified {@code key} or an empty array if no
	 *         data with such key exists in the storage.
	 * @throws NullPointerException if {@code key} is null.
	 */
	byte[] get(String key);

	/**
	 * Removes data associated with a specified {@code key} if any.
	 * 
	 * @param key key to remove data associated with.
	 * @return {@link Status} which represents result of this operation.
	 * @throws NullPointerException if {@code key} is null.
	 */
	Status remove(String key);

	/**
	 * Clears the storage, removing all data in it.
	 * 
	 * @return {@link Status} which represents result of this operation.
	 */
	Status clear();
}
