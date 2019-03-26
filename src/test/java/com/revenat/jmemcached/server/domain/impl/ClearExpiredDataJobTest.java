package com.revenat.jmemcached.server.domain.impl;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.revenat.jmemcached.server.domain.DateTimeProvider;
import com.revenat.jmemcached.server.domain.impl.DefaultStorage.ClearExpiredDataJob;
import com.revenat.jmemcached.server.domain.impl.DefaultStorage.StorageItem;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ClearExpiredDataJobTest {
	private static final byte[] ANY_DATA = new byte[] {1, 2, 3};
	private static final long CURRENT_TIME_MILLIS = 1000L;
	private static final int CLEAR_DATA_INTERVAL_MILLIS = 1000;
	
	@Mock
	private DateTimeProvider dateTimeProvider;
	
	private ClearExpiredDataJob job;
	
	@Before
	public void setUp() {
		when(dateTimeProvider.getCurrentTimeInMillis()).thenReturn(CURRENT_TIME_MILLIS);
	}

	@Test
	public void shouldClearExpiredItems() throws Exception {
		Map<String, StorageItem> storage = createStorageWith(
				createStorageItem("one", 500L, ANY_DATA)
				);
		job = createNewJob(storage);
		assertThat(storage.size(), equalTo(1));
		
		makeTimePass(1000);
		job.clearExpiredItems();
		
		assertThat(storage.size(), equalTo(0));
	}

	private static ClearExpiredDataJob createNewJob(Map<String, StorageItem> storage) {
		return new ClearExpiredDataJob(storage, CLEAR_DATA_INTERVAL_MILLIS);
	}
	
	@Test
	public void shouldLeaveItemsThatNotExpired() throws Exception {
		Map<String, StorageItem> storage = createStorageWith(
				createStorageItem("one", null, ANY_DATA)
				);
		job = createNewJob(storage);
		assertThat(storage.size(), equalTo(1));
		
		makeTimePass(1000);
		job.clearExpiredItems();
		
		assertThat(storage.size(), equalTo(1));
	}
	
	@Test
	public void shouldClearExpiredItemsDuringCheckIteration() throws Exception {
		Map<String, StorageItem> storage = createStorageWith(
				createStorageItem("one", 500L, ANY_DATA),
				createStorageItem("two", 200L, ANY_DATA)
				);
		job = createNewJob(storage);
		Thread t = createDaemonThread(job);
		assertThat(storage.size(), equalTo(2));
		
		makeTimePass(1000);
		t.start();
		
		TimeUnit.MILLISECONDS.sleep(CLEAR_DATA_INTERVAL_MILLIS);
		
		assertThat(storage.size(), equalTo(0));
	}
	
	@Test
	public void shouldNotClearItemsIfTheyAreNotExpiredDuringCheckIteration() throws Exception {
		Map<String, StorageItem> storage = createStorageWith(
				createStorageItem("one", 5000L, ANY_DATA),
				createStorageItem("two", 20000L, ANY_DATA)
				);
		job = createNewJob(storage);
		Thread t = createDaemonThread(job);
		assertThat(storage.size(), equalTo(2));
		
		makeTimePass(1000);
		t.start();
		
		TimeUnit.MILLISECONDS.sleep(CLEAR_DATA_INTERVAL_MILLIS);
		
		assertThat(storage.size(), equalTo(2));
	}
	
	private void makeTimePass(int timeInMillis) {
		when(dateTimeProvider.getCurrentTimeInMillis()).thenReturn(CURRENT_TIME_MILLIS + timeInMillis);
	}

	private static Thread createDaemonThread(ClearExpiredDataJob job) {
		Thread t = new Thread(job);
		t.setDaemon(true);
		return t;
	}

	private Map<String, StorageItem> createStorageWith(StorageItem ... items) {
		Map<String, StorageItem> storage = new ConcurrentHashMap<>();
		for (StorageItem storageItem : items) {
			storage.put(storageItem.key, storageItem);
		}
		return storage;
	}

	private StorageItem createStorageItem(String key, Long ttl, byte[] data) {
		return new StorageItem(key, ttl, data, dateTimeProvider);
	}

}
