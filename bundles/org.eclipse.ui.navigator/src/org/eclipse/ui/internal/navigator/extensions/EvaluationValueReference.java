/*******************************************************************************
 * Copyright (c) 2014 Google Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     C. Sean Young <csyoung@google.com> - Bug 436645
 ******************************************************************************/

package org.eclipse.ui.internal.navigator.extensions;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;

/**
 * A reference meant to be a value object for an EvaluationReference key type;
 * weakly holds an {@link EvaluationReference} (the reference itself, not the
 * underlying object) representing this value's key. Used to support
 * ReferenceQueue based cleanups in Maps.
 *
 * Does not override equals or hashCode; uses identity comparison inherited
 * from Object.
 *
 * @see EvaluationReference
 * @since 4.5
 *
 */
public class EvaluationValueReference extends SoftReference {
	private Reference /* <EvaluationReference> */ refToKey;

	/**
	 * @param referrent
	 *            The object to be referenced
	 * @param key
	 *            The key this value is associated with
	 */
	public EvaluationValueReference(Object referrent, EvaluationReference key) {
		super(referrent);
		this.refToKey = new WeakReference(key);
	}

	/**
	 *
	 * @param referrent
	 *            The object to be referenced
	 * @param key
	 *            The key this value is associated with
	 * @param queue
	 *            The ReferenceQueue to register this instance in
	 */
	public EvaluationValueReference(Object referrent, EvaluationReference key,
			ReferenceQueue queue) {
		super(referrent, queue);
		this.refToKey = new WeakReference(key);
	}

	/**
	 * @return the key that this value was associated with, or null if this
	 *         value has been cleared or the key has been collected.
	 */
	public EvaluationReference getKey() {
		return (EvaluationReference) refToKey.get();
	}

	/**
	 * Facilitates "handing off" a particular instance of a key.
	 *
	 * @param otherValue
	 *            the value ref to copy the key from.
	 */
	void swapKey(EvaluationValueReference otherValue) {
		Reference tmp = refToKey;
		this.refToKey = otherValue.refToKey;
		otherValue.refToKey = tmp;
	}

	/**
	 * Clears this reference and the underlying reference to the key.
	 *
	 * @see java.lang.ref.Reference#clear()
	 */
	public void clear() {
		super.clear();
		// This only clears our reference to the key, not the key itself.
		refToKey.clear();
	}

	private static String toStringArrayAware(Object o) {
		// Yea, this will miss primitive arrays, but nothing is using this for those yet.
		if (o instanceof Object[]) {
			// java.util.Arrays#toString(Object[]) does not exist in Java 1.4.
			// This is the next best thing.
			return java.util.Arrays.asList((Object[]) o).toString();
		}
		return String.valueOf(o);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	public java.lang.String toString() {
		Object myRef = get();
		return "EvaluationValueReference[" + (myRef == null ? "(collected)" : toStringArrayAware(myRef)) + ']'; //$NON-NLS-1$ //$NON-NLS-2$
	}
}