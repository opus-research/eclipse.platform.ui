/*******************************************************************************
 * Copyright (c) 2007, 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Marc-Andre Laperle (Ericsson) - Bug 413278
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 497618, 368977
 ******************************************************************************/

package org.eclipse.ui.internal;

import java.util.List;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.ui.workbench.swt.internal.copy.SearchPattern;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.commands.ICommandService;

/**
 * Shows a list of open editor and parts in the current or last active workbook.
 *
 * @since 3.4
 *
 */
public class WorkbookEditorsHandler extends FilteredTableBaseHandler {

	/**
	 *
	 */
	private static final String ORG_ECLIPSE_UI_WINDOW_OPEN_EDITOR_DROP_DOWN = "org.eclipse.ui.window.openEditorDropDown"; //$NON-NLS-1$

	@Override
	protected Object getInput(WorkbenchPage page) {
		List<EditorReference> refs = page.getSortedEditorReferences();
		return refs;
	}

	@Override
	protected boolean isFiltered() {
		return true;
	}

	private SearchPattern searchPattern;

	SearchPattern getMatcher() {
		return searchPattern;
	}

	@Override
	protected void setMatcherString(String pattern) {
		if (pattern.length() == 0) {
			searchPattern = null;
		} else {
			SearchPattern patternMatcher = new SearchPattern();
			patternMatcher.setPattern("*" + pattern); //$NON-NLS-1$
			searchPattern = patternMatcher;
		}
	}

	@Override
	protected ViewerFilter getFilter() {
		return new ViewerFilter() {
			@Override
			public boolean select(Viewer viewer, Object parentElement, Object element) {
				SearchPattern matcher = getMatcher();
				if (matcher == null || !(viewer instanceof TableViewer)) {
					return true;
				}
				String matchName = null;
				if (element instanceof EditorReference) {
					matchName = ((EditorReference) element).getTitle();
				}
				if (matchName == null) {
					return false;
				}
				return matcher.matches(matchName);
			}
		};
	}

	@Override
	protected ColumnLabelProvider getColumnLabelProvider() {
		return new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof EditorReference) {
					EditorReference er = ((EditorReference) element);
					if (er.isDirty()) {
						return "*" + er.getTitle(); //$NON-NLS-1$
					}
					return er.getTitle();
				}
				return super.getText(element);
			}

			@Override
			public Image getImage(Object element) {
				if (element instanceof EditorReference) {
					return ((EditorReference) element).getTitleImage();
				}
				return super.getImage(element);
			}

			@Override
			public String getToolTipText(Object element) {
				if (element instanceof EditorReference) {
					return ((EditorReference) element).getTitleToolTip();
				}
				return super.getToolTipText(element);
			};
		};
	}

	@Override
	protected ParameterizedCommand getBackwardCommand() {
		return null;
	}

	@Override
	protected ParameterizedCommand getForwardCommand() {
		final ICommandService commandService = window.getWorkbench().getService(ICommandService.class);
		final Command command = commandService.getCommand(ORG_ECLIPSE_UI_WINDOW_OPEN_EDITOR_DROP_DOWN);
		ParameterizedCommand commandF = new ParameterizedCommand(command, null);
		return commandF;
	}

	@Override
	protected String getTableHeader(IWorkbenchPart activePart) {
		return "Select Editor"; //$NON-NLS-1$
	}

}
