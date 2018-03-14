/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Alexander Kuppe, Versant GmbH - bug 215797
 *     Sascha Zak - bug 282874
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 440810, 440136
 *******************************************************************************/

package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.internal.e4.compatibility.ModeledPageLayout;
import org.eclipse.ui.internal.registry.ActionSetRegistry;
import org.eclipse.ui.internal.registry.IActionSetDescriptor;
import org.eclipse.ui.internal.registry.PerspectiveDescriptor;

/**
 *
 */
public class Perspective {

	private final PerspectiveDescriptor descriptor;
	private final WorkbenchPage page;
	private final List<IActionSetDescriptor> alwaysOnActionSets;
	private final List<IActionSetDescriptor> alwaysOffActionSets;
	private final MPerspective layout;

	/**
	 * @param desc
	 *            can be null
	 * @param layout
	 *            non null
	 * @param page
	 *            non null
	 */
	public Perspective(PerspectiveDescriptor desc, MPerspective layout, WorkbenchPage page) {
		this.page = page;
		this.layout = layout;
		descriptor = desc;
		alwaysOnActionSets = new ArrayList<IActionSetDescriptor>(2);
		alwaysOffActionSets = new ArrayList<IActionSetDescriptor>(2);
	}

	public void initActionSets() {
		if (descriptor != null) {
			List<IActionSetDescriptor> temp = new ArrayList<IActionSetDescriptor>();
			List<String> ids = ModeledPageLayout.getIds(layout, ModeledPageLayout.ACTION_SET_TAG);
			createInitialActionSets(temp, ids);
			for (IActionSetDescriptor descriptor : temp) {
				if (!alwaysOnActionSets.contains(descriptor)) {
					alwaysOnActionSets.add(descriptor);
				}
			}
		}

	}

	/**
	 * Create the initial list of action sets.
	 */
	protected void createInitialActionSets(List<IActionSetDescriptor> outputList, List<String> stringList) {
		ActionSetRegistry reg = WorkbenchPlugin.getDefault().getActionSetRegistry();
		for (String id : stringList) {
			IActionSetDescriptor desc = reg.findActionSet(id);
			if (desc != null) {
				outputList.add(desc);
			} else {
				// plugin with actionSet was removed
				// we remember then so it's available when added back
			}
		}
	}

	/**
	 * Dispose the perspective and all views contained within.
	 */
	public void dispose() {
	}

	/**
	 * Returns the perspective.
	 *
	 * @return can return null!
	 */
	public IPerspectiveDescriptor getDesc() {
		return descriptor;
	}

	/**
	 * Returns the new wizard shortcuts associated with this perspective.
	 *
	 * @return an array of new wizard identifiers
	 */
	public String[] getNewWizardShortcuts() {
		return page.getNewWizardShortcuts();
	}

	/**
	 * Returns the perspective shortcuts associated with this perspective.
	 *
	 * @return an array of perspective identifiers
	 */
	public String[] getPerspectiveShortcuts() {
		return page.getPerspectiveShortcuts();
	}

	/**
	 * Returns the ids of the parts to list in the Show In... dialog. This is a
	 * List of Strings.
	 *
	 * @return non null list of strings
	 */
	public List<?> getShowInPartIds() {
		return page.getShowInPartIds();
	}

	/**
	 * Returns the show view shortcuts associated with this perspective.
	 *
	 * @return an array of view identifiers
	 */
	public String[] getShowViewShortcuts() {
		return page.getShowViewShortcuts();
	}

	private void removeAlwaysOn(IActionSetDescriptor descriptor) {
		if (descriptor == null) {
			return;
		}
		boolean removed = alwaysOnActionSets.remove(descriptor);
		if (removed) {
			page.perspectiveActionSetChanged(this, descriptor, ActionSetManager.CHANGE_HIDE);
		}
	}

