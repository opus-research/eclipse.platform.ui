/*******************************************************************************
 * Copyright (c) 2010, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.addons.dndaddon;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.e4.ui.internal.workbench.swt.AbstractPartRenderer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.IPresentationEngine;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

/**
 *
 */
public class StackDropAgent extends DropAgent {
	private Rectangle tabArea;
	private MPartStack dropStack;
	private CTabFolder dropCTF;

	private ArrayList<Rectangle> itemRects;
	private int curDropIndex = -2;
	private Rectangle centerArea;

	/**
	 * @param manager
	 */
	public StackDropAgent(DnDManager manager) {
		super(manager);
	}

	/**
	 * Returns the stack for the given element or
	 *
	 * @param info
	 */
	private MPartStack getStackFor(MUIElement dragElement, DnDInfo info) {
		if (info.curElement instanceof MPartStack) {
			return (MPartStack) info.curElement;
		}
		return SplitDropAgent2.getStackAt(dragElement, info, dndManager.getModelService());
	}

	@Override
	public boolean canDrop(MUIElement dragElement, DnDInfo info) {
		// We only except stack elements and whole stacks
		if (!(dragElement instanceof MStackElement) && !(dragElement instanceof MPartStack)) {
			System.out.println("StackDropAgent wrong element type");
			return false;
		}

		MPartStack stack = getStackFor(dragElement, info);

		// We have to be over a stack ourselves
		if (stack == null) {
			System.out.println("StackDropAgent not dragging over a stack (type is: " + info.curElement + ")");
			return false;
		}

		if (stack.getTags().contains(IPresentationEngine.STANDALONE)) {
			System.out.println("StackDropAgent this is a standalone view");
			return false;
		}

		// We only work for CTabFolders
		if (!(stack.getWidget() instanceof CTabFolder)) {
			System.out.println("StackDropAgent only works for CTabFolder");
			return false;
		}

		// We can't drop stacks onto itself
		// if (stack == dragElement)
		// return false;

		// You can only drag MParts from window to window
		// NOTE: Disabled again due to too many issues, see bug 445305 for details
		// if (!(dragElement instanceof MPart)) {
		EModelService ms = dndManager.getModelService();
		MWindow dragElementWin = ms.getTopLevelWindowFor(dragElement);
		MWindow dropWin = ms.getTopLevelWindowFor(stack);
		if (dragElementWin != dropWin) {
			System.out.println("StackDropAgent dragging over wrong window");
			return false;
		}
		// }

		CTabFolder ctf = (CTabFolder) stack.getWidget();
		// only allow dropping into the the area
		Rectangle areaRect = getTabAreaRect(ctf);
		Rectangle centerBounds = getCenterBounds(ctf);
		boolean inArea = areaRect.contains(info.cursorPos) || centerBounds.contains(info.cursorPos);
		if (inArea) {
			tabArea = areaRect;
			centerArea = centerBounds;
			dropStack = stack;
			dropCTF = ctf;
			createInsertRects();
		} else {
			System.out.println("StackDropAgent not dragging over proper area");
		}

		return inArea;
	}

	private Rectangle getCenterBounds(CTabFolder ctf) {
		Rectangle centerBounds = ctf.getBounds();
		int heightSplitRegion = Math.min(centerBounds.height / 3, 64);
		int widthSplitRegion = Math.min(centerBounds.width / 3, 64);
		Geometry.expand(centerBounds, -widthSplitRegion, -widthSplitRegion, -heightSplitRegion, -heightSplitRegion);
		centerBounds = Display.getCurrent().map(ctf.getParent(), null, centerBounds);
		return centerBounds;
	}

	private Rectangle getTabAreaRect(CTabFolder theCTF) {
		Rectangle ctfBounds = theCTF.getBounds();
		ctfBounds.height = theCTF.getTabHeight();

		Rectangle displayBounds = Display.getCurrent().map(theCTF.getParent(), null, ctfBounds);
		return displayBounds;
	}

