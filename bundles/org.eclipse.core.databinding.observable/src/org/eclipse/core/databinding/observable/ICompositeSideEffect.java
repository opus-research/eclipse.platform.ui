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

import java.util.function.Consumer;

import org.eclipse.core.internal.databinding.observable.CompositeSideEffect;

/**
 * An {@link ICompositeSideEffect} is used to aggregate several
 * {@link ISideEffect} instances together in one. So all {@link ISideEffect}
 * methods called on an instance of a {@link ICompositeSideEffect} will be
 * delegated to the aggregated {@link ISideEffect} objects.
 *
 * @since 1.6
 */
public interface ICompositeSideEffect extends ISideEffect, Consumer<ISideEffect> {

	/**
	 * This factory method creates an {@link ICompositeSideEffect}.
	 *
	 * @return ICompositeSideEffect
	 */
	static ICompositeSideEffect create() {
		return new CompositeSideEffect();
	}

	/**
	 * This factory method creates an {@link ICompositeSideEffect}.
	 *
	 * @param realm
	 *            {@link Realm}
	 *
	 * @return ICompositeSideEffect
	 */
	static ICompositeSideEffect create(Realm realm) {
		return new CompositeSideEffect(realm);
	}
}
