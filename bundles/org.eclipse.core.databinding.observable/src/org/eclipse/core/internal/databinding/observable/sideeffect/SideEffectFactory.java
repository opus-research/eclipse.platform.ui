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

import org.eclipse.core.databinding.observable.ISideEffect;
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

	private Consumer<ISideEffect> sideEffectConsumer;
	private Consumer<ISideEffect> disposalConsumer;

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

	/**
	 * Constructor.
	 *
	 * @param sideEffectConsumer
	 *            {@link Consumer}, where the {@link ISideEffect} objects, which
	 *            are created by this factory are passed to.
	 * @param disposalConsumer
	 *            a consumer which will be notified once an {@link ISideEffect}
	 *            which has been created by this factory is disposed. The
	 *            disposed {@link ISideEffect} is passed to this Consumer.
	 *
	 */
	public SideEffectFactory(Consumer<ISideEffect> sideEffectConsumer, Consumer<ISideEffect> disposalConsumer) {
		this.sideEffectConsumer = sideEffectConsumer;
		this.disposalConsumer = disposalConsumer;
	}

	@Override
	public ISideEffect createPaused(Runnable runnable) {
		ISideEffect sideEffect = ISideEffect.createPaused(runnable);
		if (sideEffect instanceof SideEffect) {
			((SideEffect) sideEffect).setDisposalConsumer(disposalConsumer);
		}
		sideEffectConsumer.accept(sideEffect);

		return sideEffect;
	}

	@Override
	public ISideEffect createPaused(Realm realm, Runnable runnable) {
		ISideEffect sideEffect = ISideEffect.createPaused(realm, runnable);
		if (sideEffect instanceof SideEffect) {
			((SideEffect) sideEffect).setDisposalConsumer(disposalConsumer);
		}
		sideEffectConsumer.accept(sideEffect);

		return sideEffect;
	}

	@Override
	public ISideEffect create(Runnable runnable) {
		ISideEffect sideEffect = ISideEffect.create(runnable);
		if (sideEffect instanceof SideEffect) {
			((SideEffect) sideEffect).setDisposalConsumer(disposalConsumer);
		}
		sideEffectConsumer.accept(sideEffect);

		return sideEffect;
	}

	@Override
	public <T> ISideEffect create(Supplier<T> supplier, Consumer<T> consumer) {
		ISideEffect sideEffect = ISideEffect.create(supplier, consumer);
		if (sideEffect instanceof SideEffect) {
			((SideEffect) sideEffect).setDisposalConsumer(disposalConsumer);
		}
		sideEffectConsumer.accept(sideEffect);

		return sideEffect;
	}

	@Override
	public <T> ISideEffect createResumed(Supplier<T> supplier, Consumer<T> consumer) {
		ISideEffect sideEffect = ISideEffect.createResumed(supplier, consumer);
		if (sideEffect instanceof SideEffect) {
			((SideEffect) sideEffect).setDisposalConsumer(disposalConsumer);
		}
		sideEffectConsumer.accept(sideEffect);

		return sideEffect;
	}
}
