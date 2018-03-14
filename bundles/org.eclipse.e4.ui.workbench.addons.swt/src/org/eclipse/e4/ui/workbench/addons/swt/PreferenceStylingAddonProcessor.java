/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.addons.swt;

import java.util.List;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MAddon;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

public class PreferenceStylingAddonProcessor {
	@Execute
	void addCleanupAddon(MApplication app, EModelService modelService) {
		List<MAddon> addons = app.getAddons();
		for (MAddon addon : addons) {
			if (addon.getContributionURI().contains(
					"ui.workbench.addons.preferencestylingaddon.PreferenceStylingAddon")) { //$NON-NLS-1$
				return;
			}
		}

		MAddon addon = modelService.createModelElement(MAddon.class);
		addon.setElementId("PreferenceStylingAddon"); //$NON-NLS-1$
		addon.setContributionURI("bundleclass://org.eclipse.e4.ui.workbench.addons.swt/org.eclipse.e4.ui.workbench.addons.preferencestylingaddon.PreferenceStylingAddon"); //$NON-NLS-1$
		app.getAddons().add(addon);
	}
}
