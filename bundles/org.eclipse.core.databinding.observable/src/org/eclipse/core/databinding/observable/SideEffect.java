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
package org.eclipse.core.databinding.observable;

import org.eclipse.core.runtime.Assert;

/**
 * TODO: Rework this documentation and provide more examples
 *
 * A {@link ISideEffect} holds a Runnable which it executes once upon activation
 * and again every time any tracked getter the Runnable touches changes. A
 * {@link ISideEffect} can be used much like a listener, except that it is
 * associated with the code it affects rather than being attached to the
 * observables it listens to.
 * <p>
 * Example usage:
 *
 * <pre>
 * ObservableValue<String> firstName = ...
 * ObservableValue<String> lastName = ...
 * ObservableValue<Boolean> showFullNamePreference = ...
 * Label userName = ...
 *
 * ISideEffect sideEffect = ISideEffect.create(() -> {
 *     String name = showFullNamePreference.get()
 *         ? (firstName.get() + " " + lastName.get())
 *         : firstName.get();
 *     userName.setText("Your name is " + name);
 * });
 * </pre>
 * <p>
 * The above example demonstrates how to use a {@link ISideEffect} to fill in a
 * label with a user's first and last names. Every time the firstName, lastName,
 * or showFullNamePreference observables change state, the {@link ISideEffect}
 * will re-run and cause the label to update. The {@link ISideEffect} keeps
 * track of which observables to listen to based on what TrackedGetters it
 * invokes during its run method.
 * <p>
 * The same thing could be accomplished by attaching listeners to all three
 * observables, but there are several advantages to using {@link ISideEffect}
 * over listeners.
 * <ul>
 * <li>The {@link ISideEffect} can self-optimize based on branches in the run
 * method, and remove its listeners from any {@link IObservable} which wasn't
 * used on the most recent run. In the above example, there is no need to listen
 * to the lastName field when showFullNamePreference is false.
 * <li>The {@link ISideEffect} will batch changes together and run
 * asynchronously. So if firstName and lastName change at the same time, the
 * {@link ISideEffect} will only recompute itself once.
 * <li>Since the {@link ISideEffect} doesn't need to be explicitly attached to
 * the observables it affects, it is impossible for it to get out of sync with
 * the underlying data.
 * </ul>
 * <p>
 * Please be aware of a common anti-pattern. It is bad form to create
 * {@link IObservable}s inside a {@link ISideEffect} unless you also cache the
 * {@link IObservable} somewhere so that subsequent runs of the
 * {@link ISideEffect} will use the same instance of the observable. Otherwise
 * it is easy to create infinite event cycles where each run of the
 * {@link ISideEffect} creates an {@link IObservable} and each
 * {@link IObservable} fires a change events that re-runs the
 * {@link ISideEffect}
 *
 * @since 1.6
 */
final class SideEffect implements ISideEffect {
	static final ISideEffect NULL_SIDE_EFFECT = new ISideEffect() {
		@Override
		public void dispose() {
		}

		@Override
		public void pause() {
		}

		@Override
		public void resume() {
		}

		@Override
		public void resumeAndRunIfDirty() {
		}

		@Override
		public void runIfDirty() {
		}
	};
	/**
	 * True if we've been dirtied since the last time we executed
	 * {@link #runnable}. A side-effect becomes dirtied if:
	 * <ul>
	 * <li>{@link #markDirty()} is called
	 * <li>one of its dependencies changes
	 * <li>it was newly created without executing the runnable
	 * </ul>
	 */
	private boolean dirty;
	/**
	 * True iff PrivateInterface is currently enqueued in a call to
	 * realm.asyncExec
	 */
	private boolean asyncScheduled;
	private boolean resumed;
	private Runnable runnable;
	/**
	 * Dependencies which we are currently listening for change events from
	 */
	private IObservable[] dependencies;
	private Realm realm;

	private class PrivateInterface implements IChangeListener, Runnable {
		@Override
		public void handleChange(ChangeEvent event) {
			markDirtyInternal();
		}

		@Override
		public void run() {
			asyncScheduled = false;
			update();
		}
	}

	private PrivateInterface privateInterface = new PrivateInterface();

	SideEffect(Runnable runnable) {
		this(Realm.getDefault(), runnable);
	}

	SideEffect(Realm realm, Runnable runnable) {
		this.runnable = runnable;
		this.realm = realm;
		this.dirty = true;
	}

	SideEffect(Runnable runnable, IObservable... dependencies) {
		this.dependencies = dependencies;
		this.runnable = runnable;
		this.dirty = false;
		this.resumed = true;
		this.realm = Realm.getDefault();

		for (IObservable next : dependencies) {
			next.addChangeListener(privateInterface);
		}
	}

	@Override
	public void resume() {
		checkState();
		if (resumed) {
			return;
		}
		resumed = true;
		if (dirty) {
			scheduleUpdate();
		}
	}


	@Override
	public void pause() {
		checkState();
		resumed = false;
		if (dirty) {
			// No need to continue listening if we're already dirtied, since
			// we'll just end up running again after we're resumed
			stopListening();
			dependencies = null;
		}
	}

	@Override
	public void resumeAndRunIfDirty() {
		checkState();
		resumed = true;
		update();
	}

	private void update() {
		if (dirty && resumed) {
			dirty = false;
			// Hold a reference to the old dependencies to prevent them from
			// being garbage collected until we've computed the new set. In the
			// event that a dependency is lazily created, this prevents it from
			// being destroyed and immediately recreated.

			// Stop listening for dependency changes.
			stopListening();

			// This line will do the following:
			// - Run the calculate method
			// - While doing so, add any observable that is touched to the
			// dependencies list
			IObservable[] newDependencies = ObservableTracker.runAndMonitor(runnable, null, null);

			// If the side-effect disposed itself, exit without attaching any
			// listeners.
			if (isDisposed()) {
				return;
			}

			for (IObservable next : newDependencies) {
				next.addChangeListener(privateInterface);
			}

			dependencies = newDependencies;
		}
	}

	/**
	 * Disposes this SideEffect. Disposing a SideEffect will detach any
	 * listeners in use and will stop it from executing further.
	 */
	@Override
	public void dispose() {
		checkRealm();
		resumed = false;
		stopListening();
		dependencies = null;
		runnable = null;
	}

	private boolean isDisposed() {
		return runnable == null;
	}

	@Override
	public void runIfDirty() {
		checkState();
		update();
	}

	private void stopListening() {
		if (dependencies != null) {
			for (IObservable observable : dependencies) {
				observable.removeChangeListener(privateInterface);
			}
		}
	}

	private void markDirtyInternal() {
		if (!dirty) {
			dirty = true;

			if (resumed) {
				scheduleUpdate();
			} else {
				stopListening();
				dependencies = null;
			}
		}
	}

	private void scheduleUpdate() {
		if (this.asyncScheduled) {
			return;
		}

		this.asyncScheduled = true;
		realm.asyncExec(privateInterface);
	}

	private void checkState() {
		if (isDisposed()) {
			throw new IllegalStateException("This SideEffect has been disposed!"); //$NON-NLS-1$
		}
		checkRealm();
	}

	private void checkRealm() {
		Assert.isTrue(realm.isCurrent(), "This operation must be run within the observable's realm"); //$NON-NLS-1$
	}
}
