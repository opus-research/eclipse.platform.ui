/*******************************************************************************
 * Copyright (c) 2007, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Exit the workbench. Invocation calls {@link Display#close()}, which may
 * prompt the user (via a hook installed by
 * <code>org.eclipse.ui.internal.ide.application.IDEWorkbenchAdvisor</code>).
 *
 * @since 3.4
 *
 */
public class QuitHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbench workbench = HandlerUtil.getActiveWorkbenchWindow(event).getService(IWorkbench.class);
		workbench.getDisplay().close();
		return null;
	}
}
