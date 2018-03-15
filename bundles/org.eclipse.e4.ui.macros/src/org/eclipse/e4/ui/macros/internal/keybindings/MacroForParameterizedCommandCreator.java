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

import java.util.Map;
import javax.inject.Inject;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.e4.core.macros.IMacroCommand;
import org.eclipse.e4.core.macros.IMacroCommandCreator;
import org.eclipse.e4.ui.bindings.keys.KeyBindingDispatcher;

/**
 * Creator for macros which were created from parameterized commands.
 */
public class MacroForParameterizedCommandCreator implements IMacroCommandCreator {

	@Inject
	private CommandManager fCommandManager;

	@Inject
	private KeyBindingDispatcher fDispatcher;

	@Override
	public IMacroCommand create(Map<String, String> stringMap) throws Exception {
		return MacroForParameterizedCommand.fromMap(stringMap, fCommandManager, fDispatcher);
	}

}