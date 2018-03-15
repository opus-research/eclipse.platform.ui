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

package org.eclipse.e4.ui.databinding.service;

import org.eclipse.core.databinding.observable.ISideEffect;
import org.eclipse.core.databinding.observable.sideeffect.ISideEffectFactory;
import org.eclipse.e4.core.contexts.IContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.databinding.addon.SideEffectAddon;
import org.eclipse.e4.ui.databinding.factory.MPartSideEffects;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.osgi.service.component.annotations.Component;

/**
 * <p>
 * An {@link ISideEffectFactory} can be injected into a {@link MPart}.
 * </p>
 * <p>
 * The {@link ISideEffect} instances created by the injected
 * {@link ISideEffectFactory} are managed by the {@link SideEffectAddon}, which
 * automatically pauses, resumes and disposes the {@link ISideEffect} instances
 * of a {@link MPart}.
 * </p>
 *
 * <pre>
 * &#64;PostConstruct
 * public void createPart(Comppsite parent, ISideEffectFactory sideEffectFactory) {
 *
 * 	Button checkButton = // ...
 * 	Button enabledButton = // ...
 *
 * 	IObservableValue&lt;Boolean&gt; buttonSelectionObservable = WidgetProperties.selection().observe(checkButton);
 *
 * 	sideEffectFactory.create(() -> buttonSelectionObservable.getValue().booleanValue(), enabledButton::setEnabled);
 * }
 * </pre>
 *
 */
@Component(name = "SideEffectFactoryContextFunction", service = IContextFunction.class, property = "service.context.key=org.eclipse.core.databinding.observable.sideeffect.ISideEffectFactory")
public class SideEffectFactoryContextFunction implements IContextFunction {

	@Override
	public Object compute(IEclipseContext context, String contextKey) {
		MPart part = context.get(MPart.class);
		if (null == part) {
			// The ISideEffectFactory should only be requested by a MPart
			throw new IllegalStateException("The ISideEffectFactory is only supposed to be injected into MParts");
		}

		ISideEffectFactory sideEffectFactory = MPartSideEffects.createFactory(part);

		return sideEffectFactory;
	}
}
