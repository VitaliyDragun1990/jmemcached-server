package com.revenat.jmemcached.server.domain;

import java.time.LocalDateTime;

/**
 * This interface represents component responsible for providing
 * date/time related operations.
 * 
 * @author Vitaly Dragun
 *
 */
public interface DateTimeProvider {

	/**
	 * This returns the millisecond-based instant of time, measured from
	 * 1970-01-01T00:00Z (UTC). This is equivalent to the definition of
	 * {@link System#currentTimeMillis()}.
	 */
	long getCurrentTimeInMillis();

	/**
	 * Returns an instance of {@link LicalDateTime} representing provided
	 * {@code millis} parameter, which should be millisecond-based instant of time,
	 * measured from 1970-01-01T00:00Z (UTC). Uses server default time-zone to
	 * produce the result. If the server default time-zone is changed, then the
	 * result of this method will also change
	 * 
	 * @param millis millisecond-based instant of time, measured from
	 *               1970-01-01T00:00Z (UTC)
	 * @return an instance of {@link LicalDateTime}
	 */
	LocalDateTime getDateTimeFrom(long millis);
}
