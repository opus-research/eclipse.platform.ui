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

package org.eclipse.core.databinding.observable.sideeffect;

import org.eclipse.core.databinding.observable.ISideEffect;
import org.eclipse.core.internal.databinding.observable.sideeffect.CompositeSideEffect;

/**
 * Represents an {@link ISideEffect} that is composed of a bunch of component
 * {@link ISideEffect}s. It has the following properties:
 *
 * <ul>
 * <li>Disposing the composite will dispose all of the children.</li>
 * <li>If the composite is paused, all of the children will be paused as well.
 * </li>
 * </ul>
 *
 * Note that resuming a composite does not guarantee that all children will
 * resume. Children may also be paused externally, in which case the child must
 * be resumed both by the composite and by the external source(s) before it will
 * execute.
 * <p>
 * Children may belong to multiple composites. When this occurs, all of its
 * parent composites must be resumed in order for the child to execute and the
 * child will be disposed the first time any of its parents are disposed.
 * <p>
 * Children may be removed from a composite. When this occurs, the child may be
 * resumed immeditely if the composite was paused and disposing the composite
 * will no longer have any effect on the removed child.
 * <p>
 * Disposing a child will automatically remove it from its parent composite(s).
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
	 * Adds the given {@link ISideEffect} instance from the composite.
	 *
	 * @param sideEffect
	 *            {@link ISideEffect}
	 */
	void add(ISideEffect sideEffect);

	/**
	 * Removes the given {@link ISideEffect} instance from the composite. This
	 * has no effect if the given side-effect is not part of the composite.
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
