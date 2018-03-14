/*******************************************************************************
 * Copyright (c) 2014 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mickael Istria (Red Hat Inc.) - 226046 Allow user to specify filters
 ******************************************************************************/
package org.eclipse.ui.internal.navigator.filters;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ICheckStateProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.internal.navigator.CommonNavigatorMessages;
import org.eclipse.ui.internal.navigator.NavigatorPlugin;
import org.eclipse.ui.navigator.CommonViewer;

/**
 * Represents a tab to let user specify and manage some filters manually
 * @since 3.5.400
 */
public class UserFiltersTab extends CustomizationTab {

	private static class UserFilterRegexpEditingSupport extends EditingSupport {
		
		private boolean enableEdition = false;
		
		public UserFilterRegexpEditingSupport(CheckboxTableViewer viewer) {
			super(viewer);
		}
		
		public void setEditionEnabled(boolean enableEdition) {
			this.enableEdition = enableEdition;
		}
		
		@Override
		public CheckboxTableViewer getViewer() {
			return (CheckboxTableViewer)super.getViewer();
		}

		@Override
		protected void setValue(Object element, Object value) {
			((UserFilter)element).setRegexp((String)value);
			getViewer().update(element, null);
		}
		
		@Override
		protected Object getValue(Object element) {
			return ((UserFilter)element).getRegexp();
		}
		
		@Override
		protected CellEditor getCellEditor(Object element) {
			return new TextCellEditor(getViewer().getTable());
		}
		
		@Override
		protected boolean canEdit(Object element) {
			return this.enableEdition;
		}
	}

	private ArrayList<UserFilter> userFiltersWorkingCopy;
	
	/**
	 * @param parent
	 * @param aContentService
	 */
	public UserFiltersTab(Composite parent, CommonViewer commonViewer) {
		super(parent, commonViewer.getNavigatorContentService());
		Object data = commonViewer.getData(NavigatorPlugin.RESOURCE_REGEXP_FILTER_DATA);
		if (data != null) {
			this.userFiltersWorkingCopy = new ArrayList<UserFilter>((Collection<UserFilter>)data);
		} else {
			this.userFiltersWorkingCopy = new ArrayList<UserFilter>();
		}
		createControl();
	}

	private void createControl() {
		this.setLayout(new GridLayout(2, false));
		
		Label label = new Label(this, SWT.WRAP);
		label.setText(CommonNavigatorMessages.CommonFilterSelectionDialog_Set_custom_filters_to_apply);
		label.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, true, false, 2, 1));
		
		final CheckboxTableViewer filtersViewer = CheckboxTableViewer.newCheckList(this, SWT.BORDER | SWT.CHECK);
		filtersViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		TableLayout tableLayout = new TableLayout();
		tableLayout.addColumnData(new ColumnWeightData(200));
		filtersViewer.getTable().setLayout(tableLayout);
		filtersViewer.setContentProvider(new ArrayContentProvider());
		TableViewerColumn column = new TableViewerColumn(filtersViewer, SWT.FULL_SELECTION, 0);
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object o) {
				return ((UserFilter)o).getRegexp();
			}
		});
		final UserFilterRegexpEditingSupport editingSupport = new UserFilterRegexpEditingSupport(filtersViewer);
		column.setEditingSupport(editingSupport);
		filtersViewer.setCheckStateProvider(new ICheckStateProvider() {
			@Override
			public boolean isGrayed(Object element) {
				return false;
			}
			
			@Override
			public boolean isChecked(Object element) {
				return ((UserFilter)element).isEnabled();
			}
		});
		filtersViewer.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				((UserFilter)event.getElement()).setEnabled(event.getChecked());
			}
		});
		filtersViewer.setInput(this.userFiltersWorkingCopy);
		
		Composite buttonsComposite = new Composite(this, SWT.NONE);
		buttonsComposite.setLayoutData(new GridData(SWT.DEFAULT, SWT.TOP, false, false));
		buttonsComposite.setLayout(new GridLayout(1, false));
		
		final Button addButton = new Button(buttonsComposite, SWT.PUSH);
		addButton.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, false, false));
		addButton.setText(CommonNavigatorMessages.CommonFilterSelectionDialog_RegexpFilter_New);
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				UserFilter newFilter = new UserFilter();
				userFiltersWorkingCopy.add(newFilter);
				filtersViewer.refresh();
				filtersViewer.setSelection(new StructuredSelection(newFilter));
				editingSupport.setEditionEnabled(true);
				filtersViewer.editElement(newFilter, 0);
				editingSupport.setEditionEnabled(false);
			}
		});
		
		final Button editButton = new Button(buttonsComposite, SWT.PUSH);
		editButton.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, false, false));
		editButton.setText(CommonNavigatorMessages.CommonFilterSelectionDialog_RegexpFilter_Edit);
		editButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editingSupport.setEditionEnabled(true);
				filtersViewer.editElement(((IStructuredSelection)filtersViewer.getSelection()).getFirstElement(), 0);
				editingSupport.setEditionEnabled(false);
			}
		});
		
		final Button removeButton = new Button(buttonsComposite, SWT.PUSH);
		removeButton.setLayoutData(new GridData(SWT.FILL, SWT.DEFAULT, false, false));
		removeButton.setText(CommonNavigatorMessages.CommonFilterSelectionDialog_RegexpFilter_Remove);
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				UserFilter filter = (UserFilter) ((IStructuredSelection)filtersViewer.getSelection()).getFirstElement();
				userFiltersWorkingCopy.remove(filter);
				filtersViewer.refresh();
			}
		});
		
		filtersViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				editButton.setEnabled(!event.getSelection().isEmpty());
				removeButton.setEnabled(!event.getSelection().isEmpty());
			}
		});
		filtersViewer.setSelection(new StructuredSelection());
	}

	/**
	 * @return
	 */
	public List<UserFilter> getUserFilters() {
		return this.userFiltersWorkingCopy;
	}

}
