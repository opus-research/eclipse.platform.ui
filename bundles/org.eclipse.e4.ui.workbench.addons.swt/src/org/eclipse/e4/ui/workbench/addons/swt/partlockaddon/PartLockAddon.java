package org.eclipse.e4.ui.workbench.addons.swt.partlockaddon;

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

import org.eclipse.e4.ui.workbench.PartSizeInfo;
import org.eclipse.e4.ui.workbench.PartSizeInfo.PartResizeMode;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map.Entry;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolItem;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.UIEvents.ApplicationElement;
import org.eclipse.e4.ui.workbench.UIEvents.EventTags;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.ToolItem;
import org.osgi.service.event.Event;

/**
 * For testing. Adds a lock/unlock icon to every toolbar added to a part.
 * 
 * @author Steven Spungin
 *
 */
@SuppressWarnings("restriction")
public class PartLockAddon {

	private static Image imgLocked;
	private static Image imgUnlocked;
	final private String pluginId = "org.eclipse.e4.ui.workbench.addons.swt"; //$NON-NLS-1$
	final private String pluginPath = "platform:/plugin/" + pluginId; //$NON-NLS-1$
	final private String iconPath = pluginPath + "/icons/full/etool16"; //$NON-NLS-1$

	@PostConstruct
	private void postConstruct() {
		try {
			imgLocked = ImageDescriptor.createFromURL(new URL(iconPath + "/locked.png")) //$NON-NLS-1$
					.createImage();
			imgUnlocked = ImageDescriptor.createFromURL(new URL(iconPath + "/unlocked.png")) //$NON-NLS-1$
					.createImage();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}

	@PreDestroy
	void preDestroy() {
		if (imgLocked != null) {
			imgLocked.dispose();
		}
		if (imgUnlocked != null) {
			imgUnlocked.dispose();
		}
	}

	// Add the PartLock toggle button as the first item to every part toolbar
	@Inject
	@Optional
	private void subscribeTopicToolbar(@UIEventTopic(UIEvents.Part.TOPIC_TOOLBAR) Event event) {
		if (UIEvents.isSET(event)) {
			MToolBar toolbar = (MToolBar) event.getProperty("NewValue"); //$NON-NLS-1$
			if (toolbar == null) {
				return;
			}
			MPart part = (MPart) event.getProperty("ChangedElement"); //$NON-NLS-1$
			addItemsToToolbar(part, toolbar);
		}
	}

	// 1. When a part is added to a PartSashContainer
	// 2. Ensure a tool bar is present
	// 3. Add the PartLock toggle button as the first item
	@Inject
	@Optional
	private void subscribeTopicWidget(@UIEventTopic(UIEvents.UIElement.TOPIC_WIDGET) Event event) {
		final MUIElement changedElement = (MUIElement) event.getProperty(EventTags.ELEMENT);
		if (changedElement instanceof MPart) {
			MPart part = (MPart) changedElement;
			MToolBar toolbar = part.getToolbar();
			if (toolbar == null) {
				toolbar = MMenuFactory.INSTANCE.createToolBar();
				part.setToolbar(toolbar);
				// The PartLock toggle button will be added by another event
				// listener
			} else {
				addItemsToToolbar(part, toolbar);
			}
		}
	}

	@Inject
	@Optional
	private void subscribeTopicTransientData(
			@UIEventTopic(ApplicationElement.TOPIC_TRANSIENTDATA) Event event) {
		Entry<?, ?> entry = (Entry<?, ?>) event.getProperty("NewValue"); //$NON-NLS-1$
		if (entry == null) {
			return;
		}
		if (PartSizeInfo.KEY_TRANSIENT_DATA.equals(entry.getKey())) {
			MUIElement changedElement = (MUIElement) event.getProperty(UIEvents.EventTags.ELEMENT);
			MUIElement child = PartLockAddon.getPartSashChild(changedElement);
			MToolItem toolItem = (MToolItem) child.getTransientData().get("_lockToolItem"); //$NON-NLS-1$
			if (toolItem != null) {
				PartLockAddon.updateIcon(toolItem, PartSizeInfo.get(child));
			}
		}
	}

	private void addItemsToToolbar(MPart part, MToolBar toolbar) {
		MDirectToolItem toolItem;
		toolItem = MMenuFactory.INSTANCE.createDirectToolItem();
		toolItem.setLabel("Mode");
		toolItem.setContributionURI("bundleclass://" + pluginId + "/org.eclipse.e4.ui.workbench.addons.swt.partlockaddon.ToggleLockHandler"); //$NON-NLS-1$//$NON-NLS-2$
		toolbar.getChildren().add(toolItem);

		MUIElement ele = PartLockAddon.getPartSashChild(part);

		if (ele != null && !ele.getTransientData().containsKey("_lockToolItem")) { //$NON-NLS-1$
			ele.getTransientData().put("_lockToolItem", toolItem); //$NON-NLS-1$
			// At this point, the persisted data has not been loaded
			// into transient data, and legacy container data has not been
			// converted to persisted data
			if ("fixed".equals(ele.getPersistedState().get(PartSizeInfo.KEY_RESIZE_MODE)) //$NON-NLS-1$
					|| ele.getContainerData() != null && ele.getContainerData().contains("fixed")) { //$NON-NLS-1$
				toolItem.setIconURI(iconPath + "/locked.png"); //$NON-NLS-1$
			} else {
				toolItem.setIconURI(iconPath + "/unlocked.png"); //$NON-NLS-1$
			}
		}
	}

	/**
	 * 
	 * @param part
	 * @return The child of the immediate part sash ancestor.
	 */
	public static MUIElement getPartSashChild(MUIElement part) {
		MUIElement child = part;
		while (child != null
				&& (MUIElement) child.getParent() instanceof MPartSashContainer == false) {
			child = child.getParent();
		}
		return child;
	}

	static void updateIcon(MToolItem toolItem, PartSizeInfo info) {
		ToolItem ti = (ToolItem) toolItem.getWidget();
		if (info.getResizeMode() == PartResizeMode.FIXED) {
			ti.setImage(imgLocked);
		} else {
			ti.setImage(imgUnlocked);
		}
	}

}