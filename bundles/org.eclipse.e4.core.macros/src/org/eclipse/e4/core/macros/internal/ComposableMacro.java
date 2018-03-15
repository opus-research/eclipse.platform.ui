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
package org.eclipse.e4.core.macros.internal;

import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.eclipse.core.runtime.Assert;
import org.eclipse.e4.core.macros.Activator;
import org.eclipse.e4.core.macros.IMacroCommand;
import org.eclipse.e4.core.macros.IMacroCommandFactory;
import org.eclipse.e4.core.macros.IMacroPlaybackContext;

/**
 * This is a macro which is created from a sequence of commands which are stored
 * in-memory (and may be persisted later on).
 */
/* default */ class ComposableMacro implements IMacro {

	/**
	 * Provides the macro command id to an implementation which is able to
	 * recreate it.
	 */
	private Map<String, IMacroCommandFactory> fCommandIdToFactory;

	/**
	 * The macro commands which compose this macro.
	 */
	private List<IMacroCommand> fMacroCommands = new LinkedList<IMacroCommand>() {

		private static final long serialVersionUID = -7612234404927667052L;

		// Just making sure that we don't use random access on the linked list.
		@Override
		public IMacroCommand get(int index) {
			throw new RuntimeException(
					"get is slow and should not be used on LinkedList. If this is needed, switch to a different implementation."); //$NON-NLS-1$
		}
	};

	/**
	 * @param commandIdToFactory
	 *            Only macros which have ids available as keys in the
	 *            commandIdToFactory will be accepted.
	 */
	public ComposableMacro(Map<String, IMacroCommandFactory> commandIdToFactory) {
		fCommandIdToFactory = commandIdToFactory;
	}

	/**
	 * Adds a new macro command to this macro.
	 *
	 * @param macroCommand
	 *            the macro command to be appended to this macro.
	 */
	public void addMacroCommand(IMacroCommand macroCommand) {
		if (fCommandIdToFactory != null && !fCommandIdToFactory.containsKey(macroCommand.getId())) {
			throw new RuntimeException(
					String.format("Command: %s not properly registered through a %s extension point.", //$NON-NLS-1$
							macroCommand.getId(), MacroContextImplementation.MACRO_COMMAND_EXTENSION_POINT));
		}
		fMacroCommands.add(macroCommand);
	}

	@Override
	public void playback(IMacroPlaybackContext macroPlaybackContext) throws Exception {
		for (IMacroCommand macroCommand : fMacroCommands) {
			macroCommand.execute(macroPlaybackContext);
		}
	}

	@Override
	public void setCommandIdToFactory(Map<String, IMacroCommandFactory> commandIdToFactory) {
		this.fCommandIdToFactory = commandIdToFactory;
	}

	/**
	 * Actually returns the bytes to be written to the disk to be loaded back
	 * later on (the actual load and playback is later done by
	 * {@link SavedJSMacro}.
	 *
	 * @return an UTF-8 encoded array of bytes which can be used to rerun the
	 *         macro later on.
	 */
	/* default */ byte[] toJSBytes() {
		final StringBuilder buf = new StringBuilder(this.fMacroCommands.size() * 60);

		buf.append("// Macro generated by the Eclipse macro record engine.\n"); //$NON-NLS-1$
		buf.append("// The runMacro() function will be later run by the macro engine.\n"); //$NON-NLS-1$
		buf.append("function runMacro(){\n"); //$NON-NLS-1$

		for (IMacroCommand command : this.fMacroCommands) {
			Map<String, String> map = command.toMap();
			Assert.isNotNull(map);

			buf.append("    runCommand("); //$NON-NLS-1$
			buf.append(JSONHelper.quote(command.getId()));
			buf.append(", "); //$NON-NLS-1$
			buf.append(JSONHelper.toJSon(map));
			buf.append(");\n"); //$NON-NLS-1$
		}
		buf.append("}\n"); //$NON-NLS-1$

		try {
			return buf.toString().getBytes("UTF-8"); //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			// Make this a RuntimeException (UTF-8 should definitely be
			// supported).
			Activator.log(e);
			throw new RuntimeException(e);
		}
	}
}
