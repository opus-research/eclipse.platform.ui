/*******************************************************************************
 * Copyright (c) 2008, 2015 Angelo Zerr and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *     EclipseSource - revision
 *     IBM Corporation - ongoing development
 *******************************************************************************/
package org.eclipse.e4.ui.css.core.impl.dom;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.eclipse.e4.ui.css.core.dom.CSSProperty;
import org.eclipse.e4.ui.css.core.dom.CSSPropertyList;
import org.w3c.dom.css.CSSStyleDeclaration;
import org.w3c.dom.css.CSSValue;


/**
 * CSS computed style which concatenate list of CSSComputedStyleImpl to manage
 * styles coming from Condition Selector (ex : Label#MyId) and other selectors
 * (ex : Label).
 */
public class CSSComputedStyleImpl extends CSSStyleDeclarationImpl implements CSSStyleDeclaration {

	private List<StyleWrapper> styleRules;

	public CSSComputedStyleImpl(List<StyleWrapper> styleRules) {
		super(null);
		this.styleRules = styleRules;
		// TODO [rst] Optimize: A list of StyleWrapper instances could be sorted
		// only once after reading the stylesheet(s).
		Collections.sort( this.styleRules, StyleWrapper.COMPARATOR );
		Iterator<StyleWrapper> iterator = this.styleRules.iterator();
		while (iterator.hasNext()) {
			StyleWrapper styleWrapper = iterator.next();
			addCSSPropertyList(((CSSStyleDeclarationImpl) styleWrapper.style).getCSSPropertyList());
		}
	}

	private void addCSSPropertyList(CSSPropertyList properties) {
		int length = properties.getLength();
		for (int i = 0; i < length; i++) {
			CSSProperty property = properties.item(i);

			CSSValue value = property.getValue();
			String name = property.getName();
			if ("preferences".equals(name) && value instanceof CSSValueListImpl) {
				CSSValueListImpl valueList = (CSSValueListImpl) value;
				CSSProperty myProperty = findProperty(name);
				if (myProperty == null) {
					super.addProperty(property);
				} else {
					CSSValue myValue = myProperty.getValue();
					if (myValue instanceof CSSValueListImpl) {
						CSSValueListImpl myValueList = (CSSValueListImpl) myValue;
						myValueList.addAll(valueList);
					} else {
						super.removeProperty(name);
						super.addProperty(property);
					}

				}
			} else {
				super.removeProperty(name);
				super.addProperty(property);
			}
		}
	}

	private CSSProperty findProperty(String name) {
		if (name == null) {
			return null;
		}
		CSSPropertyList list = this.getCSSPropertyList();
		for (int i = 0; i < list.getLength(); i++) {
			CSSProperty item = list.item(i);
			if (name.equals(item.getName())) {
				return item;
			}
		}
		return null;
	}
}