	private void createInsertRects() {
		itemRects = new ArrayList<Rectangle>();
		if (dropCTF.getItems().length > 0) {
			CTabItem[] items = dropCTF.getItems();

			// First rect is from left to the center of the item
			Rectangle itemRect = items[0].getBounds();
			int centerX = itemRect.x + (itemRect.width / 2);
			itemRect.width /= 2;
			int curX = itemRect.x + itemRect.width;
			Rectangle insertRect = dropCTF.getDisplay().map(dropCTF, null, itemRect);
			itemRects.add(insertRect);

			// Process the other items
			for (int i = 1; i < items.length; i++) {
				itemRect = items[i].getBounds();
				centerX = itemRect.x + (itemRect.width / 2);
				itemRect.width = centerX - curX;
				itemRect.x = curX;
				curX = centerX;
				insertRect = dropCTF.getDisplay().map(dropCTF, null, itemRect);
				itemRects.add(insertRect);
			}

			// Finally, add a rectangle from the center of the last element to the end
			itemRect.x = curX;
			itemRect.width = dropCTF.getBounds().width - curX;
			insertRect = dropCTF.getDisplay().map(dropCTF, null, itemRect);
			itemRects.add(insertRect);
		} else {
			// Empty stack, whole area is index == 0
			itemRects.add(tabArea);
		}
	}

	private int getDropIndex(DnDInfo info) {
		if (itemRects == null)
			return -1;

		for (Rectangle itemRect : itemRects) {
			if (itemRect.contains(info.cursorPos))
				return itemRects.indexOf(itemRect);
		}
		return -1;
	}

	@Override
	public void dragLeave(MUIElement dragElement, DnDInfo info) {
		dndManager.clearOverlay();

		if (dndManager.getFeedbackStyle() == DnDManager.HOSTED) {
			if (dragElement.getParent() != null)
				dndManager.hostElement(dragElement, 16, 10);
		} else {
			dndManager.setHostBounds(null);
		}

		tabArea = null;
		curDropIndex = -2;

		super.dragLeave(dragElement, info);
	}

