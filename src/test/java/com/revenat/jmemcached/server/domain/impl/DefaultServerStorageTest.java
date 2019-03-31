package com.revenat.jmemcached.server.domain.impl;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.revenat.jmemcached.protocol.model.Status;
import com.revenat.jmemcached.server.domain.DateTimeProvider;

@RunWith(MockitoJUnitRunner.Silent.class)
public class DefaultServerStorageTest {
	private static final byte[] ANY_DATA = new byte[] {1, 2, 3};
	private static final int CLEAR_DATA_INTERVAL_MILLIS = 1000;

	@Mock
	private DateTimeProvider dateTimeProvider;
	
	private DefaultServerStorage storage;
	
	@Before
	public void setUp() {
		storage = new DefaultServerStorage(dateTimeProvider, CLEAR_DATA_INTERVAL_MILLIS);
	}
	
	@Test(expected = NullPointerException.class)
	public void shouldNotAllowToPutWithNullKey() throws Exception {
		storage.put(null, ANY_DATA);
	}
	
	@Test(expected = NullPointerException.class)
	public void shouldNotAllowToPutWithNullData() throws Exception {
		storage.put("one", null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void shouldNotAllowToPutWithEmptyData() throws Exception {
		storage.put("one", new byte[0]);
	}
	
	@Test
	public void shouldReturnStatusAddedIfPutWithUniqueKey() throws Exception {
		Status status = storage.put("one", ANY_DATA);
		
		assertThat(status, equalTo(Status.ADDED));
	}
	
	@Test
	public void shouldReturnStatusReplacedIfPutWithAlreadyStoredKey() throws Exception {
		storage.put("one", ANY_DATA);
		Status status = storage.put("one", ANY_DATA);
		
		assertThat(status, equalTo(Status.REPLACED));
	}
	
	@Test(expected = NullPointerException.class)
	public void shouldNotAllowToGetWithNullKey() throws Exception {
		storage.get(null);
	}
	
	@Test
	public void shouldAllowToReturnDataByKey() throws Exception {
		storage.put("one", ANY_DATA);
		
		byte[] result = storage.get("one");
		
		assertThat(result, equalTo(ANY_DATA));
	}
	
	@Test
	public void shouldReturnEmptyArrayForUnknownKey() throws Exception {
		byte[] result = storage.get("one");
		
		assertThat(result.length, equalTo(0));
	}
	
	@Test(expected = NullPointerException.class)
	public void shouldNotAllowToRemoveWithEmptyKey() throws Exception {
		storage.remove(null);
	}
	
	@Test
	public void shouldAllowToRemoveData() throws Exception {
		storage.put("one", ANY_DATA);
		storage.remove("one");
		
		byte[] data = storage.get("one");
		
		assertEmptyData(data);
	}
	
	@Test
	public void shouldReturnStatusRemovedIfDataWasRemoved() throws Exception {
		storage.put("one", ANY_DATA);
		
		Status status = storage.remove("one");
		
		assertThat(status, equalTo(Status.REMOVED));
	}
	
	@Test
	public void shouldReturnStatusNotFoundIfDataNotFoundForRemoving() throws Exception {
		Status status = storage.remove("one");
		
		assertThat(status, equalTo(Status.NOT_FOUND));
	}
	
	@Test
	public void shouldAllowToCearTheStore() throws Exception {
		storage.put("one", ANY_DATA);
		storage.put("two", ANY_DATA);
		
		storage.clear();
		
		assertEmptyData(storage.get("one"));
		assertEmptyData(storage.get("two"));
	}
	
	@Test
	public void shouldReturnsStatusClearWhenStorageIsCleared() throws Exception {
		Status status = storage.clear();
		
		assertThat(status, equalTo(Status.CLEARED));
	}
	
	@Test
	public void shouldClearExpiredData() throws Exception {
		storage.put("one", 500L, ANY_DATA);
		makeTimePass(1000L);
		
		assertEmptyData(storage.get("one"));
	}
	
	@Test
	public void shouldConsiderDataWithNegativeTtlAlreadyExpired() throws Exception {
		storage.put("one", -500L, ANY_DATA);
		
		assertEmptyData(storage.get("one"));
	}

	private void makeTimePass(long elapsedTime) throws InterruptedException {
		when(dateTimeProvider.getCurrentTimeInMillis()).thenReturn(elapsedTime);
		TimeUnit.MILLISECONDS.sleep(elapsedTime);
	}

	private static void assertEmptyData(byte[] data) {
		assertThat(data.length, equalTo(0));
	}
}
