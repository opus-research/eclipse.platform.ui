/*******************************************************************************
 * Copyright (c) 2015, 2017 Conrad Groth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Conrad Groth - Testing my fix, that validation status is set in the correct realm
 ******************************************************************************/
package org.eclipse.core.tests.internal.databinding;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateSetStrategy;
import org.eclipse.core.databinding.observable.IDisposeListener;
import org.eclipse.core.databinding.observable.list.ObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.set.ObservableSet;
import org.eclipse.core.databinding.observable.set.WritableSet;
import org.eclipse.core.databinding.util.ILogger;
import org.eclipse.core.databinding.util.Policy;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.tests.databinding.observable.ThreadRealm;
import org.junit.Test;

import junit.framework.TestCase;

public class DifferentRealmsBindingTest extends TestCase {

	ThreadRealm targetAndModelRealm = createRealm();
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

		validationRealm.init(Thread.currentThread());
		dbc = new DataBindingContext(validationRealm);
		Policy.setLog(logger);
	}

	@Override
	protected void tearDown() throws Exception {
		dbc.dispose();
	}

	public void testListBindingValidationRealm() throws Throwable {
		final ObservableList model = new WritableList(targetAndModelRealm);
		final ObservableList target = new WritableList(targetAndModelRealm);

		dbc.bindList(target, model);
		targetAndModelRealm.waitUntilBlocking();
		targetAndModelRealm.processQueue();
		targetAndModelRealm.unblock();
		assertTrue(errorStatusses.toString(), errorStatusses.isEmpty());
	}

	public void testSetBindingValidationRealm() throws Throwable {
		final ObservableSet model = new WritableSet(targetAndModelRealm);
		final ObservableSet target = new WritableSet(targetAndModelRealm);

		dbc.bindSet(target, model);
		targetAndModelRealm.waitUntilBlocking();
		targetAndModelRealm.processQueue();
		targetAndModelRealm.unblock();
		assertTrue(errorStatusses.toString(), errorStatusses.isEmpty());
	}

	@Test(timeout = 5000)
	public void testDisposingBothsObservablesConcurrently() throws InterruptedException {
		final CountDownLatch latch1 = new CountDownLatch(1);
		final CountDownLatch latch2 = new CountDownLatch(1);
		ThreadRealm targetRealm = createRealm();
		final IObservableSet<String> target = new WritableSet<String>(targetRealm) {
			@Override
			public synchronized void dispose() {
				super.dispose();

			}
			@Override
			public synchronized void removeDisposeListener(IDisposeListener listener) {
				latch1.countDown();
				try {
					latch2.await();
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new RuntimeException();
				}
			}
		};
		ThreadRealm modelRealm = createRealm();
		final IObservableSet<String> model = new WritableSet<>(modelRealm);

		ThreadRealm validationRealm = createRealm();
		DataBindingContext context = new DataBindingContext(validationRealm);

		context.getValidationRealm()
				.exec(() -> context.bindSet(target, model, new UpdateSetStrategy(UpdateSetStrategy.POLICY_NEVER),
				new UpdateSetStrategy(UpdateSetStrategy.POLICY_NEVER)));
		validationRealm.processQueue();

		targetRealm.exec(() -> target.dispose());
		targetRealm.processQueue();

		assertTrue(latch1.await(2, TimeUnit.SECONDS));

		modelRealm.exec(() -> model.dispose());
		modelRealm.processQueue();

		assertTrue(errorStatusses.toString(), errorStatusses.isEmpty());
	}

	private ThreadRealm createRealm() {
		final ThreadRealm realm = new ThreadRealm();
		new Thread(() -> {
			realm.init(Thread.currentThread());
			realm.block();
		}).start();
		return realm;
	}
}
