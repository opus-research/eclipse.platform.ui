/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.help;

import java.util.Map;
import java.util.WeakHashMap;
import javax.inject.Inject;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.internal.commands.util.Util;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.internal.HandlerServiceImpl;
import org.eclipse.e4.core.commands.internal.ICommandHelpService;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.ui.internal.handlers.E4HandlerProxy;

/**
 * @since 3.5
 */
@SuppressWarnings("restriction")
public class CommandHelpServiceImpl implements ICommandHelpService {

	@Inject
	ECommandService commandService;

	private Map<IHandler, String> helpContextIdsByHandler = new WeakHashMap<IHandler, String>();

	@Override
	public String getHelpContextId(String commandId, IEclipseContext context)
			throws NotDefinedException {
		if (commandId == null || context == null) {
			return null;
		}

		Command command = commandService.getCommand(commandId);
		if (!command.isDefined()) {
			throw new NotDefinedException("The command is not defined. " //$NON-NLS-1$
					+ command.getId());
		}

		IHandler handler = (IHandler) HandlerServiceImpl.lookUpHandler(context, commandId);
		if (handler instanceof E4HandlerProxy) {
			handler = ((E4HandlerProxy) handler).getHandler();
		}
		String contextId = helpContextIdsByHandler.get(handler);
		if (contextId == null) {
			contextId = Util.getHelpContextId(command);
		}
		return contextId;
	}

	@Override
	public void setHelpContextId(IHandler handler, String contextId) {
		helpContextIdsByHandler.put(handler, contextId);
	}
}
