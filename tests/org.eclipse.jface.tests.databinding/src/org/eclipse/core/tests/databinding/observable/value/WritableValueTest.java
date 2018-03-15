/*******************************************************************************
 * Copyright (c) 2006, 2008 Brad Reynolds
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - bug 158687
 *     Brad Reynolds - bug 164653
 *     Matthew Hall - bug 213145
 ******************************************************************************/

package org.eclipse.core.tests.databinding.observable.value;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.databinding.conformance.MutableObservableValueContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableValueContractDelegate;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;
import org.eclipse.swt.widgets.Display;
import org.junit.Test;

import junit.framework.TestSuite;

/**
 * @since 3.2
 */
public class WritableValueTest extends AbstractDefaultRealmTestCase {
	/**
	 * All constructors delegate to the 3 arg constructor.
	 *
	 * @throws Exception
	 */
	@Test
	public void testConstructor() throws Exception {
		WritableValue value = new WritableValue(DisplayRealm.getRealm(Display
				.getDefault()));
		assertNull(value.getValue());
		assertNull(value.getValueType());
	}

	@Test
	public void testWithValueType() throws Exception {
		Object elementType = String.class;
		WritableValue value = WritableValue.withValueType(elementType);
		assertNotNull(value);
		assertEquals(Realm.getDefault(), value.getRealm());
		assertEquals(elementType, value.getValueType());
	}

	public static junit.framework.Test suite() {
		TestSuite suite = new TestSuite(WritableValueTest.class.getName());
		suite.addTest(MutableObservableValueContractTest.suite(new Delegate()));
		return suite;
	}

	/* package */static class Delegate extends
			AbstractObservableValueContractDelegate {
		@Override
		public IObservableValue createObservableValue(Realm realm) {
			return new WritableValue(realm, "", String.class);
		}

		@Override
		public void change(IObservable observable) {
			IObservableValue observableValue = (IObservableValue) observable;
			observableValue.setValue(createValue(observableValue));
		}

		@Override
		public Object getValueType(IObservableValue observable) {
			return String.class;
		}

		@Override
		public Object createValue(IObservableValue observable) {
			return observable.getValue() + "a";
		}
	}
}
