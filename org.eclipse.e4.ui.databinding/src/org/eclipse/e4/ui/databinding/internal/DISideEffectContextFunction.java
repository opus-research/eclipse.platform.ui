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

import org.eclipse.core.databinding.observable.sideeffect.CompositeSideEffect;
import org.eclipse.core.databinding.observable.sideeffect.ISideEffectFactory;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IContextFunction;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.osgi.service.component.annotations.Component;

/**
 * <p>
 * An {@link ISideEffectFactory} can be injected into a {@link MPart}.
 * </p>
 * <p>
 * The {@link ISideEffect} instances created by the injected
 * {@link ISideEffectFactory} are managed by the {@link DISideEffectLifeCycle}
 * class, which automatically pauses, resumes and disposes the
 * {@link ISideEffect} instances of a {@link MPart}.
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
@Component(service = IContextFunction.class, property = "service.context.key=org.eclipse.core.databinding.observable.sideeffect.ISideEffectFactory")
public class DISideEffectContextFunction implements IContextFunction {

	public static final String PART_USES_ISIDEEFFECT_FACTORY = "partUsesISideEffectFactory";

	@Override
	public Object compute(IEclipseContext context, String contextKey) {
		MPart part = context.get(MPart.class);
		if (null == part) {
			// The ISideEffectFactory should only be requested by a MPart
			throw new IllegalStateException("The ISideEffectFactory is only supposed to be injected into MParts");
		}

		ISideEffectFactory sideEffectFactory = getFactory(part);

		return sideEffectFactory;
	}

	/**
	 * Creates an {@link ISideEffectFactory} where the lifeCycle of created
	 * {@link ISideEffect} instances will be handled by the
	 * {@link SideEffectAddon}.
	 *
	 * @param part
	 *            {@link MPart} for which the {@link ISideEffectFactory} should
	 *            be used.
	 * @return ISideEffectFactory
	 */
	public static ISideEffectFactory getFactory(MPart part) {
		CompositeSideEffect compositeSideEffect = part.getContext().get(CompositeSideEffect.class);
		if (compositeSideEffect == null) {
			CompositeSideEffect newCompositeSideEffect = new CompositeSideEffect();
			part.getTags().add(PART_USES_ISIDEEFFECT_FACTORY);
			part.getContext().set(CompositeSideEffect.class, newCompositeSideEffect);
			// let the DISideEffectLifeCycle handle the life cycle of the
			// CompositeSideEffect
			ContextInjectionFactory.make(DISideEffectLifeCycle.class, part.getContext());

			compositeSideEffect = newCompositeSideEffect;
		}
		return ISideEffectFactory.createFactory(compositeSideEffect::add);
	}

}
