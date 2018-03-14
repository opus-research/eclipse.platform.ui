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

package org.eclipse.ui.internal.e4.migration;

import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;

/**
 * @since 3.5
 *
 */
public interface IModelBuilderFactory {

	ApplicationBuilder createApplicationBuilder(WorkbenchMementoReader reader);

	WindowBuilder createWindowBuilder(WindowReader windowReader);

	PerspectiveBuilder createPerspectiveBuilder(PerspectiveReader perspReader,
			MPerspectiveStack perspStack, MWindow window);

	PerspectiveBuilder createPerspectiveBuilder(PerspectiveReader perspReader);

}
