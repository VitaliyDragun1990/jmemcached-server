package com.revenat.jmemcached.server.domain.impl;

import static java.util.Objects.requireNonNull;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revenat.jmemcached.protocol.model.Status;
import com.revenat.jmemcached.server.domain.DateTimeProvider;
import com.revenat.jmemcached.server.domain.ServerStorage;

/**
 * Default implementation of the {@link ServerStorage} interface which periodically
 * checks for expired items in the store and deletes the found ones.
 * 
 * @author Vitaly Dragun
 *
 */
class DefaultServerStorage implements ServerStorage {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServerStorage.class);
	private static final String CLEAR_THREAD_NAME = "expiredDataCleanerThread";
	private static final String KEY_CAN_NOT_BE_NULL = "key can not be null";

	private final InnerStorage storage;
	private final ExecutorService executorService;
	private final ExpiredDataCleaner expiredDataCleaner;

	DefaultServerStorage(DateTimeProvider dateTimeProvider, int clearDataInterval) {
		this.storage = new InnerStorage(dateTimeProvider);
		this.executorService = createClearExpiredDataExecutorService();
		this.expiredDataCleaner = new ExpiredDataCleaner(storage, clearDataInterval);
		this.executorService.submit(expiredDataCleaner);
	}

	private ExecutorService createClearExpiredDataExecutorService() {
		return Executors.newSingleThreadExecutor(createClearExpiredDataThreadFactory());
	}

	private ThreadFactory createClearExpiredDataThreadFactory() {
		return job -> {
			Thread clearExpiredDataJobThread = new Thread(job, CLEAR_THREAD_NAME);
			clearExpiredDataJobThread.setPriority(Thread.MIN_PRIORITY);
			clearExpiredDataJobThread.setDaemon(true);
			return clearExpiredDataJobThread;
		};
	}

	@Override
	public Status put(String key, long ttl, byte[] data) {
		return putInStorage(key, ttl, data);
	}

	private Status putInStorage(String key, Long ttl, byte[] data) {
		requireNonNull(key, KEY_CAN_NOT_BE_NULL);
		requireNonNull(data, "data can not be null");
		requireNotEmpty(data);

		byte[] oldData = storage.put(key, ttl, data);
		Status status = oldData == null ? Status.ADDED : Status.REPLACED;
		LOGGER.debug("Data with key '{}' was {} in the storage", key, status);
		return status;
	}

	private static void requireNotEmpty(byte[] data) {
		if (data.length == 0) {
			throw new IllegalArgumentException("data can not be empty");
		}
	}

	@Override
	public Status put(String key, byte[] data) {
		return putInStorage(key, null, data);
	}

	@Override
	public byte[] get(String key) {
		requireNonNull(key, KEY_CAN_NOT_BE_NULL);

		byte[] data = storage.get(key);
		if (data == null) {
			LOGGER.debug("Data with key '{}' was not found in the storage", key);
			return new byte[0];
		}
		LOGGER.debug("Data with key '{}' was retrieved from the storage", key);
		return data;
	}

	@Override
	public Status remove(String key) {
		requireNonNull(key, KEY_CAN_NOT_BE_NULL);

		byte[] data =  storage.remove(key);
		Status status = data != null ? Status.REMOVED : Status.NOT_FOUND;
		LOGGER.debug("Data with key '{}' was {} in/from the storage", key, status);
		return status;
	}

	@Override
	public Status clear() {
		storage.clear();
		LOGGER.debug("Storage has been cleared");
		return Status.CLEARED;
	}

	@Override
	public void close() throws Exception {
		executorService.shutdown();
	}
	
	/**
	 * This inner class represents in-memory storage for server clients data
	 * and fully supports concurrent modification operations, which is essential
	 * in case of the server multithreading nature.
	 * 
	 * @author Vitaly Dragun
	 *
	 */
	static class InnerStorage implements Iterable<StorageItem>{
		private Map<String, StorageItem> items = new ConcurrentHashMap<>();
		private final DateTimeProvider dateTimeProvider;
		
		InnerStorage(DateTimeProvider dateTimeProvider) {
			this.dateTimeProvider = dateTimeProvider;
		}
		
		byte[] put(String key, Long ttl, byte[] data) {
			StorageItem item = new StorageItem(key, ttl, data, dateTimeProvider);
			StorageItem oldItem = items.put(key, item);
			return oldItem != null ? oldItem.data : null;
		}
		
		byte[] get(String key) {
			StorageItem item = items.get(key);
			return (item != null && !item.isExpired()) ? item.data : null;
		}
		
		byte[] remove(String key) {
			StorageItem item = items.remove(key);
			return item != null ? item.data : null;
		}
		
		void clear() {
			items.clear();
		}

		@Override
		public Iterator<StorageItem> iterator() {
			return items.values().iterator();
		}
	}

	/**
	 * This inner class is a {@link DefaultServerStorage} specific component which
	 * represents single item in the store.
	 * 
	 * @author Vitaly Dragun
	 *
	 */
	static class StorageItem {
		final String key;
		final Long ttl;
		final byte[] data;
		private final DateTimeProvider dateTimeProvider;

		StorageItem(String key, Long ttl, byte[] data, DateTimeProvider dateTimeProvider) {
			this.key = key;
			this.data = data;
			this.ttl = ttl != null ? ttl + dateTimeProvider.getCurrentTimeInMillis() : null;
			this.dateTimeProvider = dateTimeProvider;
		}

		boolean isExpired() {
			return ttl != null && ttl.longValue() < dateTimeProvider.getCurrentTimeInMillis();
		}

		@Override
		public String toString() {
			String s = String.format("[%s]=%d bytes", key, data.length);
			if (ttl != null) {
				s += String.format(" (%s)", dateTimeProvider.getDateTimeFrom(ttl.longValue()));
			}
			return s;
		}
	}

	/**
	 * This inner class is strictly specific to {@link DefaultServerStorage} and
	 * represents a cleaning task that should be done in a separate thread and is
	 * aimed to clean expired {@link StorageItem}s from the store.
	 * 
	 * @author Vitaly Dragun
	 *
	 */
	static class ExpiredDataCleaner implements Runnable {
		private static final Logger LOGGER = LoggerFactory.getLogger(ExpiredDataCleaner.class);

		private final InnerStorage storage;
		private final int clearDataIntervalMillis;

		ExpiredDataCleaner(InnerStorage storage, int clearDataIntervalMillis) {
			this.storage = storage;
			this.clearDataIntervalMillis = clearDataIntervalMillis;
		}

		@Override
		public void run() {
			LOGGER.debug("{} started with interval {} millis", Thread.currentThread().getName(),
					clearDataIntervalMillis);
			while (notInterrupted()) {
				LOGGER.trace("Invoke cleaning job");
				clearExpiredItems();
				try {
					sleepUntilNextCheck();
				} catch (InterruptedException e) {
					break;
				}
			}
		}

		private boolean notInterrupted() {
			return !Thread.interrupted();
		}
		
		void clearExpiredItems() {
			for (StorageItem item : storage) {
				if (item.isExpired()) {
					storage.remove(item.key);
					LOGGER.debug("Removed expired StorageItem={}", item);
				}
			}
		}

		private void sleepUntilNextCheck() throws InterruptedException {
			TimeUnit.MILLISECONDS.sleep(clearDataIntervalMillis);
		}
	}
}
