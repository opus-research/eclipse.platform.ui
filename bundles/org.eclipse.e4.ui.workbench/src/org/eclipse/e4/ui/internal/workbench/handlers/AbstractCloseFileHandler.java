/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.internal.workbench.PartServiceImpl;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.modeling.EPartService;

/**
 * Abstract pure E4 implementation of basic file close handling, including Class All/Close Others.
 * Subclasses only need to pass in appropriate arguments to the constructor.
 * <p>
 * <strong>Note:</strong> As of Eclipse 4.3 (Kepler), this class shows individual Save dialogs for
 * each dirty part, as the integrated save dialog has not yet been factored into a pure E4 solution.
 * </p>
 */
public abstract class AbstractCloseFileHandler {

	private boolean closeActivePart;
	private boolean includeSiblings;

	/**
	 * Configure the abstract close handler.
	 * 
	 * @param closeActivePart
	 *            <code>true</code> if the active part is to be closed
	 * @param includeSiblings
	 *            <code>true</code> if the active part's siblings are to be closed
	 * 
	 */
	protected AbstractCloseFileHandler(boolean closeActivePart, boolean includeSiblings) {
		this.closeActivePart = closeActivePart;
		this.includeSiblings = includeSiblings;
	}

	/**
	 * Execute the handler
	 * 
	 * @param part
	 * @param partService
	 */
	@Execute
	public void execute(MPart part, EPartService partService) {
		MElementContainer<MUIElement> partContainer = getParent(part);
		if (partContainer == null)
			return;

		List<MPart> partsToClose = includeSiblings ? gatherSiblingParts(part, closeActivePart)
				: new ArrayList<MPart>(
						(part.isToBeRendered() && isClosable(part)) ? Collections
								.singletonList(part) : Collections.<MPart> emptyList());

		closeParts(partContainer, partsToClose, partService);
	}

	/**
	 * Test whether the handler can execute
	 * 
	 * @return <code>true</code> if the handler can execute
	 */
	@CanExecute
	public boolean canExecute() {
		return true;
	}

	private MElementContainer<MUIElement> getParent(MPart part) {
		MElementContainer<MUIElement> parent = part.getParent();
		if (parent == null) {
			MPlaceholder placeholder = part.getCurSharedRef();
			return placeholder == null ? null : placeholder.getParent();
		}
		return parent;
	}

	private boolean isClosable(MPart part) {
		// if it's a shared part check its current ref
		if (part.getCurSharedRef() != null) {
			return !(part.getCurSharedRef().getTags().contains(IPresentationEngine.NO_CLOSE));
		}

		return part.isCloseable();
	}

	private List<MPart> getCloseableSiblingParts(MPart part) {
		// broken out from closeSiblingParts so it can be used to determine how
		// many closeable siblings are available
		MElementContainer<MUIElement> container = getParent(part);
		List<MPart> closeableSiblings = new ArrayList<MPart>();
		if (container == null)
			return closeableSiblings;

		List<MUIElement> children = container.getChildren();
		for (MUIElement child : children) {
			// If the element isn't showing skip it
			if (!child.isToBeRendered())
				continue;

			MPart otherPart = null;
			if (child instanceof MPart)
				otherPart = (MPart) child;
			else if (child instanceof MPlaceholder) {
				MUIElement otherItem = ((MPlaceholder) child).getRef();
				if (otherItem instanceof MPart)
					otherPart = (MPart) otherItem;
			}
			if (otherPart == null)
				continue;

			if (part.equals(otherPart))
				continue; // skip selected item
			if (otherPart.isToBeRendered() && isClosable(otherPart))
				closeableSiblings.add(otherPart);
		}
		return closeableSiblings;
	}

	/**
	 * Close the passed parts of the passed parts, prompting for save for those that are not saved.
	 * 
	 * @param partContainer
	 *            the container of the parts to close
	 * @param parts
	 *            the parts to close
	 * @param partService
	 *            the part service
	 * @throws NullPointerException
	 *             if any argument is <code>null</code>
	 */
	protected void closeParts(MElementContainer<MUIElement> partContainer, List<MPart> parts,
			EPartService partService) {
		if (partContainer == null || parts == null || partService == null) {
			throw new NullPointerException();
		}

		boolean saveOK = false;
		if (partService instanceof PartServiceImpl) {
			// TODO In Luna, EPartService should have a public method for this.
			// HACK: EPartService doesn't have this as API yet
			final PartServiceImpl impl = (PartServiceImpl) partService;
			saveOK = impl.saveParts(parts, true);
		}
		if (saveOK) {
			// hide (close) the parts, but do the selected part last
			// to avoid unnecessary part activations
			final MUIElement selectedElement = partContainer.getSelectedElement();
			final MUIElement selectedPartCandidate = selectedElement instanceof MPlaceholder ? ((MPlaceholder) selectedElement)
					.getRef() : selectedElement;
			final MPart selectedPart = selectedPartCandidate instanceof MPart ? (MPart) selectedPartCandidate
					: null;
			for (MPart part : parts) {
				if (part != selectedPart) {
					// part may not have been saved by user choice, ensure it is not marked dirty.
					part.setDirty(false);
					partService.hidePart(part);
				}
			}
			if (parts.contains(selectedPart)) {
				selectedPart.setDirty(false);
				partService.hidePart(selectedPart);
			}
		}
	}

	protected List<MPart> gatherSiblingParts(MPart part, boolean includeThisPart) {
		List<MPart> others = getCloseableSiblingParts(part);

		// add the current part last so that we unrender obscured items first
		if (includeThisPart && part.isToBeRendered() && isClosable(part)) {
			others.add(part);
		}
		return others;
	}

}