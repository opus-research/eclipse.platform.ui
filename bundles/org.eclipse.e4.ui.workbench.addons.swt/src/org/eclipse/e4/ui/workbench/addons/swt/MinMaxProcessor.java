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
 *     Louis-Michel Mathurin <mathurin.lm@agiledss.com> - Bug 432146
 *******************************************************************************/
package org.eclipse.e4.ui.workbench.addons.swt;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

/**
 * Model processors which adds the MinMax add-on to the application model
 */
public class MinMaxProcessor {
	@Execute
	void addMinMaxAddon(MApplication app, EModelService modelService) {
		final String contributionURI = "bundleclass://org.eclipse.e4.ui.workbench.addons.swt/org.eclipse.e4.ui.workbench.addons.minmax.MinMaxAddon"; //$NON-NLS-1$
		final String id = "MinMaxAddon"; //$NON-NLS-1$

		ProcessorUtil.addAddon(app, modelService, contributionURI, id);
	}
}
