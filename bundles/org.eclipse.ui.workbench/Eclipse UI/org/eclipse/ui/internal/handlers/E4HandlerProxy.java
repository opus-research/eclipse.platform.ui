/*******************************************************************************
 * Copyright (c) 2010, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.handlers;

import java.util.Collections;
import java.util.Map;
import javax.inject.Named;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.HandlerEvent;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandler2;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.e4.core.commands.ExpressionContext;
import org.eclipse.e4.core.commands.internal.HandlerServiceHandler;
import org.eclipse.e4.core.commands.internal.HandlerServiceImpl;
import org.eclipse.e4.core.commands.internal.SetEnabled;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.internal.workbench.Activator;
import org.eclipse.e4.ui.internal.workbench.Policy;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.menus.UIElement;

/**
 * @since 3.5
 * 
 */
public class E4HandlerProxy implements IHandlerListener, IElementUpdater {
	public HandlerActivation activation = null;
	private Command command;
	private IHandler handler;

	public E4HandlerProxy(Command command, IHandler handler) {
		this.command = command;
		this.handler = handler;
		handler.addHandlerListener(this);
	}

	@CanExecute
	public boolean canExecute(IEclipseContext context, @Optional IEvaluationContext staticContext,
			MApplication application) {
		if (handler instanceof IHandler2) {
			Object ctx = staticContext;
			if (ctx == null) {
				ctx = new ExpressionContext(application.getContext());
			}
			((IHandler2) handler).setEnabled(ctx);
		}
		return handler.isEnabled();
	}

	@Execute
	public Object execute(IEclipseContext context,
			@Optional @Named(HandlerServiceImpl.PARM_MAP) Map parms, @Optional Event trigger,
			@Optional IEvaluationContext staticContext) throws ExecutionException,
			NotHandledException {
		Activator.trace(Policy.DEBUG_CMDS, "execute " + command + " and " //$NON-NLS-1$ //$NON-NLS-2$
				+ handler + " with: " + context, null); //$NON-NLS-1$
		IEvaluationContext appContext = staticContext;
		if (appContext == null) {
			appContext = new ExpressionContext(context);
		}
		ExecutionEvent event = new ExecutionEvent(command, parms == null ? Collections.EMPTY_MAP
				: parms, trigger, appContext);
		if (handler != null && handler.isHandled()) {
				final Object returnValue = handler.execute(event);
				return returnValue;
		}
		return null;
	}

	public IHandler getHandler() {
		return handler;
	}

	public void handlerChanged(HandlerEvent handlerEvent) {
		IHandler handler = command.getHandler();
		if (handler instanceof HandlerServiceHandler) {
			IEclipseContext appContext = ((Workbench) PlatformUI.getWorkbench()).getApplication()
					.getContext();
			if (HandlerServiceImpl.lookUpHandler(appContext, command.getId()) == this) {
				((HandlerServiceHandler) handler).fireHandlerChanged(handlerEvent);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.commands.IElementUpdater#updateElement(org.eclipse.ui.
	 * menus.UIElement, java.util.Map)
	 */
	public void updateElement(UIElement element, Map parameters) {
		if (handler instanceof IElementUpdater) {
			((IElementUpdater) handler).updateElement(element, parameters);
		}
	}

	@SetEnabled
	void setEnabled(@Optional IEvaluationContext evalContext) {
		if (evalContext == null) {
			IEclipseContext appContext = ((Workbench) PlatformUI.getWorkbench()).getApplication()
					.getContext();
			evalContext = new ExpressionContext(appContext);
		}
		if (handler instanceof IHandler2) {
			((IHandler2) handler).setEnabled(evalContext);
		}
	}
}
