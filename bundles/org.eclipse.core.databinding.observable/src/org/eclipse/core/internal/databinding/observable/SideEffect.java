/*******************************************************************************
 * Copyright (c) 2015 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stefan Xenos (Google) - initial API and implementation
 ******************************************************************************/
package org.eclipse.core.internal.databinding.observable;

import java.util.function.Consumer;
import java.util.function.Supplier;

import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.IObservable;
import org.eclipse.core.databinding.observable.ISideEffect;
import org.eclipse.core.databinding.observable.ObservableTracker;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ListenerList;

/**
 * Concrete implementation of the {@link ISideEffect} interface.
 *
 * @since 1.6
 */
public final class SideEffect implements ISideEffect {
	/**
	 * Holds a singleton side-effect which does nothing.
	 */
	public static final ISideEffect NULL_SIDE_EFFECT = new ISideEffect() {
		@Override
		public void dispose() {
		}

		@Override
		public void pause() {
		}

		@Override
		public void resume() {
		}

		@Override
		public void resumeAndRunIfDirty() {
		}

		@Override
		public void runIfDirty() {
		}

		@Override
		public boolean isDisposed() {
			return true;
		}

		@Override
		public void addDisposeListener(Consumer<ISideEffect> disposalConsumer) {
		}

		@Override
		public void removeDisposeListener(Consumer<ISideEffect> disposalConsumer) {
		}
	};

	/**
	 * True if we've been dirtied since the last time we executed
	 * {@link #runnable}. A side-effect becomes dirtied if:
	 * <ul>
	 * <li>{@link #markDirty()} is called
	 * <li>one of its dependencies changes
	 * <li>it was newly created without executing the runnable
	 * </ul>
	 */
	private boolean dirty;
	/**
	 * True if PrivateInterface is currently enqueued in a call to
	 * realm.asyncExec
	 */
	private boolean asyncScheduled;
	private int pauseCount;
	private Runnable runnable;
	/**
	 * Dependencies which we are currently listening for change events from
	 */
	private IObservable[] dependencies;
	private Realm realm;

	private PrivateInterface privateInterface = new PrivateInterface();

	private ListenerList<Consumer<ISideEffect>> disposalConsumer;

	/**
	 * Creates a SideEffect in the paused state that wraps the given runnable on
	 * the default Realm.
	 *
	 * @param runnable
	 *            the runnable to execute.
	 */
	public SideEffect(Runnable runnable) {
		this(Realm.getDefault(), runnable);
	}

	/**
	 * Creates a SideEffect in the given realm that wraps the given runnable.
	 *
	 * @param realm
	 *            the realm to use for this SideEffect.
	 * @param runnable
	 *            the runnable to execute.
	 */
	public SideEffect(Realm realm, Runnable runnable) {
		this.runnable = runnable;
		this.realm = realm;
		this.dirty = true;
	}

	/**
	 * Creates a SideEffect with the given initial set of dependencies in the
	 * default realm that wraps the given runnable.
	 *
	 * @param runnable
	 *            the runnable to wrap
	 * @param dependencies
	 *            the initial set of dependencies
	 */
	public SideEffect(Runnable runnable, IObservable... dependencies) {
		this.dependencies = dependencies;
		this.runnable = runnable;
		this.dirty = false;
		this.pauseCount = 0;
		this.realm = Realm.getDefault();

		for (IObservable next : dependencies) {
			next.addChangeListener(privateInterface);
		}
	}

	@Override
	public void resume() {
		checkRealm();
		pauseCount--;
		if (dirty && pauseCount == 0) {
			scheduleUpdate();
		}
	}

	@Override
	public void pause() {
		checkRealm();
		pauseCount++;
		if (dirty && pauseCount == 1) {
			// No need to continue listening if we're already dirtied, since
			// we'll just end up running again after we're resumed
			stopListening();
			dependencies = null;
		}
	}

	@Override
	public void resumeAndRunIfDirty() {
		checkRealm();
		pauseCount--;
		update();
	}

	private void update() {
		if (dirty && pauseCount <= 0) {
			dirty = false;
			// Hold a reference to the old dependencies to prevent them from
			// being garbage collected until we've computed the new set. In the
			// event that a dependency is lazily created, this prevents it from
			// being destroyed and immediately recreated.

			// Stop listening for dependency changes.
			stopListening();

			// This line will do the following:
			// - Run the calculate method
			// - While doing so, add any observable that is touched to the
			// dependencies list
			IObservable[] newDependencies = ObservableTracker.runAndMonitor(runnable, null, null);

			// If the side-effect disposed itself, exit without attaching any
			// listeners.
			if (isDisposed()) {
				return;
			}

			for (IObservable next : newDependencies) {
				next.addChangeListener(privateInterface);
			}

			dependencies = newDependencies;
		}
	}

	@Override
	public void dispose() {
		checkRealm();
		if (isDisposed()) {
			return;
		}
		pauseCount = 0;
		stopListening();
		dependencies = null;
		runnable = null;
		if (disposalConsumer != null) {
			disposalConsumer.forEach(dc -> dc.accept(SideEffect.this));
			disposalConsumer.clear();
			disposalConsumer = null;
		}
	}

	@Override
	public boolean isDisposed() {
		return runnable == null;
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
	public void runIfDirty() {
		checkRealm();
		update();
	}

	private void stopListening() {
		if (dependencies != null) {
			for (IObservable observable : dependencies) {
				observable.removeChangeListener(privateInterface);
			}
		}
	}

	private void markDirtyInternal() {
		if (!dirty) {
			dirty = true;

			if (pauseCount <= 0) {
				scheduleUpdate();
			} else {
				stopListening();
				dependencies = null;
			}
		}
	}

	private void scheduleUpdate() {
		if (this.asyncScheduled) {
			return;
		}

		this.asyncScheduled = true;
		realm.asyncExec(privateInterface);
	}

	private void checkRealm() {
		Assert.isTrue(realm.isCurrent(), "This operation must be run within the observable's realm"); //$NON-NLS-1$
	}

	/**
	 * Creates a runnable which will execute the given supplier and pass the
	 * result to the given consumer while suppressing all tracked getters from
	 * the consumer.
	 *
	 * @param supplier
	 *            supplier to execute
	 * @param consumer
	 *            a consumer that will receive the value and in which tracked
	 *            getters will be suppressed.
	 * @return a newly constructed runnable
	 */
	public static <T> Runnable makeRunnable(Supplier<T> supplier, Consumer<T> consumer) {
		return () -> {
			T value = supplier.get();

			ObservableTracker.setIgnore(true);
			try {
				consumer.accept(value);
			} finally {
				ObservableTracker.setIgnore(false);
			}
		};
	}

	private class PrivateInterface implements IChangeListener, Runnable {
		@Override
		public void handleChange(ChangeEvent event) {
			markDirtyInternal();
		}

		@Override
		public void run() {
			asyncScheduled = false;
			update();
		}
	}
}