	protected void addAlwaysOff(IActionSetDescriptor descriptor) {
		if (descriptor == null) {
			return;
		}
		if (alwaysOffActionSets.contains(descriptor)) {
			return;
		}
		alwaysOffActionSets.add(descriptor);
		page.perspectiveActionSetChanged(this, descriptor, ActionSetManager.CHANGE_MASK);
		removeAlwaysOn(descriptor);
	}

	protected void addAlwaysOn(IActionSetDescriptor descriptor) {
		if (descriptor == null) {
			return;
		}
		if (alwaysOnActionSets.contains(descriptor)) {
			return;
		}
		alwaysOnActionSets.add(descriptor);
		page.perspectiveActionSetChanged(this, descriptor, ActionSetManager.CHANGE_SHOW);
		removeAlwaysOff(descriptor);
	}

	private void removeAlwaysOff(IActionSetDescriptor descriptor) {
		if (descriptor == null) {
			return;
		}
		boolean removed = alwaysOffActionSets.remove(descriptor);
		if (removed) {
			page.perspectiveActionSetChanged(this, descriptor, ActionSetManager.CHANGE_UNMASK);
		}
	}

	/**
	 * Returns the ActionSets read from perspectiveExtensions in the registry.
	 */
	protected List<?> getPerspectiveExtensionActionSets() {
		if (descriptor == null) {
			return Collections.emptyList();
		}
		return page.getPerspectiveExtensionActionSets(descriptor.getOriginalId());
	}

	public void turnOnActionSets(IActionSetDescriptor[] newArray) {
		for (IActionSetDescriptor descriptor : newArray) {
			addActionSet(descriptor);
		}
	}

	public void turnOffActionSets(IActionSetDescriptor[] toDisable) {
		for (IActionSetDescriptor descriptor : toDisable) {
			turnOffActionSet(descriptor);
		}
	}

	public void turnOffActionSet(IActionSetDescriptor toDisable) {
		removeActionSet(toDisable);
	}

	// for dynamic UI
	protected void addActionSet(IActionSetDescriptor newDesc) {
		IContextService service = page.getWorkbenchWindow().getService(IContextService.class);
		try {
			service.deferUpdates(true);
			for (IActionSetDescriptor desc : alwaysOnActionSets) {
				if (desc.getId().equals(newDesc.getId())) {
					removeAlwaysOn(desc);
					removeAlwaysOff(desc);
					break;
				}
			}
			addAlwaysOn(newDesc);
			final String actionSetID = newDesc.getId();

			// Add Tags
			String tag = ModeledPageLayout.ACTION_SET_TAG + actionSetID;
			if (!layout.getTags().contains(tag)) {
				layout.getTags().add(tag);
			}
		} finally {
			service.deferUpdates(false);
		}
	}

	// for dynamic UI
	protected void removeActionSet(IActionSetDescriptor toRemove) {
		String id = toRemove.getId();
		IContextService service = page.getWorkbenchWindow().getService(IContextService.class);
		try {
			service.deferUpdates(true);
			for (IActionSetDescriptor desc : alwaysOnActionSets) {
				if (desc.getId().equals(id)) {
					removeAlwaysOn(desc);
					break;
				}
			}

			for (IActionSetDescriptor desc : alwaysOffActionSets) {
				if (desc.getId().equals(id)) {
					removeAlwaysOff(desc);
					break;
				}
			}
			addAlwaysOff(toRemove);
			// remove tag
			String tag = ModeledPageLayout.ACTION_SET_TAG + id;
			layout.getTags().remove(tag);
		} finally {
			service.deferUpdates(false);
		}
	}

	public IActionSetDescriptor[] getAlwaysOnActionSets() {
		return alwaysOnActionSets.toArray(new IActionSetDescriptor[alwaysOnActionSets.size()]);
	}

	public IActionSetDescriptor[] getAlwaysOffActionSets() {
		return alwaysOffActionSets.toArray(new IActionSetDescriptor[alwaysOffActionSets.size()]);
	}

	public void updateActionBars() {
		page.getActionBars().getMenuManager().updateAll(true);
		page.resetToolBarLayout();
	}

}
