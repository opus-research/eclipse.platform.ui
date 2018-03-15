/*******************************************************************************
 * Copyright (c) 2017 Fabio Zadrozny and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Fabio Zadrozny - initial API and implementation - http://eclip.se/8519
 *******************************************************************************/
package org.eclipse.e4.ui.macros;

import java.util.Map;

/**
 * A service which allows to register commands to be accepted during a macro
 * record/playback operation (by default should load the commands accepted
 * through the org.eclipse.e4.ui.macros.acceptedCommands extension point, but
 * it's possible to programmatically change it as needed later on).
 */
public interface EAcceptedCommands {

	/**
	 * @param commandId
	 *            the id of the command
	 * @return whether the command should be accepted when recording macros.
	 */
	boolean isCommandAccepted(String commandId);

	/**
	 * @param commandId
	 *            the id of the command.
	 * @return whether the command should be recorded for playback when
	 *         recording a macro.
	 */
	boolean isCommandRecorded(String commandId);

	/**
	 * @param commandId
	 *            the command id to be accepted or rejected during macro
	 *            record/playback.
	 * @param acceptCommand
	 *            true means the command will be accepted and false means it'll
	 *            be rejected during macro record/playback.
	 * @param recordActivation
	 *            if true, the command activation will be automatically recorded
	 *            in the macro playback mode (only used if acceptCommand ==
	 *            true).
	 */
	void setCommandAccepted(String commandId, boolean acceptCommand, boolean recordActivation);

	/**
	 * @return a map where the keys are the command ids registered and the
	 *         values whether the command should have its activation recorded.
	 *         Note that the user should be able to mutate it as wanted without
	 *         any side effects to the accepted commands.
	 */
	Map<String, Boolean> getCommandsAccepted();

}
