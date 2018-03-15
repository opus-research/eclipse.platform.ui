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

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.ISideEffect;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.sideeffect.ISideEffectFactory;
import org.eclipse.core.internal.databinding.observable.SideEffect;

/**
 * A factory to create {@link ISideEffect} objects, which are applied to the
 * given {@link Consumer} in {@link ISideEffectFactory#createFactory(Consumer)}.
 *
 * @since 3.3
 *
 */
public final class SideEffectFactory implements ISideEffectFactory {

	/**
	 * Returns a singleton factory instance that creates unmanaged side-effects.
	 */
	public static SideEffectFactory FACTORY = new SideEffectFactory(sideEffect -> {
	});

	private Consumer<ISideEffect> sideEffectConsumer;

	/**
	 * Constructor.
	 *
	 * @param sideEffectConsumer
	 *            {@link Consumer}, where the {@link ISideEffect} objects, which
	 *            are created by this factory are passed to.
	 *
	 */
	public SideEffectFactory(Consumer<ISideEffect> sideEffectConsumer) {
		this.sideEffectConsumer = sideEffectConsumer;
	}

	@Override
	public ISideEffect createPaused(Runnable runnable) {
		ISideEffect sideEffect = new SideEffect(runnable);
		sideEffectConsumer.accept(sideEffect);

		return sideEffect;
	}

	@Override
	public ISideEffect createPaused(Realm realm, Runnable runnable) {
		ISideEffect sideEffect = new SideEffect(realm, runnable);
		sideEffectConsumer.accept(sideEffect);

		return sideEffect;
	}

	@Override
	public ISideEffect create(Runnable runnable) {
		ISideEffect sideEffect;
		IObservable[] dependencies = ObservableTracker.runAndMonitor(runnable, null, null);

		if (dependencies.length == 0) {
			sideEffect = SideEffect.NULL_SIDE_EFFECT;
		} else {
			sideEffect = new SideEffect(runnable, dependencies);
		}

		sideEffectConsumer.accept(sideEffect);

		return sideEffect;
	}

	@Override
	public <T> ISideEffect create(Supplier<T> supplier, Consumer<T> consumer) {
		return create(SideEffect.makeRunnable(supplier, consumer));
	}

	@Override
	public <T> ISideEffect createResumed(Supplier<T> supplier, Consumer<T> consumer) {
		ISideEffect sideEffect = new SideEffect(SideEffect.makeRunnable(supplier, consumer));
		sideEffect.resume();
		sideEffectConsumer.accept(sideEffect);

		return sideEffect;
	}

	@Override
	public <T> ISideEffect consumeOnceAsync(Supplier<T> supplier, Consumer<T> consumer) {
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

		result[0] = ISideEffect.getFactory().createPaused(theRunnable);
		result[0].resume();

		sideEffectConsumer.accept(result[0]);

		return result[0];
	}
}
