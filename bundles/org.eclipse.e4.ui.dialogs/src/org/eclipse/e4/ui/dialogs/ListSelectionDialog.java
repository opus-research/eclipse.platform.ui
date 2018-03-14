/*******************************************************************************
 * Copyright (c) 2014 vogella GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz <scholzsimon@vogella.com> - Bug 446652
 *******************************************************************************/
package org.eclipse.e4.ui.dialogs;

import java.util.Arrays;
import java.util.Collection;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.databinding.property.Properties;
import org.eclipse.e4.ui.dialogs.textbundles.DialogMessages;
import org.eclipse.jface.databinding.viewers.IViewerObservableSet;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.SelectionDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * A standard dialog which solicits a collection of selections from the user.
 * This class is configured with an arbitrary data model represented by content
 * and label provider objects. The <code>getResult</code> method returns the
 * selected elements.
 * <p>
 * Example:
 *
 * <pre>
 * ListSelectionDialog&lt;MPart&gt; dlg = new ListSelectionDialog&lt;MPart&gt;(getShell(),
 * 		input, new BaseWorkbenchContentProvider(),
 * 		new WorkbenchLabelProvider(), &quot;Select the resources to save:&quot;,
 * 		MPart.class);
 * dlg.setInitialSelections(dirtyEditors);
 * dlg.setTitle(&quot;Save Resources&quot;);
 * dlg.open();
 * </pre>
 *
 * </p>
 *
 * @param <T>
 */
public class ListSelectionDialog<T> extends SelectionDialog<T> {

	// sizing constants
	private final static int SIZING_SELECTION_WIDGET_HEIGHT = 250;

	private final static int SIZING_SELECTION_WIDGET_WIDTH = 300;
	// the root element to populate the viewer with
	private Object inputElement;

	// providers for populating this dialog
	private ILabelProvider labelProvider;

	private IStructuredContentProvider contentProvider;

	// the visual selection widget group
	private CheckboxTableViewer viewer;

	private Class<T> elementType;

	private DataBindingContext dbc;

	public static <T> SelectionDialog<T> create(Shell parentShell,
			Collection<String> input, Class<T> elementType) {

		return new ListSelectionDialog<T>(parentShell, input, elementType,
				ArrayContentProvider.getInstance(), new LabelProvider(),
				DialogMessages.ListSelection_title,
				DialogMessages.ListSelection_message);
	}

	public static <T> SelectionDialog<T> create(Shell parentShell,
			Object input, Class<T> elementType,
			IStructuredContentProvider contentProvider,
			ILabelProvider labelProvider) {

		return new ListSelectionDialog<T>(parentShell, input, elementType,
				contentProvider, labelProvider,
				DialogMessages.ListSelection_title,
				DialogMessages.ListSelection_message);
	}

	public static <T> SelectionDialog<T> create(Shell parentShell,
			Object input, Class<T> elementType,
			IStructuredContentProvider contentProvider,
			ILabelProvider labelProvider, String title, String message) {

		return new ListSelectionDialog<T>(parentShell, input, elementType,
				contentProvider, labelProvider, title != null ? title
						: DialogMessages.ListSelection_title,
				message != null ? message
						: DialogMessages.ListSelection_message);
	}

	@Override
	public boolean close() {
		if (dbc != null) {
			dbc.dispose();
		}
		return super.close();
	}

	/**
	 * Creates a list selection dialog.
	 *
	 * @param parentShell
	 *            the parent shell
	 * @param input
	 *            the root element to populate this dialog with
	 * @param elementType
	 *            type of the elements, which are shown in this dialog
	 * @param contentProvider
	 *            the content provider for navigating the model
	 * @param labelProvider
	 *            the label provider for displaying model elements
	 * @param message
	 *            the message to be displayed at the top of this dialog, or
	 *            <code>null</code> to display a default message
	 */
	protected ListSelectionDialog(Shell parentShell, Object input,
			Class<T> elementType, IStructuredContentProvider contentProvider,
			ILabelProvider labelProvider, String title, String message) {
		super(parentShell);
		this.elementType = elementType;
		this.inputElement = input;
		this.contentProvider = contentProvider;
		this.labelProvider = labelProvider;
		setTitle(title);
		setMessage(message);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		dbc = new DataBindingContext();

		initializeDialogUnits(composite);

		createMessageArea(composite);

		viewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER);
		GridDataFactory
				.swtDefaults()
				.hint(SIZING_SELECTION_WIDGET_WIDTH,
						SIZING_SELECTION_WIDGET_HEIGHT)
				.applyTo(viewer.getControl());

		viewer.setLabelProvider(labelProvider);
		viewer.setContentProvider(contentProvider);

		// bind input to the viewer
		IViewerObservableValue viewerInputObservable = ViewerProperties.input()
				.observe(viewer);
		dbc.bindValue(viewerInputObservable, new WritableValue(inputElement,
				null));

		// bind result to selected items in the viewer
		Collection<T> initialElementSelections = getInitialSelection();
		IObservableSet resultSet = Properties.selfSet(elementType).observe(
				initialElementSelections);
		setResult(resultSet);

		IViewerObservableSet viewerCheckedElementsObservable = ViewerProperties
				.checkedElements(elementType).observe(viewer);
		dbc.bindSet(viewerCheckedElementsObservable, resultSet);

		addSelectionButtons(composite);

		Dialog.applyDialogFont(composite);

		return composite;
	}

	private void addSelectionButtons(Composite parent) {
		Composite buttonComposite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		buttonComposite.setLayout(layout);
		buttonComposite.setLayoutData(new GridData(SWT.END, SWT.TOP, true,
				false));

		Button selectButton = createButton(buttonComposite,
				IDialogConstants.SELECT_ALL_ID,
				DialogMessages.SelectionDialog_selectLabel, false);

		selectButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// select all elements with databinding
				Object[] elements = contentProvider.getElements(inputElement);
				getResult().addAll(
						(Collection<? extends T>) Arrays.asList(elements));
			}
		});

		Button deselectButton = createButton(buttonComposite,
				IDialogConstants.DESELECT_ALL_ID,
				DialogMessages.SelectionDialog_deselectLabel, false);

		deselectButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// deselect all elements with databinding
				getResult().clear();
			}
		});
	}

}
