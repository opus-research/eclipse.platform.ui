/*******************************************************************************
 * Copyright (c) 2017 Simeon Andreev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simeon Andreev - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.tests.multipageeditor;

import java.io.ByteArrayInputStream;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.tests.harness.util.UITestCase;

/**
 * Validates that opening a multi-page editor results in a focused editor.
 *
 * @since 3.5
 *
 */
public class OpenMultiPageEditorTest extends UITestCase {

	public static class MultiPageEditorForTest extends MultiPageEditorPart {

		static final String EDITOR_ID = "org.eclipse.ui.tests.multipageeditor.OpenMultiPageEditorTest.MultiPageEditorForTest"; //$NON-NLS-1$

		private EditorForTest editor;

		public MultiPageEditorForTest() {
		}

		private AtomicBoolean focus = new AtomicBoolean(false);

		@Override
		protected void createPages() {
			editor = new EditorForTest();
			try {
				addPage(editor, getEditorInput());
			} catch (PartInitException e) {
				throw new AssertionError(e);
			}

			class FocusListener extends FocusAdapter {
				@Override
				public void focusGained(FocusEvent e) {
					focus.set(true);
				}

				@Override
				public void focusLost(FocusEvent e) {
					focus.set(false);
				}
			}

			StyledText textWidget = editor.getTextWidget();
			textWidget.addFocusListener(new FocusListener());
		}

		boolean hasFocus() {
			return focus.get();
		}

		@Override
		public void doSave(IProgressMonitor monitor) {
			// nothing to do
		}

		@Override
		public void doSaveAs() {
			// nothing to do
		}

		@Override
		public boolean isSaveAsAllowed() {
			return false;
		}
	}

	private static final String PROJECT_NAME = OpenMultiPageEditorTest.class.getSimpleName();

	private IFile testFile;

	public OpenMultiPageEditorTest(String testName) {
		super(testName);
	}

	@Override
	protected void doSetUp() throws Exception {
		super.doSetUp();

		IWorkspace workspace = ResourcesPlugin.getWorkspace();

		IProject testProject = workspace.getRoot().getProject(PROJECT_NAME);
		if (!testProject.exists()) {
			testProject.create(null);
		}
		testProject.open(null);

		testFile = testProject.getFile("testFile.txt"); //$NON-NLS-1$
		testFile.create(new ByteArrayInputStream("".getBytes()), true, null);
	}

	@Override
	protected void doTearDown() throws Exception {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject testProject = workspace.getRoot().getProject(PROJECT_NAME);
		if (testProject.exists()) {
			testProject.delete(true, true, null);
		}

		super.doTearDown();
	}

	public void testOpenedEditorHasFocus() throws Exception {
		IWorkbenchWindow activeWorkbenchWindow = openTestWindow();
		IWorkbenchPage page = activeWorkbenchWindow.getActivePage();
		IEditorPart part = IDE.openEditor(page, testFile, MultiPageEditorForTest.EDITOR_ID);

		processEvents();
		processUiEvents();

		MultiPageEditorForTest multiPageEditor = (MultiPageEditorForTest) part;
		assertTrue("multi-page editor doesn't have focus after opening", multiPageEditor.hasFocus());
	}

	private void processUiEvents() {
		while (fWorkbench.getDisplay().readAndDispatch()) {
		}
	}

	private static class EditorForTest extends TextEditor {
		public StyledText getTextWidget() {
			return getSourceViewer().getTextWidget();
		}
	}
}
