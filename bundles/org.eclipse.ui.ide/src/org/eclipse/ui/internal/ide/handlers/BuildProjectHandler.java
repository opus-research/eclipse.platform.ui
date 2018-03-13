/*******************************************************************************
 * Copyright (c) 2012, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Freescale - Bug 411287 - Quick Access > Build Project is offered even if no valid selection exists
 ******************************************************************************/

package org.eclipse.ui.internal.ide.handlers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.BuildAction;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.FileEditorInput;

/**
 * Default Handler for 'Build Project' command
 * 
 * @since 4.3
 * 
 */
public class BuildProjectHandler extends AbstractHandler {

	private Map buildActions = Collections.synchronizedMap(new HashMap());
	
	/**
	 * @throws ExecutionException
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		if (window != null) {

			ISelection currentSelection = HandlerUtil
					.getCurrentSelection(event);

			if (currentSelection instanceof IStructuredSelection) {
				runBuildAction(window, currentSelection);
			} else {
				currentSelection = extractSelectionFromEditorInput(HandlerUtil
						.getActiveEditorInput(event));
				runBuildAction(window, currentSelection);
			}
		}
		return null;
	}

	private ISelection extractSelectionFromEditorInput(
			IEditorInput activeEditorInput) {
		if (activeEditorInput instanceof FileEditorInput) {
			IProject project = ((FileEditorInput) activeEditorInput).getFile()
					.getProject();
			return new StructuredSelection(project);
		}

		return null;
	}

	private void runBuildAction(IWorkbenchWindow window, 
			ISelection currentSelection) { 
		BuildAction buildAction = getBuildAction(window); 
		buildAction.selectionChanged((IStructuredSelection) currentSelection); 
		buildAction.run(); 
	} 
	
	private BuildAction getBuildAction(IWorkbenchWindow window) {
		
		// Try to get the buildAction from our cache
		BuildAction buildAction = (BuildAction) buildActions.get(window);
		if (buildAction != null) {
			return buildAction;
		}
		
		// Create a new buildAction for the window and cache it
		buildAction = new BuildAction(window, 
				IncrementalProjectBuilder.INCREMENTAL_BUILD);
		buildActions.put(window, buildAction);
		
		// Set up a listener to remove the cache entry when the window is closed
		window.addPageListener(new IPageListener() {
			public void pageOpened(IWorkbenchPage page) {
			}
			public void pageClosed(IWorkbenchPage page) {
				IWorkbenchWindow closedWindow = page.getWorkbenchWindow();
				buildActions.remove(closedWindow);
				closedWindow.removePageListener(this);
			}
			public void pageActivated(IWorkbenchPage page) {
			}
		});
		
		return buildAction;
	}

	/* (non-Javadoc) 
	 * @see org.eclipse.core.commands.AbstractHandler#setEnabled(java.lang.Object) 
	 */ 
	public void setEnabled(Object evaluationContext) { 
		boolean enabled = false; 
		if ((evaluationContext instanceof IEvaluationContext)) { 
			IEvaluationContext context = (IEvaluationContext) evaluationContext; 
			Object object = context.getVariable(ISources.ACTIVE_WORKBENCH_WINDOW_NAME); 
			if (object instanceof IWorkbenchWindow) { 
				BuildAction buildAction = getBuildAction((IWorkbenchWindow) object); 
				enabled = buildAction.isEnabled(); 
			} 
		} 
		setBaseEnabled(enabled); 
	} 		  

}