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
package org.eclipse.e4.ui.macros.internal.actions;

import javax.inject.Inject;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.macros.EMacroContextService;

/**
 * Toggles macro recording.
 */
public class ToggleMacroRecordActionImpl {

	@Inject
	private EMacroContextService fMacroContext;

	/**
	 * Toggles macro recording.
	 */
	@Execute
	public void execute() {
		fMacroContext.toggleMacroRecord();
	}

}
