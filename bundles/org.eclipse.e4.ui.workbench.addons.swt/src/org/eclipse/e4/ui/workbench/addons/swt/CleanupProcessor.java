/*******************************************************************************
 * Copyright (c) 2013, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 429421
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.addons.swt;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

/**
 * Model processors which adds the cleanup add-on to the application model
 */
public class CleanupProcessor {
	@Execute
	void addCleanupAddon(MApplication app, EModelService modelService) {
		final String contributionURI = "bundleclass://org.eclipse.e4.ui.workbench.addons.swt/org.eclipse.e4.ui.workbench.addons.cleanupaddon.CleanupAddon"; //$NON-NLS-1$
		final String id = "CleanupAddon"; //$NON-NLS-1$

		ProcessorUtil.addAddon(app, modelService, contributionURI, id);
	}
}
