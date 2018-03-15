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
 * Extension with the public API for dealing with macros.
 *
 * Can be accessed by getting it as a service:
 * <p>
 * &nbsp;&nbsp;&nbsp;site.getService(EMacroContext.class)
 * </p>
 * or by having it injected:
 * <p>
 * &nbsp;&nbsp;&nbsp;@Inject<br/>
 * &nbsp;&nbsp;&nbsp;EMacroContext fMacroContext;
 * </p>
 * <p>
 * The idea is that clients will become aware that a macro record is taking
 * place (through {@link #isRecording()} and will add their related macro
 * commands through {@link #addMacroCommand(IMacroCommand)}.
 * </p>
 * <p>
 * It's also important to note that any command added through addMacroCommand
 * also needs to have an {@link IMacroCommandFactory} registered through the
 * org.eclipse.e4.core.macros.macroCommandsFactory extension point (with a match
 * through {@link org.eclipse.e4.core.macros.IMacroCommand#getId()}).
 * </p>
 */
public interface EMacroContext {

	/**
	 * @return whether a macro is currently being recorded.
	 */
	boolean isRecording();

	/**
	 * @return whether a macro is currently being played back.
	 */
	boolean isPlayingBack();

	/**
	 * Adds a command to be added to the current macro being recorded. Such a
	 * command also needs to have an {@link IMacroCommandFactory} registered through
	 * the org.eclipse.e4.core.macros.macroCommandsFactory extension point (with a
	 * match through {@link org.eclipse.e4.core.macros.IMacroCommand#getId()})
	 *
	 * Does nothing if there's no macro being currently recorded.
	 *
	 * @param macroCommand
	 *            the command to be added to the macro currently being recorded.
	 */
	void addMacroCommand(IMacroCommand macroCommand);

	/**
	 * Toggles the macro record mode (i.e.: if it's currently not recording,
	 * starts recording a macro, otherwise, stops the current record -- at which
	 * point a macro should be saved with what was recorded so far).
	 *
	 * Note that when playing back, calling toggleMacroRecord() should do
	 * nothing (while it's Ok to start recording and then playback a previous
	 * macro to add previously recorded commands to the current macro, the other
	 * way around is not true).
	 */
	void toggleMacroRecord();

	/**
	 * Plays back the last recorded macro.
	 *
	 * Note: it's Ok to call it while a macro is being recorded (which should
	 * playback the given macro and add its contents to the new macro being
	 * recorded).
	 */
	void playbackLastMacro();

	/**
	 * Adds a macro listener to be notified on changes in the macro
	 * record/playback state.
	 *
	 * @param listener
	 *            the listener to be added.
	 */
	void addMacroContextListener(IMacroContextListener listener);

	/**
	 * @param listener
	 *            the macro listener which should no longer be notified of
	 *            changes.
	 */
	void removeMacroContextListener(IMacroContextListener listener);

}
