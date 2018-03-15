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

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.macros.EMacroContext;
import org.eclipse.e4.ui.bindings.keys.IKeyBindingInterceptor;
import org.eclipse.e4.ui.macros.EAcceptedCommands;
import org.eclipse.swt.widgets.Event;

/**
 * An interceptor for the KeyBindingsDispatcher which will create macro commands
 * after a command is successfully executed and to stop actions which can't be
 * executed during a macro record/playback session.
 */
public class KeyBindingDispatcherInterceptor implements IKeyBindingInterceptor {


	private IEclipseContext fEclipseContext;

	private EAcceptedCommands fAcceptedCommands;

	/**
	 * The KeyBindingDispatcherInterceptor has to know about the macro context
	 * so that it can:
	 *
	 * <p>
	 * <ul>
	 * <li>Skip running commands which aren't accepted when recording or playing
	 * back a macro.</li>
	 * <li>Actually record commands which are run.</li>
	 * </ul>
	 * </p>
	 *
	 * <p>
	 * It's <strong>not</strong> responsible for recording keystrokes which
	 * don't map to commands. For this, the actual control or part has to do
	 * that. I.e.: the text editor is responsible for recording key events in
	 * the text editor (this is done so that each part takes full responsibility
	 * for what it's doing related to the macro and there's no need to make
	 * hacks related to it, as each part should know what it expects).
	 * </p>
	 */
	private EMacroContext fMacroContext;

	/**
	 * @param macroContext
	 *            the macro context to which this KeyBindingDispatcher is
	 *            related.
	 * @param acceptedCommands
	 *            service to be used to provide the accepted commands in macro
	 *            record/playback.
	 * @param eclipseContext
	 *            the eclipse context.
	 */
	public KeyBindingDispatcherInterceptor(EMacroContext macroContext, EAcceptedCommands acceptedCommands,
			IEclipseContext eclipseContext) {
		this.fMacroContext = macroContext;
		this.fAcceptedCommands = acceptedCommands;
		this.fEclipseContext = eclipseContext;
	}

	@Override
	public void postExecuteCommand(ParameterizedCommand parameterizedCommand, Event trigger, boolean commandDefined,
			boolean commandHandled) {
		boolean executedCommand = commandDefined && commandHandled;
		if (executedCommand) {
			// Properly record it in the macro.
			if (fMacroContext.isRecording()) {
				if (fAcceptedCommands.isCommandRecorded(parameterizedCommand.getId())) {
					MacroForParameterizedCommand macroCommand = new MacroForParameterizedCommand(parameterizedCommand,
							trigger);
					ContextInjectionFactory.inject(macroCommand, fEclipseContext);
					fMacroContext.addMacroCommand(macroCommand);
				}
			}
		}
	}

	@Override
	public boolean executeCommand(ParameterizedCommand cmd, Event event) {
		// We can only run the command if we're not recording/playing
		// back or if it's accepted.
		if (fMacroContext == null || (!fMacroContext.isRecording() && !fMacroContext.isPlayingBack())
				|| fAcceptedCommands.isCommandAccepted(cmd.getId())) {
			// Ok, allow it to go through
			return false;
		}
		// Intercept the command.
		return true;
	}
}
