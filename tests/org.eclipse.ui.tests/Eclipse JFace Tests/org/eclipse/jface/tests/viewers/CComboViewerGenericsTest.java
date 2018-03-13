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
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Composite;

/**
 * This is the generic version of the CComboViewerTest class
 * @see org.eclipse.jface.tests.viewers.CComboViewerTest
 */
public class CComboViewerGenericsTest extends StructuredViewerGenericsTest {
	public CComboViewerGenericsTest(String name) {
		super(name);
	}

	protected StructuredViewer<TestElement, TestElement> createViewer(
			Composite parent) {
		CCombo cCombo = new CCombo(parent, SWT.READ_ONLY | SWT.BORDER);
		ComboViewer<TestElement, TestElement> viewer = new ComboViewer<TestElement, TestElement>(cCombo);
		viewer.setContentProvider(new TestModelContentProviderGenerics());
		return viewer;
	}

	protected int getItemCount() {
		TestElement first = fRootElement.getFirstChild();
		CCombo list = (CCombo) fViewer.testFindItem(first);
		return list.getItemCount();
	}

	protected String getItemText(int at) {
		CCombo list = (CCombo) fViewer.getControl();
		return list.getItem(at);
	}

	public static void main(String args[]) {
		junit.textui.TestRunner.run(CComboViewerGenericsTest.class);
	}

	/**
	 * TODO: Determine if this test is applicable to ComboViewer
	 */
	public void testInsertChild() {

	}
}
