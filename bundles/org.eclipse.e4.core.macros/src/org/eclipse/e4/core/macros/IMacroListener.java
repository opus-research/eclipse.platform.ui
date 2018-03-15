/*******************************************************************************
 * Copyright (c) 2017 Fabio Zadrozny and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Fabio Zadrozny - initial API and implementation - https://bugs.eclipse.org/bugs/show_bug.cgi?id=8519
 *******************************************************************************/
package org.eclipse.e4.core.macros;

/**
 * Listener for changes in the EMacroContext.
 */
public interface IMacroListener {

	/**
	 * Called when a record started/stopped or a playback started/stopped. Note
	 * that a macro may be played back under a record session (although the
	 * opposite is not true).
	 *
	 * @param macroContext
	 *            the macro context where the change happened.
	 */
	public void onMacroStateChanged(EMacroContext macroContext);
}
