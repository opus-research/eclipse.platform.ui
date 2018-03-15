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
import java.util.function.Consumer;

import org.eclipse.core.databinding.observable.ISideEffect;
import org.eclipse.core.databinding.observable.sideeffect.ICompositeSideEffect;
import org.eclipse.core.runtime.ListenerList;

/**
 * Implementation of the ICompositeSideEffect.
 *
 * @since 1.6
 *
 */
public final class CompositeSideEffect implements ICompositeSideEffect {

	private List<ISideEffect> sideEffects;
	private int pauseCount;
	private boolean isDisposed;

	private ListenerList<Consumer<ISideEffect>> disposalConsumer;

	/**
	 * Default constructor of an CompositeSideEffect.
	 */
	public CompositeSideEffect() {
		sideEffects = new ArrayList<>();
	}

	@Override
	public void dispose() {
		if (isDisposed) {
			return;
		}
		sideEffects.forEach(s -> s.dispose());
		sideEffects.clear();
		isDisposed = true;
		if (disposalConsumer != null) {
			disposalConsumer.forEach(dc -> dc.accept(CompositeSideEffect.this));
			disposalConsumer.clear();
			disposalConsumer = null;
		}
	}

	@Override
	public boolean isDisposed() {
		return this.isDisposed;
	}

	/**
	 * Add an disposal consumer for this {@link ISideEffect} instance.
	 *
	 * @param disposalConsumer
	 *            a consumer which will be notified once this
	 *            {@link ISideEffect} is disposed.
	 */
	@Override
	public void addDisposeListener(Consumer<ISideEffect> disposalConsumer) {
		if (null == this.disposalConsumer) {
			this.disposalConsumer = new ListenerList<>();
		}
		this.disposalConsumer.add(disposalConsumer);
	}

	/**
	 * Remove an disposal consumer for this {@link ISideEffect} instance.
	 *
	 * @param disposalConsumer
	 *            a consumer which is supposed to be removed from the dispose
	 *            listener list.
	 */
	@Override
	public void removeDisposeListener(Consumer<ISideEffect> disposalConsumer) {
		this.disposalConsumer.remove(disposalConsumer);
	}

	@Override
	public void pause() {
		pauseCount++;
		if (pauseCount == 1) {
			sideEffects.forEach(s -> s.pause());
		}
	}

	@Override
	public void resume() {
		pauseCount--;
		if (pauseCount == 0) {
			sideEffects.forEach(s -> s.resume());
		}
	}

	@Override
	public void resumeAndRunIfDirty() {
		pauseCount--;
		if (pauseCount == 0) {
			sideEffects.forEach(s -> s.resumeAndRunIfDirty());
		}
	}

	@Override
	public void runIfDirty() {
		if (pauseCount <= 0) {
			sideEffects.forEach(s -> s.runIfDirty());
		}
	}

	@Override
	public void add(ISideEffect sideEffect) {
		if (!sideEffect.isDisposed()) {
			sideEffects.add(sideEffect);
			if (pauseCount > 0) {
				sideEffect.pause();
			}
		}
	}

	@Override
	public void remove(ISideEffect sideEffect) {
		sideEffects.remove(sideEffect);
		if (pauseCount > 0) {
			sideEffect.resume();
		}
	}
}
