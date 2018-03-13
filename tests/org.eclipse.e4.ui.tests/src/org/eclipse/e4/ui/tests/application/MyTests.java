/*******************************************************************************
 * Copyright (c) 2009, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.ui.tests.application;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.E4XMIResourceFactory;
import org.eclipse.e4.ui.internal.workbench.swt.E4Application;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.commands.MHandler;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarElement;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;

public class MyTests extends TestCase {

	public void xtest1() {

		MApplication application = loadModelFromFile("org.eclipse.e4.ui.tests/xmi/myEclipse1.e4xmi");

		EModelService modelService = getModelService(application);

		List<Object> result = modelService.findElements(application, null,
				null, null);
		System.out.println("All: " + result.size());

		List<MHandler> result2 = modelService.findElements(application, null,
				MHandler.class, null);
		System.out.println("MHandler: " + result2.size());

		System.out.println("MMenu: "
				+ modelService.findElements(application, null, MMenu.class,
						null).size());

		System.out.println("MMenuElement[remote]: "
				+ modelService.findElements(application, "remotes",
						MMenuElement.class, null).size());

		System.out.println("MToolBar: "
				+ modelService.findElements(application, null, MToolBar.class,
						null).size());

		System.out.println("MToolBarElement: "
				+ modelService.findElements(application, null,
						MToolBarElement.class, null).size());

		// assertEquals(elements3.size(), 0);
	}

	public void testXX() {
		System.out.println(MMenuElement.class.isAssignableFrom(MMenu.class));
		System.out.println(MMenu.class.isAssignableFrom(MMenuElement.class));
	}

	public void xtestTagOnly() {
		MApplication application = getModel();
		EModelService modelService = getModelService(application);

		List<String> tags = new ArrayList<String>();
		tags.add("x");

		long start = System.currentTimeMillis();
		List<MToolBar> result = modelService.findElements(application, null,
				null, tags);
		System.out.println(System.currentTimeMillis() - start);
		start = System.currentTimeMillis();

		start = System.currentTimeMillis();
		result = modelService.findElements(application, null, null, tags,
				EModelService.IN_ACTIVE_PERSPECTIVE);
		System.out.println(System.currentTimeMillis() - start);
		start = System.currentTimeMillis();

		start = System.currentTimeMillis();
		result = modelService.findElements(application, null, null, null);
		System.out.println(System.currentTimeMillis() - start);
		start = System.currentTimeMillis();

		System.out.println(result.size());

	}

	public void testFindToolbar() {
		MApplication application = getModel();
		EModelService modelService = getModelService(application);
		List<MToolBar> result = modelService.findElements(application, null,
				MToolBar.class, null);
		System.out.println(result.size());
		// for (MToolBar bar : result) {
		// System.out.println(bar.getClass().getSimpleName());
		// System.out.println(bar.getElementId());
		// }

		// assertEquals(1, result.size());
	}

	// public void testFindAllToolBars() {
	// MApplication application = getModel();
	// EModelService modelService = getModelService(application);
	// List<MToolBar> result = modelService.findElements(application, null,
	// MToolBar.class, null);
	// System.out.println(result.size()); //
	// assertEquals(1, result.size());
	// }

	public void testFindToolbarElement() {
		MApplication application = getModel();
		EModelService modelService = getModelService(application);
		List<MToolBarElement> result = modelService.findElements(application,
				null, MToolBarElement.class, null);
		System.out.println(result.size());
	}

	// public void testFindAllToolBarElements() {
	// MApplication application = getModel();
	// EModelService modelService = getModelService(application);
	// List<MToolBarElement> result = modelService.findElements(application,
	// null, MToolBarElement.class, null);
	// System.out.println(result.size());
	// }

	public void testFindMainMenu() {

	}

	/**
	 * @param application
	 * @return
	 */
	private EModelService getModelService(MApplication application) {
		EModelService modelService = (EModelService) application.getContext()
				.get(EModelService.class.getName());

		assertNotNull(modelService);
		return modelService;
	}

	private MApplication getModel() {
		return loadModelFromFile("org.eclipse.e4.ui.tests/xmi/ide.e4xmi");
	}

	private MApplication loadModelFromFile(String path) {
		URI uri = URI.createPlatformPluginURI(path, true);

		Resource resource = new E4XMIResourceFactory().createResource(uri);
		try {
			resource.load(null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			fail(e.getMessage());
		}

		MApplication application = (MApplication) resource.getContents().get(0);

		IEclipseContext applicationContext = E4Application
				.createDefaultContext();
		application.setContext(applicationContext);
		return application;
	}

}