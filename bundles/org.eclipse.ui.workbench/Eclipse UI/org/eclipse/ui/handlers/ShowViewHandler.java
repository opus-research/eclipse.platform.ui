/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 430988
 *******************************************************************************/
package org.eclipse.ui.handlers;

import java.util.Map;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.descriptor.basic.MPartDescriptor;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.dialogs.ShowViewDialog;

/**
 * Shows the given view. If no view is specified in the parameters, then this
 * opens the view selection dialog.
 *
 * @since 3.1
 */
public final class ShowViewHandler extends AbstractHandler {

	@Override
	public final Object execute(final ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow workbenchWindow = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		Shell shell = HandlerUtil.getActiveShell(event);
		// Get the view identifier, if any.
		IEclipseContext ctx = (IEclipseContext) workbenchWindow.getService(IEclipseContext.class);
		EModelService modelService = (EModelService) workbenchWindow.getService(EModelService.class);
		EPartService partService = (EPartService) workbenchWindow.getService(EPartService.class);
		MApplication app = (MApplication) workbenchWindow.getService(MApplication.class);
		MWindow window = (MWindow) workbenchWindow.getService(MWindow.class);

		final Map parameters = event.getParameters();
		final Object value = parameters.get(IWorkbenchCommandConstants.VIEWS_SHOW_VIEW_PARM_ID);

		if (value == null) {
			openOther(shell, app, window, modelService, ctx, partService);
		} else {
			try {
				openView((String) value, partService);
			} catch (PartInitException e) {
				throw new ExecutionException("Part could not be initialized", e); //$NON-NLS-1$
			}
		}

		return null;
	}

	/**
	 * Opens a view selection dialog, allowing the user to chose a view.
	 */
	private final void openOther(final Shell shell, MApplication app, MWindow window,
			EModelService modelService,
			IEclipseContext context,
			EPartService partService) {

		final ShowViewDialog dialog = new ShowViewDialog(shell, app, window, modelService, context);
		dialog.open();

		if (dialog.getReturnCode() == Window.CANCEL) {
			return;
		}

		final MPartDescriptor[] descriptors = dialog.getSelection();
		for (MPartDescriptor descriptor : descriptors) {
			partService.showPart(descriptor.getElementId(), PartState.ACTIVATE);
		}
	}

	/**
	 * Opens the view with the given identifier.
	 *
	 * @param viewId
	 *            The view to open; must not be <code>null</code>
	 * @throws PartInitException
	 *             If the part could not be initialized.
	 */
	private final void openView(final String viewId, EPartService partService)
			throws PartInitException {
		partService.showPart(viewId, PartState.ACTIVATE);
	}
}
