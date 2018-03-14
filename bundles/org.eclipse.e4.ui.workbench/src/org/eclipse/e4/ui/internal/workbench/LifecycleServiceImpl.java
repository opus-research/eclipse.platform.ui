/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench;

import java.lang.annotation.Annotation;
import javax.inject.Inject;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.contributions.IContributionFactory;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.MLifecycleAware;
import org.eclipse.e4.ui.model.application.MLifecycleContribution;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.workbench.modeling.ELifecycleService;
import org.eclipse.e4.ui.workbench.modeling.EModelService;

/**
 *
 */
public class LifecycleServiceImpl implements ELifecycleService {

	private final EModelService modelService;
	private final IContributionFactory factory;
	private final MApplication application;

	@Inject
	public LifecycleServiceImpl(EModelService modelService, IContributionFactory factory,
			MApplication application) {
		this.modelService = modelService;
		this.factory = factory;
		this.application = application;
	}

	public boolean process(Class<? extends Annotation> annotation, MLifecycleAware element,
			IEclipseContext localContext) {
		IEclipseContext context = modelService.getContainingContext((MUIElement) element);
		if (context == null)
			context = application.getContext();

		// First iterate over the local LC handlers
		for (MLifecycleContribution c : element.getLifeCycleHandler()) {
			if (!runLCHandler(c, annotation, element, context, localContext))
				return false;
		}

		// Now iterate over the 'global' LC handlers
		for (MLifecycleContribution c : application.getLifeCycleHandler()) {
			if (!runLCHandler(c, annotation, element, context, localContext))
				return false;
		}

		return true;
	}

	private boolean runLCHandler(MLifecycleContribution c, Class<? extends Annotation> annotation,
			MLifecycleAware element, IEclipseContext context, IEclipseContext localContext) {
		if (c.getObject() == null) {
			c.setObject(factory.create(c.getContributionURI(), context));
		}

		Object o = ContextInjectionFactory.invoke(c.getObject(), annotation, context, localContext,
				Boolean.TRUE);
		if (o instanceof Boolean) {
			return (Boolean) o;
		}

		return true;
	}
}
