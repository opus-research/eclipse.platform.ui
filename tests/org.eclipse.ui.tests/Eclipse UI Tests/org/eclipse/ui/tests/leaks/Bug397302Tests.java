/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.leaks;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISourceProviderListener;
import org.junit.Assert;
import org.junit.Test;

/**
 * @since 3.5
 *
 */
public class Bug397302Tests {
	
	/**
	 * @since 3.5
	 *
	 */
	private static final class TestListener implements ISourceProviderListener {
		/**
		 */
		public TestListener() {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.ISourceProviderListener#sourceChanged(int, java.lang.String, java.lang.Object)
		 */
		public void sourceChanged(int sourcePriority, String sourceName,
				Object sourceValue) {
			// do nothing
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.ISourceProviderListener#sourceChanged(int, java.util.Map)
		 */
		public void sourceChanged(int sourcePriority, Map sourceValuesByName) {
			// do nothing
		}
	}

	private static final class TestSourceProvider extends AbstractSourceProvider {

		/* (non-Javadoc)
		 * @see org.eclipse.ui.ISourceProvider#dispose()
		 */
		public void dispose() {
			// do nothing
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.ISourceProvider#getCurrentState()
		 */
		public Map getCurrentState() {
			return Collections.EMPTY_MAP;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ui.ISourceProvider#getProvidedSourceNames()
		 */
		public String[] getProvidedSourceNames() {
			return new String[] {};
		}
		
	}

	/**
	 * Reproduce the problem, as described in the bug report.
	 */
	@Test
	public void testBugAsDescribed() {
		final TestSourceProvider testSourceProvider = new TestSourceProvider();
		final WeakReference<ISourceProviderListener> listenerARef = new WeakReference<ISourceProviderListener>(new TestListener());
		final WeakReference<ISourceProviderListener> listenerBRef = new WeakReference<ISourceProviderListener>(new TestListener());
		
		testSourceProvider.addSourceProviderListener(listenerARef.get());
		testSourceProvider.addSourceProviderListener(listenerBRef.get());
		
		testSourceProvider.removeSourceProviderListener(listenerARef.get());
		testSourceProvider.removeSourceProviderListener(listenerBRef.get());
		
		Assert.assertNotNull("Reference A", listenerARef.get());
		Assert.assertNotNull("Reference B", listenerBRef.get());
		
		// Test: The bug asserts that B has been leaked. Force a GC, and test whether
		// our weak references have gone null of not. If there is no leak, then both 
		// should be null.
		System.gc();
		
		Assert.assertNull("Reference A", listenerARef.get());
		Assert.assertNull("Reference B", listenerBRef.get());
		
		// Need this to prevent the above GC call from sweeping everything up. See this only when NOT
		// in debug?!?
		testSourceProvider.addSourceProviderListener(new TestListener());
		
	}

}
