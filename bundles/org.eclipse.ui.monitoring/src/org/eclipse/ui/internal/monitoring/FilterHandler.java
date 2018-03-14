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

/**
 * Checks if the {@link UiFreezeEvent} matches any defined filters.
 */
public class FilterHandler {
	private final String[] filters;

	public FilterHandler(String unparsedFilters) {
		filters = unparsedFilters.split(","); //$NON-NLS-1$
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
			String fullyQualifiedMethodName = element.getClassName() + '.' + element.getMethodName();
			for (String filter : filters) {
				if (fullyQualifiedMethodName.equals(filter)) {
					return true;
				}
			}
		}
		return false;
	}
}
