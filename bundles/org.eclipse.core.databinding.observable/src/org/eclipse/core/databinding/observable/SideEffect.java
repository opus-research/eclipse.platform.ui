package org.eclipse.core.databinding.observable;

import org.eclipse.core.runtime.Assert;

/**
 * A {@link SideEffect} holds a Runnable which it executes once upon activation
 * and again every time any TrackedGetter the Runnable touches changes. A
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
 * SideEffect sideEffect = SideEffect.create(() -> {
 *     String name = showFullNamePreference.get()
 *         ? (firstName.get() + " " + lastName.get())
 *         : firstName.get();
 *     userName.setText("Your name is " + name);
 * }).activate();
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
public final class SideEffect {
	private boolean dirty = true;
	private Runnable toRun;
	private IObservable[] dependencies;
	private boolean activated;
	private Realm realm;

	private class PrivateInterface implements IChangeListener, Runnable {
		@Override
		public void handleChange(ChangeEvent event) {
			makeDirty();
		}

		@Override
		public void run() {
			update();
		}
	}

	private PrivateInterface privateInterface = new PrivateInterface();

	private SideEffect(Runnable runnable) {
		this(Realm.getDefault(), runnable);
	}

	private SideEffect(Realm realm, Runnable runnable) {
		toRun = runnable;
		this.realm = realm;
	}

	/**
	 * Creates a new {@link SideEffect} on the default Realm but does not
	 * activate it immediately. Callers are responsible for invoking
	 * {@link #activate()} or {@link #makeDirty()} when they want the runnable
	 * to begin executing.
	 *
	 * @param runnable
	 *            the runnable to execute. Must be idempotent.
	 * @return a newly-created {@link SideEffect} which has not yet been
	 *         activated. Callers are responsible for calling {@link #dispose()}
	 *         on the result when it is no longer needed.
	 */
	public static SideEffect create(Runnable runnable) {
		return new SideEffect(runnable);
	}

	/**
	 * Creates a new {@link SideEffect} on the given Realm but does not activate
	 * it immediately. Callers are responsible for invoking {@link #activate()}
	 * or {@link #makeDirty()} when they want the runnable to begin executing.
	 *
	 * @param realm
	 *            the realm to execute
	 * @param runnable
	 *            the runnable to execute. Must be idempotent.
	 * @return a newly-created {@link SideEffect} which has not yet been
	 *         activated. Callers are responsible for calling {@link #dispose()}
	 *         on the result when it is no longer needed.
	 */
	public static SideEffect create(Realm realm, Runnable runnable) {
		return new SideEffect(realm, runnable);
	}

	/**
	 * Activates a SideEffect, causing it at its earliest convenience and begin
	 * reacting to change events every time one of the TrackedGetters it uses
	 * changes state. Activating a SideEffect which has already been activated
	 * has no effect.
	 *
	 * @return this
	 */
	public SideEffect activate() {
		checkRealm();
		if (activated) {
			return this;
		}
		activated = true;
		update();
		return this;
	}

	private void update() {
		if (dirty && activated) {
			dirty = false;
			// Hold a reference to the old dependencies to prevent them from
			// being garbage collected
			// until we've computed the new set. In the event that a dependency
			// is lazily created,
			// this prevents it from being destroyed and immediately recreated.

			// Stop listening for dependency changes.
			stopListening();

			// This line will do the following:
			// - Run the calculate method
			// - While doing so, add any observable that is touched to the
			// dependencies list
			IObservable[] newDependencies = ObservableTracker.runAndMonitor(toRun, null, null);

			// If the side-effect disposed itself, exit without attaching any
			// listeners.
			if (!activated) {
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
	public void dispose() {
		checkRealm();
		activated = false;
		stopListening();
		dependencies = null;
	}

	/**
	 * Causes the side effect to run synchronously if and only if it is
	 * currently dirty (that is, if on of its dependencies has changed or
	 * makeDirty has been invoked since the last time it ran).
	 *
	 * @return this
	 */
	public SideEffect apply() {
		checkRealm();
		update();
		return this;
	}

	private void stopListening() {
		if (dependencies != null) {
			for (int i = 0; i < dependencies.length; i++) {
				IObservable observable = dependencies[i];

				observable.removeChangeListener(privateInterface);
			}
		}
	}

	protected final void makeDirty() {
		if (!dirty) {
			dirty = true;

			realm.asyncExec(privateInterface);
		}
	}

	/**
	 * Schedules this {@link SideEffect} to be re-run at the earliest
	 * convenience.
	 *
	 * @return this
	 */
	public SideEffect invalidate() {
		checkRealm();
		makeDirty();
		return this;
	}

	private void checkRealm() {
		Assert.isTrue(realm.isCurrent(), "This operation must be run within the observable's realm"); //$NON-NLS-1$
	}
}
