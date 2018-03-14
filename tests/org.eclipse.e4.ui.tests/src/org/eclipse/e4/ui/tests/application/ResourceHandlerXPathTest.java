/*******************************************************************************
 * Copyright (c) 2010, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Steven Spungin <steven@spungin.tv> - Bug 437958
 ******************************************************************************/

package org.eclipse.e4.ui.tests.application;

import java.util.List;
import org.eclipse.e4.ui.internal.workbench.ResourceHandler;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MMenuElement;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;

public class ResourceHandlerXPathTest extends ResourceHandlerTest {

	public void testXPathModelProcessor() {

		URI uri = URI.createPlatformPluginURI("org.eclipse.e4.ui.tests/xmi/modelprocessor/base.e4xmi", true);
		ResourceHandler handler = createHandler(uri);
		Resource resource = handler.loadMostRecentModel();
		MApplication application = (MApplication) resource.getContents().get(0);
		assertNotNull(application);

		/**
		 * We will now test the various ways an element can be contributed to
		 * multiple parents. ModelFragments.e4mi has been configured to add 2
		 * menus to the Main Menu. These menus will receive our test
		 * contributions.
		 */
		MMenu mainMenu = application.getChildren().get(0).getMainMenu();
		assertNotNull(mainMenu);
		MMenu menu1 = (MMenu) findByElementId(mainMenu.getChildren(), "fragment.contributedMenu1");
		assertNotNull(menu1);
		MMenu menu2 = (MMenu) findByElementId(mainMenu.getChildren(), "fragment.contributedMenu2");
		assertNotNull(menu2);
		// Method 1 - comma separated list of parentElementIds
		assertNotNull(findByElementId(menu1.getChildren(), "fragment.contributedMenuItem.csv"));
		assertNotNull(findByElementId(menu2.getChildren(), "fragment.contributedMenuItem.csv"));
		// Method 2 - xpath
		assertNotNull(findByElementId(menu1.getChildren(), "fragment.contributedMenuItem.xpath"));
		assertNotNull(findByElementId(menu2.getChildren(), "fragment.contributedMenuItem.xpath"));
	}

	/**
	 * @param children
	 * @param id
	 * @return the MMenuElement or null if not found
	 */
	private Object findByElementId(List<MMenuElement> children, String id) {
		for (MMenuElement item : children) {
			if (id.equals(item.getElementId())) {
				return item;
			}
		}
		return null;
	}

}
