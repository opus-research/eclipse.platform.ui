/*******************************************************************************
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Martin Koci <Martin.Kocicak.Koci@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.utils;

public class SharedStringBuilder extends ThreadLocal<StringBuilder> {

	@Override
	protected StringBuilder initialValue() {
		return new StringBuilder();
	}

	@Override
	public StringBuilder get() {
		StringBuilder sb = super.get();
		sb.setLength(0);
		return sb;
	}
}