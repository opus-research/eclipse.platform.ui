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

import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.internal.databinding.conversion.StringToByteConverter;
import org.eclipse.core.internal.databinding.validation.StringToByteValidator;

import com.ibm.icu.text.NumberFormat;

/**
 * @since 1.1
 */
public class StringToByteValidatorTest extends
		StringToNumberValidatorTestHarness {

	protected Number getInRangeNumber() {
		return new Byte(Byte.MAX_VALUE);
	}

	protected String getInvalidString() {
		return "1.1";
	}

	protected Number getOutOfRangeNumber() {
		return new Integer(Byte.MAX_VALUE + 1);
	}

	protected NumberFormat setupNumberFormat() {
		return NumberFormat.getIntegerInstance();
	}

	protected IValidator setupValidator(NumberFormat numberFormat) {
		StringToByteConverter converter = StringToByteConverter.toByte(numberFormat, false);
		return new StringToByteValidator(converter);
	}
}
