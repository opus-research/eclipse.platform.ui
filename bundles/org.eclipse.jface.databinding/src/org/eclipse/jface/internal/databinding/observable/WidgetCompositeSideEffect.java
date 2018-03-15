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

package org.eclipse.jface.internal.databinding.observable;

import java.util.function.Consumer;

import org.eclipse.core.databinding.observable.ICompositeSideEffect;
import org.eclipse.core.databinding.observable.ISideEffect;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Widget;

/**
 * Implementation of an ICompositeSideEffect, which uses a given Widget to
 * attach an {@link DisposeListener} so that all {@link ISideEffect} objects
 * will be disposed automatically on Widget disposal.
 *
 * @since 1.8
 *
 */
public final class WidgetCompositeSideEffect implements ICompositeSideEffect {

	private ICompositeSideEffect compositeSideEffect;

	/**
	 * Default constructor for an {@link WidgetCompositeSideEffect}.
	 *
	 * @param disposeableWidget
	 *            {@link Widget} where a dispose listener will be attached to
	 *            automatically dispose this {@link ICompositeSideEffect}.
	 *
	 */
	public WidgetCompositeSideEffect(Widget disposeableWidget) {
		this(Realm.getDefault(), disposeableWidget);
	}

	/**
	 * Constructor for an {@link WidgetCompositeSideEffect}.
	 *
	 * @param realm
	 *            {@link Realm} for the instance to be created.
	 * @param disposeableWidget
	 *            {@link Widget} where a dispose listener will be attached to
	 *            automatically dispose this {@link ICompositeSideEffect}.
	 */
	public WidgetCompositeSideEffect(Realm realm, Widget disposeableWidget) {
		compositeSideEffect = ICompositeSideEffect.create(realm);
		if (disposeableWidget != null && !disposeableWidget.isDisposed()) {
			disposeableWidget.addDisposeListener(e -> WidgetCompositeSideEffect.this.dispose());
		}
	}

	@Override
	public void dispose() {
		this.compositeSideEffect.dispose();
	}

	@Override
	public void pause() {
		compositeSideEffect.pause();
	}

	@Override
	public void resume() {
		compositeSideEffect.resume();
	}

	@Override
	public void resumeAndRunIfDirty() {
		compositeSideEffect.resumeAndRunIfDirty();
	}

	@Override
	public void runIfDirty() {
		compositeSideEffect.runIfDirty();
	}

	@Override
	public Consumer<ISideEffect> getSideEffectConsumer() {
		return compositeSideEffect.getSideEffectConsumer();
	}

}
