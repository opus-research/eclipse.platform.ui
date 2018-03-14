/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steven Spungin <steven@spungin.tv> - initial API and implementation
 *******************************************************************************/

package org.eclipse.e4.ui.workbench.renderers.swt;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.e4.ui.model.application.ui.MGenericTile;
import org.eclipse.e4.ui.model.application.ui.MUIElement;

/**
 * Methods to manage sash container sizes information.
 */
public class SashUtil {

	/**
	 * @param sashContainer
	 * @return
	 */
	static public List<MUIElement> getVisibleChildren(
			MGenericTile<?> sashContainer) {
		List<MUIElement> visKids = new ArrayList<MUIElement>();
		for (MUIElement child : sashContainer.getChildren()) {
			if (child.isToBeRendered() && child.isVisible())
				visKids.add(child);
		}
		return visKids;
	}

	/**
	 * If a container does not contain any relative weights, the last item is
	 * converted to relative and set to the default weight. <br/ >
	 * <br />
	 * At least 1 relative weight container must NOT contain a max value. If all
	 * contain a max value, the last one will have the max value removed.
	 * 
	 * @param sashContainer
	 */
	static public void validateContainerData(MGenericTile<?> sashContainer) {
		boolean foundNoMax = false;
		MUIElement lastRelative = null;

		List<MUIElement> visibleChildren = getVisibleChildren(sashContainer);
		for (MUIElement ele : visibleChildren) {
			SizeInfo sizeInfo = SizeInfo.parse(ele.getContainerData());
			if (!sizeInfo.isDefaultAbsolute()) {
				lastRelative = ele;
				if (sizeInfo.getMaxValue() == null) {
					foundNoMax = true;
				}
			}
		}
		if (lastRelative == null && visibleChildren.isEmpty() == false) {
			lastRelative = visibleChildren.get(visibleChildren.size() - 1);
			lastRelative.setContainerData("10000%"); //$NON-NLS-1$
		}

		if (foundNoMax == false && lastRelative != null) {
			SizeInfo info = SizeInfo.parse(lastRelative.getContainerData());
			info.setMaxValue(null);
			lastRelative.setContainerData(info.getEncodedParameters());

		}
	}

	/**
	 * @param visibleChildren
	 * @return The total relative weight of items
	 */
	static public double getTotalWeight(List<MUIElement> visibleChildren) {
		double total = 0;
		for (MUIElement item : visibleChildren) {
			SizeInfo info = SizeInfo.parse(item.getContainerData());
			if (!info.isDefaultAbsolute()) {
				total += info.getDefaultValue();
			}
		}
		return total;
	}

	/**
	 * 
	 * @param isHorizontal
	 * @param totalSize
	 * @param sashWidth
	 * @param visibleChildren
	 * @return The available pixels for relative containers. This is the total
	 *         width minus the absolute widths and sash widths.
	 */
	static public int getAvailableRelative(boolean isHorizontal, int totalSize,
			int sashWidth, List<MUIElement> visibleChildren) {
		int availableRelative;
		int totalFixed = 0;
		for (MUIElement item : visibleChildren) {
			SizeInfo info = SizeInfo.parse(item.getContainerData());
			if (info.isDefaultAbsolute()) {
				totalFixed += info.getValueConstrained(0, 0);
			}
		}
		int sashSpace = (visibleChildren.size() - 1) * sashWidth;
		if (isHorizontal) {
			availableRelative = totalSize - totalFixed - sashSpace;
		} else {
			availableRelative = totalSize - totalFixed - sashSpace;
		}
		return availableRelative;
	}

}
