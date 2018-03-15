/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.api;

import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IWorkbenchWindow;

public class MockWorkbenchWindowActionDelegate extends MockActionDelegate
		implements IActionDelegate2 {
    public static MockWorkbenchWindowActionDelegate lastDelegate;

    public static String SET_ID = "org.eclipse.ui.tests.api.MockActionSet";

    public static String ID = "org.eclipse.ui.tests.api.MockWindowAction";

    /**
     * Constructor for MockWorkbenchWindowActionDelegate
     */
    public MockWorkbenchWindowActionDelegate() {
        super();
        lastDelegate = this;
    }

    @Override
	public void init(IWorkbenchWindow window) {
        callHistory.add("init");
    }

    @Override
	public void dispose() {
        callHistory.add("dispose");
    }

	@Override
	public void init(IAction action) {
		callHistory.add("init");
	}

	@Override
	public void runWithEvent(IAction action, Event event) {
		callHistory.add("runWithEvent");
	}
}

