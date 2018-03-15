/*******************************************************************************
 * Copyright (c) 2007, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Marc-Andre Laperle (Ericsson) - Bug 413278
 *     Patrik Suzzi <psuzzi@gmail.com> - Bug 497618, 368977, 504088, 506019
 ******************************************************************************/

package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.e4.ui.css.swt.theme.ITheme;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.renderers.swt.StackRenderer;
import org.eclipse.e4.ui.workbench.swt.internal.copy.SearchPattern;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

/**
 * Shows a list of open editor and parts in the current or last active workbook.
 *
 * @since 3.4
 *
 */
public class WorkbookEditorsHandler extends FilteredTableBaseHandler {

	/**
	 * Preference node for the workbench SWT renderer
	 */
	private static final String ORG_ECLIPSE_E4_UI_WORKBENCH_RENDERERS_SWT = "org.eclipse.e4.ui.workbench.renderers.swt"; //$NON-NLS-1$

	/**
	 * Id for the command that opens the editor drop down
	 */
	private static final String ORG_ECLIPSE_UI_WINDOW_OPEN_EDITOR_DROP_DOWN = "org.eclipse.ui.window.openEditorDropDown"; //$NON-NLS-1$

	private static final String DEFAULT_THEME = "org.eclipse.e4.ui.css.theme.e4_default"; //$NON-NLS-1$

	/**
	 * E4 Tag used to identify the active part
	 */
	private static final String TAG_ACTIVE = "active"; //$NON-NLS-1$

	private Font boldFont;

	private SearchPattern searchPattern;

	/**
	 * Gets the preference "show most recently used tabs" (MRU tabs)
	 *
	 * @return Returns the enableMRU.
	 */
	private static boolean isMruEnabled() {
		IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode(ORG_ECLIPSE_E4_UI_WORKBENCH_RENDERERS_SWT);
		boolean initialMRUValue = preferences.getBoolean(StackRenderer.MRU_KEY_DEFAULT, StackRenderer.MRU_DEFAULT);
		boolean enableMRU = preferences.getBoolean(StackRenderer.MRU_KEY, initialMRUValue);
		return enableMRU;
	}

	private static boolean isCustomThemeUsed() {
		IThemeEngine engine = PlatformUI.getWorkbench().getService(IThemeEngine.class);
		if (engine != null) {
			ITheme activeTheme = engine.getActiveTheme();
			if (activeTheme != null) {
				return !DEFAULT_THEME.equals(activeTheme.getId());
			}
		}
		return false;
	}

	@Override
	protected Object getInput(WorkbenchPage page) {
		List<EditorReference> refs;
		if (isMruEnabled()) {
			// sorted, MRU order
			refs = page.getSortedEditorReferences();
		} else {
			// non sorted, First Opened order
			refs = new ArrayList<>();
			for (IEditorReference ier : page.getEditorReferences()) {
				refs.add((EditorReference) ier);
			}
		}
		return refs;
	}

	@Override
	protected boolean isFiltered() {
		return true;
	}

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

	/**
	 * Specializes
	 * {@link FilteredTableBaseHandler#setLabelProvider(TableViewerColumn)} by
	 * providing custom styles to the table cells
	 */
	@Override
	protected void setLabelProvider(final TableViewerColumn tableViewerColumn) {

		tableViewerColumn.setLabelProvider(new StyledCellLabelProvider() {

			@Override
			public void update(ViewerCell cell) {
				Object element = cell.getElement();
				if (element instanceof WorkbenchPartReference) {
					WorkbenchPartReference ref = (WorkbenchPartReference) element;
					String text = getWorkbenchPartReferenceText(ref);
					cell.setText(text);
					cell.setImage(ref.getTitleImage());
					// get the model to define the style
					MPart model = ref.getModel();
					// build the style range
					StyleRange style = new StyleRange();
					style.start = 0;
					style.length = cell.getText().length();
					// if active use the bold font
					if (isActiveEditor(model)) {
						style.font = getBoldFont(cell);
					}
					// if hidden use a light gray background
					if (isHiddenEditor(model)) {
						// but only if we are in the default theme
						// (TODO: we should support theme colors here)
						if (!isCustomThemeUsed()) {
							cell.setBackground(
									Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_BACKGROUND));
						}
					}
					cell.setStyleRanges(new StyleRange[] { style });
				}
			}

			@Override
			public String getToolTipText(Object element) {
				if (element instanceof WorkbenchPartReference) {
					WorkbenchPartReference ref = (WorkbenchPartReference) element;
					return ref.getTitleToolTip();
				}
				return super.getToolTipText(element);
			}
		});

		ColumnViewerToolTipSupport.enableFor(tableViewerColumn.getViewer());
	}

	/** True if the given model represents the active editor */
	protected boolean isActiveEditor(MPart model) {
		if (model == null || model.getTags() == null) {
			return false;
		}
		return model.getTags().contains(TAG_ACTIVE);
	}

	/** True is the given model represents an hidden editor */
	protected boolean isHiddenEditor(MPart model) {
		if (model == null || model.getParent() == null || !(model.getParent().getRenderer() instanceof StackRenderer)) {
			return false;
		}
		StackRenderer renderer = (StackRenderer) model.getParent().getRenderer();
		CTabItem item = renderer.findItemForPart(model);
		return (item != null && !item.isShowing());
	}

	/**
	 * @param cell
	 * @return Returns the boldFont.
	 */
	private Font getBoldFont(ViewerCell cell) {
		if (boldFont == null) {
			FontData data = cell.getFont().getFontData()[0];
			boldFont = JFaceResources.getFontRegistry().getBold(data.getName());
		}
		return boldFont;
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

}
