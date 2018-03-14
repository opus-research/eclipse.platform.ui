/*******************************************************************************
 * Copyright (c) 2015 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stefan Xenos - initial API and implementation
 ******************************************************************************/
package org.eclipse.core.tests.databinding;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.databinding.observable.ISideEffect;
import org.eclipse.core.databinding.observable.SideEffect;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.jface.tests.databinding.AbstractDefaultRealmTestCase;

/**
 * Test cases for the {@link SideEffect} class
 *
 * @since 3.2
 */
public class SideEffectTest extends AbstractDefaultRealmTestCase {
	// TODO: Add test cases for {@link SideEffect#create(Runnable)},
	// {@link SideEffect#create(java.util.function.Supplier,
	// java.util.function.Consumer)}
	// {@link SideEffect#pause()}, and {@link SideEffect#resume()}
	// - Validate that runIfDirty does nothing when paused

	private SideEffect sideEffect;
	private int sideEffectInvocations;

	private WritableValue<String> defaultDependency;
	private WritableValue<String> alternateDependency;
	private WritableValue<Boolean> useDefaultDependency;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		defaultDependency = new WritableValue<>("", null);
		alternateDependency = new WritableValue<>("", null);
		useDefaultDependency = new WritableValue<>(true, null);

		sideEffect = SideEffect.createPaused(() -> {
			if (useDefaultDependency.getValue()) {
				defaultDependency.getValue();
			} else {
				alternateDependency.getValue();
			}
			sideEffectInvocations++;
		});
	}

	public void testSideEffectDoesntRunUntilActivated() throws Exception {
		runAsync();
		assertEquals(0, sideEffectInvocations);
	}

	public void testSideEffectRunsWhenActivated() throws Exception {
		sideEffect.resume();
		runAsync();
		assertEquals(1, sideEffectInvocations);
	}

	public void testActivatingSideEffectMultipleTimesHasNoEffect() throws Exception {
		sideEffect.resume();
		sideEffect.resume();
		runAsync();
		assertEquals(1, sideEffectInvocations);
	}

	public void testSideEffectSelectsCorrectDependency() throws Exception {
		// Run the side-effect once
		sideEffect.resume();
		runAsync();
		assertEquals(1, sideEffectInvocations);

		// Confirm that the SideEffect is reacting to defaultDependency
		defaultDependency.setValue("foo");
		runAsync();
		assertEquals(2, sideEffectInvocations);

		// Confirm that the SideEffect is not reacting to alternateDependency
		alternateDependency.setValue("foo");
		runAsync();
		assertEquals(2, sideEffectInvocations);

		// Now change the branch that the side effect ran through and ensure
		// that it selected the correct new dependency (and removed the old one)
		useDefaultDependency.setValue(false);
		runAsync();
		assertEquals(3, sideEffectInvocations);

		// Confirm that the SideEffect is not reacting to defaultDependency
		defaultDependency.setValue("bar");
		runAsync();
		assertEquals(3, sideEffectInvocations);

		// Confirm that the SideEffect is reacting to alternateDependency
		alternateDependency.setValue("bar");
		runAsync();
		assertEquals(4, sideEffectInvocations);
	}

	public void testChangingMultipleDependenciesOnlyRunsTheSideEffectOnce() throws Exception {
		sideEffect.resume();
		runAsync();
		assertEquals(1, sideEffectInvocations);

		defaultDependency.setValue("Foo");
		alternateDependency.setValue("Foo");
		useDefaultDependency.setValue(false);

		runAsync();
		assertEquals(2, sideEffectInvocations);
	}

	public void testChangingDependencyRerunsSideEffect() throws Exception {
		// Run the side-effect once
		sideEffect.resume();
		runAsync();

		assertEquals(1, sideEffectInvocations);
		// Now change the dependency
		defaultDependency.setValue("Foo");
		runAsync();

		// Ensure that the side effect ran again as a result
		assertEquals(2, sideEffectInvocations);
	}

	public void testChangingUnrelatedNodeDoesntRunSideEffect() throws Exception {
		// Run the side-effect once
		sideEffect.resume();
		runAsync();

		assertEquals(1, sideEffectInvocations);
		// Now change the currently-unused dependency
		alternateDependency.setValue("Bar");
		runAsync();

		// Ensure that the side effect did not run again
		assertEquals(1, sideEffectInvocations);
	}

	public void testDeactivatedSideEffectWontRunWhenTriggeredByDependency() throws Exception {
		// Run the side-effect once
		sideEffect.resume();
		runAsync();

		assertEquals(1, sideEffectInvocations);
		// Now deactivate the side-effect and trigger one of its dependencies
		defaultDependency.setValue("Foo");
		sideEffect.dispose();
		runAsync();

		// Ensure that the side effect did not run again
		assertEquals(1, sideEffectInvocations);
	}

	public void testDeactivatedSideEffectWontRunWhenApplyInvoked() throws Exception {
		// Run the side-effect once
		sideEffect.resume();
		runAsync();

		assertEquals(1, sideEffectInvocations);
		sideEffect.dispose();
		sideEffect.runIfDirty();
		runAsync();

		// Ensure that the side effect did not run again
		assertEquals(1, sideEffectInvocations);
	}

	public void testApplyDoesNothingIfSideEffectNotDirty() throws Exception {
		// Run the side-effect once
		sideEffect.resume();
		runAsync();

		assertEquals(1, sideEffectInvocations);
		// Now deactivate the side-effect and trigger one of its dependencies
		sideEffect.runIfDirty();

		// Ensure that the side effect did not run again
		assertEquals(1, sideEffectInvocations);
	}

	public void testApplyRunsSynchronously() throws Exception {
		sideEffect.resume();
		sideEffect.markDirty();
		assertEquals(1, sideEffectInvocations);
		sideEffect.runIfDirty();
		assertEquals(2, sideEffectInvocations);
	}

	public void testApplyRunsIfDirty() throws Exception {
		sideEffect.resume();
		runAsync();
		assertEquals(1, sideEffectInvocations);
		defaultDependency.setValue("Foo");
		sideEffect.runIfDirty();
		assertEquals(2, sideEffectInvocations);
	}

	public void testNestedDependencyChangeAndApplyCompletes() throws Exception {
		AtomicBoolean hasRun = new AtomicBoolean();
		WritableValue<Object> invalidator = new WritableValue<Object>(new Object(), null);
		ISideEffect innerSideEffect = SideEffect.create(() -> {
			invalidator.getValue();
		});

		SideEffect.createPaused(() -> {
			// Make sure that there are no infinite loops.
			assertFalse(hasRun.get());
			hasRun.set(true);
			invalidator.setValue(new Object());
			innerSideEffect.runIfDirty();
		}).resume();

		runAsync();
		assertTrue(hasRun.get());
	}

	public void testNestedInvalidateAndApplyCompletes() throws Exception {
		AtomicBoolean hasRun = new AtomicBoolean();
		SideEffect innerSideEffect = SideEffect.createPaused(() -> {
		});

		innerSideEffect.resume();

		SideEffect.createPaused(() -> {
			// Make sure that there are no infinite loops.
			assertFalse(hasRun.get());
			hasRun.set(true);
			innerSideEffect.markDirty();
			innerSideEffect.runIfDirty();
		}).resume();

		runAsync();
		assertTrue(hasRun.get());
	}

	public void testNestedSideEffectCreation() throws Exception {
		AtomicBoolean hasRun = new AtomicBoolean();

		// Make sure that creating a SideEffect within another side effect works
		// propely.
		SideEffect.createPaused(() -> {
			SideEffect.createPaused(() -> {
				assertFalse(hasRun.get());
				hasRun.set(true);
			}).resume();
		}).resume();
		runAsync();
		assertTrue(hasRun.get());
	}

	public void testInvalidateSelf() throws Exception {
		AtomicInteger runCount = new AtomicInteger();
		// Make sure that if a side effect invalidates it self, it will run at
		// least once more but eventually stop.
		SideEffect[] sideEffect = new SideEffect[1];
		sideEffect[0] = SideEffect.createPaused(() -> {
			assertTrue(runCount.get() < 2);
			int count = runCount.incrementAndGet();
			if (count == 1) {
				sideEffect[0].markDirty();
			}
		});
		sideEffect[0].resume();
		runAsync();
		assertEquals(2, runCount.get());
	}

	// Doesn't currently work, but this would be a desirable property for
	// SideEffect to have
	// public void testInvalidateDependency() throws Exception {
	// AtomicInteger runCount = new AtomicInteger();
	// WritableValue<Object> invalidator = new WritableValue<Object>(new
	// Object(), null);
	// // Make sure that if a side effect invalidates itself, it will run at
	// // least once more but will eventually stop.
	// SideEffect[] sideEffect = new SideEffect[1];
	// sideEffect[0] = SideEffect.create(() -> {
	// invalidator.getValue();
	// assertTrue(runCount.get() < 2);
	// int count = runCount.incrementAndGet();
	// if (count == 1) {
	// invalidator.setValue(new Object());
	// }
	// });
	// sideEffect[0].activate();
	// runAsync();
	// assertEquals(2, runCount.get());
	// }
}
