/*******************************************************************************
 * Copyright (c) 2007, 2009 Brad Reynolds and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Brad Reynolds - initial API and implementation
 *     Matthew Hall - bugs 208858, 213145, 246625, 194734
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.beans;

import static org.junit.Assert.assertSame;

import java.beans.PropertyDescriptor;

import org.eclipse.core.databinding.beans.BeansObservables;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.IObservableCollection;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.internal.databinding.beans.BeanObservableListDecorator;
import org.eclipse.jface.databinding.conformance.MutableObservableListContractTest;
import org.eclipse.jface.databinding.conformance.delegate.AbstractObservableCollectionContractDelegate;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.swt.widgets.Display;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.RunWith;
import org.junit.runners.AllTests;

import junit.framework.TestSuite;

/**
 * @since 3.3
 */
public class BeanObservableListDecoratorTest {
	private Bean bean;
	private PropertyDescriptor propertyDescriptor;
	private IObservableList observableList;
	private BeanObservableListDecorator decorator;

	@Before
	public void setUp() throws Exception {

		bean = new Bean();
		propertyDescriptor = new PropertyDescriptor(
				"list", Bean.class,"getList","setList");
		observableList = BeansObservables.observeList(
				DisplayRealm.getRealm(Display.getDefault()), bean, "list");
		decorator = new BeanObservableListDecorator(observableList, propertyDescriptor);
	}

	@Test
	public void testGetDelegate() throws Exception {
		assertSame(observableList, decorator.getDecorated());
	}

	@Test
	public void testGetObserved() throws Exception {
		assertSame(bean, decorator.getObserved());
	}

	@Test
	public void testGetPropertyDescriptor() throws Exception {
		assertSame(propertyDescriptor, decorator.getPropertyDescriptor());
	}

	@Test
	public void testSuite() throws Exception {
		JUnitCore.runClasses(Suite.class);
	}

	@RunWith(AllTests.class)
	public static class Suite {
		public static junit.framework.Test suite() {
			TestSuite suite = new TestSuite(BeanObservableListDecoratorTest.class.getName());
			suite.addTest(MutableObservableListContractTest.suite(new Delegate()));
			return suite;
		}
	}

	static class Delegate extends AbstractObservableCollectionContractDelegate {
		@Override
		public IObservableCollection createObservableCollection(Realm realm,
				int elementCount) {
			final WritableList delegate = new WritableList(realm);
			for (int i = 0; i < elementCount; i++)
				delegate.add(createElement(delegate));
			return new BeanObservableListDecorator(delegate, null);
		}

		private int counter;

		@Override
		public Object createElement(IObservableCollection collection) {
			return Integer.toString(counter++);
		}

		@Override
		public void change(IObservable observable) {
			IObservableList list = (IObservableList) observable;
			list.add(createElement(list));
		}
	}
}
