/*******************************************************************************
 * Copyright (c) 2015 Google Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     C. Sean Young <csyoung@google.com> - Bug 436645
 ******************************************************************************/

package org.eclipse.ui.tests.navigator;

import org.eclipse.ui.internal.navigator.VisibilityAssistant;
import org.eclipse.ui.internal.navigator.extensions.NavigatorContentDescriptor;
import org.eclipse.ui.internal.navigator.extensions.NavigatorContentDescriptorManager.EvaluationCache;
import org.eclipse.ui.navigator.INavigatorActivationService;
import org.eclipse.ui.navigator.INavigatorViewerDescriptor;
import org.eclipse.ui.tests.navigator.util.TestNavigatorActivationService;
import org.eclipse.ui.tests.navigator.util.TestNavigatorViewerDescriptor;
import org.junit.Assert;

// TODO There needs to be a test for the manager itself as well.
// TODO This class name is pretty dang long, can there be a shorter one?
public class NavigatorContentDescriptorManagerEvaluationCacheTest extends NavigatorTestBase {
	EvaluationCacheExposed cache;

	public NavigatorContentDescriptorManagerEvaluationCacheTest() {
		_navigatorInstanceId = TEST_VIEWER_PROGRAMMATIC;
	}

	// This is to expose protected methods here so we can call them directly.
	private static final class EvaluationCacheExposed extends EvaluationCache {

		EvaluationCacheExposed(VisibilityAssistant anAssistant) {
			super(anAssistant);
		}

		public NavigatorContentDescriptor[] getDescriptorsPublic(Object anElement, boolean toComputeOverrides) {
			return getDescriptors(anElement, toComputeOverrides);
		}

		public void setDescriptorsPublic(Object anElement, NavigatorContentDescriptor[] theDescriptors, boolean toComputeOverrides) {
			setDescriptors(anElement, theDescriptors, toComputeOverrides);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.tests.navigator.NavigatorTestBase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		INavigatorViewerDescriptor mockViewerDescript = new TestNavigatorViewerDescriptor();
		INavigatorActivationService mockActivationService = new TestNavigatorActivationService();
		VisibilityAssistant mockAssistant = new VisibilityAssistant(mockViewerDescript, mockActivationService);

		cache = new EvaluationCacheExposed(mockAssistant);
	}

	private void doSimpleAddGet(boolean toComputeOverrides) throws Exception {
		Object key = new Object();
		NavigatorContentDescriptor[] value = new NavigatorContentDescriptor[0];
		cache.setDescriptorsPublic(key, value, toComputeOverrides);
		Assert.assertSame(value, cache.getDescriptorsPublic(key, toComputeOverrides));
		// The other "half" of the cache should not have this.
		Assert.assertNull(cache.getDescriptorsPublic(key, !toComputeOverrides));
	}

	public void testSimpleAddGetNotOverrides() throws Exception {
		doSimpleAddGet(false);
	}

	public void testSimpleAddGetOverrides() throws Exception {
		doSimpleAddGet(true);
	}

	private void doNotSameInstEqual(boolean toComputeOverrides) throws Exception {
		java.util.List<String> key = new java.util.ArrayList<String>(2);
		key.add("Hi");
		key.add("There");
		NavigatorContentDescriptor[] value = new NavigatorContentDescriptor[0];
		// Should find it under the original key.
		cache.setDescriptorsPublic(key, value, toComputeOverrides);
		// Equal thing but different instance should still be equal.
		java.util.List key2 = new java.util.ArrayList<String>(key);
		// Should also find it under this new, equal key.
		Assert.assertSame(value, cache.getDescriptorsPublic(key2, toComputeOverrides));
		// The other "half" of the cache should not have this for either key.
		Assert.assertNull(cache.getDescriptorsPublic(key, !toComputeOverrides));
		Assert.assertNull(cache.getDescriptorsPublic(key2, !toComputeOverrides));
	}

	public void testNotSameInstEqualNotOverrides() throws Exception {
		doNotSameInstEqual(false);
	}

	public void testNotSameInstEqualOverrides() throws Exception {
		doNotSameInstEqual(true);
	}

	private void doTestReplace(boolean toComputeOverrides) throws Exception {
		Object key = new Object();
		NavigatorContentDescriptor[] value1 = new NavigatorContentDescriptor[0];
		cache.setDescriptorsPublic(key, value1, toComputeOverrides);
		Assert.assertSame(value1, cache.getDescriptorsPublic(key, toComputeOverrides));
		NavigatorContentDescriptor[] value2 = new NavigatorContentDescriptor[0];
		cache.setDescriptorsPublic(key, value2, toComputeOverrides);
		Assert.assertSame(value2, cache.getDescriptorsPublic(key, toComputeOverrides));
	}

	public void testReplaceNotOverrides() throws Exception {
		doTestReplace(false);
	}

	public void testReplaceOverrides() throws Exception {
		doTestReplace(true);
	}

	public void testOnVisibilityOrActivationChangeClearsCaches() throws Exception {
		Object key = new Object();
		NavigatorContentDescriptor[] value1 = new NavigatorContentDescriptor[0];
		cache.setDescriptorsPublic(key, value1, false);
		// Make sure they actually got inserted.
		Assert.assertSame(value1, cache.getDescriptorsPublic(key, false));
		NavigatorContentDescriptor[] value2 = new NavigatorContentDescriptor[0];
		cache.setDescriptorsPublic(key, value2, true);
		Assert.assertSame(value2, cache.getDescriptorsPublic(key, true));
		cache.onVisibilityOrActivationChange();
		// Now trying to find them should give null (non present).
		Assert.assertNull(cache.getDescriptorsPublic(key, false));
		Assert.assertNull(cache.getDescriptorsPublic(key, true));
	}

	// TODO Some way to reliably test the clearing of entries. Possibly using
	// java.lang.ref.Reference#enqueue().
}
