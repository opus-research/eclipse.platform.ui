/*******************************************************************************
 * Copyright (c) 2007, 2008 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Matthew Hall - bug 213145
 ******************************************************************************/

package org.eclipse.core.tests.databinding.observable.set;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.ObservableSet;
import org.eclipse.core.databinding.observable.set.SetDiff;
import org.eclipse.jface.databinding.conformance.ObservableCollectionContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableCollectionContractDelegate;

import junit.framework.TestSuite;

/**
 * @since 1.1
 */
public class ObservableSetTest {
	public static junit.framework.Test suite() {
		TestSuite suite = new TestSuite(ObservableSetTest.class.getName());
		suite.addTest(ObservableCollectionContractTest.suite(new Delegate()));
		return suite;
	}

	private static class Delegate extends AbstractObservableCollectionContractDelegate {
		private Delegate() {
		}

		@Override
		public void change(IObservable observable) {
			((ObservableSetStub) observable).fireSetChange(Diffs.createSetDiff(new HashSet(), new HashSet()));
		}

		@Override
		public Object createElement(IObservableCollection collection) {
			return Integer.toString(collection.size());
		}

		@Override
		public Object getElementType(IObservableCollection collection) {
			return String.class;
		}

		@Override
		public IObservableCollection createObservableCollection(Realm realm,
				int elementCount) {
			IObservableSet set = new ObservableSetStub(realm, new HashSet(), String.class);

			for (int i = 0; i < elementCount; i++) {
				set.add(Integer.toString(i));
			}

			return set;
		}
	}

	private static class ObservableSetStub extends ObservableSet {
		/**
		 * @param wrappedSet
		 * @param elementType
		 */
		protected ObservableSetStub(Realm realm, Set wrappedSet, Object elementType) {
			super(realm, wrappedSet, elementType);
		}

		@Override
		public void fireSetChange(SetDiff diff) {
			super.fireSetChange(diff);
		}
	}
}
