/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.e4.ui.internal.workbench.swt;

import static org.eclipse.e4.ui.css.swt.CSSSWTConstants.CSS_CLASS_NAME_KEY;
import static org.eclipse.e4.ui.css.swt.CSSSWTConstants.CSS_ENGINE_KEY;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class CSSRenderingUtilsTest extends TestCase {
	private Shell shell;
	private CSSRenderingUtils utils;
	private CSSEngine engine;
	private CSSEngine originalEngine;
	private List<String> executedEngineMethodNames = new ArrayList<String>();

	@Override
	protected void setUp() throws Exception {
		shell = Display.getDefault().getActiveShell();
		utils = new CSSRenderingUtils();

		engine = (CSSEngine) Proxy.newProxyInstance(
				getClass().getClassLoader(),
				new Class<?>[] { CSSEngine.class }, new InvocationHandler() {
					public Object invoke(Object proxy, Method method,
							Object[] args) throws Throwable {
						executedEngineMethodNames.add(method.getName());
						return null;
					}
				});
		originalEngine = (CSSEngine) shell.getDisplay().getData(CSS_ENGINE_KEY);
		shell.getDisplay().setData(CSS_ENGINE_KEY, engine);
	}

	@Override
	protected void tearDown() throws Exception {
		shell.getDisplay().setData(CSS_ENGINE_KEY, originalEngine);
	}

	public void testAddCSSClass() throws Exception {
		CTabItem tabItem = new CTabItem(new CTabFolder(shell, 0), 0);
		tabItem.setData(CSS_CLASS_NAME_KEY, "someClass otherClass");

		boolean result = utils.addCSSClass(tabItem, "newClass");

		assertTrue(result);
		assertEquals("someClass otherClass newClass",
				tabItem.getData(CSS_CLASS_NAME_KEY));
		assertEquals(1, executedEngineMethodNames.size());
		assertTrue(executedEngineMethodNames.contains("applyStyles"));

		tabItem.dispose();
	}

	public void testAddCSSClassWhenAddedClassAlreadySet() throws Exception {
		CTabItem tabItem = new CTabItem(new CTabFolder(shell, 0), 0);
		tabItem.setData(CSS_CLASS_NAME_KEY, "someClass newClass");

		boolean result = utils.addCSSClass(tabItem, "newClass");

		assertTrue(result);
		assertEquals("someClass newClass", tabItem.getData(CSS_CLASS_NAME_KEY));
		assertEquals(0, executedEngineMethodNames.size());

		tabItem.dispose();
	}

	public void testAddCSSClassWhenNoCSSStylingForWidget() throws Exception {
		CTabItem tabItem = new CTabItem(new CTabFolder(shell, 0), 0);

		boolean result = utils.addCSSClass(tabItem, "newClass");

		assertFalse(result);
		assertNull(tabItem.getData(CSS_CLASS_NAME_KEY));
		assertEquals(0, executedEngineMethodNames.size());

		tabItem.dispose();
	}

	public void testAddCSSClassWhenNotCSSEngine() throws Exception {
		CTabItem tabItem = new CTabItem(new CTabFolder(shell, 0), 0);
		tabItem.setData(CSS_CLASS_NAME_KEY, "someClass");
		shell.getDisplay().setData(CSS_ENGINE_KEY, null);

		boolean result = utils.addCSSClass(tabItem, "newClass");

		assertFalse(result);
		assertEquals("someClass", tabItem.getData(CSS_CLASS_NAME_KEY));
		assertEquals(0, executedEngineMethodNames.size());

		tabItem.dispose();
	}

	public void testRemoveCSSClass() throws Exception {
		CTabItem tabItem = new CTabItem(new CTabFolder(shell, 0), 0);
		tabItem.setData(CSS_CLASS_NAME_KEY, "someClass newClass otherClass");

		boolean result = utils.removeCSSClass(tabItem, "newClass");

		assertTrue(result);
		assertEquals("someClass otherClass",
				tabItem.getData(CSS_CLASS_NAME_KEY));
		assertEquals(1, executedEngineMethodNames.size());
		assertTrue(executedEngineMethodNames.contains("applyStyles"));

		tabItem.dispose();
	}

	public void testRemoveCSSClassWhenClassAddedIsNotPresent() throws Exception {
		CTabItem tabItem = new CTabItem(new CTabFolder(shell, 0), 0);
		tabItem.setData(CSS_CLASS_NAME_KEY, "someClass otherClass");

		boolean result = utils.removeCSSClass(tabItem, "newClass");

		assertTrue(result);
		assertEquals("someClass otherClass",
				tabItem.getData(CSS_CLASS_NAME_KEY));
		assertEquals(0, executedEngineMethodNames.size());

		tabItem.dispose();
	}

	public void testRemoveCSSClassWhenNotCSSEngine() throws Exception {
		CTabItem tabItem = new CTabItem(new CTabFolder(shell, 0), 0);
		tabItem.setData(CSS_CLASS_NAME_KEY, "someClass");
		shell.getDisplay().setData(CSS_ENGINE_KEY, null);

		boolean result = utils.removeCSSClass(tabItem, "newClass");

		assertFalse(result);
		assertEquals("someClass", tabItem.getData(CSS_CLASS_NAME_KEY));
		assertEquals(0, executedEngineMethodNames.size());

		tabItem.dispose();
	}
}
