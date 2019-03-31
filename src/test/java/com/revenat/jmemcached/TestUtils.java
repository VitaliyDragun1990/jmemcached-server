package com.revenat.jmemcached;

import java.lang.reflect.Field;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.slf4j.Logger;

/**
 * Utility class with helper methods to facilitate testing.
 * 
 * @author Vitaly Dragun
 *
 */
public class TestUtils {

	/**
	 * Allows to set provided {@code logger} instance as class-level logger for
	 * class specified by the {@code clazz} parameter. Provided class must have
	 * field with name specified by the {@code loggerFieldName} parameter.
	 * @throws IllegalAccessException
	 */
	public static void setLoggerViaReflection(Class<?> clazz, Logger logger, String loggerFieldName)
			throws IllegalAccessException {
		Field loggerField = FieldUtils.getField(clazz, loggerFieldName, true);
		FieldUtils.removeFinalModifier(loggerField);
		loggerField.set(clazz, logger);
	}
}
