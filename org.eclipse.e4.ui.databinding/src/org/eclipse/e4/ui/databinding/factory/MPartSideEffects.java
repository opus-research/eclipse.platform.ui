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

package org.eclipse.e4.ui.databinding.factory;

import org.eclipse.core.databinding.observable.ISideEffect;
import org.eclipse.core.databinding.observable.sideeffect.ICompositeSideEffect;
import org.eclipse.core.databinding.observable.sideeffect.ISideEffectFactory;
import org.eclipse.e4.ui.databinding.addon.SideEffectAddon;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;

/**
 * A factory for the creation of an {@link ISideEffectFactory}, which is used to
 * create {@link ISideEffect} objects for a {@link MPart}.
 *
 * @see SideEffectAddon
 *
 */
public final class MPartSideEffects {

	public static final String PART_USES_ISIDEEFFECT_FACTORY = "partUsesISideEffectFactory";

	private MPartSideEffects() {
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
	public static ISideEffectFactory createFactory(MPart part) {
		ICompositeSideEffect compositeSideEffect = part.getContext().get(ICompositeSideEffect.class);
		if (compositeSideEffect == null) {
			ICompositeSideEffect newCompositeSideEffect = ICompositeSideEffect.create();
			part.getTags().add(PART_USES_ISIDEEFFECT_FACTORY);
			part.getContext().set(ICompositeSideEffect.class, newCompositeSideEffect);
			compositeSideEffect = newCompositeSideEffect;
		}
		return ISideEffectFactory.createFactory(compositeSideEffect::add);
	}

}
