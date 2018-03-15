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

package org.eclipse.core.internal.databinding.observable.sideeffect;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.observable.ISideEffect;
import org.eclipse.core.databinding.observable.sideeffect.ICompositeSideEffect;

/**
 * Implementation of the ICompositeSideEffect.
 *
 * @since 1.6
 *
 */
public final class CompositeSideEffect implements ICompositeSideEffect {

	private List<ISideEffect> sideEffects;

	/**
	 * Default constructor of an CompositeSideEffect.
	 */
	public CompositeSideEffect() {
		sideEffects = new ArrayList<>();
	}

	@Override
	public void dispose() {
		sideEffects.forEach(s -> s.dispose());
		sideEffects.clear();
	}

	@Override
	public void pause() {
		sideEffects.forEach(s -> s.pause());
	}

	@Override
	public void resume() {
		sideEffects.forEach(s -> s.resume());
	}

	@Override
	public void resumeAndRunIfDirty() {
		sideEffects.forEach(s -> s.resumeAndRunIfDirty());
	}

	@Override
	public void runIfDirty() {
		sideEffects.forEach(s -> s.runIfDirty());
	}

	@Override
	public void add(ISideEffect sideEffect) {
		sideEffects.add(sideEffect);
	}

	@Override
	public void remove(ISideEffect sideEffect) {
		sideEffects.remove(sideEffect);
	}
}
