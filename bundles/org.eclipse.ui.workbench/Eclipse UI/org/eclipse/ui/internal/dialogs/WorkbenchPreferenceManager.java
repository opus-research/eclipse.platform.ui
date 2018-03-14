/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jan-Hendrik Diederich, Bredex GmbH - bug 201052
 *******************************************************************************/
package org.eclipse.ui.internal.dialogs;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IRegistryChangeEvent;
import org.eclipse.core.runtime.IRegistryChangeListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.dynamichelpers.ExtensionTracker;
import org.eclipse.core.runtime.dynamichelpers.IExtensionChangeHandler;
import org.eclipse.core.runtime.dynamichelpers.IExtensionTracker;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.IPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.internal.preferences.WorkbenchPreferenceExpressionNode;
import org.eclipse.ui.internal.registry.IWorkbenchRegistryConstants;
import org.eclipse.ui.internal.registry.PreferencePageRegistryReader;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * The WorkbenchPreferenceManager is the manager that can handle categories and
 * preference nodes.
 */
public class WorkbenchPreferenceManager extends PreferenceManager implements
		IExtensionChangeHandler {
	private IEventBroker eventBroker;

	private CSSThemeChangedHandler cssThemeChangedHandler;

	/**
	 * Create a new instance of the receiver with the specified seperatorChar
	 * 
	 * @param separatorChar
	 */
	public WorkbenchPreferenceManager(char separatorChar) {
		super(separatorChar, new WorkbenchPreferenceExpressionNode("")); //$NON-NLS-1$
        
		IExtensionTracker tracker = PlatformUI.getWorkbench().getExtensionTracker();
		tracker.registerHandler(this, ExtensionTracker.createExtensionPointFilter(getExtensionPointFilter()));

		// add a listener for keyword deltas. If any occur clear all page caches
		Platform.getExtensionRegistry().addRegistryChangeListener(
				new IRegistryChangeListener() {

					/*
					 * (non-Javadoc)
					 * 
					 * @see org.eclipse.core.runtime.IRegistryChangeListener#registryChanged(org.eclipse.core.runtime.IRegistryChangeEvent)
					 */
					@Override
					public void registryChanged(IRegistryChangeEvent event) {
						if (event.getExtensionDeltas(PlatformUI.PLUGIN_ID,
								IWorkbenchRegistryConstants.PL_KEYWORDS).length > 0) {
							for (Iterator j = getElements(
									PreferenceManager.POST_ORDER).iterator(); j
									.hasNext();) {
								((WorkbenchPreferenceNode) j.next())
										.clearKeywords();
							}
						}
					}
				});

		eventBroker = (IEventBroker) PlatformUI.getWorkbench().getService(IEventBroker.class);
		if (eventBroker != null) {
			cssThemeChangedHandler = new CSSThemeChangedHandler(this);
			eventBroker.subscribe(IThemeEngine.Events.THEME_CHANGED, cssThemeChangedHandler);
		}
	}

	public void dispose() {
		if (eventBroker != null) {
			eventBroker.unsubscribe(cssThemeChangedHandler);
			cssThemeChangedHandler.dispose();
		}
	}

	/**
	 * Add the pages and the groups to the receiver.
	 * 
	 * @param pageContributions
	 */
	public void addPages(Collection pageContributions) {

		// Add the contributions to the manager
		Iterator iterator = pageContributions.iterator();
		while (iterator.hasNext()) {
			Object next = iterator.next();
			if (next instanceof WorkbenchPreferenceNode) {
				WorkbenchPreferenceNode wNode = (WorkbenchPreferenceNode) next;
				addToRoot(wNode);
				registerNode(wNode);
			}
		}

	}

	/**
	 * Register a node with the extension tracker.
	 * 
	 * @param node
	 *            register the given node and its subnodes with the extension
	 *            tracker
	 */
	private void registerNode(WorkbenchPreferenceNode node) {
		PlatformUI.getWorkbench().getExtensionTracker().registerObject(
				node.getConfigurationElement().getDeclaringExtension(), node,
				IExtensionTracker.REF_WEAK);
		IPreferenceNode[] subNodes = node.getSubNodes();
		for (int i = 0; i < subNodes.length; i++) {
			registerNode((WorkbenchPreferenceNode) subNodes[i]);
		}

	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.dynamicHelpers.IExtensionChangeHandler#addExtension(org.eclipse.core.runtime.dynamicHelpers.IExtensionTracker, org.eclipse.core.runtime.IExtension)
	 */
	@Override
	public void addExtension(IExtensionTracker tracker, IExtension extension) {
		IConfigurationElement[] elements = extension.getConfigurationElements();
		for (int i = 0; i < elements.length; i++) {
			WorkbenchPreferenceNode node = PreferencePageRegistryReader
					.createNode(elements[i]);
			if (node == null) {
				continue;
			}
			registerNode(node);
			String category = node.getCategory();
			if (category == null) {
				addToRoot(node);
			} else {
				IPreferenceNode parent = null;
				for (Iterator j = getElements(PreferenceManager.POST_ORDER)
						.iterator(); j.hasNext();) {
					IPreferenceNode element = (IPreferenceNode) j
							.next();
					if (category.equals(element.getId())) {
						parent = element;
						break;
					}
				}
				if (parent == null) {
					// Could not find the parent - log
					String message = "Invalid preference category path: " + category + " (bundle: " + node.getPluginId() + ", page: " + node.getId() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ 
					WorkbenchPlugin.log(StatusUtil.newStatus(IStatus.WARNING, message, null));
					addToRoot(node);
				} else {
					parent.add(node);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.dynamicHelpers.IExtensionAdditionHandler#getExtensionPointFilter()
	 */
	private IExtensionPoint getExtensionPointFilter() {
		return Platform.getExtensionRegistry().getExtensionPoint(
				PlatformUI.PLUGIN_ID, IWorkbenchRegistryConstants.PL_PREFERENCES);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.dynamicHelpers.IExtensionChangeHandler#removeExtension(org.eclipse.core.runtime.IExtension, java.lang.Object[])
	 */
	@Override
	public void removeExtension(IExtension extension, Object[] objects) {
		for (int i = 0; i < objects.length; i++) {
			if (objects[i] instanceof IPreferenceNode) {
				IPreferenceNode wNode = (IPreferenceNode) objects[i];
				wNode.disposeResources();
				deepRemove(getRoot(), wNode);
			}
		}
	}

	/**
	 * Removes the node from the manager, searching through all subnodes.
	 * 
	 * @param parent
	 *            the node to search
	 * @param nodeToRemove
	 *            the node to remove
	 * @return whether the node was removed
	 */
	private boolean deepRemove(IPreferenceNode parent,
			IPreferenceNode nodeToRemove) {
		if (parent == nodeToRemove) {
			if (parent == getRoot()) {
				removeAll(); // we're removing the root
				return true;
			}
		}

		if (parent.remove(nodeToRemove)) {
			return true;
		}

		IPreferenceNode[] subNodes = parent.getSubNodes();
		for (int i = 0; i < subNodes.length; i++) {
			if (deepRemove(subNodes[i], nodeToRemove)) {
				return true;
			}
		}
		return false;
	}

	private static class CSSThemeChangedHandler implements EventHandler {
		private PreferenceManager preferenceManager;
		private Map<String, IPreferenceStore> overriddenPropertyToStore = new HashMap<String, IPreferenceStore>();

		public CSSThemeChangedHandler(PreferenceManager preferenceManager) {
			this.preferenceManager = preferenceManager;
		}

		@Override
		public void handleEvent(Event event) {
			resetToDefaultPreferenceValues();
			IThemeEngine themeEngine = (IThemeEngine) event.getProperty(IThemeEngine.Events.THEME_ENGINE);
			overridePreferences(preferenceManager.getRootSubNodes(), themeEngine);
		}

		public void dispose() {
			resetToDefaultPreferenceValues();
		}

		private void overridePreferences(IPreferenceNode[] nodes, IThemeEngine themeEngine) {
			for (IPreferenceNode node : nodes) {
				PreferenceNode nodeOverridable = new PreferenceNode(node.getId());

				themeEngine.applyStyles(nodeOverridable, false);

				overridePreferences((org.eclipse.jface.preference.PreferenceNode) node,
						nodeOverridable.getOverriddenPreferences());

				IPreferenceNode[] subNodes = node.getSubNodes();
				if (subNodes != null && subNodes.length > 0) {
					overridePreferences(subNodes, themeEngine);
				}
			}
		}

		private void overridePreferences(org.eclipse.jface.preference.PreferenceNode node,
				Map<String, String> toOverride) {
			if (toOverride.isEmpty()) {
				return;
			}
			
			IPreferencePage page = node.getPage();
			if (page == null) {
				node.createPage();
				page = node.getPage();
				if (!(page instanceof PreferencePage)) {
					page.dispose();
					page = null;
				}
			}

			if (!(page instanceof PreferencePage)) {
				return;
			}

			IPreferenceStore store = ((PreferencePage) page).getPreferenceStore();
			for (Map.Entry<String, String> entry : toOverride.entrySet()) {
				if (!store.isDefault(entry.getKey())) {
					// preference has been overridden manually with the
					// preference dialog
					continue;
				}
				if (entry.getValue().length() > 0) {
					overriddenPropertyToStore.put(entry.getKey(), store);
					store.putValue(entry.getKey(), entry.getValue());
				} else {
					resetToDefaultPreferenceValue(store, entry.getKey());
				}
			}
		}

		// TODO: Perform more tests of it
		private void resetToDefaultPreferenceValues() {
			for (Map.Entry<String, IPreferenceStore> entry : overriddenPropertyToStore.entrySet()) {
				resetToDefaultPreferenceValue(entry.getValue(), entry.getKey());
			}
			overriddenPropertyToStore.clear();
		}

		private void resetToDefaultPreferenceValue(IPreferenceStore store, String preferenceName) {
			store.putValue(preferenceName, store.getDefaultString(preferenceName));
		}
	}
}
