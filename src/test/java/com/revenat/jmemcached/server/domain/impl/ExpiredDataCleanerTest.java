package com.revenat.jmemcached.server.domain.impl;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.revenat.jmemcached.server.domain.DateTimeProvider;
import com.revenat.jmemcached.server.domain.impl.DefaultServerStorage.ExpiredDataCleaner;
import com.revenat.jmemcached.server.domain.impl.DefaultServerStorage.InnerStorage;
import com.revenat.jmemcached.server.domain.impl.DefaultServerStorage.StorageItem;

@RunWith(MockitoJUnitRunner.Silent.class)
public class ExpiredDataCleanerTest {
	private static final byte[] ANY_DATA = new byte[] {1, 2, 3};
	private static final long CURRENT_TIME_MILLIS = 1000L;
	private static final int CLEAR_DATA_INTERVAL_MILLIS = 1000;
	
	@Mock
	private DateTimeProvider dateTimeProvider;
	
	private ExpiredDataCleaner cleaner;
	
	@Before
	public void setUp() {
		when(dateTimeProvider.getCurrentTimeInMillis()).thenReturn(CURRENT_TIME_MILLIS);
	}

	@Test
	public void shouldClearExpiredItems() throws Exception {
		InnerStorage storage = createStorage();
		storage.put( "one", 500L, ANY_DATA);
		
		cleaner = createNewCleaner(storage);
		assertStorageSize(storage, 1);
		
		makeTimePass(1000);
		cleaner.clearExpiredItems();
		
		assertStorageSize(storage, 0);
	}
	
	@Test
	public void shouldLeaveItemsThatNotExpired() throws Exception {
		InnerStorage storage = createStorage();
		storage.put( "one", null, ANY_DATA);
		cleaner = createNewCleaner(storage);
		assertStorageSize(storage, 1);
		
		makeTimePass(1000);
		cleaner.clearExpiredItems();
		
		assertStorageSize(storage, 1);
	}
	
	@Test
	public void shouldClearExpiredItemsDuringCheckIteration() throws Exception {
		InnerStorage storage = createStorage();
		storage.put("one", 500L, ANY_DATA);
		storage.put("two", 200L, ANY_DATA);
		cleaner = createNewCleaner(storage);
		Thread t = createDaemonThread(cleaner);
		assertStorageSize(storage, 2);
		
		makeTimePass(1000);
		t.start();
		
		TimeUnit.MILLISECONDS.sleep(CLEAR_DATA_INTERVAL_MILLIS);
		
		assertStorageSize(storage, 0);
	}
	
	@Test
	public void shouldNotClearItemsIfTheyAreNotExpiredDuringCheckIteration() throws Exception {
		InnerStorage storage = createStorage();
		storage.put("one", 5000L, ANY_DATA);
		storage.put("two", 20000L, ANY_DATA);
		cleaner = createNewCleaner(storage);
		Thread t = createDaemonThread(cleaner);
		assertStorageSize(storage, 2);
		
		makeTimePass(1000);
		t.start();
		
		TimeUnit.MILLISECONDS.sleep(CLEAR_DATA_INTERVAL_MILLIS);
		
		assertStorageSize(storage, 2);
	}
	
	@SuppressWarnings("unused")
	private static void assertStorageSize(InnerStorage storage, int expectedSize) {
		int size = 0;
		for (StorageItem item : storage) {
			size++;
		}
		assertThat(size, equalTo(expectedSize));
	}
	
	private static ExpiredDataCleaner createNewCleaner(InnerStorage storage) {
		return new ExpiredDataCleaner(storage, CLEAR_DATA_INTERVAL_MILLIS);
	}
	
	private void makeTimePass(int timeInMillis) {
		when(dateTimeProvider.getCurrentTimeInMillis()).thenReturn(CURRENT_TIME_MILLIS + timeInMillis);
	}

	private static Thread createDaemonThread(ExpiredDataCleaner job) {
		Thread t = new Thread(job);
		t.setDaemon(true);
		return t;
	}
	
	private InnerStorage createStorage() {
		return new InnerStorage(dateTimeProvider);
	}
}
