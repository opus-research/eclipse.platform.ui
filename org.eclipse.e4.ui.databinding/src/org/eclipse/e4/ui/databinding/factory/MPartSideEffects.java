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
import org.eclipse.e4.ui.model.application.ui.basic.MPart;

/**
 * A factory for the creation of an {@link ISideEffectFactory}, which is used to
 * create {@link ISideEffect} objects for a {@link MPart}.
 *
 */
public final class MPartSideEffects {

	private MPartSideEffects() {
	}

	/**
	 * Creates an {@link ISideEffectFactory} which will dispose all created
	 * {@link ISideEffect} instances automatically in case the given
	 * {@link MPart} is not rendered any more.
	 *
	 * @param part
	 *            {@link MPart} where the {@link ISideEffectFactory} should be
	 *            used.
	 * @return ISideEffectFactory
	 */
	public static ISideEffectFactory createFactory(MPart part) {
		ICompositeSideEffect compositeSideEffect = part.getContext().get(ICompositeSideEffect.class);
		if (compositeSideEffect == null) {
			ICompositeSideEffect newCompositeSideEffect = ICompositeSideEffect.create();
			part.getContext().set(ICompositeSideEffect.class, newCompositeSideEffect);
			compositeSideEffect = newCompositeSideEffect;
		}
		return ISideEffectFactory.createFactory(compositeSideEffect::add);
	}

}
