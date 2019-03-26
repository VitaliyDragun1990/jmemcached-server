package com.revenat.jmemcached.server.domain.impl;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;

import com.revenat.jmemcached.server.domain.DateTimeProvider;

/**
 * Default implementation of the {@link DateTimeProvider}
 * 
 * @author Vitaly Dragun
 *
 */
class DefaultDateTimeProvider implements DateTimeProvider {
	private final Clock clock;

	DefaultDateTimeProvider(Clock clock) {
		this.clock = clock;
	}

	@Override
	public long getCurrentTimeInMillis() {
		return clock.millis();
	}

	@Override
	public LocalDateTime getDateTimeFrom(long millis) {
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), clock.getZone());
	}

}
