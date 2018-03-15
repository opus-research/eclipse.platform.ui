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
package org.eclipse.e4.ui.macros.internal;

import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.macros.EAcceptedCommands;

/**
 * An implementation which gathers the commands which should be accepted during
 * macro record/playback mode from the org.eclipse.e4.ui.macros.acceptedCommands
 * extension point and allows clients to programatically configure it later on.
 */
public class AcceptedCommandsImplementation implements EAcceptedCommands {

	/**
	 * @param eclipseContext
	 *            the Eclipse context.
	 *
	 */
	@Inject
	public AcceptedCommandsImplementation(IEclipseContext eclipseContext) {
		Assert.isNotNull(eclipseContext);
		this.fEclipseContext = eclipseContext;
	}

	/**
	 * A map which maps accepted command ids when recording a macro to whether
	 * they should be recorded as a command to be played back later on.
	 */
	private Map<String, Boolean> fMacroAcceptedCommandIds;

	private IEclipseContext fEclipseContext;

	/**
	 * @return a set with the commands that are accepted when macro recording.
	 */
	private Map<String, Boolean> getInternalAcceptedCommands() {
		if (fMacroAcceptedCommandIds == null) {
			fMacroAcceptedCommandIds = new HashMap<>();
			IExtensionRegistry registry = fEclipseContext.get(IExtensionRegistry.class);
			if (registry != null) {
				for (IConfigurationElement ce : registry
						.getConfigurationElementsFor("org.eclipse.e4.ui.macros.acceptedCommands")) { //$NON-NLS-1$
					if ("acceptedCommand".equals(ce.getName()) && ce.getAttribute("id") != null //$NON-NLS-1$ //$NON-NLS-2$
							&& ce.getAttribute("recordActivation") != null) { //$NON-NLS-1$
						Boolean recordActivation = Boolean.parseBoolean(ce.getAttribute("recordActivation")) //$NON-NLS-1$
								? Boolean.TRUE
								: Boolean.FALSE;
						fMacroAcceptedCommandIds.put(ce.getAttribute("id"), recordActivation); //$NON-NLS-1$
					}
				}
			}
		}
		return fMacroAcceptedCommandIds;
	}

	@Override
	public boolean isCommandAccepted(String commandId) {
		return getInternalAcceptedCommands().containsKey(commandId);
	}

	@Override
	public boolean isCommandRecorded(String commandId) {
		Map<String, Boolean> macroAcceptedCommands = getInternalAcceptedCommands();
		return macroAcceptedCommands.get(commandId);
	}

	@Override
	public void setCommandAccepted(String commandId, boolean acceptCommand, boolean recordActivation) {
		if (acceptCommand) {
			getInternalAcceptedCommands().put(commandId, recordActivation);
		} else {
			getInternalAcceptedCommands().remove(commandId);
		}
	}

	@Override
	public Map<String, Boolean> getCommandsAccepted() {
		Map<String, Boolean> internalAcceptedCommands = getInternalAcceptedCommands();
		return new HashMap<>(internalAcceptedCommands); // Create a copy
	}
}
