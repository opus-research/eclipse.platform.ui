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

package org.eclipse.e4.ui.workbench.renderers.swt;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.e4.ui.internal.workbench.swt.CSSConstants;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.internal.workbench.swt.PartRenderingEngine;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.impl.ApplicationFactoryImpl;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.impl.BasicFactoryImpl;
import org.eclipse.e4.ui.services.IStylingEngine;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Display;

public class StackRendererTest extends TestCase {
	private IEclipseContext context;
	private E4Workbench wb;
	private MPart part1;
	private MPart part2;
	private CTabItemStylingMethodsListener executedMethodsListener;
	private MPartStack partStack;

	@Override
	protected void setUp() throws Exception {
		context = E4Application.createDefaultContext();
		context.set(E4Workbench.PRESENTATION_URI_ARG,
				PartRenderingEngine.engineURI);

		MApplication application = ApplicationFactoryImpl.eINSTANCE
				.createApplication();
		MWindow window = BasicFactoryImpl.eINSTANCE.createWindow();
		partStack = BasicFactoryImpl.eINSTANCE.createPartStack();
		part1 = BasicFactoryImpl.eINSTANCE.createPart();
		part1.setLabel("some title");

		part2 = BasicFactoryImpl.eINSTANCE.createPart();
		part2.setLabel("some title2");

		application.getChildren().add(window);
		application.setSelectedElement(window);

		window.getChildren().add(partStack);
		partStack.getChildren().add(part1);
		partStack.getChildren().add(part2);

		application.setContext(context);
		context.set(MApplication.class.getName(), application);

		executedMethodsListener = new CTabItemStylingMethodsListener(part1);

		wb = new E4Workbench(application, context);
		wb.getContext().set(
				IStylingEngine.class,
				(IStylingEngine) Proxy.newProxyInstance(getClass()
						.getClassLoader(),
						new Class<?>[] { IStylingEngine.class },
						executedMethodsListener));

		wb.createAndRunUI(window);
		while (Display.getDefault().readAndDispatch())
			;
	}

	@Override
	protected void tearDown() throws Exception {
		if (wb != null) {
			wb.close();
		}
		context.dispose();
	}

	public void testTagsChangeHandlerWhenBusyTagAddEvent() throws Exception {
		part1.getTags().add(CSSConstants.CSS_BUSY_CLASS);

		assertEquals(1,
				executedMethodsListener
						.getMethodExecutionCount("setClassnameAndId(.+)"));
		assertTrue(executedMethodsListener
				.isMethodExecuted("setClassnameAndId(.+MPart "
						+ CSSConstants.CSS_BUSY_CLASS + ".+)"));
	}

	public void testTagsChangeHandlerWhenBusyTagRemoveEvent() throws Exception {
		part1.getTags().add(CSSConstants.CSS_BUSY_CLASS);
		part1.getTags().remove(CSSConstants.CSS_BUSY_CLASS);

		assertEquals(2,
				executedMethodsListener
						.getMethodExecutionCount("setClassnameAndId(.+)"));
		assertTrue(executedMethodsListener
				.isMethodExecuted("setClassnameAndId(.+MPart "
						+ CSSConstants.CSS_BUSY_CLASS + ".+)"));
		assertTrue(executedMethodsListener
				.isMethodExecuted("setClassnameAndId(.+MPart.+)"));
	}

	public void testTagsChangeHandlerWhenContentChangedOfNotActivePart()
			throws Exception {
		CTabFolder tabFolder = (CTabFolder) partStack.getWidget();
		tabFolder.setSelection(getTabItem(part2));

		part1.getTags().add(CSSConstants.CSS_CONTENT_CHANGE_CLASS);

		assertEquals(1,
				executedMethodsListener
						.getMethodExecutionCount("setClassnameAndId(.+)"));
		assertTrue(executedMethodsListener
				.isMethodExecuted("setClassnameAndId(.+MPart "
						+ CSSConstants.CSS_HIGHLIGHTED_CLASS + ".+)"));
	}

	public void testTagsChangeHandlerWhenContentChangedOfSelectedPart()
			throws Exception {
		CTabFolder tabFolder = (CTabFolder) partStack.getWidget();
		tabFolder.setSelection(getTabItem(part1));

		part1.getTags().add(CSSConstants.CSS_CONTENT_CHANGE_CLASS);

		assertEquals(1,
				executedMethodsListener
						.getMethodExecutionCount("setClassnameAndId(.+)"));
		assertTrue(executedMethodsListener
				.isMethodExecuted("setClassnameAndId(.+MPart.+)"));
	}

	public void testTagsChangeHandlerWhenContentChangeForPartAndItGetsActive()
			throws Exception {
		part1.getTags().add(CSSConstants.CSS_HIGHLIGHTED_CLASS);
		part1.getTags().add(CSSConstants.CSS_ACTIVE_CLASS);

		assertFalse(part1.getTags()
				.contains(CSSConstants.CSS_HIGHLIGHTED_CLASS));
		assertEquals(1,
				executedMethodsListener
						.getMethodExecutionCount("setClassnameAndId(.+)"));
		assertTrue(executedMethodsListener
				.isMethodExecuted("setClassnameAndId(.+MPart "
						+ CSSConstants.CSS_ACTIVE_CLASS + ".+)"));
	}

	public void testTagsChangeHandlerWhenNotSupportedTagModifiedEvent()
			throws Exception {
		part1.getTags().add("not supported tag");

		assertEquals(0,
				executedMethodsListener
						.getMethodExecutionCount("setClassnameAndId(.+)"));
	}

	public void testTagsChangeHandlerWhenNotTagReleatedEvent() throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put(UIEvents.EventTags.ELEMENT, part1);

		context.get(IEventBroker.class).send(
				UIEvents.ApplicationElement.TOPIC_ELEMENTID.replace(
						UIEvents.ALL_SUB_TOPICS, UIEvents.EventTypes.ADD),
				params);

		assertEquals(0,
				executedMethodsListener
						.getMethodExecutionCount("setClassnameAndId(.+)"));
	}

	// helper functions
	private static class CTabItemStylingMethodsListener implements
			InvocationHandler {
		private MPart part;
		private List<String> methods;

		public CTabItemStylingMethodsListener(MPart part) {
			this.part = part;
			methods = new ArrayList<String>();
		}

		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {
			if (isTabItemForPart(args[0])) {
				methods.add(String.format("%s(%s)", method.getName(),
						Arrays.toString(args)));
			}
			return null;
		}

		private boolean isTabItemForPart(Object obj) {
			return obj instanceof CTabItem
					&& part.getLabel().equals(((CTabItem) obj).getText());
		}

		public boolean isMethodExecuted(String methodPattern) {
			return getMethodExecutionCount(methodPattern) > 0;
		}

		public int getMethodExecutionCount(String methodPattern) {
			int result = 0;
			for (String method : methods) {
				if (method.matches(methodPattern)) {
					result++;
				}
			}
			return result;
		}
	}

	private CTabItem getTabItem(MPart part) {
		for (CTabItem tabItem : ((CTabFolder) partStack.getWidget()).getItems()) {
			if (part.getLabel().equals(tabItem.getText())) {
				return tabItem;
			}
		}

		throw new IllegalArgumentException(
				"No such CTabItem item for given part: " + part);
	}
}
