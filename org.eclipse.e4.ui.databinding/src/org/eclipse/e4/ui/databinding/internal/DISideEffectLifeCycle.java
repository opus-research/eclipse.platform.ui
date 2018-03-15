/*******************************************************************************
 * Copyright (c) 2016 vogella GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz <simon.scholz@vogella.com> - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.ui.databinding.internal;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.core.databinding.observable.sideeffect.CompositeSideEffect;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.IWorkbench;

/**
 * This class is supposed to be created with an {@link IEclipseContext} of a
 * {@link MPart}.
 * 
 * <pre>
 * ContextInjectionFactory.make(DISideEffectLifeCycle.class, part.getContext());
 * </pre>
 * 
 * It disposes, pauses and resumes a given {@link CompositeSideEffect} according
 * to the state of the {@link MPart}.
 */
public class DISideEffectLifeCycle {

	private CompositeSideEffect compositeSideEffect;

	@Inject
	public DISideEffectLifeCycle(CompositeSideEffect compositeSideEffect) {
		this.compositeSideEffect = compositeSideEffect;
	}

	@PreDestroy
	public void dispose() {
		compositeSideEffect.dispose();
	}

	@Inject
	public void checkPartOnTop(@Optional @Named(IWorkbench.ON_TOP) Boolean onTop) {
		if (onTop.booleanValue()) {
			compositeSideEffect.resumeAndRunIfDirty();
		} else {
			compositeSideEffect.pause();
		}
	}
}
