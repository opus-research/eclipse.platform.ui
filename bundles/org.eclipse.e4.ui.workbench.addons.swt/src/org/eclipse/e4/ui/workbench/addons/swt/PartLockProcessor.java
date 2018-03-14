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

package org.eclipse.e4.ui.workbench.addons.swt;

import java.util.List;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MAddon;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

/**
 * Model processors which adds the PartLock add-on to the application model
 */

public class PartLockProcessor {
	@Execute
	void addPartLockAddon(MApplication app, EModelService modelService) {
		List<MAddon> addons = app.getAddons();

		// prevent multiple copies
		for (MAddon addon : addons) {
			if (addon.getContributionURI().contains(
					"ui.workbench.addons.swt.partlockaddon.PartLockAddon")) //$NON-NLS-1$
				return;
		}

		// adds the add-on to the application model
		MAddon partLoackAddon = modelService.createModelElement(MAddon.class);
		partLoackAddon.setElementId("PartLockAddon"); //$NON-NLS-1$
		partLoackAddon
				.setContributionURI("bundleclass://org.eclipse.e4.ui.workbench.addons.swt/org.eclipse.e4.ui.workbench.addons.swt.partlockaddon.PartLockAddon"); //$NON-NLS-1$
		app.getAddons().add(partLoackAddon);

		System.out.println("PartLockProcessor has installed the PartLockAddon"); //$NON-NLS-1$
	}
}
