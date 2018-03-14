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

/**
 * Interface for side-effects. A side-effect is a runnable which executes in
 * response to an observable changing.
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
	 * {@link SideEffect} is dirty, it will be run at the earliest opportunity.
	 * <p>
	 * Has no effect if the {@link SideEffect} is already resumed.
	 */
	void resume();

	/**
	 * Resumes a paused {@link ISideEffect}, causing it to start reacting to
	 * changes in TrackedGetters invoked by its runnable. It will continue to
	 * react to changes until it is either paused or disposed. If the
	 * {@link SideEffect} is dirty, it will be run immediately.
	 * <p>
	 * Has no effect if the {@link SideEffect} is already resumed.
	 */
	void resumeAndRunIfDirty();

	/**
	 * Causes the side effect to run synchronously if and only if it is
	 * currently dirty (that is, if one of its dependencies has changed since
	 * the last time it ran). Does nothing if the {@link ISideEffect} is
	 * currently paused.
	 */
	void runIfDirty();
}
