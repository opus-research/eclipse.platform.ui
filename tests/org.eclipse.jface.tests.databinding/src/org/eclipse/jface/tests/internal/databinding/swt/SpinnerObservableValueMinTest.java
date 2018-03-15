/*******************************************************************************
 * Copyright (c) 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - bug 213145, 194734, 195222
 *******************************************************************************/

package org.eclipse.jface.tests.internal.databinding.swt;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.conformance.ObservableDelegateTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableValueContractDelegate;
import org.eclipse.jface.databinding.conformance.swt.SWTMutableObservableValueContractTest;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

import junit.framework.TestSuite;

/**
 * @since 3.2
 */
public class SpinnerObservableValueMinTest extends ObservableDelegateTest {
	private Delegate delegate;

	private Spinner spinner;

	private IObservableValue observable;

	public SpinnerObservableValueMinTest() {
		this(null);
	}

	public SpinnerObservableValueMinTest(String testName) {
		super(testName, new Delegate());
	}

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();

		delegate = (Delegate) getObservableContractDelegate();
		observable = (IObservableValue) getObservable();
		spinner = delegate.spinner;
	}

	@Override
	protected IObservable doCreateObservable() {
		return getObservableContractDelegate().createObservable(
				DisplayRealm.getRealm(Display.getDefault()));
	}

	@Test
	public void testGetValue() throws Exception {
		int min = 100;
		spinner.setMinimum(min);
		assertEquals(Integer.valueOf(min), observable.getValue());
	}

	@Test
	public void testSetValue() throws Exception {
		int min = 100;
		observable.setValue(Integer.valueOf(min));
		assertEquals(min, spinner.getMinimum());
	}

	@Test
	public void testSuite() throws Exception {
		JUnitCore.runClasses(Suite.class);
	}

	@RunWith(AllTests.class)
	public static class Suite {
		public static junit.framework.Test suite() {
			TestSuite suite = new TestSuite(SpinnerObservableValueMinTest.class.toString());
			suite.addTest(SWTMutableObservableValueContractTest.suite(new Delegate()));
			return suite;
		}
	}

	/* package */static class Delegate extends
			AbstractObservableValueContractDelegate {
		private Shell shell;

		Spinner spinner;

		@Override
		public void setUp() {
			shell = new Shell();
			spinner = new Spinner(shell, SWT.NONE);
			spinner.setMaximum(1000);
		}

		@Override
		public void tearDown() {
			shell.dispose();
		}

		@Override
		public IObservableValue createObservableValue(Realm realm) {
			return WidgetProperties.minimum().observe(realm, spinner);
		}

		@Override
		public void change(IObservable observable) {
			IObservableValue observableValue = (IObservableValue) observable;
			observableValue.setValue(createValue(observableValue));
		}

		@Override
		public Object getValueType(IObservableValue observable) {
			return Integer.TYPE;
		}

		@Override
		public Object createValue(IObservableValue observable) {
			return createIntegerValue(observable);
		}

		private Integer createIntegerValue(IObservableValue observable) {
			return Integer.valueOf(((Integer) observable.getValue()).intValue() + 1);
		}
	}
}
