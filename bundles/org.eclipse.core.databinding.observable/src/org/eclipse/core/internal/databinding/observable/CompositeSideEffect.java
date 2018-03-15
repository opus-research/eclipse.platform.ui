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

package org.eclipse.core.internal.databinding.observable;

import java.util.function.Consumer;

import org.eclipse.core.databinding.observable.ICompositeSideEffect;
import org.eclipse.core.databinding.observable.ISideEffect;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.WritableList;

/**
 * Implementation of the ICompositeSideEffect.
 *
 * @since 1.6
 *
 */
public final class CompositeSideEffect implements ICompositeSideEffect {

	private IObservableList<ISideEffect> sideEffects;

	/**
	 * Default constructor of an CompositeSideEffect.
	 */
	public CompositeSideEffect() {
		this(Realm.getDefault());
	}

	/**
	 * Constructor of an CompositeSideEffect.
	 *
	 * @param realm
	 *            {@link Realm} for the CompositeSideEffect instance.
	 *
	 */
	public CompositeSideEffect(Realm realm) {
		sideEffects = new WritableList<>(realm);
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
	public Consumer<ISideEffect> getSideEffectConsumer() {
		return sideEffects::add;
	}

}
