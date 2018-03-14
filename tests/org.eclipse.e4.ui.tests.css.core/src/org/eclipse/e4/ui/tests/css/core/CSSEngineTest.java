/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.tests.css.core;

import java.util.Date;

import junit.framework.TestCase;

import org.eclipse.e4.ui.css.core.dom.IElementProvider;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.impl.engine.CSSEngineImpl;
import org.eclipse.e4.ui.tests.css.core.util.TestElement;
import org.w3c.css.sac.SelectorList;
import org.w3c.dom.Element;

public class CSSEngineTest extends TestCase {
	private static class TestCSSEngine extends CSSEngineImpl {
		@Override
		public void reapply() {
		}
	}

	public void testSelectorMatch() throws Exception {
		TestCSSEngine engine = new TestCSSEngine();
		SelectorList list = engine.parseSelectors("Date");
		engine.setElementProvider(new IElementProvider() {
			@Override
			public Element getElement(Object element, CSSEngine engine) {
				return new TestElement(element.getClass().getSimpleName(),
						engine);
			}
		});
		assertFalse(engine.matches(list.item(0), new Object(), null));
		assertTrue(engine.matches(list.item(0), new Date(), null));
	}

	public void testSelectorMatchOneOf() throws Exception {
		TestCSSEngine engine = new TestCSSEngine();
		SelectorList list = engine.parseSelectors("E[a~='B']");

		engine.setElementProvider(new IElementProvider() {
			@Override
			public Element getElement(Object element, CSSEngine engine) {
				Element result = new TestElement("E", engine);
				result.setAttribute("a", "B ABC");
				return result;
			}
		});
		assertTrue(engine.matches(list.item(0), new Object(), null));

		engine.setElementProvider(new IElementProvider() {
			@Override
			public Element getElement(Object element, CSSEngine engine) {
				Element result = new TestElement("E", engine);
				result.setAttribute("a", "ABC B");
				return result;
			}
		});
		assertTrue(engine.matches(list.item(0), new Object(), null));

		engine.setElementProvider(new IElementProvider() {
			@Override
			public Element getElement(Object element, CSSEngine engine) {
				Element result = new TestElement("E", engine);
				result.setAttribute("a", "ABC");
				return result;
			}
		});
		assertFalse(engine.matches(list.item(0), new Object(), null));
	}
}
