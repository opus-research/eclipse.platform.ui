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

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.core.runtime.Assert;

/**
 * TODO: Rework this documentation and provide more examples
 *
 * A {@link SideEffect} holds a Runnable which it executes once upon activation
 * and again every time any tracked getter the Runnable touches changes. A
 * {@link SideEffect} can be used much like a listener, except that it is
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
 * IDisposable sideEffect = SideEffect.create(() -> {
 *     String name = showFullNamePreference.get()
 *         ? (firstName.get() + " " + lastName.get())
 *         : firstName.get();
 *     userName.setText("Your name is " + name);
 * });
 * </pre>
 * <p>
 * The above example demonstrates how to use a {@link SideEffect} to fill in a
 * label with a user's first and last names. Every time the firstName, lastName,
 * or showFullNamePreference observables change state, the {@link SideEffect}
 * will re-run and cause the label to update. The {@link SideEffect} keeps track
 * of which observables to listen to based on what TrackedGetters it invokes
 * during its run method.
 * <p>
 * The same thing could be accomplished by attaching listeners to all three
 * observables, but there are several advantages to using {@link SideEffect}
 * over listeners.
 * <ul>
 * <li>The {@link SideEffect} can self-optimize based on branches in the run
 * method, and remove its listeners from any {@link IObservable} which wasn't
 * used on the most recent run. In the above example, there is no need to listen
 * to the lastName field when showFullNamePreference is false.
 * <li>The {@link SideEffect} will batch changes together and run
 * asynchronously. So if firstName and lastName change at the same time, the
 * {@link SideEffect} will only recompute itself once.
 * <li>Since the {@link SideEffect} doesn't need to be explicitly attached to
 * the observables it affects, it is impossible for it to get out of sync with
 * the underlying data.
 * </ul>
 * <p>
 * Please be aware of a common anti-pattern. It is bad form to create
 * {@link IObservable}s inside a {@link SideEffect} unless you also cache the
 * {@link IObservable} somewhere so that subsequent runs of the
 * {@link SideEffect} will use the same instance of the observable. Otherwise it
 * is easy to create infinite event cycles where each run of the
 * {@link SideEffect} creates an {@link IObservable} and each
 * {@link IObservable} fires a change events that re-runs the {@link SideEffect}
 *
 * @since 1.6
 */
