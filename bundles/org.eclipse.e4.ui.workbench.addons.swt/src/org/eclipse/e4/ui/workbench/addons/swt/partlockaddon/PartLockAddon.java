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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map.Entry;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainer;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuFactory;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarElement;
import org.eclipse.e4.ui.model.application.ui.menu.MToolItem;
import org.eclipse.e4.ui.workbench.PartSizeInfo;
import org.eclipse.e4.ui.workbench.PartSizeInfo.PartResizeMode;
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
public class PartLockAddon {

	private static Image imgLocked;
	private static Image imgUnlocked;
	final private String pluginId = "org.eclipse.e4.ui.workbench.addons.swt"; //$NON-NLS-1$
	final private String pluginPath = "platform:/plugin/" + pluginId; //$NON-NLS-1$
	final private String iconPath = pluginPath + "/icons/full/etool16"; //$NON-NLS-1$
	final String uri = "bundleclass://" + pluginId //$NON-NLS-1$
			+ "/org.eclipse.e4.ui.workbench.addons.swt.partlockaddon.ToggleLockHandler"; //$NON-NLS-1$

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

	@Inject
	@Optional
	private void subscribeTopicWidget(@UIEventTopic(UIEvents.UIElement.TOPIC_WIDGET) Event event) {
		MUIElement changedElement = (MUIElement) event.getProperty(EventTags.ELEMENT);
		if (!UIEvents.isSET(event)) {
			return;
		}

		if (changedElement instanceof MPlaceholder) {
			changedElement = ((MPlaceholder) changedElement).getRef();
		}

		if (changedElement instanceof MPart) {

			MToolBar toolbar;
			MPart part = (MPart) changedElement;
			toolbar = part.getToolbar();
			if (toolbar == null) {
				toolbar = MMenuFactory.INSTANCE.createToolBar();
				part.setToolbar(toolbar);
				addItemsToToolbar(part, toolbar);
				// The PartLock toggle button will be added by another event
				// listener
			} else {
				addItemsToToolbar(part, toolbar);
			}
		}
	}

	// 1. When a part is added to a PartSashContainer
	// 2. Ensure a tool bar is present
	// 3. Add the PartLock toggle button as the first item
	@Inject
	@Optional
	private void subscribeTopicChildren(
			@UIEventTopic(UIEvents.ElementContainer.TOPIC_CHILDREN) Event event) {
		MUIElement changedElement = (MUIElement) event.getProperty(EventTags.ELEMENT);

		if (UIEvents.isADD(event)) {
			if (changedElement instanceof MPlaceholder) {
				changedElement = ((MPlaceholder) changedElement).getCurSharedRef();
			}

			if (changedElement instanceof MPart) {
				MPart part = (MPart) changedElement;

				MToolBar toolbar = ((MPart) changedElement).getToolbar();
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
			MUIElement child = PartLockAddon.getSashChild(changedElement);
			MToolItem toolItem = (MToolItem) child.getTransientData().get("_lockToolItem"); //$NON-NLS-1$
			if (toolItem != null) {
				PartLockAddon.updateIcon(toolItem, PartSizeInfo.get(child));
			}
		}
	}

	private void addItemsToToolbar(MPart part, MToolBar toolbar) {
		for (MToolBarElement item : toolbar.getChildren()) {
			if (item instanceof MDirectToolItem) {
				MDirectToolItem dti = (MDirectToolItem) item;
				if (uri.equals(dti.getContributionURI())) {
					return;
				}
			}
		}

		MDirectToolItem toolItem;
		toolItem = MMenuFactory.INSTANCE.createDirectToolItem();
		toolItem.setContributionURI(uri);
		toolItem.setLabel("Mode");
		toolbar.getChildren().add(0, toolItem);
		toolItem.setIconURI(iconPath + "/unlocked.png"); //$NON-NLS-1$

		MUIElement ele = PartLockAddon.getSashChild(part);
		if (ele == null) {
			ele = part;
		}

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
	public static MUIElement getSashChild(MUIElement part) {
		MUIElement child = part;
		if (child.getCurSharedRef() != null) {
			child = child.getCurSharedRef();
		}
		while (child != null
				&& (MUIElement) child.getParent() instanceof MPartSashContainer == false) {
			child = child.getParent();
		}
		return child;
	}

	static void updateIcon(MToolItem toolItem, PartSizeInfo info) {
		ToolItem ti = (ToolItem) toolItem.getWidget();
		if (ti != null) {
			if (info.getResizeMode() == PartResizeMode.FIXED) {
				ti.setImage(imgLocked);
			} else {
				ti.setImage(imgUnlocked);
			}
		}
	}

}