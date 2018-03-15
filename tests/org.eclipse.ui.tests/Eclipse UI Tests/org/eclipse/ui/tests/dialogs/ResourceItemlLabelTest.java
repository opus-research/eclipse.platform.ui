/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Lucas Bullen - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.dialogs;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.FilteredResourcesSelectionDialog;
import org.eclipse.ui.dialogs.FilteredResourcesSelectionDialog.ResourceItemLabelProvider;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * Tests that resources are highlighted to match user search input. See Bug
 * 519525, 520250, and 520251 for references.
 *
 * @since 3.0
 */
public class ResourceItemlLabelTest extends UITestCase {

    /**
	 * Constructs a new instance of <code>ResourceItemlLabelTest</code>.
	 *
	 * @param name
	 *            The name of the test to be run.
	 */
    public ResourceItemlLabelTest(String name) {
        super(name);
    }

	private IProject project;

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();
		project = ResourcesPlugin.getWorkspace().getRoot()
				.getProject(getClass().getName() + "_" + System.currentTimeMillis());
		project.create(new NullProgressMonitor());

		project.open(new NullProgressMonitor());
	}

	/**
	 * Tests that the highlighting matches basic substrings
	 *
	 * @throws Exception
	 */
	public void testSubstringMatch() {
		Position[] atBeginning = { new Position(0, 2) };
		testStyleRanges(atBeginning, getStyleRanges("te", "test.txt"));

		Position[] inMiddle = { new Position(1, 2) };
		testStyleRanges(inMiddle, getStyleRanges("es", "test.txt"));

		Position[] atEnd = { new Position(5, 3) };
		testStyleRanges(atEnd, getStyleRanges("txt", "test.txt"));

		Position[] duplicate = { new Position(0, 1) };
		testStyleRanges(duplicate, getStyleRanges("t", "test.txt"));

		Position[] full = { new Position(0, 8) };
		testStyleRanges(full, getStyleRanges("test.txt", "test.txt"));
	}

	/**
	 * Tests that the highlighting matches CamelCase searches
	 *
	 * @throws Exception
	 */
	public void testCamelCaseMatch() {
		Position[] atBeginning = { new Position(0, 1), new Position(4, 1) };
		testStyleRanges(atBeginning, getStyleRanges("TT", "ThisTest.txt"));

		Position[] nextToEachother = { new Position(0, 1), new Position(4, 2) };
		testStyleRanges(nextToEachother, getStyleRanges("TAT", "ThisATest.txt"));

		Position[] withSubstrings = { new Position(0, 2), new Position(4, 2) };
		testStyleRanges(withSubstrings, getStyleRanges("ThTe", "ThisTest.txt"));
	}

	/**
	 * Tests that the highlighting matches searches using '*' and '?'
	 */
	public void testPatternMatch() {
		Position[] questionMark = { new Position(0, 1), new Position(2, 2) };
		testStyleRanges(questionMark, getStyleRanges("t?st", "test.txt"));

		Position[] star = { new Position(0, 1), new Position(6, 2) };
		testStyleRanges(star, getStyleRanges("t*xt", "test.txt"));

		Position[] both = { new Position(0, 1), new Position(2, 1), new Position(6, 2) };
		testStyleRanges(both, getStyleRanges("t?s*xt", "test.txt"));
	}

	/**
	 * Tests that the highlighting matches extension searches
	 */
	public void testExtensionMatch() {
		Position[] basic = { new Position(9, 2) };
		testStyleRanges(basic, getStyleRanges("MF", "MANIFEST.MF"));

		Position[] withSubstring = { new Position(0, 1), new Position(8, 3) };
		testStyleRanges(withSubstring, getStyleRanges("M.MF", "MANIFEST.MF"));
	}

	private void testStyleRanges(Position[] expected, StyleRange[] actual) {
		assertEquals("Length of StyleRanges is incorrect: " + printStyleRanges(actual), expected.length, actual.length);
		for (int i = 0; i < actual.length; i++) {
			assertEquals("Start of StyleRange at index " + i + " is incorrect.", expected[i].offset, actual[i].start);
			assertEquals("Length of StyleRange at index " + i + " is incorrect.", expected[i].length, actual[i].length);
		}
	}

	private String printStyleRanges(StyleRange[] styleRanges) {
		if (styleRanges == null) {
			return "null";
		}
		if (styleRanges.length == 0) {
			return "[]";
		}
		StringBuilder builder = new StringBuilder();
		builder.append('[');
		for (StyleRange range : styleRanges) {
			builder.append("{start: ");
			builder.append(range.start);
			builder.append(", length: ");
			builder.append(range.length);
			builder.append("}, ");
		}
		builder.setLength(builder.length() - 2);
		builder.append(']');
		return builder.toString();
	}

	private StyleRange[] getStyleRanges(String searchString, String fileName) {
		Shell windowShell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		FilteredResourcesSelectionDialog dialog = new FilteredResourcesSelectionDialog(windowShell, true, project,
				IResource.FILE);
		dialog.setInitialPattern(searchString);
		dialog.create();

		ResourceItemLabelProvider labelProvider = dialog.new ResourceItemLabelProvider();
		StyledString styledString = labelProvider.getStyledText(project.getFile(fileName));
		return styledString.getStyleRanges();
	}

	@Override
	protected void doTearDown() throws Exception {
		if (project != null) {
			project.delete(true, null);
		}
		super.doTearDown();
	}
}
