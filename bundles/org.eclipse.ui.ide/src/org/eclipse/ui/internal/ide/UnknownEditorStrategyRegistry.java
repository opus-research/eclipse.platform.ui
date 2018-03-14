/*******************************************************************************
 * Copyright (c) 2015 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.ide;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.ide.IUnknownEditorStrategy;

/**
 * @since 3.12
 *
 */
public class UnknownEditorStrategyRegistry {

	private static final String EXTENSION_POINT_ID = IDEWorkbenchPlugin.IDE_WORKBENCH + ".unknownEditorStrategy"; //$NON-NLS-1$

	private static Map<String, String> idsToLabel;

	/**
	 * @param strategyId
	 *            The strategy to look for
	 * @return an instance of the strategy, or null if no strategy is found for
	 *         this id
	 */
	public static IUnknownEditorStrategy getStrategy(String strategyId) {
		if (strategyId == null) {
			return null;
		}
		IExtensionRegistry extRegistry = Platform.getExtensionRegistry();
		IConfigurationElement[] extensions = extRegistry.getConfigurationElementsFor(EXTENSION_POINT_ID);
		if (extensions != null) {
			for (IConfigurationElement extension : extensions) {
				if (strategyId.equals(readAttribute(extension, "id"))) { //$NON-NLS-1$
					try {
						return (IUnknownEditorStrategy) extension.createExecutableExtension("class"); //$NON-NLS-1$
					} catch (CoreException ex) {
						IDEWorkbenchPlugin.log(ex.getMessage(), ex);
						return null;
					}
				}
			}
		}
		return null;
	}

	private static String readAttribute(IConfigurationElement extension, String attribute) {
		String res = extension.getAttribute(attribute);
		if (res == null) {
			IDEWorkbenchPlugin.log("Missing attribute '" + attribute + "'id' for extension to " + EXTENSION_POINT_ID //$NON-NLS-1$ //$NON-NLS-2$
					+ " contributed by " + extension.getContributor().getName()); //$NON-NLS-1$
		}
		return res;
	}

	/**
	 * @return
	 */
	public static Set<String> getAllStrategies() {
		idsToLabel = new HashMap<>();
		IExtensionRegistry extRegistry = Platform.getExtensionRegistry();
		IConfigurationElement[] extensions = extRegistry.getConfigurationElementsFor(EXTENSION_POINT_ID);
		if (extensions != null) {
			for (IConfigurationElement extension : extensions) {
				String id = readAttribute(extension, "id"); //$NON-NLS-1$
				String label = readAttribute(extension, "label"); //$NON-NLS-1$
				if (id != null) {
					idsToLabel.put(id, label);
				}
			}
		}
		return idsToLabel.keySet();
	}

	/**
	 * @param id
	 * @return
	 */
	public static String getLabel(String id) {
		if (!idsToLabel.containsKey(id)) {
			// populate cache
			getAllStrategies();
		}
		if (idsToLabel.containsKey(id)) {
			return idsToLabel.get(id);
		}
		return null;
	}

}
