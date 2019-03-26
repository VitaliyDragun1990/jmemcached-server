package com.revenat.jmemcached.server.domain.impl;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
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
import com.revenat.jmemcached.server.domain.Storage;

/**
 * Default implementation of the {@link Storage} interface which periodically
 * checks for expired items in the store and deletes the found ones.
 * 
 * @author Vitaly Dragun
 *
 */
class DefaultStorage implements Storage {
	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultStorage.class);
	private static final String CLEAR_THREAD_NAME = "clearExpiredDataJobThread";
	private static final String KEY_CAN_NOT_BE_NULL = "key can not be null";

	private final Map<String, StorageItem> storage;
	private final ExecutorService executorService;
	private final Runnable clearExpiredDataJob;
	private final DateTimeProvider dateTimeProvider;

	DefaultStorage(DateTimeProvider dateTimeProvider, int clearDataInterval) {
		this.storage = createStorage();
		this.executorService = createClearExpiredDataExecutorService();
		this.clearExpiredDataJob = createClearExpiredDataJob(clearDataInterval);
		this.executorService.submit(clearExpiredDataJob);
		this.dateTimeProvider = dateTimeProvider;
	}

	private Map<String, StorageItem> createStorage() {
		return new ConcurrentHashMap<>();
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

	private Runnable createClearExpiredDataJob(int clearDataIntervalMillis) {
		return new ClearExpiredDataJob(storage, clearDataIntervalMillis);
	}

	@Override
	public Status put(String key, long ttl, byte[] data) {
		return putInStorage(key, ttl, data);
	}

	private Status putInStorage(String key, Long ttl, byte[] data) {
		requireNonNull(key, KEY_CAN_NOT_BE_NULL);
		requireNonNull(data, "data can not be null");
		requireNotEmpty(data);

		StorageItem oldItem = storage.put(key, new StorageItem(key, ttl, data, dateTimeProvider));
		Status status = oldItem == null ? Status.ADDED : Status.REPLACED;
		LOGGER.debug("Data with key {} was {} in storage", key, status);
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

		StorageItem item = storage.get(key);
		if (item == null || item.isExpired()) {
			LOGGER.debug("Data with key {} was not found in storage", key);
			return new byte[0];
		}
		LOGGER.debug("Data with key {} was retrieved from storage", key);
		return item.getData();
	}

	@Override
	public Status remove(String key) {
		requireNonNull(key, KEY_CAN_NOT_BE_NULL);

		StorageItem item = storage.remove(key);
		Status status = item != null && !item.isExpired() ? Status.REMOVED : Status.NOT_FOUND;
		LOGGER.debug("Data with key {} was {} in/from storage", key, status);
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
		// DO nothing. Daemon threads destroy automatically.
	}

	/**
	 * This inner class is a {@link DefaultStorage} specific component which
	 * represents single item in the store.
	 * 
	 * @author Vitaly Dragun
	 *
	 */
	static class StorageItem {
		final String key;
		final Long ttl;
		private final byte[] data;
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

		public byte[] getData() {
			return Arrays.copyOf(data, data.length);
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
	 * This inner class is strictly specific to {@link DefaultStorage} and
	 * represents a clearing job that should be done in a separate thread and is
	 * aimed to clear expired {@link StorageItem}s from the store.
	 * 
	 * @author Vitaly Dragun
	 *
	 */
	static class ClearExpiredDataJob implements Runnable {
		private static final Logger LOGGER = LoggerFactory.getLogger(ClearExpiredDataJob.class);

		private final Map<String, StorageItem> storage;
		private final int clearDataIntervalMillis;

		ClearExpiredDataJob(Map<String, StorageItem> storage, int clearDataIntervalMillis) {
			this.storage = storage;
			this.clearDataIntervalMillis = clearDataIntervalMillis;
		}

		@Override
		public void run() {
			LOGGER.debug("{} started with interval {} millis", Thread.currentThread().getName(),
					clearDataIntervalMillis);
			while (isShouldContinue()) {
				LOGGER.trace("Invoke clearing job");
				clearExpiredItems();
				try {
					sleepUntilNextCheck();
				} catch (InterruptedException e) {
					break;
				}
			}
		}

		private boolean isShouldContinue() {
			return !Thread.interrupted();
		}

		private void sleepUntilNextCheck() throws InterruptedException {
			TimeUnit.MILLISECONDS.sleep(clearDataIntervalMillis);
		}

		void clearExpiredItems() {
			for (Map.Entry<String, StorageItem> entry : storage.entrySet()) {
				if (entry.getValue().isExpired()) {
					StorageItem item = storage.remove(entry.getKey());
					LOGGER.debug("Removed expired StorageItem={}", item);
				}
			}
		}
	}
}
