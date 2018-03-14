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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.e4.ui.css.core.dom.ElementAdapter;
import org.eclipse.e4.ui.css.core.dom.IElementProvider;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.impl.engine.CSSEngineImpl;
import org.eclipse.e4.ui.tests.css.core.util.TestElement;
import org.junit.Test;
import org.w3c.css.sac.SelectorList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class CSSEngineTest {
	private static class TestCSSEngine extends CSSEngineImpl {
		public void setWidgetProvider(Class clazz, IElementProvider provider) {
			widgetsMap.put(clazz.getName(), provider);
		}

		@Override
		public void reapply() {
		}
	}

	public abstract static class BaseElement extends ElementAdapter {
		public BaseElement(Object object, CSSEngine engine) {
			super(object, engine);
		}

		@Override
		public Node getParentNode() {
			return null;
		}

		@Override
		public NodeList getChildNodes() {
			return null;
		}

		@Override
		public String getNamespaceURI() {
			return null;
		}

		@Override
		public String getCSSId() {
			return null;
		}

		@Override
		public String getCSSClass() {
			return null;
		}

		@Override
		public String getCSSStyle() {
			return null;
		}

		@Override
		public String getLocalName() {
			return null;
		}

		@Override
		public String getAttribute(String arg0) {
			return null;
		}
	}

	public static class CollectionElement extends BaseElement {
		public CollectionElement(Collection control, CSSEngine engine) {
			super(control, engine);
		}
	}

	public static class SetElement extends BaseElement {
		public SetElement(Set object, CSSEngine engine) {
			super(object, engine);
		}
	}

	@Test
	public void testBug363053() {
		TestCSSEngine engine = new TestCSSEngine();
		// must be class not interface
		engine.setWidgetProvider(AbstractCollection.class,
				new IElementProvider() {
			@Override
			public Element getElement(Object element, CSSEngine engine) {
				return new CollectionElement((Collection) element, engine);
			}
		});
		engine.setWidgetProvider(AbstractSet.class, new IElementProvider() {
			@Override
			public Element getElement(Object element, CSSEngine engine) {
				return new SetElement((Set) element, engine);
			}
		});

		// in bug 363053, the first widget-based element provider is
		// set as the engine's configured element provider, causing
		// second assertion to fail
		Element element = engine.getElement(new ArrayList());
		assertTrue(element instanceof CollectionElement);

		element = engine.getElement(new HashSet());
		assertTrue(element instanceof SetElement);
	}

	@Test
	public void testSelectorMatch() throws IOException {
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

}
