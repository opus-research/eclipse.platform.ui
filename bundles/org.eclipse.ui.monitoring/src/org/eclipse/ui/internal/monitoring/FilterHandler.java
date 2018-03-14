/*******************************************************************************
 * Copyright (C) 2014, Google Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marcus Eng (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.monitoring;

import org.eclipse.ui.monitoring.StackSample;
import org.eclipse.ui.monitoring.UiFreezeEvent;

import java.lang.management.ThreadInfo;
import java.util.Arrays;

/**
 * Checks if the {@link UiFreezeEvent} matches any defined filters.
 */
public class FilterHandler {
	/**
	 * Groups the class name and method name defined in the filter.
	 */
	private class Filter implements Comparable<Filter>{
		final String className;
		final String methodName;

		public Filter(String className, String methodName) {
			this.className = className;
			this.methodName = methodName;
		}
		
		@Override
		public int compareTo(Filter other) {
			if (!this.methodName.equals(other.methodName)) {
				return this.methodName.compareTo(other.methodName);
			} else if (!this.className.equals(other.className)) {
				return this.className.compareTo(other.className);
			}
			return 0;
		}
	}

	private final Filter[] filters;

	public FilterHandler(String unparsedFilters) {
		String[] rawFilters = unparsedFilters.split(","); //$NON-NLS-1$
		filters = new Filter[rawFilters.length];

		for (int i = 0; i < rawFilters.length; i++) {
			String currentFilter = rawFilters[i];
			int period = currentFilter.lastIndexOf('.');

			Filter filter = new Filter(currentFilter.substring(0, period),
					currentFilter.substring(period + 1));

			filters[i] = filter;
		}
		
		Arrays.sort(filters);
	}

	/**
	 * Returns {@code true} if the {@link UiFreezeEvent} can be logged after checking the
	 * contained {@link StackSample}s against the defined filters.
	 */
	public boolean shouldLogEvent(UiFreezeEvent event, long displayThreadId) {
		for (int i = 0; i < event.getSampleCount(); i++) {
			StackSample sample = event.getStackTraceSamples()[i];
			if (hasFilteredTraces(sample.getStackTraces(), displayThreadId)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Checks if the stack trace contains fully qualified methods that were specified to be ignored.
	 */
	private boolean hasFilteredTraces(ThreadInfo[] stackTraces, long displayThreadId) {
		for (ThreadInfo threadInfo : stackTraces) {
			if (threadInfo.getThreadId() == displayThreadId) {
				return matchesFilter(threadInfo.getStackTrace());
			}
		}

		MonitoringPlugin.logError(Messages.FilterHandler_missing_thread_error, null);
		return false;
	}

	private boolean matchesFilter(StackTraceElement[] stackTraces) {
		for (StackTraceElement element : stackTraces) {
			for (Filter filter : filters ) {
				if (element.getMethodName().equals(filter.methodName)
						&& element.getClassName().equals(filter.className)) {
					return true;
				}
			}
		}
		return false;
	}
}
