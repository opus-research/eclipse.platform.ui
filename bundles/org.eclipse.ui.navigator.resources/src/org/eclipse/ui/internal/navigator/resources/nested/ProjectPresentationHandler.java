/*******************************************************************************
 * Copyright (c) 2014 Red Hat Inc., and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - initial API and implementation
 ******************************************************************************/
package org.eclipse.ui.internal.navigator.resources.nested;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.navigator.resources.plugin.WorkbenchNavigatorPlugin;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.ICommonFilterDescriptor;
import org.eclipse.ui.navigator.INavigatorFilterService;

public class ProjectPresentationHandler extends AbstractHandler {
	
	private static final String NEST_PARAMETER = WorkbenchNavigatorPlugin.PLUGIN_ID + ".nestedested.enabled"; //$NON-NLS-1$

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (part instanceof CommonNavigator) {
			CommonNavigator navigator = (CommonNavigator)part;
			boolean previousNest = navigator.getNavigatorContentService().getActivationService().isNavigatorExtensionActive(NestedProjectsContentProvider.EXTENSION_ID);
			String newNestParam = event.getParameter(ProjectPresentationHandler.NEST_PARAMETER);
			boolean newNest = false;
			if (newNestParam != null) {
				newNest = Boolean.parseBoolean(newNestParam);
			}
			if (newNest != previousNest) {
				WorkbenchNavigatorPlugin.getDefault().getPreferenceStore().setValue(ProjectPresentationHandler.NEST_PARAMETER, newNest);
				INavigatorFilterService filterService = navigator.getNavigatorContentService().getFilterService();
				Set<String> filters = new HashSet<String>();
				for (ICommonFilterDescriptor desc : filterService.getVisibleFilterDescriptors()) {
					if (filterService.isActive(desc.getId())) {
						filters.add(desc.getId());
					}
				}
				if (newNest) {
					navigator.getNavigatorContentService().getActivationService().activateExtensions(new String[] { NestedProjectsContentProvider.EXTENSION_ID }, false);
					filters.add(HideTopLevelProjectIfNested.EXTENSION_ID);
					filters.add(HideFolderWhenProjectIsShownAsNested.EXTENTSION_ID);
				} else {
					navigator.getNavigatorContentService().getActivationService().deactivateExtensions(new String[] { NestedProjectsContentProvider.EXTENSION_ID }, false);
					filters.remove(HideTopLevelProjectIfNested.EXTENSION_ID);
					filters.remove(HideFolderWhenProjectIsShownAsNested.EXTENTSION_ID);
				}
				filterService.activateFilterIdsAndUpdateViewer(filters.toArray(new String[filters.size()]));
				filterService.persistFilterActivationState();
				navigator.getNavigatorContentService().getActivationService().persistExtensionActivations();
				navigator.getCommonViewer().refresh();
			}

			HandlerUtil.updateRadioState(event.getCommand(), Boolean.toString(newNest));
			// TODO refresh selection
			return Boolean.valueOf(newNest);
		}
		
		return false;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

}
