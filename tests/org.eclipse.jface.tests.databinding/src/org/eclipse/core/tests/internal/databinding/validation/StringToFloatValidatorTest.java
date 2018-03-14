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

package org.eclipse.core.tests.internal.databinding.validation;

import org.eclipse.core.databinding.conversion.StringToNumberConverter;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.internal.databinding.validation.StringToFloatValidator;

import com.ibm.icu.text.NumberFormat;

/**
 * @since 1.1
 */
public class StringToFloatValidatorTest extends
		StringToNumberValidatorTestHarness {

	protected Number getInRangeNumber() {
		return new Float(1);
	}

	protected String getInvalidString() {
		return "1a";
	}

	protected Number getOutOfRangeNumber() {
		return new Double(Double.MAX_VALUE);
	}

	protected NumberFormat setupNumberFormat() {
		return NumberFormat.getInstance();
	}

	protected IValidator setupValidator(NumberFormat numberFormat) {
		StringToNumberConverter converter = StringToNumberConverter.toFloat(numberFormat, false);
		return new StringToFloatValidator(converter);
	}
}
