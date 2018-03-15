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
package org.eclipse.e4.ui.macros.internal.keybindings;

import javax.inject.Inject;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.macros.EMacroContext;
import org.eclipse.e4.core.macros.IMacroContextListener;
import org.eclipse.e4.ui.bindings.keys.KeyBindingDispatcher;
import org.eclipse.e4.ui.macros.EAcceptedCommands;

/**
 * A macro listener that will install the KeyBindingDispatcherInterceptor when
 * in a record/playback context.
 */
public class KeyBindingDispatcherInterceptorInstaller implements IMacroContextListener {

	@Inject
	private KeyBindingDispatcher fDispatcher;

	@Inject
	private EAcceptedCommands fAcceptedCommands;

	@Inject
	private IEclipseContext fEclipseContext;

	/**
	 * The interceptor for keybinding commands (created only when actually in a
	 * record/playback context, null otherwise).
	 */
	private KeyBindingDispatcherInterceptor fInterceptor;

	@Override
	public void onMacroStateChanged(EMacroContext macroContext) {
		if (macroContext.isRecording() || macroContext.isPlayingBack()) {
			if (fInterceptor == null) {
				fInterceptor = new KeyBindingDispatcherInterceptor(macroContext, fAcceptedCommands, fEclipseContext);
				fDispatcher.addInterceptor(fInterceptor);
			}
		} else {
			if (fInterceptor != null) {
				fDispatcher.removeInterceptor(fInterceptor);
				fInterceptor = null;
			}
		}
	}
}
