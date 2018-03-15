/*******************************************************************************
 * Copyright (c) 2015, 2017 Conrad Groth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Conrad Groth - Testing my fix, that validation status is set in the correct realm, Bug 502084
 ******************************************************************************/
package org.eclipse.core.tests.internal.databinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.list.ComputedList;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.set.ComputedSet;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.util.ILogger;
import org.eclipse.core.databinding.util.Policy;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.tests.databinding.observable.ThreadRealm;

import junit.framework.TestCase;

public class DifferentRealmsBindingTest extends TestCase {

	ThreadRealm targetAndModelRealm = new ThreadRealm();
	ThreadRealm validationRealm = new ThreadRealm();

	List<IStatus> errorStatusses = new ArrayList<>();

	DataBindingContext dbc;
	ILogger logger = new ILogger() {
		@Override
		public void log(IStatus status) {
			if (!status.isOK()) {
				errorStatusses.add(status);
			}
		}
	};

	@Override
	protected void setUp() throws Exception {
		errorStatusses.clear();
		new Thread() {
			@Override
			public void run() {
				targetAndModelRealm.init(Thread.currentThread());
				targetAndModelRealm.block();
			}
		}.start();

		validationRealm.init(Thread.currentThread());
		dbc = new DataBindingContext(validationRealm);
		Policy.setLog(logger);
	}

	@Override
	protected void tearDown() throws Exception {
		dbc.dispose();
	}

	public void testListBindingValidationRealm() throws Throwable {
		final IObservableList<String> model = new WritableList<>(targetAndModelRealm);
		final IObservableList<String> target = new WritableList<>(targetAndModelRealm);

		dbc.bindList(target, model);
		targetAndModelRealm.waitUntilBlocking();
		targetAndModelRealm.processQueue();
		targetAndModelRealm.unblock();
		assertTrue(errorStatusses.toString(), errorStatusses.isEmpty());
	}

	public void testSetBindingValidationRealm() throws Throwable {
		final IObservableSet<String> model = new WritableSet<>(targetAndModelRealm);
		final IObservableSet<String> target = new WritableSet<>(targetAndModelRealm);

		dbc.bindSet(target, model);
		targetAndModelRealm.waitUntilBlocking();
		targetAndModelRealm.processQueue();
		targetAndModelRealm.unblock();
		assertTrue(errorStatusses.toString(), errorStatusses.isEmpty());
	}

	public void testBindComputedListToWritableListInDifferentRealm() {
		// The validationRealm is the current realm.
		final IObservableValue<String> modelValue = new WritableValue<>(validationRealm);
		final IObservableList<String> model = new ComputedList<String>(validationRealm) {
			@Override
			protected List<String> calculate() {
				return Collections.singletonList(modelValue.getValue());
			}
		};
		final IObservableList<String> target = new WritableList<>(targetAndModelRealm);

		dbc.bindList(target, model);
		modelValue.setValue("Test");
		targetAndModelRealm.waitUntilBlocking();
		targetAndModelRealm.processQueue();
		targetAndModelRealm.unblock();
		assertTrue(errorStatusses.toString(), errorStatusses.isEmpty());
	}

	public void testBindComputedSetToWritableSetInDifferentRealm() {
		// The validationRealm is the current realm.
		final IObservableValue<String> modelValue = new WritableValue<>(validationRealm);
		final IObservableSet<String> model = new ComputedSet<String>(validationRealm) {
			@Override
			protected Set<String> calculate() {
				return Collections.singleton(modelValue.getValue());
			}
		};
		final IObservableSet<String> target = new WritableSet<>(targetAndModelRealm);

		dbc.bindSet(target, model);
		modelValue.setValue("Test");
		targetAndModelRealm.waitUntilBlocking();
		targetAndModelRealm.processQueue();
		targetAndModelRealm.unblock();
		assertTrue(errorStatusses.toString(), errorStatusses.isEmpty());
	}

	public void testBindComputedValueToWritableValueInDifferentRealm() {
		// The validationRealm is the current realm.
		final IObservableValue<String> modelValue = new WritableValue<>(validationRealm);
		final IObservableValue<String> model = new ComputedValue<String>(validationRealm) {
			@Override
			protected String calculate() {
				return modelValue.getValue();
			}
		};
		final IObservableValue<String> target = new WritableValue<>(targetAndModelRealm);

		dbc.bindValue(target, model);
		modelValue.setValue("Test");
		targetAndModelRealm.waitUntilBlocking();
		targetAndModelRealm.processQueue();
		targetAndModelRealm.unblock();
		assertTrue(errorStatusses.toString(), errorStatusses.isEmpty());
	}
}
