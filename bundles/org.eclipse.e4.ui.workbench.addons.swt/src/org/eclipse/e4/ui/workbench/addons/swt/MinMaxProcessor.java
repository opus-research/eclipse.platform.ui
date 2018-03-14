/*******************************************************************************
 * Copyright (c) 2013, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 426553
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.addons.swt;

import java.util.List;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MAddon;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

/**
 * Adds the MinxMaxAddon to the application model. Registered via plugin.xml
 */

public class MinMaxProcessor {
	@Execute
	void addMinMaxAddon(MApplication app, EModelService modelService) {

		// search for existing add-on in the model
		List<MAddon> elements = modelService.findElements(app,
				"ui.workbench.addons.minmax.MinMaxAddon", MAddon.class, null); //$NON-NLS-1$
		// prevent multiple additions of the addon to the model
		if (elements.size() > 0) {
			return;
		}

		// insert the add-on into the model
		MAddon minMaxAddon = modelService.createModelElement(MAddon.class);
		minMaxAddon.setElementId("MinMaxAddon"); //$NON-NLS-1$
		minMaxAddon
				.setContributionURI("bundleclass://org.eclipse.e4.ui.workbench.addons.swt/org.eclipse.e4.ui.workbench.addons.minmax.MinMaxAddon"); //$NON-NLS-1$
		app.getAddons().add(minMaxAddon);
	}
}
