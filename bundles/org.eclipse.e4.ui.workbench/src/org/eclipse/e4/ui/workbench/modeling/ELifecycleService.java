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

package org.eclipse.e4.ui.workbench.modeling;

import java.lang.annotation.Annotation;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MLifecycleAware;

/**
 * @since 1.1
 *
 */
public interface ELifecycleService {
	/**
	 * Process the given event (as defined by the annotation) by iterating through all the
	 * MLifeCycleContribution elements for the given element as well as iterating through the
	 * contributions from the MApplication.
	 * <p>
	 * 
	 * </p
	 * ?
	 * 
	 * @param annotation
	 *            The class specifying the annotation to use
	 * @param element
	 *            The element containing the contributions
	 * @param localContext
	 *            The context to use to invoke the
	 * @return
	 */
	public boolean process(Class<? extends Annotation> annotation, MLifecycleAware element,
			IEclipseContext localContext);
}
