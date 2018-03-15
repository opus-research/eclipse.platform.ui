/*******************************************************************************
 * Copyright (c) 2016 vogella GmbH and others. All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Simon Scholz <simon.scholz@vogella.com> - initial API and
 * implementation
 ******************************************************************************/

package org.eclipse.e4.ui.tests.workbench;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.e4.ui.internal.workbench.PartOnTopManager;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.internal.workbench.swt.PartRenderingEngine;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MContext;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.swt.widgets.Display;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;
import org.osgi.service.log.LogService;

/**
 * This test class is used to validate the correctness of the
 * {@link PartOnTopManager}.
 */
public class PartOnTopManagerTest {

	protected IEclipseContext appContext;
	protected E4Workbench wb;
	private EModelService ems;

	private boolean logged = false;

	private LogListener listener = new LogListener() {
		@Override
		public void logged(LogEntry entry) {
			if (!logged) {
				logged = entry.getLevel() == LogService.LOG_ERROR;
			}
		}
	};

	@Before
	public void setUp() throws Exception {
		logged = false;
		appContext = E4Application.createDefaultContext();
		appContext.set(IWorkbench.PRESENTATION_URI_ARG, PartRenderingEngine.engineURI);

		final Display d = Display.getDefault();
		appContext.set(Realm.class, DisplayRealm.getRealm(d));
		appContext.set(UISynchronize.class, new UISynchronize() {
			@Override
			public void syncExec(Runnable runnable) {
				d.syncExec(runnable);
			}

			@Override
			public void asyncExec(Runnable runnable) {
				d.asyncExec(runnable);
			}
		});

		LogReaderService logReaderService = appContext.get(LogReaderService.class);
		logReaderService.addLogListener(listener);
		ems = appContext.get(EModelService.class);
	}

	@After
	public void tearDown() throws Exception {
		LogReaderService logReaderService = appContext.get(LogReaderService.class);
		logReaderService.removeLogListener(listener);
		if (wb != null) {
			wb.close();
		}
		appContext.dispose();
	}

	private boolean isPartOnTop(MContext context) {
		if (context.getContext() != null) {
			Object object = context.getContext().get(IWorkbench.ON_TOP);
			return Boolean.TRUE.equals(object);
		}

		return false;
	}

	@Test
	public void test_PartOnTop() {
		MApplication application = ems.createModelElement(MApplication.class);
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPart part = ems.createModelElement(MPart.class);
		part.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		window.getChildren().add(part);
		window.setSelectedElement(part);

		application.setContext(appContext);
		appContext.set(MApplication.class, application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		assertTrue(isPartOnTop(part));
	}

	@Test
	public void test_PlaceholderOnTop() {
		MApplication application = ems.createModelElement(MApplication.class);
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);

		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPart part = ems.createModelElement(MPart.class);
		part.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		window.getSharedElements().add(part);

		MPlaceholder placeholder = ems.createModelElement(MPlaceholder.class);
		placeholder.setRef(part);

		perspective.getChildren().add(placeholder);
		perspective.setSelectedElement(placeholder);

		application.setContext(appContext);
		appContext.set(MApplication.class, application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		assertTrue(isPartOnTop(part));
	}

	@Test
	public void test_PartOnTopStackSwitch() {
		MApplication application = ems.createModelElement(MApplication.class);
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		window.getChildren().add(partStack);
		window.setSelectedElement(partStack);

		MPart part = ems.createModelElement(MPart.class);
		part.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		partStack.getChildren().add(part);
		partStack.setSelectedElement(part);
		MPart secondPart = ems.createModelElement(MPart.class);
		secondPart.setContributionURI(
				"bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		partStack.getChildren().add(secondPart);

		application.setContext(appContext);
		appContext.set(MApplication.class, application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		assertTrue(isPartOnTop(part));
		assertFalse(isPartOnTop(secondPart));

		partStack.setSelectedElement(secondPart);

		assertFalse(isPartOnTop(part));
		assertTrue(isPartOnTop(secondPart));
	}

	@Test
	public void test_PlaceholderOnTopStackSwitch() {
		MApplication application = ems.createModelElement(MApplication.class);
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);

		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		perspective.getChildren().add(partStack);
		perspective.setSelectedElement(partStack);

		MPart part = ems.createModelElement(MPart.class);
		part.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		window.getSharedElements().add(part);

		MPlaceholder placeholder = ems.createModelElement(MPlaceholder.class);
		placeholder.setRef(part);

		partStack.getChildren().add(placeholder);
		partStack.setSelectedElement(placeholder);
		MPart secondPart = ems.createModelElement(MPart.class);
		secondPart.setContributionURI(
				"bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		partStack.getChildren().add(secondPart);

		application.setContext(appContext);
		appContext.set(MApplication.class, application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		assertTrue(isPartOnTop(part));
		assertFalse(isPartOnTop(secondPart));

		partStack.setSelectedElement(secondPart);


		assertFalse(isPartOnTop(part));
		assertTrue(isPartOnTop(secondPart));

		partStack.setSelectedElement(placeholder);

		assertTrue(isPartOnTop(part));
		assertFalse(isPartOnTop(secondPart));

	}

	@Test
	public void test_PartOnTopPerspectiveSwitch() {
		MApplication application = ems.createModelElement(MApplication.class);
		MWindow window = ems.createModelElement(MWindow.class);
		application.getChildren().add(window);
		application.setSelectedElement(window);

		MPerspectiveStack perspectiveStack = ems.createModelElement(MPerspectiveStack.class);
		window.getChildren().add(perspectiveStack);

		MPerspective perspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(perspective);
		perspectiveStack.setSelectedElement(perspective);

		MPerspective secondPerspective = ems.createModelElement(MPerspective.class);
		perspectiveStack.getChildren().add(secondPerspective);

		MPartStack partStack = ems.createModelElement(MPartStack.class);
		perspective.getChildren().add(partStack);
		perspective.setSelectedElement(partStack);

		MPart part = ems.createModelElement(MPart.class);
		part.setContributionURI("bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		partStack.getChildren().add(part);
		partStack.setSelectedElement(part);
		MPart secondPart = ems.createModelElement(MPart.class);
		secondPart.setContributionURI(
				"bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		partStack.getChildren().add(secondPart);

		MPart secondPerspectivePart = ems.createModelElement(MPart.class);
		secondPerspectivePart.setContributionURI(
				"bundleclass://org.eclipse.e4.ui.tests/org.eclipse.e4.ui.tests.workbench.SampleView");
		secondPerspective.getChildren().add(secondPerspectivePart);

		application.setContext(appContext);
		appContext.set(MApplication.class, application);

		wb = new E4Workbench(application, appContext);
		wb.createAndRunUI(window);

		assertTrue(isPartOnTop(part));
		assertFalse(isPartOnTop(secondPart));

		EPartService partService = window.getContext().get(EPartService.class);
		partService.switchPerspective(secondPerspective);

		assertFalse(isPartOnTop(part));
		assertFalse(isPartOnTop(secondPart));

		assertTrue(isPartOnTop(secondPerspectivePart));
	}
}
