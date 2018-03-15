/*******************************************************************************
 * Copyright (c) 2015 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stefan Xenos (Google) - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.databinding.observable;

import java.util.function.Consumer;

import org.eclipse.core.databinding.observable.sideeffect.ISideEffectFactory;
import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.internal.databinding.observable.sideeffect.SideEffectFactory;

/**
 * An {@link ISideEffect} allows you to run code whenever one or more
 * observables change. An {@link ISideEffect} is a lot like a listener except
 * that it doesn't need to be attached to anything. Instead, it reacts
 * automatically to changes in tracked getters that are invoked by the listener.
 * <p>
 * Observables form a directed graph of dependencies. Classes like
 * {@link WritableValue} form the inputs to the graph (nodes which have only
 * outputs), classes like {@link ComputedValue} form the interior nodes (they
 * receive inputs from observables and produce an output which is used by other
 * observables), and {@link ISideEffect} is used for the leaf nodes (nodes which
 * receive inputs but produce no output).
 * <p>
 * Side-effects have a life-cycle which passes through a number of states:
 * <ul>
 * <li>Paused: The side-effect will listen for changes but will not react to
 * them. If any change occurs while the side-effect is paused, it will react
 * when and if the side-effect is resumed. Some side-effects are paused
 * immediately on construction. This is useful, for example, for creating a
 * side-effect in an object's constructor which should not begin running until a
 * later time. When using a side-effect to update a control or a view, it is
 * common to pause the side-effect when the view is hidden and resume the
 * side-effect when the view becomes visible.</li>
 * <li>Resumed: The side-effect will listen for changes and react to them
 * asynchronously. Side-effects may be paused and resumed any number of times.
 * </li>
 * <li>Disposed: The side-effect will not listen to or react to changes. It will
 * also remove any strong references to its dependencies. Once a side-effect
 * enters the disposed state it remains in that state until it is garbage
 * collected.</li>
 * </ul>
 *
 * Example usage:
 *
 * <pre>
 * IObservableValue&lt;String&gt; firstName = ...
 * IObservableValue&lt;String&gt; lastName = ...
 * IObservableValue&lt;Boolean&gt; showFullNamePreference = ...
 * Label userName = ...
 *
 * ISideEffect sideEffect = ISideEffect.create(() -&gt; {
 *     String name = showFullNamePreference.get()
 *         ? (firstName.get() + " " + lastName.get())
 *         : firstName.get();
 *     userName.setText("Your name is " + name);
 * });
 * </pre>
 * <p>
 * The above example uses an {@link ISideEffect} to fill in a label with a
 * user's name. It will react automatically to changes in the username and the
 * showFullNamePreference.
 * <p>
 * The same thing could be accomplished by attaching listeners to all three
 * observables, but there are several advantages to using {@link ISideEffect}
 * over listeners.
 * <ul>
 * <li>The {@link ISideEffect} can self-optimize based on branches in the run
 * method. It will remove listeners from any {@link IObservable} which wasn't
 * used on the most recent run. In the above example, there is no need to listen
 * to the lastName field when showFullNamePreference is false.
 * <li>The {@link ISideEffect} will batch changes together and run
 * asynchronously. If firstName and lastName change at the same time, the
 * {@link ISideEffect} will only run once.
 * <li>Since the {@link ISideEffect} doesn't need to be explicitly attached to
 * the observables it affects, it is impossible for it to get out of sync with
 * the underlying data.
 * </ul>
 * <p>
 * Please be aware of a common anti-pattern. Don't create new observables inside
 * an {@link ISideEffect} unless you remember them for future runs. Creating new
 * observables inside an {@link ISideEffect} can easily create infinite loops.
 *
 * <pre>
 * // Bad: May create an infinite loop, since each AvatarObservable instance may
 * // fire an asynchronous event after creation
 * void createControls() {
 * 	ISideEffect sideEffect = ISideEffect.create(() -&gt; {
 * 		IObservableValue&lt;Image&gt; myAvatar = new AvatarObservable();
 *
 * 		myIcon.setImage(myAvatar.getValue());
 * 	});
 * }
 *
 * // Good: The AvatarObservable instance is remembered between invocations of
 * // the side-effect.
 * void createControls() {
 * 	final IObservableValue&lt;Image&gt; myAvatar = new AvatarObservable();
 * 	ISideEffect sideEffect = ISideEffect.create(() -&gt; {
 * 		myIcon.setImage(myAvatar.getValue());
 * 	});
 * }
 * </pre>
 *
 * @since 1.6
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ISideEffect {
	/**
	 * Disposes the side-effect, detaching all listeners and deallocating all
	 * memory used by the side-effect. The side-effect will not execute again
	 * after this method is invoked.
	 * <p>
	 * This method may be invoked more than once.
	 */
	void dispose();

	/**
	 * Returns true if this side-effect has been disposed. A disposed side-
	 * effect will never execute again or retain any strong references to the
	 * observables it uses. A side-effect which has not been disposed has some
	 * possibility of executing again in the future and of retaining strong
	 * references to observables.
	 *
	 * @return true if this side-effect has been disposed.
	 */
	boolean isDisposed();

	/**
	 * Pauses an {@link ISideEffect}, preventing it from running again until it
	 * is resumed. Note that the side-effect will continue listening to its
	 * dependencies while it is paused. If a dependency changes while the
	 * {@link ISideEffect} is paused, the {@link ISideEffect} will run again
	 * after it is resumed.
	 * <p>
	 * A side-effect may be paused and resumed any number of times. You should
	 * use pause instead of dispose if there is a chance you may want to resume
	 * the SideEffect later.
	 * <p>
	 *
	 */
	void pause();

	/**
	 * This increments the count of the number of times the {@link ISideEffect}
	 * has been resumed. If the side-effect has been resumed more times than it
	 * has been paused, it leaves the paused state and enters the resumed state.
	 * <p>
	 * When a {@link ISideEffect} is resumed, it starts reacting to changes in
	 * tracked getters invoked by its runnable. It will continue to react to
	 * changes until it is either paused or disposed. If the {@link ISideEffect}
	 * is dirty, it will be run at the earliest opportunity after this method
	 * returns.
	 */
	void resume();

	/**
	 * This increments the count of the number of times the {@link ISideEffect}
	 * has been resumed. If the side-effect has been resumed more times than it
	 * has been paused, it leaves the paused state and enters the resumed state.
	 * <p>
	 * When a {@link ISideEffect} is resumed, it starts reacting to changes in
	 * tracked getters invoked by its runnable. It will continue to react to
	 * changes until it is either paused or disposed. If the {@link ISideEffect}
	 * is dirty, it will be run synchronously.
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
	 * Add a listener that will be invoked when this {@link ISideEffect}
	 * instance is disposed. The listener will not be invoked if the receiver
	 * has already been disposed at the time the listener is attached.
	 *
	 * @param disposalConsumer
	 *            a consumer which will be notified once this
	 *            {@link ISideEffect} is disposed.
	 */
	void addDisposeListener(Consumer<ISideEffect> disposalConsumer);

	/**
	 * Remove a dispose listener from this {@link ISideEffect} instance. Has no
	 * effect if no such listener was previously attached.
	 *
	 * @param disposalConsumer
	 *            a consumer which is supposed to be removed from the dispose
	 *            listener list.
	 */
	void removeDisposeListener(Consumer<ISideEffect> disposalConsumer);

	/**
	 * Returns a factory for creating {@link ISideEffect} instances. The
	 * instance created by this factory will not be managed by the factory in
	 * any way after creation. It will be the caller's responsibility to dispose
	 * the {@link ISideEffect} instances when they are no longer needed.
	 *
	 * @return a factory that returns unmanaged {@link ISideEffect} instances.
	 */
	static ISideEffectFactory getFactory() {
		return SideEffectFactory.FACTORY;
	}
}
