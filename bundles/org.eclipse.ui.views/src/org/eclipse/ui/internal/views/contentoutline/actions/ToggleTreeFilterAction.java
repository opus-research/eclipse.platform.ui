/*******************************************************************************
 * Copyright (c) 2015 Denis Zygann and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Denis Zygann - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal.views.contentoutline.actions;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.e4.ui.dialogs.filteredtree.FilteredTree;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.internal.views.properties.PropertiesMessages;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * Class to create a toggle action, which shows/hides the filter control from a
 * {@link FilteredTree}.
 *
 */
@SuppressWarnings("restriction")
public class ToggleTreeFilterAction extends Action {

    /**
     * The ID of the filter action.
     */
    public static final String TOGGLE_FILTER_TREE_ACTION_ID = "toggleFilterTreeActionId"; //$NON-NLS-1$

    private FilteredTree filteredTree;

    /**
     * Creates a ToggleTreeFilterAction.
     *
     * @param filteredTree
     *            {@link FilteredTree}, which contains the filter control
     */
    public ToggleTreeFilterAction(FilteredTree filteredTree) {
        this.filteredTree = filteredTree;
        Bundle bundle = FrameworkUtil.getBundle(getClass());
        URL filterIconUrl = FileLocator.find(bundle, new Path("icons/full/elcl16/filter_ps.png"), null); //$NON-NLS-1$
        setImageDescriptor(ImageDescriptor.createFromURL(filterIconUrl));
        setToolTipText(PropertiesMessages.ShowFilterTextToggle_toolTip);
        setId(TOGGLE_FILTER_TREE_ACTION_ID);
    }

    @Override
    public void run() {
        if (filteredTree != null || !filteredTree.isDisposed()) {
            filteredTree.setShowFilterControls(isChecked());
        }
    }

    /**
     * @return Returns the filteredTree.
     */
	public FilteredTree getFilteredTree() {
        return filteredTree;
    }

    /**
     * @param filteredTree
     *            The filteredTree to set.
     */
    public void setFilteredTree(FilteredTree filteredTree) {
        this.filteredTree = filteredTree;
    }
}
