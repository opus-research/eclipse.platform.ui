/*******************************************************************************
 * Copyright (c) 2013 Hendrik Still and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Hendrik Still<hendrik.still@gammas.de> - initial implementation
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

/**
 * This is the generic version of the StructuredViewerTest class
 * @see org.eclipse.jface.tests.viewers.StructuredViewerTest
 */
public class ComboViewerGenericsTest extends StructuredViewerGenericsTest {
    public ComboViewerGenericsTest(String name) {
        super(name);
    }

    protected StructuredViewer<TestElement,TestElement> createViewer(Composite parent) {
        ComboViewer<TestElement,TestElement> viewer = new ComboViewer<TestElement,TestElement>(parent);
        viewer.setContentProvider(new TestModelContentProviderGenerics());
        return viewer;
    }

    protected int getItemCount() {
        TestElement first = fRootElement.getFirstChild();
        Combo list = (Combo) fViewer.testFindItem(first);
        return list.getItemCount();
    }

    protected String getItemText(int at) {
        Combo list = (Combo) fViewer.getControl();
        return list.getItem(at);
    }

    public static void main(String args[]) {
        junit.textui.TestRunner.run(ComboViewerGenericsTest.class);
    }

    /**
     * TODO: Determine if this test is applicable to ComboViewer 
     */
    public void testInsertChild() {

    }
}
