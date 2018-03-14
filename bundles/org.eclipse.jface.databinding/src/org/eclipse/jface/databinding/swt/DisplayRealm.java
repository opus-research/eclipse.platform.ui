/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt Carter - bug 170668
 *     Brad Reynolds - bug 170848
 *     Matthew Hall - bugs 180746, 207844, 245647, 248621, 232917, 194734,
 *                    195222, 256543, 213893, 262320, 264286, 266563, 306203
 *     Michael Krauter - bug 180223
 *     Boris Bokowski - bug 245647
 *     Tom Schindl - bug 246462
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 327086
 *     Jeanderson Candido <http://jeandersonbc.github.io> - Bug 413611
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 449022
 *******************************************************************************/
package org.eclipse.jface.databinding.swt;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.swt.widgets.Display;

/**
 * This class is used to get a {@link Realm} for a certain {@link Display}.
 *
 * @since 1.6
 *
 */
public class DisplayRealm extends Realm {

	private static List<DisplayRealm> realms = new ArrayList<DisplayRealm>();

	/**
	 * Returns the realm representing the UI thread for the given display.
	 *
	 * @param display
	 * @return the realm representing the UI thread for the given display
	 */
	public static Realm getRealm(final Display display) {
		synchronized (realms) {
			for (DisplayRealm element : realms) {
				if (element.display == display) {
					return element;
				}
			}
			DisplayRealm result = new DisplayRealm(display);
			realms.add(result);
			return result;
		}
	}

	private Display display;

	/**
	 * @param display
	 */
	private DisplayRealm(Display display) {
		this.display = display;
	}

	@Override
	public boolean isCurrent() {
		return Display.getCurrent() == display;
	}

	@Override
	public void asyncExec(final Runnable runnable) {
		Runnable safeRunnable = new Runnable() {
			@Override
			public void run() {
				safeRun(runnable);
			}
		};
		if (!display.isDisposed()) {
			display.asyncExec(safeRunnable);
		}
	}

	@Override
	public void timerExec(int milliseconds, final Runnable runnable) {
		if (!display.isDisposed()) {
			Runnable safeRunnable = new Runnable() {
				@Override
				public void run() {
					safeRun(runnable);
				}
			};
			display.timerExec(milliseconds, safeRunnable);
		}
	}

	@Override
	public int hashCode() {
		return (display == null) ? 0 : display.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final DisplayRealm other = (DisplayRealm) obj;
		if (display == null) {
			if (other.display != null)
				return false;
		} else if (!display.equals(other.display))
			return false;
		return true;
	}
}