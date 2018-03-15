/*******************************************************************************
 * Copyright (c) 2006, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.tests.commands;

import java.util.Collection;

import org.eclipse.core.expressions.EvaluationResult;
import org.eclipse.core.expressions.Expression;
import org.eclipse.core.expressions.ExpressionInfo;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.ui.ISources;

public class ActiveActionSetExpression extends Expression {
	private String actionSetId;

	private String[] expressionInfo;

	public ActiveActionSetExpression(String id) {
		this(id, new String[] { ISources.ACTIVE_ACTION_SETS_NAME });
	}

	public ActiveActionSetExpression(String id, String[] info) {
		actionSetId = id;
		expressionInfo = info;
	}

	@Override
	public void collectExpressionInfo(ExpressionInfo info) {
		for (String element : expressionInfo) {
			info.addVariableNameAccess(element);
		}
	}

	@Override
	public EvaluationResult evaluate(IEvaluationContext context) {
		final Object variable = context
				.getVariable(ISources.ACTIVE_ACTION_SETS_NAME);
		if (variable != null) {
			if (((Collection<?>) variable).contains(actionSetId)) {
				return EvaluationResult.TRUE;
			}
		}
		return EvaluationResult.FALSE;
	}

}