/*******************************************************************************
 * Copyright (c) 2015 vogella GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 450414
 ******************************************************************************/
package org.eclipse.ui.internal.handler;

import java.util.List;

import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MCompositePart;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;

/**
 * Our sample handler extends AbstractHandler, an IHandler base class.
 * @see org.eclipse.core.commands.IHandler
 * @see org.eclipse.core.commands.AbstractHandler
 */
public class SplitHandler {

	/**
	 * This method set appropriate tags for splitting editors, which trigger the
	 * split editor functionality.
	 *
	 * @param workbenchPage
	 *            {@link IWorkbenchPage}
	 * @param modelService
	 *            {@link EModelService}
	 * @param shell
	 *            {@link Shell}
	 * @param isHorizontal
	 *            parameter of the command
	 */
	@Execute
	public void execute(IWorkbenchPage workbenchPage, EModelService modelService, Shell shell,
			@Named("Splitter.isHorizontal") String isHorizontal) {
		// Only works for the active editor
		IEditorPart activeEditor = workbenchPage.getActiveEditor();
		if (activeEditor == null)
			return;

		MPart editorPart = activeEditor.getSite().getService(MPart.class);
		if (editorPart == null)
			return;

		// Get services
		modelService =  editorPart.getContext().get(EModelService.class);

		MPartStack stack = getStackFor(editorPart);
		if (stack == null)
			return;

		shell.setRedraw(false);
		try {
			// Determine which part has the tags
			MStackElement stackSelElement = stack.getSelectedElement();
			MPart taggedEditor = editorPart;
			if (stackSelElement instanceof MCompositePart) {
				List<MPart> innerElements = modelService.findElements(stackSelElement, null, MPart.class, null);
				taggedEditor = innerElements.get(1); // '0' is the composite part
			}

			if (Boolean.valueOf(isHorizontal).booleanValue()) {
				if (taggedEditor.getTags().contains(IPresentationEngine.SPLIT_VERTICAL)) {
					taggedEditor.getTags().remove(IPresentationEngine.SPLIT_VERTICAL);
				} else {
					editorPart.getTags().remove(IPresentationEngine.SPLIT_HORIZONTAL);
					editorPart.getTags().add(IPresentationEngine.SPLIT_VERTICAL);
				}
			} else {
				if (taggedEditor.getTags().contains(IPresentationEngine.SPLIT_HORIZONTAL)) {
					taggedEditor.getTags().remove(IPresentationEngine.SPLIT_HORIZONTAL);
				} else {
					editorPart.getTags().remove(IPresentationEngine.SPLIT_VERTICAL);
					editorPart.getTags().add(IPresentationEngine.SPLIT_HORIZONTAL);
				}
			}
		} finally {
			shell.setRedraw(true);
		}
	}

	private MPartStack getStackFor(MPart part) {
		MUIElement presentationElement = part.getCurSharedRef() == null ? part : part.getCurSharedRef();
		MUIElement parent = presentationElement.getParent();
		while (parent != null && !(parent instanceof MPartStack))
			parent = parent.getParent();

		return (MPartStack) parent;
	}
}
