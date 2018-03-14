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
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ISideEffect {
	/**
	 * Disposes the side-effect, detaching all listeners and deallocating all
	 * memory used by the side-effect. The side-effect will not execute again
	 * after this method is invoked, and no other public methods may be invoked
	 * on the side-effect after invoking this one.
	 */
	void dispose();

	/**
	 * Pauses a {@link ISideEffect}, preventing it from running again until it
	 * is resumed. Note that the side-effect will continue listening to its
	 * dependencies while it is paused. If a dependency changes while paused,
	 * the {@link ISideEffect} will run again after it is resumed.
	 * <p>
	 * A side-effect may be paused and resumed any number of times. You should
	 * use pause instead of dispose if there is a chance you may want to resume
	 * the SideEffect later.
	 */
	void pause();

	/**
	 * Resumes a paused {@link ISideEffect}, causing it to start reacting to
	 * changes in tracked getters invoked by its runnable. It will continue to
	 * react to changes until it is either paused or disposed. If the
	 * {@link ISideEffect} is dirty, it will be run at the earliest opportunity.
	 * <p>
	 * Has no effect if the {@link ISideEffect} is already resumed.
	 */
	void resume();

	/**
	 * Resumes a paused {@link ISideEffect}, causing it to start reacting to
	 * changes in TrackedGetters invoked by its runnable. It will continue to
	 * react to changes until it is either paused or disposed. If the
	 * {@link ISideEffect} is dirty, it will be run synchronously.
	 * <p>
	 * This is a convenience method which is fully equivalent to calling
	 * {@link #resume} followed by {@link #runIfDirty}, but slightly faster.
	 * <p>
	 */
	void resumeAndRunIfDirty();

	/**
	 * Causes the side effect to run synchronously if and only if it is
	 * currently dirty (that is, if one of its dependencies has changed since
	 * the last time it ran). Does nothing if the {@link ISideEffect} is
	 * currently paused.
	 */
	void runIfDirty();

	/**
	 * Creates a new {@link ISideEffect} on the default {@link Realm} but does
	 * not run it immediately. Callers are responsible for invoking
	 * {@link #resume()} or {@link #resumeAndRunIfDirty()} when they want the
	 * runnable to begin executing.
	 *
	 * @param runnable
	 *            the runnable to execute. Must be idempotent.
	 * @return a newly-created {@link ISideEffect} which has not yet been
	 *         activated. Callers are responsible for calling {@link #dispose()}
	 *         on the result when it is no longer needed.
	 */
	static ISideEffect createPaused(Runnable runnable) {
		return new SideEffect(runnable);
	}

	/**
	 * Creates a new {@link ISideEffect} on the given Realm but does not
	 * activate it immediately. Callers are responsible for invoking
	 * {@link #resume()} when they want the runnable to begin executing.
	 *
	 * @param realm
	 *            the realm to execute
	 * @param runnable
	 *            the runnable to execute. Must be idempotent.
	 * @return a newly-created {@link ISideEffect} which has not yet been
	 *         activated. Callers are responsible for calling {@link #dispose()}
	 *         on the result when it is no longer needed.
	 */
	static ISideEffect createPaused(Realm realm, Runnable runnable) {
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
	static ISideEffect create(Runnable runnable) {
		IObservable[] dependencies = ObservableTracker.runAndMonitor(runnable, null, null);

		if (dependencies.length == 0) {
			return SideEffect.NULL_SIDE_EFFECT;
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
	static <T> ISideEffect create(Supplier<T> supplier, Consumer<T> consumer) {
		return ISideEffect.create(() -> {
			T value = supplier.get();

			ObservableTracker.setIgnore(true);
			try {
				consumer.accept(value);
			} finally {
				ObservableTracker.setIgnore(false);
			}
		});
	}

	/**
	 * Runs the given supplier until it returns a non-null result. The first
	 * time it returns a non-null result, that result will be passed to the
	 * consumer and the ISideEffect will dispose itself. As long as the supplier
	 * returns null, any tracked getters it invokes will be monitored for
	 * changes. If they change, the supplier will be run again at some point in
	 * the future.
	 * <p>
	 * The resulting ISideEffect will be dirty and resumed, so the first
	 * invocation of the supplier will be asynchronous. If the caller needs it
	 * to be invoked synchronously, they can call {@link #runIfDirty()}
	 * <p>
	 * Unlike {@link #create(Supplier, Consumer)}, the consumer does not need to
	 * be idempotent.
	 * <p>
	 * This method is used for gathering asynchronous data before opening an
	 * editor, saving to disk, opening a dialog box, or doing some other
	 * operation which should only be performed once.
	 * <p>
	 * Consider the following example, which displays the content of a text file
	 * in a message box without doing any file I/O on the UI thread.
	 * <p>
	 *
	 * <code><pre>
	 * Observable<String> loadFileAsString(String filename) {
	 *   // Uses another thread to load the given filename. The resulting observable returns
	 *   // null if the file is not yet loaded or contains the file contents if the file is
	 *   // fully loaded
	 *   // ...
	 * }
	 *
	 * void showFileContents(Shell parentShell, String filename) {
	 *   ObservableValue<String> webPageContent = loadFileAsString(someUrl);
	 *   ISideEffect.runOnce(webPageContent::getValue(), (content) -> {
	 *   	MessageDialog.openInformation(parentShell, "Your file contains", content);
	 *   })
	 * }
	 * </pre></code>
	 *
	 * @param supplier
	 *            supplier which returns null if the side-effect should continue
	 *            to wait or returns a non-null value to be passed to the
	 *            consumer if it is time to invoke the consumer
	 * @param consumer
	 *            a (possibly non-idempotent) consumer which will receive the
	 *            first non-null result returned by the consumer.
	 * @return a side-effect which can be used to control this operation. If it
	 *         is disposed before the consumer is invoked, the consumer will
	 *         never be invoked. It will not invoke the supplier if it is
	 *         paused.
	 */
	static <T> ISideEffect runOnce(Supplier<T> supplier, Consumer<T> consumer) {
		final ISideEffect[] result = new ISideEffect[1];

		Runnable theRunnable = () -> {
			T value = supplier.get();

			if (value != null) {
				ObservableTracker.setIgnore(true);
				try {
					consumer.accept(value);
				} finally {
					ObservableTracker.setIgnore(false);
				}

				result[0].dispose();
			}
		};

		result[0] = ISideEffect.createPaused(theRunnable);
		result[0].resume();

		return result[0];
	}
}