public final class SideEffect implements ISideEffect {
	private static final ISideEffect NULL_SIDE_EFFECT = new ISideEffect() {
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

	private SideEffect(Runnable runnable) {
		this(Realm.getDefault(), runnable);
	}

	private SideEffect(Realm realm, Runnable runnable) {
		this.runnable = runnable;
		this.realm = realm;
		this.dirty = true;
	}

	private SideEffect(Runnable runnable, IObservable... dependencies) {
		this.dependencies = dependencies;
		this.runnable = runnable;
		this.dirty = false;
		this.resumed = true;
		this.realm = Realm.getDefault();

		for (IObservable next : dependencies) {
			next.addChangeListener(privateInterface);
		}
	}

	/**
	 * Creates a new {@link SideEffect} on the default {@link Realm} but does
	 * not run it immediately. Callers are responsible for invoking
	 * {@link #resume()} or {@link #resumeAndRunIfDirty()} when they want the
	 * runnable to begin executing.
	 *
	 * @param runnable
	 *            the runnable to execute. Must be idempotent.
	 * @return a newly-created {@link SideEffect} which has not yet been
	 *         activated. Callers are responsible for calling {@link #dispose()}
	 *         on the result when it is no longer needed.
	 */
	public static SideEffect createPaused(Runnable runnable) {
		return new SideEffect(runnable);
	}

	/**
	 * Creates a new {@link SideEffect} on the given Realm but does not activate
	 * it immediately. Callers are responsible for invoking {@link #resume()} or
	 * {@link #markDirty()} when they want the runnable to begin executing.
	 *
	 * @param realm
	 *            the realm to execute
	 * @param runnable
	 *            the runnable to execute. Must be idempotent.
	 * @return a newly-created {@link SideEffect} which has not yet been
	 *         activated. Callers are responsible for calling {@link #dispose()}
	 *         on the result when it is no longer needed.
	 */
	public static SideEffect createPaused(Realm realm, Runnable runnable) {
		return new SideEffect(realm, runnable);
	}

	/**
	 * Runs the given runnable once synchronously. The runnable will then run
	 * again after any tracked getter invoked by the runnable changes. It will
	 * continue doing so until the returned {@link ISideEffect} is disposed. The
	 * returned {@link ISideEffect} is associated with the default realm. The
	 * caller must dispose the returned {@link ISideEffect} when they are done
	 * with it.
	 *
	 * @param runnable
	 *            an idempotent runnable which will be executed once
	 *            synchronously then additional times after any tracked getter
	 *            it uses changes state
	 * @return an {@link ISideEffect} interface that may be used to stop the
	 *         side-effect from running. The {@link Runnable} will not be
	 *         executed anymore after the dispose method is invoked.
	 */
	public static ISideEffect create(Runnable runnable) {
		IObservable[] dependencies = ObservableTracker.runAndMonitor(runnable, null, null);

		if (dependencies.length == 0) {
			return NULL_SIDE_EFFECT;
		}

		return new SideEffect(runnable, dependencies);
	}

	/**
	 * Runs the supplier and passes its result to the consumer. The same thing
	 * will happen again after any tracked getter invoked by the supplier
	 * changes. It will continue to do so until the given {@link ISideEffect} is
	 * disposed. The returned {@link ISideEffect} is associated with the default
	 * realm. The caller must dispose the returned {@link ISideEffect} when they
	 * are done with it.
	 *
	 * @param supplier
	 *            a supplier which will compute a value and be monitored for
	 *            changes in tracked getters. It should be side-effect-free.
	 * @param consumer
	 *            a consumer which will receive the value. It should be
	 *            idempotent. It will not be monitored for tracked getters.
	 *
	 * @return an {@link ISideEffect} interface that may be used to stop the
	 *         side-effect from running. The {@link Runnable} will not be
	 *         executed anymore after the dispose method is invoked.
	 */
	public static <T> ISideEffect create(Supplier<T> supplier, Consumer<T> consumer) {
		return create(() -> {
			T value = supplier.get();

			ObservableTracker.setIgnore(true);
			try {
				consumer.accept(value);
			} finally {
				ObservableTracker.setIgnore(false);
			}
		});
	}


	@Override
	public void resume() {
		checkRealm();
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
		checkRealm();
		resumed = false;
		if (dirty) {
			// No need to continue listening if we're already dirtied, since
			// we'll just end up running again after we're resumed
			stopListening();
			dependencies = null;
		}
	}

	/**
	 * Resumes a paused {@link SideEffect}, causing it to start reacting to
	 * changes in TrackedGetters invoked by its runnable. It will continue to
	 * react to changes until it is either paused or disposed. If the
	 * {@link SideEffect} is dirty, it will be run synchronously.
	 * <p>
	 * This is a convenience method which is fully equivalent to calling
	 * {@link #resume} followed by {@link #runIfDirty}, but slightly faster.
	 * <p>
	 * Has no effect if the {@link SideEffect} is already resumed.
	 */
	@Override
	public void resumeAndRunIfDirty() {
		checkRealm();
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
			if (!resumed) {
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
	}

	@Override
	public void runIfDirty() {
		checkRealm();
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

	/**
	 * Marks this {@link SideEffect} as dirty and schedules it to be re-run at
	 * its earliest convenience. A paused side effect will not run until it is
	 * resumed.
	 */
	public void markDirty() {
		checkRealm();
		markDirtyInternal();
	}

	private void checkRealm() {
		Assert.isTrue(realm.isCurrent(), "This operation must be run within the observable's realm"); //$NON-NLS-1$
	}
}
