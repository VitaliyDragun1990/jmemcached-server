package com.revenat.jmemcached.server.domain.impl;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.time.LocalDateTime;
import java.util.Arrays;

import org.junit.Test;

import com.revenat.jmemcached.server.domain.DateTimeProvider;
import com.revenat.jmemcached.server.domain.impl.DefaultServerStorage.InnerStorage;
import com.revenat.jmemcached.server.domain.impl.DefaultServerStorage.StorageItem;

public class InnerStorageTest {
	
	private DateTimeProviderStub dateTimeProvider = new DateTimeProviderStub();

	private InnerStorage innerStorage = new InnerStorage(dateTimeProvider);
	
	@Test
	public void shouldBeEmptyAfterCreation() throws Exception {
		innerStorage = new InnerStorage(dateTimeProvider);
		
		assertEmpty(innerStorage);
	}
	
	@Test
	public void shouldAllowToStoreDataWithGivenKey() throws Exception {
		String key = "any key";
		byte[] data = {1, 2, 3};
		
		innerStorage.put(key, null, data);
		
		assertContains(innerStorage, key, data);
	}
	
	@Test
	public void shouldAllowToGetOldDataIfStoringWithTheSameKey() throws Exception {
		String key = "any key";
		byte[] data = {1, 2, 3};
		innerStorage.put(key, null, data);
		
		byte[] oldData = innerStorage.put(key, null, new byte[] {1, 1, 1});
		
		assertThat(oldData, equalTo(data));
	}
	
	@Test
	public void shouldReturnNullWhenStoringAndThereisNoOldDataWithGivenKey() throws Exception {
		String key = "any key";
		byte[] data = {1, 2, 3};
		
		byte[] oldData = innerStorage.put(key, null, data);
		
		assertThat(oldData, nullValue());
	}
	
	@Test
	public void shouldReturnNullIfThereIsNoDataWithGivenKey() throws Exception {
		byte[] data = innerStorage.get("any key");
		
		assertThat(data, nullValue());
	}
	
	@Test
	public void shouldAllowToRemoveDataWithGivenKey() throws Exception {
		String key = "any key";
		byte[] data = {1, 2, 3};
		innerStorage.put(key, null, data);
		
		innerStorage.remove(key);
		
		assertEmpty(innerStorage);
	}
	
	@Test
	public void shouldReturnRemovedData() throws Exception {
		String key = "any key";
		byte[] data = {1, 2, 3};
		innerStorage.put(key, null, data);
		
		byte[] removedData = innerStorage.remove(key);
		
		assertThat(removedData, equalTo(data));
	}
	
	@Test
	public void shouldReturnNullIfNoDataToRemoveWithGivenkey() throws Exception {
		byte[] removedData = innerStorage.remove("any key");

		assertThat(removedData, nullValue());
	}
	
	@Test
	public void shouldContainAllAddedItems() throws Exception {
		String keyA = "any key";
		byte[] dataA = {1, 2, 3};
		String keyB = "another key";
		byte[] dataB = {4, 5, 6};
		innerStorage.put(keyA, null, dataA);
		innerStorage.put(keyB, null, dataB);
		
		assertContains(innerStorage, keyA, dataA);
		assertContains(innerStorage, keyB, dataB);
	}
	
	@Test
	public void shouldAllowToClearStorage() throws Exception {
		String keyA = "any key";
		byte[] dataA = {1, 2, 3};
		String keyB = "another key";
		byte[] dataB = {4, 5, 6};
		innerStorage.put(keyA, null, dataA);
		innerStorage.put(keyB, null, dataB);
		
		innerStorage.clear();
		
		assertEmpty(innerStorage);
	}
	
	@Test
	public void shouldTreadExpiredItemsAsNotExistent() throws Exception {
		String key = "any key";
		byte[] data = {1, 2, 3};
		long timeToLive = 1000L;
		innerStorage.put(key, timeToLive, data);
		
		makeTimePass(timeToLive + 1L);
		
		assertThat(innerStorage.get(key), nullValue());
	}
	
	@Test
	public void shouldAllowToRemoveWhileIterating() throws Exception {
		String keyA = "any key";
		byte[] dataA = {1, 2, 3};
		String keyB = "another key";
		byte[] dataB = {4, 5, 6};
		innerStorage.put(keyA, null, dataA);
		innerStorage.put(keyB, null, dataB);
		
		for (StorageItem item : innerStorage) {
			if (item.key.equals(keyA)) {
				innerStorage.remove(keyA);
			}
		}
		
		assertContains(innerStorage, keyB, dataB);
		assertThat(innerStorage.get(keyA), nullValue());
		
	}

	private void makeTimePass(long timeinMillis) {
		dateTimeProvider.setCurrentTimeInMillis(timeinMillis);
	}

	private static void assertEmpty(InnerStorage storage) {
		if (storage.iterator().hasNext()) {
			fail("storage should be empty");
		}
	}

	private static void assertContains(InnerStorage storage, String key, byte[] data) {
		boolean isContained = false;
		for (StorageItem item : storage) {
			if (item.key.equals(key) && Arrays.equals(item.data, data)) {
				isContained = true;
			}
		}
		if (!isContained) {
			fail("storage does not contain item with key=" + key + " and data=" + Arrays.toString(data));
		}
	}

	private static class DateTimeProviderStub implements DateTimeProvider {
		private long currentTimeInMillis = 0;

		@Override
		public long getCurrentTimeInMillis() {
			return currentTimeInMillis;
		}
		
		void setCurrentTimeInMillis(long currentTimeInMillis) {
			this.currentTimeInMillis = currentTimeInMillis;
		}

		@Override
		public LocalDateTime getDateTimeFrom(long millis) {
			return null;
		}
		
	}
}
