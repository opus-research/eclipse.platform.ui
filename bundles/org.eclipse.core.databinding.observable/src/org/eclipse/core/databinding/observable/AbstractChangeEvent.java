/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.core.databinding.observable;

/**
 * Generic change event denoting that the state of an {@link IObservable} object
 * has changed. This event does not carry information about the kind of change
 * that occurred.
 * 
 * @param <EV>
 * 
 * @since 1.5
 * 
 */
public abstract class AbstractChangeEvent<EV extends AbstractChangeEvent<EV>>
		extends ObservableEvent<EV> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3289969713357065239L;

	static final Object TYPE = new Object();

	/**
	 * Creates a new change event object.
	 * 
	 * @param source
	 *            the observable that changed state
	 */
	public AbstractChangeEvent(IObservable source) {
		super(source);
	}

	protected Object getListenerType() {
		return TYPE;
	}
}
