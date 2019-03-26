package com.revenat.jmemcached.server.domain.impl;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.revenat.jmemcached.server.domain.DateTimeProvider;
import com.revenat.jmemcached.server.domain.impl.DefaultStorage.StorageItem;

@RunWith(MockitoJUnitRunner.Silent.class)
public class StorageItemTest {
	private static final LocalDateTime DATE_TIME = LocalDateTime.now();
	private static final long CURRENT_TIME_IN_MILLIS = 1000L;
	private static final String ANY_KEY = "key";
	private static final byte[] ANY_DATA = new byte[] { 1, 2, 3 };

	@Mock
	private DateTimeProvider dateTimeProvider;

	private StorageItem storageItem;

	@Before
	public void setUp() {
		when(dateTimeProvider.getCurrentTimeInMillis()).thenReturn(CURRENT_TIME_IN_MILLIS);
		when(dateTimeProvider.getDateTimeFrom(anyLong())).thenReturn(DATE_TIME);
	}

	@Test
	public void shouldReturnCopyOfTheData() throws Exception {
		storageItem = new StorageItem(ANY_KEY, null, ANY_DATA, dateTimeProvider);
		byte[] copy = storageItem.getData();
		copy[0] = 100;

		assertArrayEquals(ANY_DATA, storageItem.getData());
	}

	@Test
	public void shouldBeExpiredFromTheBeginingIfCreatedWithNegativeTtl() throws Exception {
		long negativeTtl = -1L;
		storageItem = new StorageItem(ANY_KEY, negativeTtl, ANY_DATA, dateTimeProvider);

		assertTrue("Should be expired with negative ttl", storageItem.isExpired());
	}

	@Test
	public void shouldBeExpiredIfTtlTimeElapsed() throws Exception {
		long positiveTtl = 100L;
		storageItem = new StorageItem(ANY_KEY, positiveTtl, ANY_DATA, dateTimeProvider);
		assertFalse("Should not be expired from the beginning with positive ttl", storageItem.isExpired());

		when(dateTimeProvider.getCurrentTimeInMillis()).thenReturn(CURRENT_TIME_IN_MILLIS + positiveTtl + 1);
		assertTrue("Should be expired after elapsed time > ttl", storageItem.isExpired());
	}

	@Test
	public void shouldNeverExpiresIfCreatedWithNullTtl() throws Exception {
		storageItem = new StorageItem(ANY_KEY, null, ANY_DATA, dateTimeProvider);
		when(dateTimeProvider.getCurrentTimeInMillis()).thenReturn(Long.MAX_VALUE);

		assertFalse("Should not expire with null ttl", storageItem.isExpired());
	}

	@Test
	public void shouldContainTimeToLiveDateInStringIfCreatedWithTtl() throws Exception {
		storageItem = new StorageItem(ANY_KEY, 100L, ANY_DATA, dateTimeProvider);
		String s = storageItem.toString();

		assertThat(s, containsString(String.format("(%s)",DATE_TIME)));
	}

	@Test
	public void shouldContainKeyInStringRepresentation() throws Exception {
		storageItem = new StorageItem(ANY_KEY, null, ANY_DATA, dateTimeProvider);
		String s = storageItem.toString();

		assertThat(s, containsString(String.format("[%s]", ANY_KEY)));
	}
	
	@Test
	public void shouldContainDataLengthInStringRepresentation() throws Exception {
		storageItem = new StorageItem(ANY_KEY, null, ANY_DATA, dateTimeProvider);
		String s = storageItem.toString();

		assertThat(s, containsString(String.format("%d bytes", ANY_DATA.length)));
	}

}
