/*******************************************************************************
 * Copyright (C) 2014, Google Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *	   Marcus Eng (Google) - initial API and implementation
 *	   Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.ui.internal.monitoring;

import java.lang.management.ThreadInfo;
import java.util.Arrays;

import org.eclipse.ui.monitoring.StackSample;
import org.eclipse.ui.monitoring.UiFreezeEvent;

/**
 * Checks if the {@link UiFreezeEvent} matches any defined filters.
 */
public class FilterHandler {
	/**
	 * Groups the class name and method name defined in the filter.
	 */
	private class StackFrame implements Comparable<StackFrame> {
		final String className;
		final String methodName;

		public StackFrame(String className, String methodName) {
			this.className = className;
			this.methodName = methodName;
		}

		@Override
		public int compareTo(StackFrame other) {
			int c = methodName.compareTo(other.methodName);
			if (c != 0) {
				return c;
			}
			return className.compareTo(other.className);
		}
	}

	private final StackFrame[] filterMethods;

	/**
	 * Creates the filter.
	 *
	 * @param commaSeparatedMethods comma separated fully qualified method names to filter on
	 */
	public FilterHandler(String commaSeparatedMethods) {
		String[] methods = commaSeparatedMethods.split(","); //$NON-NLS-1$
		filterMethods = new StackFrame[methods.length];

		for (int i = 0; i < methods.length; i++) {
			String method = methods[i];
			int lastDot = method.lastIndexOf('.');
			filterMethods[i] = lastDot >= 0 ?
					new StackFrame(method.substring(0, lastDot), method.substring(lastDot + 1)) :
					new StackFrame("", method); //$NON-NLS-1$
		}

		Arrays.sort(filterMethods);
	}

	/**
	 * Returns {@code true} if the stack samples do not contain filtered stack frames in the stack
	 * traces of the display thread.
	 *
	 * @param stackSamples the array containing stack trace samples for a long event in the first
	 *     {@code numSamples} elements
	 * @param numSamples the number of valid stack trace samples in the {@code stackSamples} array
	 * @param displayThreadId the ID of the display thread
	 */
	public boolean shouldLogEvent(StackSample[] stackSamples, int numSamples,
			long displayThreadId) {
		if (filterMethods.length > 0) {
			for (int i = 0; i < numSamples; i++) {
				if (hasFilteredTraces(stackSamples[i].getStackTraces(), displayThreadId)) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Checks if the stack trace of the display thread contains any frame that matches the filter.
	 */
	private boolean hasFilteredTraces(ThreadInfo[] stackTraces, long displayThreadId) {
		for (ThreadInfo threadInfo : stackTraces) {
			if (threadInfo.getThreadId() == displayThreadId) {
				for (StackTraceElement element : threadInfo.getStackTrace()) {
					if (matchesFilter(element)) {
						return true;
					}
				}
				return false;
			}
		}

		MonitoringPlugin.logError(Messages.FilterHandler_missing_thread_error, null);
		return false;
	}

	/**
	 * Checks whether the given stack frame matches the filter.
	 */
	boolean matchesFilter(StackTraceElement stackFrame) {
		String methodName = stackFrame.getMethodName();
		String className = stackFrame.getClassName();
		// Binary search.
		int low = 0;
		int high = filterMethods.length;
		while (low < high) {
			int mid = (low + high) >>> 1;
			StackFrame filter = filterMethods[mid];
			int c = methodName.compareTo(filter.methodName);
			if (c == 0) {
				c = className.compareTo(filter.className);
			}
			if (c == 0) {
				return true;
			} else if (c < 0) {
				high = mid;
			} else {
				low = mid + 1;
			}
		}
		return false;
	}
}
