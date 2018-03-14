/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Louis-Michel Mathurin <mathurin.lm@agiledss.com> - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.workbench.addons.swt;

import java.util.List;
import org.eclipse.e4.ui.model.application.MAddon;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MApplicationElement;
import org.eclipse.e4.ui.model.application.MContribution;
import org.eclipse.e4.ui.workbench.Selector;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

/**
 *
 */
public class ProcessorUtil {

	/**
	 * @param app
	 * @param modelService
	 * @param URI
	 * @param id
	 */
	public static void addAddon(MApplication app, EModelService modelService, final String URI,
			final String id) {

		List<MAddon> addons = modelService.findElements(app, MAddon.class, EModelService.ANYWHERE,
				new Selector() {
					@Override
					public boolean select(MApplicationElement element) {

						if (!(element instanceof MContribution)) {
							return false;
						}

						MContribution contrib = (MContribution) element;

						return contrib.getContributionURI().equals(URI);
					}
				});

		// prevent multiple copies
		if (addons.size() > 0) {
			return;
		}

		// adds the add-on to the application model
		MAddon dndAddon = modelService.createModelElement(MAddon.class);
		dndAddon.setElementId(id);
		dndAddon.setContributionURI(URI);
		app.getAddons().add(dndAddon);
	}
}
