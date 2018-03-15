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
package org.eclipse.e4.core.macros.internal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.SimpleScriptContext;
import org.eclipse.core.runtime.Assert;
import org.eclipse.e4.core.macros.IMacroCommand;
import org.eclipse.e4.core.macros.IMacroCommandFactory;
import org.eclipse.e4.core.macros.IMacroPlaybackContext;

/**
 * Actually loads a macro from a JS file to be played back. Works with the
 * contents saved from {@link ComposableMacro#toJSBytes()}.
 * <p>
 * Currently the saved macro is a JavaScript file to be played back again with a
 * "runMacro" function which may have multiple "runCommand" calls to run a
 * command which was previously persisted with {@link IMacroCommand#toMap()}.
 * </p>
 */
public class SavedJSMacro implements IMacro {

	/**
	 * The file which contains the contents of the macro.
	 */
	private final File fFile;

	/**
	 * Provides the macro command id to an implementation which is able to
	 * recreate it.
	 */
	private Map<String, IMacroCommandFactory> fMacroCommandIdToFactory;

	/**
	 * Creates a macro which is backed up by the contents of a javascript file.
	 *
	 * @param file
	 *            the file with the contents of the macro.
	 */
	public SavedJSMacro(File file) {
		this.fFile = file;
	}

	@SuppressWarnings({ "rawtypes" })
	public static void runCommand(Map<String, IMacroCommandFactory> macroCommandIdToFactory,
			IMacroPlaybackContext macroPlaybackContext, String macroCommandId, Object commandParameters) throws Exception {
		Assert.isNotNull(macroCommandIdToFactory, "Before running, fMacroCommandIdToFactory must be set."); //$NON-NLS-1$
		Map<String, String> stringMap = new HashMap<>();
		Map m = (Map) commandParameters;
		Set<Map.Entry> entrySet = m.entrySet();
		for (Map.Entry entry : entrySet) {
			Object key = entry.getKey();
			Object value = entry.getValue();
			stringMap.put(key.toString(), value.toString());
		}
		IMacroCommandFactory macroFactory = macroCommandIdToFactory.get(macroCommandId);
		if (macroFactory == null) {
			throw new RuntimeException("Unable to find IMacroCommandFactory for command: " + macroCommandId); //$NON-NLS-1$
		}
		IMacroCommand command = macroFactory.create(stringMap);
		command.execute(macroPlaybackContext);
	}

	@Override
	public void setMacroCommandIdToFactory(Map<String, IMacroCommandFactory> macroCommandIdToFactory) {
		this.fMacroCommandIdToFactory = macroCommandIdToFactory;
	}

	@Override
	public void playback(IMacroPlaybackContext macroPlaybackContext) throws Exception {
		ScriptEngineManager manager = new ScriptEngineManager();
		ScriptEngine engine = manager.getEngineByName("nashorn"); //$NON-NLS-1$
		SimpleScriptContext context = new SimpleScriptContext();
		context.setBindings(engine.createBindings(), ScriptContext.ENGINE_SCOPE);
		Bindings engineScope = context.getBindings(ScriptContext.ENGINE_SCOPE);

		// Setup the default context.
		engineScope.put("__macroCommandIdToFactory", fMacroCommandIdToFactory); //$NON-NLS-1$
		engineScope.put("__macroPlaybackContext", macroPlaybackContext); //$NON-NLS-1$

		engine.eval("" + //$NON-NLS-1$
				"__macro = Java.type('org.eclipse.e4.core.macros.internal.SavedJSMacro');\n" //$NON-NLS-1$
				+ "function runCommand(macroMacroCommandId, commandParameters){" //$NON-NLS-1$
				+ "__macro.runCommand(__macroCommandIdToFactory, __macroPlaybackContext, macroMacroCommandId, commandParameters);" //$NON-NLS-1$
				+ "}" //$NON-NLS-1$
				+ "", context); //$NON-NLS-1$

		// The contents to execute are actually built at:
		// org.eclipse.e4.core.macros.internal.ComposableMacro.toJSBytes()
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fFile), "UTF-8"))) { //$NON-NLS-1$
			// Let any exception running it go through.
			// It should define a runMacro() command which we can run later on.
			engine.eval(reader, context);

			// Actually run the macro now.
			engine.eval("runMacro();", context); //$NON-NLS-1$
		}
	}
}