	@Override
	public boolean track(MUIElement dragElement, DnDInfo info) {

		boolean inCenter = centerArea.contains(info.cursorPos);
		if (!(inCenter || tabArea.contains(info.cursorPos)) || dropStack == null || !dropStack.isToBeRendered()) {
			System.out.println("track returning false since out of region");
			return false;
		}
		int dropIndex = getDropIndex(info);
		if (dropIndex == -1) {
			if (inCenter) {
				dropIndex = itemRects == null ? 0 : itemRects.size() - 1;
			} else {
				System.out.println("track returning true - no tabs under cursor");
				return true;
			}
		}
		if (curDropIndex == dropIndex)
			return true;
		curDropIndex = dropIndex;

		dndManager.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_HAND));

		if (dropStack.getChildren().indexOf(dragElement) == dropIndex)
			return true;

		if (dndManager.getFeedbackStyle() == DnDManager.HOSTED) {
			dock(dragElement, dropIndex);
			Display.getCurrent().update();
			showFrame(dragElement);
		} else {
			if (dropIndex < dropCTF.getItemCount()) {
				Rectangle itemBounds = dropCTF.getItem(dropIndex).getBounds();
				itemBounds.width = 2;
				itemBounds = Display.getCurrent().map(dropCTF, null, itemBounds);
				dndManager.frameRect(itemBounds);
			} else if (dropCTF.getItemCount() > 0) {
				Rectangle itemBounds = dropCTF.getItem(dropIndex - 1).getBounds();
				itemBounds.x = itemBounds.x + itemBounds.width;
				itemBounds.width = 2;
				itemBounds = Display.getCurrent().map(dropCTF, null, itemBounds);
				dndManager.frameRect(itemBounds);
			} else {
				Rectangle fr = new Rectangle(tabArea.x, tabArea.y, tabArea.width, tabArea.height);
				fr.width = 2;
				dndManager.frameRect(fr);
			}

			if (dndManager.getFeedbackStyle() == DnDManager.GHOSTED) {
				Rectangle ca = dropCTF.getClientArea();
				ca = Display.getCurrent().map(dropCTF, null, ca);
				dndManager.setHostBounds(ca);
			}
		}

		return true;
	}

	/**
	 * @param dragElement
	 * @param dropIndex
	 */
	private void dock(MUIElement dragElement, int dropIndex) {
		// Adjust the index if necessary
		int elementIndex = dropStack.getChildren().indexOf(dragElement);
		if (elementIndex != -1 && !(dragElement instanceof MPartStack)) {
			// Get the index of this CTF entry
			Control dragCtrl = (Control) dragElement.getWidget();
			for (CTabItem cti : dropCTF.getItems()) {
				if (dragCtrl == cti.getControl()) {
					int itemIndex = dropCTF.indexOf(cti);
					if (dropIndex > 0 && itemIndex < dropIndex)
						dropIndex--;
				}
			}
		}

		// 'dropIndex' is now the index of the CTabItem to put ourselves before
		// we need to adjust this to be a model index
		int ctfItemCount = dropCTF.getItemCount();
		if (dropIndex < ctfItemCount) {
			CTabItem item = dropCTF.getItem(dropIndex);
			MUIElement itemModel = (MUIElement) item.getData(AbstractPartRenderer.OWNING_ME);

			// if we're going before ourselves its a NO-OP
			if (itemModel == dragElement)
				return;

			dropIndex = itemModel.getParent().getChildren().indexOf(itemModel);
			// if the item is dropped at the last position, there is
			// no existing item to put ourselves before
			// so we'll just go to the end.
		} else if (dropIndex == ctfItemCount) {
			dropIndex = dropStack.getChildren().size();
		}

		if (dragElement instanceof MStackElement) {
			if (dragElement.getParent() != null)
				dragElement.getParent().getChildren().remove(dragElement);

			if (dropIndex >= 0 && dropIndex < dropStack.getChildren().size())
				dropStack.getChildren().add(dropIndex, (MStackElement) dragElement);
			else
				dropStack.getChildren().add((MStackElement) dragElement);

			// (Re)active the element being dropped
			dropStack.setSelectedElement((MStackElement) dragElement);
		} else {
			MPartStack stack = (MPartStack) dragElement;
			MStackElement curSel = stack.getSelectedElement();
			List<MStackElement> kids = stack.getChildren();

			// First move over all *non-selected* elements
			int selIndex = kids.indexOf(curSel);
			boolean curSelProcessed = false;
			while (kids.size() > 1) {
				// Offset the 'get' to account for skipping 'curSel'
				MStackElement kid = curSelProcessed ? kids.get(kids.size() - 2) : kids.get(kids
						.size() - 1);
				if (kid == curSel) {
					curSelProcessed = true;
					continue;
				}

				kids.remove(kid);
				if (dropIndex >= 0 && dropIndex < dropStack.getChildren().size())
					dropStack.getChildren().add(dropIndex, kid);
				else
					dropStack.getChildren().add(kid);
			}

			// Finally, move over the selected element
			kids.remove(curSel);
			dropIndex = dropIndex + selIndex;
			if (dropIndex >= 0 && dropIndex < dropStack.getChildren().size())
				dropStack.getChildren().add(dropIndex, curSel);
			else
				dropStack.getChildren().add(curSel);

			// (Re)active the element being dropped
			dropStack.setSelectedElement(curSel);
		}
	}

	/**
	 * @param dragElement
	 */
	private void showFrame(MUIElement dragElement) {
		CTabFolder ctf = (CTabFolder) dropStack.getWidget();
		CTabItem[] items = ctf.getItems();
		CTabItem item = null;
		for (int i = 0; i < items.length; i++) {
			if (items[i].getData(AbstractPartRenderer.OWNING_ME) == dragElement) {
				item = items[i];
				break;
			}
		}

		Rectangle bounds = item.getBounds();
		bounds = Display.getCurrent().map(dropCTF, null, bounds);
		Rectangle outerBounds = new Rectangle(bounds.x - 3, bounds.y - 3, bounds.width + 6,
				bounds.height + 6);

		dndManager.frameRect(outerBounds);
	}

	@Override
	public boolean drop(MUIElement dragElement, DnDInfo info) {
		if (dragElement instanceof MPartStack) {
			if (info.curElement == dragElement)
				return true;
		}

		if (dndManager.getFeedbackStyle() != DnDManager.HOSTED) {
			int dropIndex = getDropIndex(info);
			if (dropIndex != -1) {
				MUIElement toActivate = dragElement instanceof MPartStack ? ((MPartStack) dragElement)
						.getSelectedElement() : dragElement;
				dock(dragElement, dropIndex);
				reactivatePart(toActivate);
			}
		}
		return true;
	}
}
