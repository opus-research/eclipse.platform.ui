/*******************************************************************************
 * Copyright (c) 2016 vogella GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz <simon.scholz@vogella.com> - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.databinding.observable;

import org.eclipse.core.internal.databinding.observable.CompositeSideEffect;

/**
 * All {@link ISideEffect} methods called on an instance of
 * {@link ICompositeSideEffect} will be invoked on all of the individual
 * {@link ISideEffect}s within the composite.
 * <p>
 * For example: invoking {@link #dispose()} on the composite will cause
 * {@link #dispose()} to be invoked on all of its constituent side-effects.
 * <p>
 * The main use of this class is to manage a group of side-effects that share
 * the same life-cycle. For example, all side-effects used to populate widgets
 * within a workbench part would likely be paused and resumed when the part is
 * made visible or invisible, and would all be disposed together when the part
 * is closed.
 *
 * @since 1.6
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICompositeSideEffect extends ISideEffect {

	/**
	 * Adds the given {@link ISideEffect} instance from the composite. This has
	 * no effect if the given side-effect is not part of the composite.
	 *
	 * @param sideEffect
	 *            {@link ISideEffect}
	 */
	void add(ISideEffect sideEffect);

	/**
	 * Removes the given {@link ISideEffect} instance to the composite.
	 *
	 * @param sideEffect
	 *            {@link ISideEffect}
	 */
	void remove(ISideEffect sideEffect);

	/**
	 * This factory method creates an {@link ICompositeSideEffect}.
	 *
	 * @return ICompositeSideEffect
	 */
	static ICompositeSideEffect create() {
		return new CompositeSideEffect();
	}

}
