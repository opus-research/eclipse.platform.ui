/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.core.tests.internal.databinding.conversion;

import org.eclipse.core.internal.databinding.conversion.StringToNumberParser;


/**
 * @since 1.1
 */
public class StringToNumberParserShortTest extends StringToNumberParserTestHarness {

	/* (non-Javadoc)
	 * @see org.eclipse.core.tests.internal.databinding.conversion.StringToNumberParserTestHarness#assertValid(java.lang.Number)
	 */
	@Override
	protected boolean assertValid(Number number) {
		return StringToNumberParser.inShortRange(number);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.tests.internal.databinding.conversion.StringToNumberParserTestHarness#getValidMax()
	 */
	@Override
	protected Number getValidMax() {
		return new Short(Short.MAX_VALUE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.tests.internal.databinding.conversion.StringToNumberParserTestHarness#getValidMin()
	 */
	@Override
	protected Number getValidMin() {
		return new Short(Short.MIN_VALUE);
	}
}
