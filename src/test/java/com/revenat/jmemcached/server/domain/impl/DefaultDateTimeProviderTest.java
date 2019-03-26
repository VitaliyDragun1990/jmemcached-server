package com.revenat.jmemcached.server.domain.impl;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.Test;

public class DefaultDateTimeProviderTest {
	private static final long CURRENT_TIME = 1000;
	private static final ZoneId DEFAUL_ZONE_ID = ZoneId.systemDefault();

	private Clock clock = Clock.fixed(Instant.ofEpochMilli(CURRENT_TIME), DEFAUL_ZONE_ID);
	
	private DefaultDateTimeProvider provider = new DefaultDateTimeProvider(clock);

	@Test
	public void shouldAllowToGetCurrentTimeInMillis() throws Exception {
		long currentTimeInMillis = provider.getCurrentTimeInMillis();
		
		assertThat(currentTimeInMillis, equalTo(CURRENT_TIME));
	}
	
	@Test
	public void shouldAllowToGetLocalDateTimeFromTimeInMillis() throws Exception {
		LocalDateTime dateTime = provider.getDateTimeFrom(CURRENT_TIME);
		
		assertThat(dateTime, equalTo(LocalDateTime.now(clock)));
	}

}
