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

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.core.databinding.observable.ISideEffect;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.internal.databinding.observable.sideeffect.SideEffectFactory;

/**
 * <p>
 * A factory to create {@link ISideEffect} objects, which are applied to the
 * given {@link Consumer} in {@link ISideEffectFactory#createFactory(Consumer)}.
 * </p>
 * Callers who provide the Consumer<ISideEffect> for the creation of an
 * {@link ISideEffectFactory}, are supposed to manage the lifecycle of the
 * aggregated {@link ISideEffect} instances, which are created by this factory.
 *
 * @see SideEffectFactory
 * @see ICompositeSideEffect
 *
 * @since 1.6
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ISideEffectFactory {

	/**
	 * <p>
	 * Creates a new {@link ISideEffectFactory} which will notify the given
	 * {@link Consumer} of every {@link ISideEffect} that is constructed by the
	 * factory.
	 * </p>
	 * <p>
	 * For example, a {@link Consumer} could be passed to this method which
	 * automatically inserts every {@link ISideEffect} into the same
	 * {@link ICompositeSideEffect}, allowing their lifecycle to be managed
	 * automatically by the object which provides the factory.
	 * </p>
	 * Callers who invoke this {@link #createFactory(Consumer)} method are
	 * supposed to manage the lifecycle of the aggregated {@link ISideEffect}
	 * instances, which are created by this factory. They do so by passing in a
	 * consumer which collects the side-effects constructed by the factory,
	 * allowing {@link ISideEffect#dispose()} to be invoked on them at a later
	 * time.
	 *
	 * @param sideEffectConsumer
	 *            a consumer which will be notified about every
	 *            {@link ISideEffect} constructed by this factory. The consumer
	 *            must guarantee that {@link ISideEffect#dispose()} will be
	 *            called on every {@link ISideEffect} it receives at some point
	 *            in the future.
	 * @return a newly constructed {@link ISideEffectFactory}
	 * @see ICompositeSideEffect
	 */
	static ISideEffectFactory createFactory(Consumer<ISideEffect> sideEffectConsumer) {
		return new SideEffectFactory(sideEffectConsumer);
	}

	/**
	 * <p>
	 * Creates a new {@link ISideEffectFactory} which will notify the given
	 * {@link Consumer} of every {@link ISideEffect} that is constructed by the
	 * factory.
	 * </p>
	 * <p>
	 * For example, a {@link Consumer} could be passed to this method which
	 * automatically inserts every {@link ISideEffect} into the same
	 * {@link ICompositeSideEffect}, allowing their lifecycle to be managed
	 * automatically by the object which provides the factory.
	 * </p>
	 * Callers who invoke this {@link #createFactory(Consumer)} method are
	 * supposed to manage the lifecycle of the aggregated {@link ISideEffect}
	 * instances, which are created by this factory. They do so by passing in a
	 * consumer which collects the side-effects constructed by the factory,
	 * allowing {@link ISideEffect#dispose()} to be invoked on them at a later
	 * time.
	 *
	 * @param sideEffectConsumer
	 *            a consumer which will be notified about every
	 *            {@link ISideEffect} constructed by this factory. The consumer
	 *            must guarantee that {@link ISideEffect#dispose()} will be
	 *            called on every {@link ISideEffect} it receives at some point
	 *            in the future.
	 * @param disposalConsumer
	 *            a consumer which will be notified once an {@link ISideEffect}
	 *            which has been created by this factory is disposed. The
	 *            disposed {@link ISideEffect} is passed to this Consumer.
	 * @return a newly constructed {@link ISideEffectFactory}
	 * @see ICompositeSideEffect
	 */
	static ISideEffectFactory createFactory(Consumer<ISideEffect> sideEffectConsumer,
			Consumer<ISideEffect> disposalConsumer) {
		return new SideEffectFactory(sideEffectConsumer, disposalConsumer);
	}

	/**
	 * Creates a new {@link ISideEffect} on the default {@link Realm} but does
	 * not run it immediately. The lifecycle of the returned {@link ISideEffect}
	 * will be managed by the factory. Callers are not responsible for disposing
	 * the resulting {@link ISideEffect}. So for example pausing, resuming and
	 * disposing should be done by the aggregating {@link Consumer} rather than
	 * on each individual {@link ISideEffect} itself.
	 *
	 * @param runnable
	 *            the runnable to execute. Must be idempotent.
	 * @return a newly-created {@link ISideEffect} which has not yet been
	 *         activated. Callers may call {@link ISideEffect#dispose()} on the
	 *         result if they wish to dispose it early, but they are not
	 *         required to do so since the lifecycle of the returned
	 *         {@link ISideEffect} is managed automatically.
	 */
	ISideEffect createPaused(Runnable runnable);

	/**
	 * Creates a new {@link ISideEffect} on the given Realm but does not
	 * activate it immediately. The lifecycle of the returned
	 * {@link ISideEffect} will be managed by the factory. Callers are not
	 * responsible for disposing the resulting {@link ISideEffect}. So for
	 * example pausing, resuming and disposing should be done by the aggregating
	 * {@link Consumer} rather than on each individual {@link ISideEffect}
	 * itself.
	 *
	 * @param realm
	 *            the realm to execute
	 * @param runnable
	 *            the runnable to execute. Must be idempotent.
	 * @return a newly-created {@link ISideEffect} which has not yet been
	 *         activated. Callers may call {@link ISideEffect#dispose()} on the
	 *         result if they wish to dispose it early, but they are not
	 *         required to do so since the lifecycle of the returned
	 *         {@link ISideEffect} is managed automatically.
	 */
	ISideEffect createPaused(Realm realm, Runnable runnable);

	/**
	 * Runs the given runnable once synchronously. The runnable will then run
	 * again after any tracked getter invoked by the runnable changes. It will
	 * continue doing so until the returned {@link ISideEffect} is disposed. The
	 * returned {@link ISideEffect} is associated with the default realm. The
	 * lifecycle of the returned {@link ISideEffect} will be managed by the
	 * factory. Callers are not responsible for disposing the resulting
	 * {@link ISideEffect}. So for example pausing, resuming and disposing
	 * should be done by the aggregating {@link Consumer} rather than on each
	 * individual {@link ISideEffect} itself.
	 *
	 * @param runnable
	 *            an idempotent runnable which will be executed once
	 *            synchronously then additional times after any tracked getter
	 *            it uses changes state
	 * @return an {@link ISideEffect} interface that may be used to stop the
	 *         side-effect from running. The {@link Runnable} will not be
	 *         executed anymore after the dispose method is invoked. Callers may
	 *         call {@link ISideEffect#dispose()} on the result if they wish to
	 *         dispose it early, but they are not required to do so since the
	 *         lifecycle of the returned {@link ISideEffect} is managed
	 *         automatically.
	 */
	ISideEffect create(Runnable runnable);

	/**
	 * Runs the supplier and passes its result to the consumer. The same thing
	 * will happen again after any tracked getter invoked by the supplier
	 * changes. It will continue to do so until the given {@link ISideEffect} is
	 * disposed. The returned {@link ISideEffect} is associated with the default
	 * realm. The lifecycle of the returned {@link ISideEffect} will be managed
	 * by the factory. Callers are not responsible for disposing the resulting
	 * {@link ISideEffect}. So for example pausing, resuming and disposing
	 * should be done by the aggregating {@link Consumer} rather than on each
	 * individual {@link ISideEffect} itself.
	 * <p>
	 * The ISideEffect will initially be in the resumed state.
	 * <p>
	 * The first invocation of this method will be synchronous. This version is
	 * slightly more efficient than {@link #createResumed(Supplier, Consumer)}
	 * and should be preferred if synchronous execution is acceptable.
	 *
	 * @param supplier
	 *            a supplier which will compute a value and be monitored for
	 *            changes in tracked getters. It should be side-effect-free.
	 * @param consumer
	 *            a consumer which will receive the value. It should be
	 *            idempotent. It will not be monitored for tracked getters.
	 *
	 * @return an {@link ISideEffect} interface that may be used to stop the
	 *         side-effect from running. The {@link Runnable} will not be
	 *         executed anymore after the dispose method is invoked. Callers may
	 *         call {@link ISideEffect#dispose()} on the result if they wish to
	 *         dispose it early, but they are not required to do so since the
	 *         lifecycle of the returned {@link ISideEffect} is managed
	 *         automatically.
	 */
	<T> ISideEffect create(Supplier<T> supplier, Consumer<T> consumer);

	/**
	 * Runs the supplier and passes its result to the consumer. The same thing
	 * will happen again after any tracked getter invoked by the supplier
	 * changes. It will continue to do so until the given {@link ISideEffect} is
	 * disposed. The returned {@link ISideEffect} is associated with the default
	 * realm. The lifecycle of the returned {@link ISideEffect} will be managed
	 * by the factory. Callers are not responsible for disposing the resulting
	 * {@link ISideEffect}. So for example pausing, resuming and disposing
	 * should be done by the aggregating {@link Consumer} rather than on each
	 * individual {@link ISideEffect} itself.
	 * <p>
	 * The ISideEffect will initially be in the resumed state.
	 * <p>
	 * The first invocation of this method will be asynchronous. This is useful,
	 * for example, when constructing an {@link ISideEffect} in a constructor
	 * since it ensures that the constructor will run to completion before the
	 * first invocation of the {@link ISideEffect}. However, this extra safety
	 * comes with a small performance cost over
	 * {@link #create(Supplier, Consumer)}.
	 *
	 * @param supplier
	 *            a supplier which will compute a value and be monitored for
	 *            changes in tracked getters. It should be side-effect-free.
	 * @param consumer
	 *            a consumer which will receive the value. It should be
	 *            idempotent. It will not be monitored for tracked getters.
	 *
	 * @return an {@link ISideEffect} interface that may be used to stop the
	 *         side-effect from running. The {@link Runnable} will not be
	 *         executed anymore after the dispose method is invoked. Callers may
	 *         call {@link ISideEffect#dispose()} on the result if they wish to
	 *         dispose it early, but they are not required to do so since the
	 *         lifecycle of the returned {@link ISideEffect} is managed
	 *         automatically.
	 */
	<T> ISideEffect createResumed(Supplier<T> supplier, Consumer<T> consumer);
}
