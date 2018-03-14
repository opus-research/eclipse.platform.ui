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

package org.eclipse.e4.ui.workbench.addons.swt.partlockaddon;

import org.eclipse.e4.ui.workbench.PartSizeInfo;
import org.eclipse.e4.ui.workbench.PartSizeInfo.PartResizeMode;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.menu.MToolItem;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;

/**
 *
 */
@SuppressWarnings("restriction")
public class ToggleLockHandler {

	@Execute
	void execute(MPart part, MToolItem toolItem) {
		toggleResizeMode(part, toolItem);
	}

	static void toggleResizeMode(MPart part, MToolItem toolItem) {
		MUIElement child = PartLockAddon.getPartSashChild(part);
		if (child == null) {
			return;
		}

		PartSizeInfo info = PartSizeInfo.get(child);
		MPartSashContainer container = (MPartSashContainer) (MUIElement) child.getParent();
		if (info.getResizeMode() == PartResizeMode.FIXED) {
			int size;
			Object widget = container.getWidget();
			if (widget instanceof Rectangle) {
				if (container.isHorizontal()) {
					size = ((Rectangle) widget).width;
				} else {
					size = ((Rectangle) widget).height;
				}

			} else {
				if (container.isHorizontal()) {
					size = ((Control) widget).getSize().x;
				} else {
					size = ((Control) widget).getSize().y;
				}
			}
			info.convertToWeighted(child, size);
			PartLockAddon.updateIcon(toolItem, info);
		} else {
			Control control = ((Control) child.getWidget());
			info.setResizeMode(PartResizeMode.FIXED);
			info.setDefaultAbsolute(true);
			if (container.isHorizontal()) {
				info.setDefaultValue(control.getSize().x);
			} else {
				info.setDefaultValue(control.getSize().y);
			}
			PartLockAddon.updateIcon(toolItem, info);
		}

		info.notifyChanged();
	}
}
