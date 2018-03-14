/*******************************************************************************
 * Copyright (c) 2008, 2014 Freescale Semiconductor and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Serge Beauchamp (Freescale Semiconductor) - [252996] initial API and implementation
 *     IBM Corporation - ongoing implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 430694
 *******************************************************************************/
package org.eclipse.ui.internal.ide.dialogs;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeMap;

import org.eclipse.core.internal.resources.FilterDescriptor;
import org.eclipse.core.resources.FileInfoMatcherDescription;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFilterMatcherDescriptor;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceFilterDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.filtermatchers.AbstractFileInfoMatcher;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.layout.TreeColumnLayout;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.FindReplaceDocumentAdapterContentProposalProvider;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.StyledString.Styler;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.ByteArrayTransfer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.MenuDetectEvent;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.TextStyle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.fieldassist.ContentAssistCommandAdapter;
import org.eclipse.ui.ide.dialogs.UIResourceFilterDescription;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;
import org.eclipse.ui.internal.ide.misc.FileInfoAttributesMatcher;
import org.eclipse.ui.internal.ide.misc.StringFileInfoMatcher;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * A widget group that displays resource filters. Includes buttons to edit,
 * remove existing filters and create new ones.
 */
public class ResourceFilterGroup {

	private Button addButton = null;
	private Button addGroupButton = null;
	private Button removeButton = null;
	private Button upButton = null;
	private Button downButton = null;
	private Button editButton = null;

	private TreeViewer filterView;
	private TreeContentProvider filterViewContentProvider;
	private Filters filters;
	private UIResourceFilterDescription[] initialFilters = new UIResourceFilterDescription[0];
	private LabelProvider labelProvider;
	private Font boldFont;
	private Font plainFont;
	private Image fileIcon = null;
	private Image folderIcon = null;
	private Image fileFolderIcon = null;
	private Image includeIcon = null;
	private Image excludeIcon = null;
	private Image inheritableIcon = null;
	private boolean tableViewCellEditorAdequatlyUsable = false;
	private Shell shell;
	private IContainer nonExistantResource = getNonExistantResource();
	private IContainer resource = nonExistantResource;

	/**
	 *
	 */
	public ResourceFilterGroup() {
		ImageDescriptor fileIconDescriptor = AbstractUIPlugin
		.imageDescriptorFromPlugin(IDEWorkbenchPlugin.IDE_WORKBENCH,
				"$nl$/icons/full/obj16/fileType_filter.png"); //$NON-NLS-1$
		if (fileIconDescriptor != null)
			fileIcon = fileIconDescriptor.createImage();

		ImageDescriptor folderIconDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(
				IDEWorkbenchPlugin.IDE_WORKBENCH,
				"$nl$/icons/full/obj16/folderType_filter.png"); //$NON-NLS-1$
		if (folderIconDescriptor != null)
			folderIcon = folderIconDescriptor.createImage();

		ImageDescriptor fileFolderIconDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(
				IDEWorkbenchPlugin.IDE_WORKBENCH,
				"$nl$/icons/full/obj16/fileFolderType_filter.png"); //$NON-NLS-1$
		if (fileFolderIconDescriptor != null)
			fileFolderIcon = fileFolderIconDescriptor.createImage();

		ImageDescriptor descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(
				IDEWorkbenchPlugin.IDE_WORKBENCH,
				"$nl$/icons/full/obj16/includeMode_filter.png"); //$NON-NLS-1$
		if (descriptor != null)
			includeIcon = descriptor.createImage();

		descriptor = AbstractUIPlugin.imageDescriptorFromPlugin(
				IDEWorkbenchPlugin.IDE_WORKBENCH,
				"$nl$/icons/full/obj16/excludeMode_filter.png"); //$NON-NLS-1$
		if (descriptor != null)
			excludeIcon = descriptor.createImage();

		ImageDescriptor inheritableIconDescriptor = AbstractUIPlugin.imageDescriptorFromPlugin(
				IDEWorkbenchPlugin.IDE_WORKBENCH,
				"$nl$/icons/full/obj16/inheritable_filter.png"); //$NON-NLS-1$
		if (inheritableIconDescriptor != null)
			inheritableIcon = inheritableIconDescriptor.createImage();
	}

	Image getImage(String string, int i) {
		if (string.equals(FilterTypeUtil.MODE))
			return new Image[] { includeIcon, excludeIcon, inheritableIcon }[i];
		if (string.equals(FilterTypeUtil.TARGET))
			return new Image[] { fileIcon, folderIcon, fileFolderIcon }[i];
		return null;
	}

	/**
	 * Set the IContainer resource to edit
	 * 
	 * @param res
	 *            the container resource
	 */

	public void setContainer(IContainer res) {
		resource = res;
	}

	private IContainer getNonExistantResource() {
		String projectName = "nonExistantProject_"; //$NON-NLS-1$
		IProject p = ResourcesPlugin.getWorkspace().getRoot().getProject(
				projectName);
		int iteration = 0;
		while (p.exists()) {
			p = ResourcesPlugin.getWorkspace().getRoot().getProject(
					projectName + iteration);
			iteration++;
		}
		return p;
	}

	class Filters extends FilterCopy {
		public Filters(IContainer resource) {
			try {
				IResourceFilterDescription[] tmp = resource.getFilters();
				children = new LinkedList();
				for (int i = 0; i < tmp.length; i++) {
					FilterCopy copy = new FilterCopy(UIResourceFilterDescription.wrap(tmp[i]));
					copy = convertLegacyMatchers(copy);
					addChild(copy);
				}
			} catch (CoreException e) {
				ErrorDialog.openError(shell, NLS.bind(
						IDEWorkbenchMessages.InternalError, null), e
						.getLocalizedMessage(), e.getStatus());
			}
		}

		public Filters(IResourceFilterDescription filters[]) {
			children = new LinkedList();
			if (filters != null) {
				for (int i = 0; i < filters.length; i++)
					addChild(new FilterCopy(UIResourceFilterDescription.wrap(filters[i])));
			}
		}

		public Filters(UIResourceFilterDescription filters[]) {
			children = new LinkedList();
			if (filters != null) {
				for (int i = 0; i < filters.length; i++)
					addChild(new FilterCopy(filters[i]));
			}
		}

		boolean changed = false;
		public LinkedList/* <IResourceFilterDescription> */trash = new LinkedList/*
																	 * <IResourceFilterDescription
																	 * >
																	 */();

		public void add(FilterCopy newFilter) {
			super.addChild(newFilter);
			changed = true;
		}

		public void remove(FilterCopy filter) {
			super.removeChild(filter);
			if (filter.original != null)
				trash.add(filter);
			changed = true;
		}

		public void moveUp(UIResourceFilterDescription filter) {
			FilterCopy[] content = getChildren();
			for (int i = 1; i < content.length; i++) {
				if (content[i] == filter) {
					FilterCopy tmp = content[i - 1];
					content[i - 1] = content[i];
					content[i] = tmp;
				}
			}
			children = new LinkedList(Arrays.asList(content));
			changed = true;
		}

		public void moveDown(UIResourceFilterDescription filter) {
			FilterCopy[] content = getChildren();
			for (int i = 0; i < (content.length - 1); i++) {
				if (content[i] == filter) {
					FilterCopy tmp = content[i + 1];
					content[i + 1] = content[i];
					content[i] = tmp;
				}
			}
			children = new LinkedList(Arrays.asList(content));
			changed = true;
		}

		@Override
		public int getChildrenLimit() {
			return Integer.MAX_VALUE;
		}

		@Override
		protected void argumentsChanged() {
			changed = true;
		}

		@Override
		public boolean hasChanged() {
			if (changed)
				return true;
			Iterator it = children.iterator();
			while (it.hasNext()) {
				FilterCopy filter = (FilterCopy) it.next();
				if (filter.hasChanged())
					return true;
			}
			return false;
		}

		public boolean isFirst(FilterCopy o) {
			if (children.size() > 0)
				return children.getFirst().equals(o);
			return false;
		}

		public boolean isLast(FilterCopy o) {
			if (children.size() > 0)
				return children.getLast().equals(o);
			return false;
		}

		@Override
		public void removeAll() {
			if (children.size() > 0) {
				super.removeAll();
				changed = true;
			}
		}
	}

	static String includeOnlyGroup = "INCLUDE_ONLY_GROUP";  //$NON-NLS-1$

	static String excludeAllGroup = "EXCLUDE_ALL_GROUP";  //$NON-NLS-1$

	class TreeContentProvider implements ITreeContentProvider {
		
		@Override
		public Object[] getChildren(Object parentElement) {
			if (parentElement == filters) {
				if (filters.getChildren().length > 0)
					return new Object[] {includeOnlyGroup, excludeAllGroup};
				return new Object[0];
			}
			if (parentElement instanceof String) {
				ArrayList list = new ArrayList();
				int mask = parentElement.equals(includeOnlyGroup) ? IResourceFilterDescription.INCLUDE_ONLY:
								IResourceFilterDescription.EXCLUDE_ALL;
				FilterCopy[] children = filters.getChildren();
				for (int i = 0; i < children.length; i++) {
					if ((children[i].getType() & mask) != 0)
						list.add(children[i]);
				}
				return list.toArray();
			}
			if (parentElement instanceof FilterCopy)
				return ((FilterCopy) parentElement).getChildren();
			return null;
		}

		@Override
		public Object getParent(Object element) {
			if (element instanceof String)
				return filters;
			if (element instanceof FilterCopy) {
				FilterCopy filterCopy = (FilterCopy) element;
				if (filterCopy.getParent() != null && filterCopy.getParent() != filters)
					return filterCopy.getParent();
				return ((filterCopy.getType() & IResourceFilterDescription.INCLUDE_ONLY) != 0) ? includeOnlyGroup: excludeAllGroup;
			}
			return null;
		}

		@Override
		public boolean hasChildren(Object element) {
			if (element instanceof FilterCopy || element instanceof String) {
				Object[] children = getChildren(element);
				return children != null && children.length > 0;
			}
			return false;
		}

		@Override
		public Object[] getElements(Object inputElement) {
			return getChildren(inputElement);
		}

		@Override
		public void dispose() {
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	class LabelProvider extends StyledCellLabelProvider  {
		private final Styler fBoldStyler;
		private final Styler fPlainStyler;
		FilterTypeUtil util;
		TreeMap/*<String, ICustomFilterArgumentUI */ customfilterArgumentMap = new TreeMap();

		public LabelProvider() {
			util = new FilterTypeUtil();
			fBoldStyler= new Styler() {
				@Override
				public void applyStyles(TextStyle textStyle) {
					textStyle.font= boldFont;
				}
			};
			fPlainStyler= new Styler() {
				@Override
				public void applyStyles(TextStyle textStyle) {
					textStyle.font= plainFont;
				}
			};
			ICustomFilterArgumentUI ui = new MultiMatcherCustomFilterArgumentUI(null, null, null);
			customfilterArgumentMap.put(ui.getID(), ui);
			ui = new DefaultCustomFilterArgumentUI(null, null, null);
			customfilterArgumentMap.put(ui.getID(), ui);
		}

		ICustomFilterArgumentUI getUI(String descriptorID) {
			ICustomFilterArgumentUI result = (ICustomFilterArgumentUI) customfilterArgumentMap.get(descriptorID);
			if (result == null)
				return result = (ICustomFilterArgumentUI) customfilterArgumentMap.get(new String()); // default ui
			return result;
		}

		String getColumnID(int index) {
			return (String) filterView.getColumnProperties()[index];
		}

		public boolean isPartialFilter(Object element) {
			FilterCopy copy = (FilterCopy) element;
			return copy.isUnderAGroupFilter();
		}

		@Override
		public void update(ViewerCell cell) {
			int columnIndex = cell.getColumnIndex();
			String column = getColumnID(columnIndex);
			FilterCopy filter = null;

			Object element = cell.getElement();
			if (element instanceof String) {
				if (column.equals(FilterTypeUtil.MODE)) {
					cell.setImage(getImage(FilterTypeUtil.MODE, element.equals(includeOnlyGroup) ? 0:1));
				}
				if (column.equals(FilterTypeUtil.MODE)) {
					if (element.equals(includeOnlyGroup))
						cell.setText(NLS
								.bind(
										IDEWorkbenchMessages.ResourceFilterPage_includeOnlyColumn,
										null));
					else
						cell.setText(NLS
							.bind(
									IDEWorkbenchMessages.ResourceFilterPage_excludeAllColumn,
									null));
				}
			}
			else {
				filter = (FilterCopy) element;
	
				if (column.equals(FilterTypeUtil.MODE)) {
					StyledString styledString = getStyleColumnText(filter);
					if (!isPartialFilter(filter)) {
						Object isInheritable = FilterTypeUtil.getValue(filter, FilterTypeUtil.INHERITABLE);
						if (((Boolean)isInheritable).booleanValue())
							styledString.append("   " + IDEWorkbenchMessages.ResourceFilterPage_recursive); //$NON-NLS-1$
						
					}
					cell.setText(styledString.toString());
					cell.setStyleRanges(styledString.getStyleRanges());

					if (!isPartialFilter(filter)) {
						Image[] images = { fileIcon, folderIcon, fileFolderIcon };

						Object index = FilterTypeUtil.getValue(filter, FilterTypeUtil.TARGET);
						cell.setImage(images[((Integer) index).intValue()]);
					}
				}
			}

			super.update(cell);
		}
		
		private StyledString getStyleColumnText(FilterCopy filter) {
			if ((filter.getChildrenLimit() > 0)) {
				String whiteSpace = " "; //$NON-NLS-1$;
				String expression = getFilterTypeName(filter);
				boolean isUnaryOperator = filter.getId().equals("org.eclipse.ui.ide.notFilterMatcher"); //$NON-NLS-1$
				StyledString buffer = new StyledString();
				if (isUnaryOperator) {
					buffer.append("NOT ", fBoldStyler); //$NON-NLS-1$
					expression = "OR"; //$NON-NLS-1$
				}
				buffer.append("(", fBoldStyler); //$NON-NLS-1$
				Object [] children = filterViewContentProvider.getChildren(filter);
				for (int i = 0; i < children.length; i++) {
					buffer.append(getStyleColumnText((FilterCopy) children[i]));
					if ((i + 1) < children.length) {
						buffer.append(whiteSpace, fPlainStyler);
						buffer.append(expression, fBoldStyler);
						buffer.append(whiteSpace, fPlainStyler);
					}
				}
				if (children.length < 2 && !isUnaryOperator) {
					if (children.length == 1)
						buffer.append(whiteSpace, fPlainStyler);
					buffer.append(expression, fBoldStyler);
				}
				buffer.append(")", fBoldStyler); //$NON-NLS-1$
				return buffer;
			}
			ICustomFilterArgumentUI ui = getUI(filter.getId());
			return ui.formatStyledText(filter, fPlainStyler, fBoldStyler);
		}

		@Override
		protected void measure(Event event, Object element) {
			super.measure(event, element);
		}

		private String getFilterTypeName(FilterCopy filter) {
			IFilterMatcherDescriptor desc = FilterTypeUtil.getDescriptor(filter
					.getId());
			if (desc != null)
				return desc.getName();
			return ""; //$NON-NLS-1$
		}
	}
	
	class CellModifier implements ICellModifier {
		@Override
		public boolean canModify(Object element, String property) {
			FilterCopy filter = (FilterCopy) element;
			if (property.equals(FilterTypeUtil.ARGUMENTS)
					&& !filter.hasStringArguments())
				return false;
			return true;
		}

		@Override
		public Object getValue(Object element, String property) {
			FilterCopy filter = (FilterCopy) element;
			return FilterTypeUtil.getValue(filter, property);
		}

		@Override
		public void modify(Object element, String property, Object value) {
			FilterCopy filter = (FilterCopy) ((TableItem) element).getData();
			FilterTypeUtil.setValue(filter, property, value);
			filterView.refresh(filter);
		}
	}

	/**
	 * Creates the widget group. Callers must call <code>dispose</code> when the
	 * group is no longer needed.
	 * 
	 * @param parent
	 *            the widget parent
	 * @return container of the widgets
	 */
	public Control createContents(Composite parent) {

        Font font = parent.getFont();
		shell = parent.getShell();

		if (resource == null) {
			Label label = new Label(parent, SWT.NONE);
			label.setText(NLS.bind(
					IDEWorkbenchMessages.ResourceFilterPage_noResource, null));
	        label.setFont(font);
			return label;
		}

		if (resource == nonExistantResource)
			filters = new Filters(initialFilters);
		else
			filters = new Filters(resource);

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 2;
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL_BOTH);
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		composite.setLayoutData(data);
		composite.setFont(font);

		Label label = new Label(composite, 0);
		label.setText(NLS.bind(IDEWorkbenchMessages.ResourceFilterPage_title,
				null));
		data = new GridData(GridData.FILL);
		data.horizontalSpan = 2;
		label.setLayoutData(data);
		label.setFont(font);
		
		createViewerGroup(composite);
		createButtonGroup(composite);

		refreshEnablement();
		return composite;
	}

	private void createViewerGroup(Composite parent) {
		Composite tableComposite = new Composite(parent, SWT.NONE);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		tableComposite.setLayoutData(data);

		filterView = new TreeViewer(tableComposite, SWT.FULL_SELECTION | SWT.BORDER
				| SWT.H_SCROLL);
		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		filterView.getTree().setLayoutData(data);
		filterView.setColumnProperties(FilterTypeUtil.columnNames);

		plainFont = filterView.getTree().getFont();
		FontData[] boldFontData= getModifiedFontData(plainFont.getFontData(), SWT.BOLD);
		boldFont = new Font(Display.getCurrent(), boldFontData);

		filterView.setAutoExpandLevel(2);
		filterViewContentProvider = new TreeContentProvider();
		filterView.setContentProvider(filterViewContentProvider);
		filterView.setInput(filters);
		filterView.getTree().setFont(parent.getFont());

		filterView.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				refreshEnablement();
			}
		});

		TreeColumn modeColumn = new TreeColumn(filterView.getTree(), 0);
		modeColumn
				.setText(NLS
						.bind(
								IDEWorkbenchMessages.ResourceFilterPage_columnFilterDescription,
								null));
		modeColumn.setData(FilterTypeUtil.MODE);
		modeColumn.setResizable(true);
		modeColumn.setMoveable(false);

		filterView.getTree().setHeaderVisible(false);
		filterView.getTree().showColumn(filterView.getTree().getColumn(0));
		labelProvider = new LabelProvider();
		filterView.setLabelProvider(labelProvider);

		CellEditor[] editors = new CellEditor[5];
		editors[0] = new ComboBoxCellEditor(filterView.getTree(),
				FilterTypeUtil.getFilterNames(false), SWT.READ_ONLY);
		editors[1] = new ComboBoxCellEditor(filterView.getTree(),
				FilterTypeUtil.getModes(), SWT.READ_ONLY);
		editors[2] = new ComboBoxCellEditor(filterView.getTree(),
				FilterTypeUtil.getTargets(), SWT.READ_ONLY);
		editors[3] = new CheckboxCellEditor(filterView.getTree());
		editors[4] = new TextCellEditor(filterView.getTree());

		if (tableViewCellEditorAdequatlyUsable) {
			filterView.setCellEditors(editors);
			filterView.setCellModifier(new CellModifier());
		}

		filterView.getTree().addMouseListener(new MouseListener() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				if (!handleEdit()) {
					ISelection selection = filterView.getSelection();
					if (selection instanceof IStructuredSelection) {
						if (((IStructuredSelection) selection).size() > 0) {
							Object firstElement = ((IStructuredSelection) selection).getFirstElement();
							filterView.setExpandedState(firstElement, !filterView.getExpandedState(firstElement));
						}
					}
				}
			}

			@Override
			public void mouseDown(MouseEvent e) {
			}

			@Override
			public void mouseUp(MouseEvent e) {
			}
		});
		FilterCopyDrag drag = new FilterCopyDrag();
		int ops = DND.DROP_MOVE;
		Transfer[] transfers = new Transfer[] { filterCopyTransfer };
		filterView.addDragSupport(ops, transfers, drag);
		filterView.addDropSupport(ops, transfers,
				new FilterCopyDrop(filterView));

		filterView.getTree().addMenuDetectListener(new MenuDetectListener() {
			@Override
			public void menuDetected(MenuDetectEvent e) {
				MenuManager mgr = new MenuManager();
				mgr.add(addSubFilterAction);
				mgr.add(addSubGroupFilterAction);
				mgr.add(new Separator());
				mgr.add(new EditFilterAction());
				mgr.add(new RemoveFilterAction());
				filterView.getControl().setMenu(
						mgr.createContextMenu(filterView.getControl()));
			}
		});
		TreeColumnLayout layout = new TreeColumnLayout();
		tableComposite.setLayout( layout );

		layout.setColumnData( modeColumn, new ColumnWeightData(100));
		filterView.setSelection(new StructuredSelection(includeOnlyGroup));
	}

	private static FontData[] getModifiedFontData(FontData[] originalData, int additionalStyle) {
		FontData[] styleData = new FontData[originalData.length];
		for (int i = 0; i < styleData.length; i++) {
			FontData base = originalData[i];
			styleData[i] = new FontData(base.getName(), base.getHeight(), base.getStyle() | additionalStyle);
		}
       	return styleData;
    }

	class EditFilterAction extends Action {

		public EditFilterAction() {
			setText(NLS
					.bind(IDEWorkbenchMessages.ResourceFilterPage_editFilterActionLabel,
							null));
		}

		@Override
		public void run() {
			handleEdit();
		}
		@Override
		public boolean isEnabled() {
			ISelection selection = filterView.getSelection();
			if (selection instanceof IStructuredSelection) {
				if (((IStructuredSelection) selection).size() > 0) {
					Object firstElement = ((IStructuredSelection) selection)
					.getFirstElement();
					return firstElement instanceof FilterCopy;
				}
			}
			return false;
		}
	}

	class RemoveFilterAction extends Action {

		public RemoveFilterAction() {
			setText(NLS
					.bind(IDEWorkbenchMessages.ResourceFilterPage_removeFilterActionLabel,
							null));
		}

		@Override
		public void run() {
			handleRemove();
		}
		@Override
		public boolean isEnabled() {
			ISelection selection = filterView.getSelection();
			if (selection instanceof IStructuredSelection) {
				return ((IStructuredSelection) selection).size() > 0;			}
			return false;
		}
	}

	Action addSubFilterAction = new AddSubFilterAction(false);

	class AddSubFilterAction extends Action {

		boolean createGroupOnly;
		
		public AddSubFilterAction(boolean createGroupOnly) {
			this.createGroupOnly = createGroupOnly;
			setText(NLS
					.bind(createGroupOnly ?
							IDEWorkbenchMessages.ResourceFilterPage_addSubFilterGroupActionLabel:
							IDEWorkbenchMessages.ResourceFilterPage_addSubFilterActionLabel,
							null));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.action.Action#run()
		 */
		@Override
		public void run() {
			ISelection selection = filterView.getSelection();
			if (selection instanceof IStructuredSelection) {
				Object firstElement = ((IStructuredSelection) selection)
				.getFirstElement();
				handleAdd(firstElement, createGroupOnly);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.action.Action#isEnabled()
		 */
		@Override
		public boolean isEnabled() {
			ISelection selection = filterView.getSelection();
			if (selection instanceof IStructuredSelection) {
				Object firstElement = ((IStructuredSelection) selection)
						.getFirstElement();
				return isAddEnabled(firstElement);
			}
			return false;
		}
	}

	private void handleAdd(boolean createGroupOnly) {
		Object selectedObject = null;
		ISelection selection = filterView.getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = ((IStructuredSelection) selection);
			selectedObject = structuredSelection.getFirstElement();
		}
		handleAdd(selectedObject, createGroupOnly);
	}
	
	private void handleAdd(Object selection, boolean createGroupOnly) {
		if (selection == null) {
			FilterCopy newFilter = new FilterCopy();
			FilterEditDialog dialog = new FilterEditDialog(resource, ResourceFilterGroup.this, shell,
					newFilter, createGroupOnly, true);
			if (dialog.open() == Window.OK) {
				addToTopLevelFilters(newFilter);
				refreshAndSelect(newFilter);
			}
		}
		else if (selection instanceof FilterCopy) {
			FilterCopy filter = (FilterCopy) selection;
			if (filter.getChildrenLimit() > 0) {
				FilterCopy newFilter = new FilterCopy();
				newFilter.setParent(filter);
				FilterTypeUtil.setValue(newFilter, FilterTypeUtil.MODE, FilterTypeUtil.getValue(filter, FilterTypeUtil.MODE));
				FilterEditDialog dialog = new FilterEditDialog(resource, ResourceFilterGroup.this, shell,
						newFilter, createGroupOnly, true);
				if (dialog.open() == Window.OK) {
					filter.addChild(newFilter);
					refreshAndSelect(newFilter);
				}
			}
			else {
				FilterCopy newFilter = new FilterCopy();
				FilterEditDialog dialog = new FilterEditDialog(resource, ResourceFilterGroup.this, shell,
						newFilter, createGroupOnly, true);
				if (dialog.open() == Window.OK) {
					addToTopLevelFilters(newFilter);
					refreshAndSelect(newFilter);
				}
			}
		}
		else if (selection instanceof String) {
			FilterCopy newFilter = new FilterCopy();
			FilterTypeUtil.setValue(newFilter, FilterTypeUtil.MODE,
					new Integer(selection.equals(includeOnlyGroup) ? 0:1));
			FilterEditDialog dialog = new FilterEditDialog(resource, ResourceFilterGroup.this, shell,
					newFilter, createGroupOnly, true);
			if (dialog.open() == Window.OK) {
				addToTopLevelFilters(newFilter);
				refreshAndSelect(newFilter);
			}
		}
	}

	private void refreshAndSelect(FilterCopy newFilter) {
		filterView.refresh();
		filterView.reveal(newFilter);
	}

	private boolean isAddEnabled(Object selection) {
		if (selection == null)
			return true;
		if (selection instanceof FilterCopy) {
			FilterCopy filter = (FilterCopy) selection;
			return filter.getChildrenLimit() > 0;
		}
		if (selection instanceof String)
			return true;
		return false;
	}

	private void addToTopLevelFilters(FilterCopy newFilter) {
		int value = ((Integer) FilterTypeUtil.getValue(newFilter, FilterTypeUtil.MODE)).intValue();
		Object[] existingChildren = filterViewContentProvider.getChildren(value == 0? includeOnlyGroup:excludeAllGroup);
		filters.add(newFilter);
		filterView.refresh();
		if (existingChildren.length == 0)
			filterView.setExpandedState(newFilter, true);
	}

	Action addSubGroupFilterAction = new AddSubFilterAction(true);

	class FilterCopyDrop extends ViewerDropAdapter {
		protected FilterCopyDrop(Viewer viewer) {
			super(viewer);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.ViewerDropAdapter#performDrop(java.lang
		 * .Object)
		 */
		@Override
		public boolean performDrop(Object data) {
			Object target = getCurrentTarget();
			if (target == null)
				target = filters;
			FilterCopy[] toDrop = (FilterCopy[]) data;

			if (target instanceof FilterCopy) {
				for (int i = 0; i < toDrop.length; i++)
					if (toDrop[i].equals(target)
							|| ((FilterCopy) target).hasParent(toDrop[i]))
						return false;
			}

			for (int i = 0; i < toDrop.length; i++) {
				if (target instanceof Filters)
					filters.add(toDrop[i]);
				if (target instanceof String) {
					FilterTypeUtil.setValue(toDrop[i], FilterTypeUtil.MODE,
							new Integer(target.equals(includeOnlyGroup) ? 0:1));
					addToTopLevelFilters(toDrop[i]);
				}
				if (target instanceof FilterCopy)
					((FilterCopy) target).addChild(toDrop[i]);
				filterView.refresh();
				filterView.reveal(toDrop[i]);
			}
			return true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.ViewerDropAdapter#validateDrop(java.lang
		 * .Object, int, org.eclipse.swt.dnd.TransferData)
		 */
		@Override
		public boolean validateDrop(Object target, int operation,
				TransferData transferType) {
			if (filterCopyTransfer.isSupportedType(transferType)) {
				if (target instanceof FilterCopy)
					return ((FilterCopy) target).canAcceptDrop();
				return true;
			}
			return false;
		}
	}

	class FilterCopyDrag implements DragSourceListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.swt.dnd.DragSourceListener#dragFinished(org.eclipse.swt
		 * .dnd.DragSourceEvent)
		 */
		@Override
		public void dragFinished(DragSourceEvent event) {
			if (event.detail == DND.DROP_MOVE) {
				// nothing
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.swt.dnd.DragSourceListener#dragSetData(org.eclipse.swt
		 * .dnd.DragSourceEvent)
		 */
		@Override
		public void dragSetData(DragSourceEvent event) {
			if (filterCopyTransfer.isSupportedType(event.dataType)) {
				event.data = getFilterCopySelection();
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.swt.dnd.DragSourceListener#dragStart(org.eclipse.swt.
		 * dnd.DragSourceEvent)
		 */
		@Override
		public void dragStart(DragSourceEvent event) {
			if (getFilterCopySelection().length == 0)
				event.doit = false;
		}
	}

	private void createButtonGroup(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.numColumns = 1;
		composite.setLayout(layout);
		GridData data = new GridData(SWT.FILL, SWT.FILL, false, false);
		composite.setLayoutData(data);
		composite.setFont(parent.getFont());

		addButton = new Button(composite, SWT.PUSH);
		addButton.setText(NLS.bind(
				IDEWorkbenchMessages.ResourceFilterPage_addButtonLabel, null));
		data = new GridData(SWT.FILL, SWT.FILL, false, false);
		addButton.setLayoutData(data);
		setButtonDimensionHint(addButton);
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleAdd(false);
			}
		});
		
		addGroupButton = new Button(composite, SWT.PUSH);
		addGroupButton.setText(NLS.bind(
				IDEWorkbenchMessages.ResourceFilterPage_addGroupButtonLabel, null));
		data = new GridData(SWT.FILL, SWT.FILL, false, false);
		addGroupButton.setLayoutData(data);
		setButtonDimensionHint(addGroupButton);
		addGroupButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleAdd(true);
			}
		});
		

		editButton = new Button(composite, SWT.PUSH);
		editButton.setText(NLS.bind(
				IDEWorkbenchMessages.ResourceFilterPage_editButtonLabel, null));
		data = new GridData(SWT.FILL, SWT.FILL, false, false);
		editButton.setLayoutData(data);
		setButtonDimensionHint(editButton);
		editButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleEdit();
			}
		});

		removeButton = new Button(composite, SWT.PUSH);
		removeButton
				.setText(NLS
						.bind(
								IDEWorkbenchMessages.ResourceFilterPage_removeButtonLabel,
								null));
		data = new GridData(SWT.FILL, SWT.FILL, false, false);
		removeButton.setLayoutData(data);
		setButtonDimensionHint(removeButton);
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleRemove();
			}
		});
	}

	private void refreshEnablement() {
		if (addButton != null) {
			ISelection selection = filterView.getSelection();
			IStructuredSelection structuredSelection = null;
			if (selection instanceof IStructuredSelection)
				structuredSelection = ((IStructuredSelection) selection);
			removeButton.setEnabled(structuredSelection != null
					&& structuredSelection.size() > 0 && !(structuredSelection.getFirstElement() instanceof String));
			editButton.setEnabled(structuredSelection != null
					&& structuredSelection.size() == 1
					&& (structuredSelection.getFirstElement() instanceof FilterCopy));
			if (upButton != null)
				upButton.setEnabled(structuredSelection != null
						&& (structuredSelection.size() > 0)
						&& !isFirst(structuredSelection.getFirstElement()));
			if (downButton != null)
				downButton.setEnabled(structuredSelection != null
						&& (structuredSelection.size() > 0)
						&& !isLast(structuredSelection.getFirstElement()));
		}
	}

	private boolean isFirst(Object o) {
		return filters.isFirst((FilterCopy) o);
	}

	private boolean isLast(Object o) {
		return filters.isLast((FilterCopy) o);
	}

	private boolean handleEdit() {
		ISelection selection = filterView.getSelection();
		if (selection instanceof IStructuredSelection) {
			Object firstElement = ((IStructuredSelection) selection)
					.getFirstElement();
			if (firstElement instanceof String) {
				handleAdd(firstElement, false);
				return true;
			}
			if (firstElement instanceof FilterCopy) {
				FilterCopy filter = (FilterCopy) firstElement;
				FilterCopy copy = new FilterCopy(filter);
				copy.setParent(filter.getParent());
				boolean isGroup = filter.getChildrenLimit() > 0;
				FilterEditDialog dialog = new FilterEditDialog(resource, this, shell, copy, isGroup, false);
				if (dialog.open() == Window.OK) {
					if (copy.hasChanged()) {
						filter.copy(copy);
						filterView.refresh();
					}
				}
				return true;
			}
		}
		return false;
	}

	private FilterCopy[] getFilterCopySelection() {
		ISelection selection = filterView.getSelection();
		IStructuredSelection structuredSelection = null;
		if (selection instanceof IStructuredSelection) {
			structuredSelection = ((IStructuredSelection) selection);
			FilterCopy[] tmp = new FilterCopy[structuredSelection.size()];
			System.arraycopy(structuredSelection.toArray(), 0, tmp, 0,
					tmp.length);
			return tmp;
		}
		return new FilterCopy[0];
	}

	private void handleRemove() {
		ISelection selection = filterView.getSelection();
		IStructuredSelection structuredSelection = null;
		if (selection instanceof IStructuredSelection) {
			structuredSelection = ((IStructuredSelection) selection);
			Iterator it = structuredSelection.iterator();
			while (it.hasNext()) {
				Object element = it.next();
				if (element instanceof FilterCopy) {
					FilterCopy filter = (FilterCopy) element;
					filter.getParent().removeChild(filter);
				}
				else {
					int mask = element.equals(includeOnlyGroup) ? IResourceFilterDescription.INCLUDE_ONLY:
						IResourceFilterDescription.EXCLUDE_ALL;
					FilterCopy[] children = filters.getChildren();
					for (int i = 0; i < children.length; i++) {
						if ((children[i].getType() & mask) != 0)
							filters.removeChild(children[i]);
					}
				}
			}
			filterView.refresh();
		}
	}

	private static void setButtonDimensionHint(Button button) {
		Assert.isNotNull(button);
		Object gd = button.getLayoutData();
		if (gd instanceof GridData) {
			((GridData) gd).widthHint = getButtonWidthHint(button);
			((GridData) gd).horizontalAlignment = GridData.FILL;
		}
	}

	private static int getButtonWidthHint(Button button) {
		button.setFont(JFaceResources.getDialogFont());
		FontMetrics fontMetrics;
		GC gc = new GC(button);
		try {
			gc.setFont(button.getFont());
			fontMetrics = gc.getFontMetrics();
		} finally {
			gc.dispose();
		}
		int widthHint = Dialog.convertHorizontalDLUsToPixels(fontMetrics,
				IDialogConstants.BUTTON_WIDTH);
		return Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT,
				true).x);
	}

	/**
	 * Apply the default state of the resource.
	 */
	public void performDefaults() {
		if (resource == null)
			return;
		filters = new Filters(resource);
		filters.removeAll();
		filterView.setInput(filters);
		filterView.refresh();
	}

	/**
	 * @return the filters that were configured on this resource
	 */
	public UIResourceFilterDescription[] getFilters() {
		FilterCopy[] newFilters = filters.getChildren();
		UIResourceFilterDescription[] result = new UIResourceFilterDescription[newFilters.length];
		for (int i = 0; i < newFilters.length; i++) {
			result[i] = newFilters[i];
		}
		return result;
	}

	/**
	 * @param filters
	 */
	public void setFilters(IResourceFilterDescription[] filters) {
		initialFilters = new UIResourceFilterDescription[filters.length];
		for (int i = 0; i < filters.length; i++)
			initialFilters[i] = UIResourceFilterDescription.wrap(filters[i]);
	}

	/**
	 * @param filters
	 */
	public void setFilters(UIResourceFilterDescription[] filters) {
		initialFilters = filters;
	}

	/**
	 * Apply the read only state and the encoding to the resource.
	 * 
	 * @return true if the filters changed
	 */
	public boolean performOk() {

		if (filters.hasChanged()) {
			if (resource == null)
				return true;

			try {
				if (resource != nonExistantResource) {
					IResourceFilterDescription[] oldFilters = resource.getFilters();
					for (int i = 0; i < oldFilters.length; i++) {
						oldFilters[i].delete(IResource.BACKGROUND_REFRESH,
								new NullProgressMonitor());
					}
					FilterCopy[] newFilters = filters.getChildren();
					for (int i = 0; i < newFilters.length; i++) {
						resource.createFilter(newFilters[i].getType(),
								newFilters[i].getFileInfoMatcherDescription(),
								IResource.BACKGROUND_REFRESH,
								new NullProgressMonitor());
					}
				}
			} catch (CoreException e) {
				ErrorDialog.openError(shell, NLS.bind(
						IDEWorkbenchMessages.InternalError, null), e
						.getLocalizedMessage(), e.getStatus());
			}
		}
		return true;
	}

	/**
	 * Disposes the group's resources.
	 */
	public void dispose() {
		disposeIcons();
	}

	private void disposeIcons() {
		Field[] fields = getClass().getDeclaredFields();
		for (int i = 0; i < fields.length; i++) {
			Class cls = fields[i].getType();
			if (cls.equals(Image.class)) {
				Image img;
				try {
					img = (Image) fields[i].get(this);
					if (img != null) {
						img.dispose();
						fields[i].set(this, null);
					}
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private FilterCopyTransfer filterCopyTransfer = new FilterCopyTransfer();

	class FilterCopyTransfer extends ByteArrayTransfer {

		private FilterCopyTransfer() {
		}

		@Override
		public void javaToNative(Object object, TransferData transferData) {
			if (object == null || !(object instanceof FilterCopy[]))
				return;
			if (isSupportedType(transferData)) {
				FilterCopy[] myTypes = (FilterCopy[]) object;
				try {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					DataOutputStream writeOut = new DataOutputStream(out);
					writeOut.writeInt(myTypes.length);
					for (int i = 0; i < myTypes.length; i++)
						writeOut.writeInt(myTypes[i].getSerialNumber());
					byte[] buffer = out.toByteArray();
					writeOut.close();
					super.javaToNative(buffer, transferData);
				} catch (IOException e) {
				}
			}
		}

		@Override
		public Object nativeToJava(TransferData transferData) {
			if (isSupportedType(transferData)) {
				byte[] buffer = (byte[]) super.nativeToJava(transferData);
				if (buffer == null)
					return null;
				FilterCopy[] myData = new FilterCopy[0];
				try {
					ByteArrayInputStream in = new ByteArrayInputStream(buffer);
					DataInputStream readIn = new DataInputStream(in);
					int size = readIn.readInt();

					LinkedList droppedFilters = new LinkedList();
					for (int i = 0; i < size; i++) {
						int serialNumber = readIn.readInt();
						FilterCopy tmp = filters
								.findBySerialNumber(serialNumber);
						if (tmp != null)
							droppedFilters.add(tmp);
					}
					myData = (FilterCopy[]) droppedFilters
							.toArray(new FilterCopy[0]);
					readIn.close();
				} catch (IOException ex) {
					return null;
				}
				return myData;
			}

			return null;
		}

		private final String MYTYPENAME = "org.eclipse.ui.ide.internal.filterCopy"; //$NON-NLS-1$
		private final int MYTYPEID = registerType(MYTYPENAME);

		@Override
		protected String[] getTypeNames() {
			return new String[] { MYTYPENAME };
		}

		@Override
		protected int[] getTypeIds() {
			return new int[] { MYTYPEID };
		}
	}
}

class FilterTypeUtil {
	static String ID = "id"; //$NON-NLS-1$
	static String TARGET = "target"; //$NON-NLS-1$
	static String MODE = "mode"; //$NON-NLS-1$
	static String ARGUMENTS = "arguments"; //$NON-NLS-1$
	static String INHERITABLE = "inheritable"; //$NON-NLS-1$

	static String[] columnNames = new String[] { MODE, TARGET, INHERITABLE };

	static String[] getModes() {
		return new String[] {
				NLS.bind(IDEWorkbenchMessages.ResourceFilterPage_includeOnly,
						null),
				NLS.bind(IDEWorkbenchMessages.ResourceFilterPage_excludeAll,
						null) };
	}

	public static void setValue(FilterCopy filter, String property, Object value) {
		if (property.equals(FilterTypeUtil.ID)) {
			IFilterMatcherDescriptor descriptor;
			if (value instanceof Integer) {
				int selection = ((Integer) value).intValue();
				descriptor = FilterTypeUtil.getDescriptorFromIndex(selection);
			} else
				descriptor = FilterTypeUtil.getDescriptorByName((String) value);
			if (descriptor != null)
				filter.setId(descriptor.getId());
		}
		if (property.equals(FilterTypeUtil.MODE)) {
			int selection = ((Integer) value).intValue();
			int type = filter.getType()
					& ~(IResourceFilterDescription.INCLUDE_ONLY | IResourceFilterDescription.EXCLUDE_ALL);
			if (selection == 0)
				filter.setType(type | IResourceFilterDescription.INCLUDE_ONLY);
			else
				filter.setType(type | IResourceFilterDescription.EXCLUDE_ALL);
		}
		if (property.equals(FilterTypeUtil.TARGET)) {
			int selection = ((Integer) value).intValue();
			int type = filter.getType()
					& ~(IResourceFilterDescription.FILES | IResourceFilterDescription.FOLDERS);
			if (selection == 0)
				filter.setType(type | IResourceFilterDescription.FILES);
			if (selection == 1)
				filter.setType(type | IResourceFilterDescription.FOLDERS);
			if (selection == 2)
				filter.setType(type | IResourceFilterDescription.FILES
						| IResourceFilterDescription.FOLDERS);
		}
		if (property.equals(FilterTypeUtil.INHERITABLE)) {
			int type = filter.getType() & ~IResourceFilterDescription.INHERITABLE;
			if (((Boolean) value).booleanValue())
				filter.setType(type | IResourceFilterDescription.INHERITABLE);
			else
				filter.setType(type);
		}
		if (property.equals(FilterTypeUtil.ARGUMENTS)) {
			filter.setArguments(value.equals("") ? null : value); //$NON-NLS-1$
		}
	}

	static IFilterMatcherDescriptor getDescriptor(String id) {
		IFilterMatcherDescriptor[] descriptors = ResourcesPlugin.getWorkspace()
				.getFilterMatcherDescriptors();
		for (int i = 0; i < descriptors.length; i++) {
			if (descriptors[i].getId().equals(id))
				return descriptors[i];
		}
		return null;
	}

	static int getDescriptorIndex(String id) {
		IFilterMatcherDescriptor descriptors[] = ResourcesPlugin.getWorkspace()
				.getFilterMatcherDescriptors();
		for (int i = 0; i < descriptors.length; i++) {
			if (descriptors[i].getId().equals(id))
				return i;
		}
		return -1;
	}

	static Object getValue(UIResourceFilterDescription filter, String property) {
		if (property.equals(ID)) {
			String id = filter.getFileInfoMatcherDescription().getId();
			int index = getDescriptorIndex(id);
			return new Integer(index);
		}
		if (property.equals(MODE)) {
			if ((filter.getType() & IResourceFilterDescription.INCLUDE_ONLY) != 0)
				return new Integer(0);
			return new Integer(1);
		}
		if (property.equals(TARGET)) {
			boolean includeFiles = (filter.getType() & IResourceFilterDescription.FILES) != 0;
			boolean includeFolders = (filter.getType() & IResourceFilterDescription.FOLDERS) != 0;
			if (includeFiles && includeFolders)
				return new Integer(2);
			if (includeFiles)
				return new Integer(0);
			if (includeFolders)
				return new Integer(1);
		}
		if (property.equals(INHERITABLE))
			return new Boolean(
					(filter.getType() & IResourceFilterDescription.INHERITABLE) != 0);

		if (property.equals(ARGUMENTS))
			return filter.getFileInfoMatcherDescription().getArguments() != null ? filter.getFileInfoMatcherDescription().getArguments() : ""; //$NON-NLS-1$
		return null;
	}

	static String[] getTargets() {
		return new String[] {
				NLS.bind(IDEWorkbenchMessages.ResourceFilterPage_files, null),
				NLS.bind(IDEWorkbenchMessages.ResourceFilterPage_folders, null),
				NLS
						.bind(
								IDEWorkbenchMessages.ResourceFilterPage_filesAndFolders,
								null) };
	}

	static String[] getFilterNames(boolean groupOnly) {
		IFilterMatcherDescriptor[] descriptors = ResourcesPlugin.getWorkspace()
				.getFilterMatcherDescriptors();
		sortDescriptors(descriptors);
		LinkedList names = new LinkedList();
		for (int i = 0; i < descriptors.length; i++) {
			// remove legacy filters
			if (descriptors[i].getId().equals(DefaultCustomFilterArgumentUI.REGEX_FILTER_ID))
				continue;
			if (descriptors[i].getId().equals(StringFileInfoMatcher.ID))
				continue;
			boolean isGroup = descriptors[i].getArgumentType().equals(
					IFilterMatcherDescriptor.ARGUMENT_TYPE_FILTER_MATCHER)
					|| descriptors[i].getArgumentType().equals(
							IFilterMatcherDescriptor.ARGUMENT_TYPE_FILTER_MATCHERS);
			if (isGroup == groupOnly)
				names.add(descriptors[i].getName());
		}
		return (String[]) names.toArray(new String[0]);
	}

	/**
	 * @param descriptors
	 */
	private static void sortDescriptors(IFilterMatcherDescriptor[] descriptors) {
		Arrays.sort(descriptors, new Comparator() {
			@Override
			public int compare(Object arg0, Object arg1) {
				if (((IFilterMatcherDescriptor) arg0).getId().equals(FileInfoAttributesMatcher.ID))
					return -1;
				if (((IFilterMatcherDescriptor) arg1).getId().equals(FileInfoAttributesMatcher.ID))
					return 1;
				return ((IFilterMatcherDescriptor) arg0).getId().compareTo(((IFilterMatcherDescriptor) arg1).getId());
			}
		});
	}

	static String getDefaultFilterID() {
		IFilterMatcherDescriptor descriptors[] = ResourcesPlugin.getWorkspace()
				.getFilterMatcherDescriptors();
		sortDescriptors(descriptors);
		for (int i = 0; i < descriptors.length; i++) {
			if (descriptors[i].getArgumentType().equals(
					IFilterMatcherDescriptor.ARGUMENT_TYPE_STRING))
				return descriptors[i].getId();
		}
		return descriptors[0].getId();
	}

	static IFilterMatcherDescriptor getDescriptorFromIndex(int index) {
		IFilterMatcherDescriptor descriptors[] = ResourcesPlugin.getWorkspace()
				.getFilterMatcherDescriptors();
		return descriptors[index];
	}

	static IFilterMatcherDescriptor getDescriptorByName(String name) {
		IFilterMatcherDescriptor[] descriptors = ResourcesPlugin.getWorkspace()
				.getFilterMatcherDescriptors();
		for (int i = 0; i < descriptors.length; i++) {
			if (descriptors[i].getName().equals(name))
				return descriptors[i];
		}
		return null;
	}
	FilterTypeUtil() {}
}

class FilterCopy extends UIResourceFilterDescription {
	Object arguments = null;
	String id = null;
	IPath path = null;
	IProject project = null;
	int type = 0;
	FilterCopy parent = null;
	LinkedList children = null;
	UIResourceFilterDescription original = null;
	int serialNumber = ++lastSerialNumber;
	static private int lastSerialNumber = 0;

	public FilterCopy(UIResourceFilterDescription filter) {
		internalCopy(filter);
		original = filter;
	}

	protected FilterCopy convertLegacyMatchers(FilterCopy copy) {
		if (copy.getId().equals(DefaultCustomFilterArgumentUI.REGEX_FILTER_ID) ||
				copy.getId().equals(StringFileInfoMatcher.ID)) {
			String pattern = (String) copy.getArguments();
			FileInfoAttributesMatcher.Argument argument = new FileInfoAttributesMatcher.Argument();
			argument.key = FileInfoAttributesMatcher.KEY_NAME;
			argument.operator = FileInfoAttributesMatcher.OPERATOR_MATCHES;
			argument.pattern = pattern;
			argument.regularExpression = copy.getId().equals(DefaultCustomFilterArgumentUI.REGEX_FILTER_ID);
			String encodedArgument = FileInfoAttributesMatcher.encodeArguments(argument);
			FilterTypeUtil.setValue(copy, FilterTypeUtil.ID, FileInfoAttributesMatcher.ID);
			FilterTypeUtil.setValue(copy, FilterTypeUtil.ARGUMENTS, encodedArgument);
		}
		return copy;
	}

	public void removeAll() {
		initializeChildren();
		Iterator it = children.iterator();
		while (it.hasNext()) {
			FilterCopy child = (FilterCopy) it.next();
			if (child.parent == this)
				child.parent = null;
		}
		children.clear();
		serializeChildren();
	}

	public void setParent(FilterCopy parent) {
		this.parent = parent;
	}

	public boolean canAcceptDrop() {
		int limit = getChildrenLimit();
		if (limit > 0) {
			FilterCopy[] tmp = getChildren();
			return (tmp == null) || (tmp.length < limit);
		}
		return false;
	}

	public boolean hasParent(FilterCopy filterCopy) {
		FilterCopy filter = this;
		do {
			if (filter.equals(filterCopy))
				return true;
			filter = filter.getParent();
		} while (filter != null);
		return false;
	}

	public FilterCopy getParent() {
		return parent;
	}

	public void copy(UIResourceFilterDescription filter) {
		internalCopy(filter);
		argumentsChanged();
	}

	private void internalCopy(UIResourceFilterDescription filter) {
		children = null;
		id = filter.getFileInfoMatcherDescription().getId();
		path = filter.getPath();
		project = filter.getProject();
		type = filter.getType();
		arguments = filter.getFileInfoMatcherDescription().getArguments();
		if (arguments instanceof FileInfoMatcherDescription[]) {
			FileInfoMatcherDescription[] descs = (FileInfoMatcherDescription[]) arguments;
			FilterCopy [] tmp = new FilterCopy[descs.length];
			for (int i = 0; i < tmp.length; i++)
				tmp[i] = new FilterCopy(this, descs[i]);
			arguments = tmp;
		}
	}

	public boolean hasChanged() {
		if (original != null) {
			return !((arguments == null ? (original.getFileInfoMatcherDescription().getArguments() == null)
					: arguments.equals(original.getFileInfoMatcherDescription().getArguments()))
					&& id.equals(original.getFileInfoMatcherDescription().getId()) && type == original
					.getType());
		}
		return true;
	}

	public FilterCopy() {
		path = null;
		project = null;
		type = IResourceFilterDescription.FILES | IResourceFilterDescription.INCLUDE_ONLY;
		id = FilterTypeUtil.getDefaultFilterID();
	}

	/**
	 * @param parent
	 * @param description
	 */
	public FilterCopy(FilterCopy parent, FileInfoMatcherDescription description) {
		children = null;
		id = description.getId();
		path = parent.getPath();
		project = parent.getProject();
		type = parent.getType();
		arguments = description.getArguments();
		if (arguments instanceof FileInfoMatcherDescription[]) {
			FileInfoMatcherDescription[] descs = (FileInfoMatcherDescription[]) arguments;
			FilterCopy [] tmp = new FilterCopy[descs.length];
			for (int i = 0; i < tmp.length; i++)
				tmp[i] = new FilterCopy(parent, descs[i]);
			arguments = tmp;
		}
	}

	public Object getArguments() {
		return arguments;
	}

	public String getId() {
		return id;
	}

	@Override
	public IPath getPath() {
		return path;
	}

	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public int getType() {
		return type;
	}

	public void setArguments(Object arguments) {
		this.arguments = arguments;
		argumentsChanged();
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setPath(IPath path) {
		this.path = path;
	}

	public void setProject(IProject project) {
		this.project = project;
	}

	public void setType(int type) {
		this.type = type;
	}

	public boolean hasStringArguments() {
		IFilterMatcherDescriptor descriptor = FilterTypeUtil.getDescriptor(id);
		if (descriptor != null)
			return descriptor.getArgumentType().equals(
					IFilterMatcherDescriptor.ARGUMENT_TYPE_STRING);
		return false;
	}

	public int getChildrenLimit() {
		IFilterMatcherDescriptor descriptor = FilterTypeUtil.getDescriptor(id);
		if (descriptor != null) {
			if (descriptor.getArgumentType().equals(
					IFilterMatcherDescriptor.ARGUMENT_TYPE_FILTER_MATCHER))
				return 1;
			if (descriptor.getArgumentType().equals(
					IFilterMatcherDescriptor.ARGUMENT_TYPE_FILTER_MATCHERS))
				return Integer.MAX_VALUE;
		}
		return 0;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof FilterCopy))
			return false;
		FilterCopy filter = (FilterCopy) o;
		return serialNumber == filter.serialNumber;
	}

	public int getSerialNumber() {
		return serialNumber;
	}

	public FilterCopy findBySerialNumber(int number) {
		LinkedList pending = new LinkedList();
		pending.add(this);
		while (!pending.isEmpty()) {
			FilterCopy filter = (FilterCopy) pending.getFirst();
			pending.removeFirst();
			if (filter.serialNumber == number)
				return filter;
			FilterCopy[] tmp = filter.getChildren();
			if (tmp != null)
				pending.addAll(Arrays.asList(tmp));
		}
		return null;
	}

	public FilterCopy[] getChildren() {
		if (getChildrenLimit() > 0) {
			initializeChildren();
			return (FilterCopy[]) children.toArray(new FilterCopy[0]);
		}
		return null;
	}

	protected void initializeChildren() {
		if (children == null) {
			if (getChildrenLimit() > 0) {
				children = new LinkedList();
				Object arguments = getArguments();
				if (arguments instanceof IResourceFilterDescription[]) {
					IResourceFilterDescription[] filters = (IResourceFilterDescription[]) arguments;
					if (filters != null)
						for (int i = 0; i < filters.length; i++) {
							FilterCopy child = new FilterCopy(UIResourceFilterDescription.wrap(filters[i]));
							child.parent = this;
							children.add(child);
						}
				}
				if (arguments instanceof FilterCopy[]) {
					FilterCopy[] filters = (FilterCopy[]) arguments;
					if (filters != null)
						for (int i = 0; i < filters.length; i++) {
							FilterCopy child = filters[i];
							child.parent = this;
							children.add(child);
						}
				}
			}
		}
	}

	protected void addChild(FilterCopy child) {
		initializeChildren();
		if (child.getParent() != null)
			child.getParent().removeChild(child);
		children.add(child);
		child.parent = this;
		serializeChildren();
	}

	protected void removeChild(FilterCopy child) {
		initializeChildren();
		children.remove(child);
		if (child.parent == this)
			child.parent = null;
		serializeChildren();
	}

	protected void serializeChildren() {
		initializeChildren();
		argumentsChanged();
	}

	protected void argumentsChanged() {
		initializeChildren();
		if (children != null)
			arguments = children.toArray(new FilterCopy[0]);
		FilterCopy up = parent;
		while (up != null) {
			up.serializeChildren();
			up = up.parent;
		}
	}

	public boolean isUnderAGroupFilter() {
		// a partial filter is a filter that is located under a group, but not
		// the root group
		if (parent != null) {
			if ((parent.getChildrenLimit() > 0) && (parent.getParent() != null))
				return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceFilterDescription#getFileInfoMatcherDescription()
	 */
	@Override
	public FileInfoMatcherDescription getFileInfoMatcherDescription() {
		
		
		Object arg = FilterCopy.this.getArguments();
		if (arg instanceof FilterCopy []) {
			FilterCopy [] filterCopies = (FilterCopy []) arg;
			FileInfoMatcherDescription[] descriptions = new FileInfoMatcherDescription[filterCopies.length];
			for (int i = 0; i < descriptions.length; i++)
				descriptions[i] = filterCopies[i].getFileInfoMatcherDescription();
			arg = descriptions;
		}
		
		FileInfoMatcherDescription desc = new FileInfoMatcherDescription(getId(), arg);
		return desc;
	}
}

class FilterEditDialog extends TrayDialog {

	private FilterCopy filter;

	protected Button filesButton;
	protected Button foldersButton;
	protected Button filesAndFoldersButton;
	protected Combo idCombo;
	protected Composite idComposite;
	protected Button includeButton;
	protected Button excludeButton;
	protected Composite argumentComposite;
	protected Button inherited;
	protected FilterTypeUtil util;
	protected boolean createGroupOnly;
	protected boolean creatingNewFilter;
	protected ResourceFilterGroup filterGroup;
	protected IResource resource;

	TreeMap/*<String, ICustomFilterArgumentUI */ customfilterArgumentMap = new TreeMap();
	ICustomFilterArgumentUI currentCustomFilterArgumentUI = new ICustomFilterArgumentUI() {
		@Override
		public Object getID() {return "dummy";} //$NON-NLS-1$
		@Override
		public void create(Composite argumentComposite, Font font) {}
		@Override
		public void dispose() {}
		@Override
		public void selectionChanged() {}
		@Override
		public String validate() {return null;}
		@Override
		public StyledString formatStyledText(FilterCopy filter,
				Styler fPlainStyler, Styler fBoldStyler) {return null;}
	};

	/**
	 * Constructor for FilterEditDialog.
	 * @param resource
	 * @param filterGroup
	 * @param parentShell
	 * @param filter
	 * @param createGroupOnly
	 * @param creatingNewFilter
	 */
	public FilterEditDialog(IResource resource, ResourceFilterGroup filterGroup, Shell parentShell, FilterCopy filter, boolean createGroupOnly, boolean creatingNewFilter) {
		super(parentShell);
		this.resource = resource;
		this.creatingNewFilter = creatingNewFilter;
		this.filterGroup = filterGroup;
		this.filter = filter;
		this.createGroupOnly = createGroupOnly;
		util = new FilterTypeUtil();
		ICustomFilterArgumentUI ui = new MultiMatcherCustomFilterArgumentUI(this, parentShell, filter);
		customfilterArgumentMap.put(ui.getID(), ui);
		ui = new DefaultCustomFilterArgumentUI(this, parentShell, filter);
		customfilterArgumentMap.put(ui.getID(), ui);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		parent.setLayoutData(data);

		Font font = parent.getFont();
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = 0;
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		composite.setLayout(layout);
		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		composite.setLayoutData(data);
		composite.setFont(font);

		Dialog.applyDialogFont(composite);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getShell(),
				IIDEHelpContextIds.EDIT_RESOURCE_FILTER_PROPERTY_PAGE);

		if (!filter.isUnderAGroupFilter()) {
			Composite topComposite = new Composite(composite, SWT.NONE);
			layout = new GridLayout();
			layout.numColumns = 1;
			layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
			layout.marginWidth = 0;
			layout.marginBottom = 0;
			layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
			layout.horizontalSpacing = 0; // convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
			topComposite.setLayout(layout);
			data = new GridData(SWT.FILL, SWT.FILL, true, false);
			topComposite.setLayoutData(data);
			topComposite.setFont(font);

			createModeArea(font, topComposite);

			createTargetArea(font, topComposite);
			
			createIdArea(font, topComposite);
		}
		else {
			layout.marginHeight = convertHorizontalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
			createIdArea(font, composite);
		}
		return composite;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TrayDialog#createButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createButtonBar(Composite parent) {
		Label label = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		label.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		Composite composite = new Composite(parent, SWT.NONE);
    	GridLayout layout = new GridLayout();
    	layout.marginWidth = 0;
    	layout.marginHeight = 0;
    	layout.horizontalSpacing = 0;
    	composite.setLayout(layout);
    	composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
    	composite.setFont(parent.getFont());

		// create help control if needed
        if (isHelpAvailable()) {
        	Control helpControl = createHelpControl(composite);
        	((GridData) helpControl.getLayoutData()).horizontalIndent = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		}
       
        Control buttonSection = dialogCreateButtonBar(composite);
        ((GridData) buttonSection.getLayoutData()).grabExcessHorizontalSpace = true;
        return composite;
	}

	private Control dialogCreateButtonBar(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		// create a layout with spacing and margins appropriate for the font
		// size.
		GridLayout layout = new GridLayout();
		layout.numColumns = 0; // this is incremented by createButton
		layout.makeColumnsEqualWidth = true;
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		composite.setLayout(layout);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_END
				| GridData.VERTICAL_ALIGN_CENTER);
		composite.setLayoutData(data);
		composite.setFont(parent.getFont());
		
		// Add the buttons to the button bar.
		createButtonsForButtonBar(composite);
		return composite;
	}

	/**
	 * @param font
	 * @param composite
	 */
	private void createInheritableArea(Font font, Composite composite) {
		Composite inheritableComposite = createGroup(font, composite, NLS.bind(
				IDEWorkbenchMessages.ResourceFilterPage_columnFilterMode,
				null), false, false, 1);
		GridLayout layout = (GridLayout) inheritableComposite.getLayout();
		layout.marginBottom = 0;
		layout.marginTop = 0;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		layout.marginLeft = 0;
		inheritableComposite.setLayout(layout);

		GridData data;
		inherited = new Button(inheritableComposite, SWT.CHECK);
		String label;
		label = IDEWorkbenchMessages.ResourceFilterPage_applyRecursivelyToFolderStructure;
		
		inherited
				.setText(NLS
						.bind(
								label,
								resource.getName()));
		data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		data.horizontalSpan = 1;
		inherited.setLayoutData(data);
		inherited.setFont(font);
		inherited.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FilterTypeUtil.setValue(filter, FilterTypeUtil.INHERITABLE,
						new Boolean(inherited.getSelection()));
			}
		});
		inherited.setSelection((((Boolean) FilterTypeUtil.getValue(filter,
				FilterTypeUtil.INHERITABLE)).booleanValue()));
	}

	private void createModeArea(Font font, Composite composite) {
		GridData data;
		Composite modeComposite = createGroup(font, composite, NLS.bind(
				IDEWorkbenchMessages.ResourceFilterPage_columnFilterMode,
				null), true, true, 1);
		String[] modes = FilterTypeUtil.getModes();
		includeButton = new Button(modeComposite, SWT.RADIO);
		includeButton.setText(modes[0]);
		data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		includeButton.setLayoutData(data);
		includeButton.setFont(font);
		includeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FilterTypeUtil.setValue(filter, FilterTypeUtil.MODE,
						new Integer(0));
			}
		});
		includeButton.setSelection(((Integer) FilterTypeUtil.getValue(
				filter, FilterTypeUtil.MODE)).intValue() == 0);
		excludeButton = new Button(modeComposite, SWT.RADIO);
		excludeButton.setText(modes[1]);
		data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		excludeButton.setLayoutData(data);
		excludeButton.setFont(font);
		excludeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FilterTypeUtil.setValue(filter, FilterTypeUtil.MODE,
						new Integer(1));
			}
		});
		excludeButton.setSelection(((Integer) FilterTypeUtil.getValue(
				filter, FilterTypeUtil.MODE)).intValue() == 1);
	}

	private void createIdArea(Font font, Composite composite) {
		if (createGroupOnly) {
			idComposite = createGroup(font, composite, new String(),
					true, true, 1);
			createMatcherCombo(idComposite, font);
			GridData data = new GridData(SWT.FILL, SWT.CENTER, true, false);
			idCombo.setLayoutData(data);
		}
		else {
			String [] matchers = FilterTypeUtil.getFilterNames(createGroupOnly);
			boolean hasMoreThanOneMatcher = matchers.length > 1;

			if (hasMoreThanOneMatcher) {
				createMatcherCombo(composite, font);
				GridData data = new GridData(SWT.LEFT, SWT.CENTER, false, false);
				idCombo.setLayoutData(data);
			}

			idComposite = createGroup(font, composite,
					hasMoreThanOneMatcher ? IDEWorkbenchMessages.ResourceFilterPage_details: matchers[0],
					true, true, 1);

			GridLayout layout = (GridLayout) idComposite.getLayout();
			layout.marginBottom = 0;
			layout.marginTop = 0;
			layout.marginWidth = 0;
			layout.marginHeight = 0;
			layout.verticalSpacing = 0;
			idComposite.setLayout(layout);
		}
		argumentComposite = new Composite(idComposite, SWT.NONE);
		setupPatternLine();
	}

	private void createMatcherCombo(Composite composite, Font font) {
		GridData data;
		idCombo = new Combo(composite, SWT.READ_ONLY);
		idCombo.setItems(FilterTypeUtil.getFilterNames(createGroupOnly));
		data = new GridData(SWT.LEFT, SWT.CENTER, true, false);
		idCombo.setLayoutData(data);
		idCombo.setFont(font);
		idCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FilterTypeUtil.setValue(filter, FilterTypeUtil.ID, idCombo
						.getItem(idCombo.getSelectionIndex()));
				if (filter.hasStringArguments())
					filter.setArguments(new String());
				setupPatternLine();
				currentCustomFilterArgumentUI.selectionChanged();
				getShell().layout(true);
				Point size = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT);
				Point shellSize = getShell().getSize();
				size.x = Math.max(size.x, shellSize.x);
				size.y = Math.max(size.y, shellSize.y);
				getShell().setSize(size);
				getShell().redraw();
			}
		});
		idCombo.select(0);
		selectComboItem(filter.getId());
		FilterTypeUtil.setValue(filter, FilterTypeUtil.ID, idCombo
				.getItem(idCombo.getSelectionIndex()));
	}
	

	ICustomFilterArgumentUI getUI(String descriptorID) {
		ICustomFilterArgumentUI result = (ICustomFilterArgumentUI) customfilterArgumentMap.get(descriptorID);
		if (result == null)
			return result = (ICustomFilterArgumentUI) customfilterArgumentMap.get(new String()); // default ui
		return result;
	}
	
	private void setupPatternLine() {
		IFilterMatcherDescriptor descriptor;
		if (createGroupOnly) {
			String item = idCombo.getItem(idCombo.getSelectionIndex());
			descriptor = FilterTypeUtil.getDescriptorByName(item);
		}
		else
			descriptor = FilterTypeUtil.getDescriptor(filter.getId());
		Font font = idComposite.getFont();
		ICustomFilterArgumentUI customFilterArgumentUI = getUI(descriptor.getId());
		if (!currentCustomFilterArgumentUI.getID().equals(customFilterArgumentUI.getID())) {
			currentCustomFilterArgumentUI.dispose();
			currentCustomFilterArgumentUI = customFilterArgumentUI;
			currentCustomFilterArgumentUI.create(argumentComposite, font);
		}
	}

	/**
	 * 
	 */
	private void selectComboItem(String filterID) {
		IFilterMatcherDescriptor descriptor = ResourcesPlugin.getWorkspace()
		.getFilterMatcherDescriptor(filterID);
		if (descriptor != null) {
			String [] items = idCombo.getItems();
			for (int i = 0; i < items.length; i++) {
				if (items[i].equals(descriptor.getName())) {
					idCombo.select(i);
					break;
				}
			}
		}
	}

	/**
	 * @param font
	 * @param composite
	 * @return the group
	 */
	private Composite createGroup(Font font, Composite composite, String text,
			boolean grabExcessVerticalSpace, boolean group, int columnCounts) {
		GridLayout layout;
		GridData data;
		Composite modeComposite;
		if (group) {
			Group modeGroup = new Group(composite, SWT.NONE);
			modeGroup.setText(text);
			modeComposite = modeGroup;
		} else {
			modeComposite = new Composite(composite, SWT.NONE);
		}
		layout = new GridLayout();
		layout.verticalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.marginLeft = 2;
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.numColumns = columnCounts;
		modeComposite.setLayout(layout);
		data = new GridData(SWT.FILL, SWT.FILL, true, grabExcessVerticalSpace);
		modeComposite.setLayoutData(data);
		modeComposite.setFont(font);
		return modeComposite;
	}

	/**
	 * @param font
	 * @param composite
	 */
	private void createTargetArea(Font font, Composite composite) {
		GridData data;
		Composite targetComposite = createGroup(font, composite,
				NLS.bind(IDEWorkbenchMessages.ResourceFilterPage_columnFilterTarget, null), false, true, 1);
		
		String[] targets = FilterTypeUtil.getTargets();
		filesButton = new Button(targetComposite, SWT.RADIO);
		filesButton.setText(targets[0]);
		data = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		filesButton.setLayoutData(data);
		filesButton.setFont(font);

		foldersButton = new Button(targetComposite, SWT.RADIO);
		foldersButton.setText(targets[1]);
		data = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		foldersButton.setLayoutData(data);
		foldersButton.setFont(font);

		filesAndFoldersButton = new Button(targetComposite, SWT.RADIO);
		filesAndFoldersButton.setText(targets[2]);
		data = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		filesAndFoldersButton.setLayoutData(data);
		filesAndFoldersButton.setFont(font);

		filesButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FilterTypeUtil.setValue(filter, FilterTypeUtil.TARGET,
						new Integer(0));
			}
		});
		foldersButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FilterTypeUtil.setValue(filter, FilterTypeUtil.TARGET,
						new Integer(1));
			}
		});
		filesAndFoldersButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				FilterTypeUtil.setValue(filter, FilterTypeUtil.TARGET,
						new Integer(2));
			}
		});
		filesButton.setSelection(((Integer) FilterTypeUtil.getValue(filter,
				FilterTypeUtil.TARGET)).intValue() == 0);
		foldersButton.setSelection(((Integer) FilterTypeUtil.getValue(filter,
				FilterTypeUtil.TARGET)).intValue() == 1);
		filesAndFoldersButton.setSelection(((Integer) FilterTypeUtil.getValue(
				filter, FilterTypeUtil.TARGET)).intValue() == 2);
		createInheritableArea(font, targetComposite);
	}

	@Override
	protected Control createContents(Composite parent) {
		Control control = super.createContents(parent);
		initialize();
		update();
		return control;
	}

	public void updateFinishControls() {
		if (getButton(OK) != null) {
			if (currentCustomFilterArgumentUI != null)
				getButton(OK).setEnabled(currentCustomFilterArgumentUI.validate() == null);
			else
				getButton(OK).setEnabled(true);
		}
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#isResizable()
	 */
	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void configureShell(Shell newShell) {
		String title = null;
		if (creatingNewFilter) {
			if (resource.getType() == IResource.PROJECT)
				title = NLS.bind(IDEWorkbenchMessages.ResourceFilterPage_newFilterDialogTitleProject, resource.getName());
			else
				title = NLS.bind(IDEWorkbenchMessages.ResourceFilterPage_newFilterDialogTitleFolder, resource.getName());
		}
		else
			title = IDEWorkbenchMessages.ResourceFilterPage_editFilterDialogTitle;
		newShell.setText(title);
		super.configureShell(newShell);
	}

	private void initialize() {
	}

	protected void update() {
	}

	@Override
	protected void okPressed() {
		// see if the initialize causes an exception
		if (filter.hasStringArguments()) {
			IFilterMatcherDescriptor desc = resource.getWorkspace().getFilterMatcherDescriptor(filter.getId());
			if (desc != null) {
				try {
					currentCustomFilterArgumentUI.validate();
					AbstractFileInfoMatcher matcher = ((FilterDescriptor) desc).createFilter();
					matcher.initialize(resource.getProject(), filter.getArguments());
				} catch (CoreException e) {
					IWorkbenchWindow window = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow();
					if (window != null) {
						ErrorDialog.openError(window.getShell(), IDEWorkbenchMessages.ResourceFilterPage_editFilterDialogTitle,
								e.getMessage(), e.getStatus());
					}
					return;
				}
			}
		}

		super.okPressed();
	}

	public int getVerticalDLUsToPixel(int flag) {
		return convertVerticalDLUsToPixels(flag);
	}

	public IResource getResource() {
		return resource;
	}

	public int getHorizontalDLUsToPixel(int flag) {
		return convertHorizontalDLUsToPixels(flag);
	}
}

interface ICustomFilterArgumentUI {

	/**
	 * @return the descriptor ID
	 */
	Object getID();

	/**
	 * @param filter
	 * @param fPlainStyler
	 * @param fBoldStyler
	 * @return the formatted StyledText
	 */
	StyledString formatStyledText(FilterCopy filter, Styler fPlainStyler,
			Styler fBoldStyler);

	/**
	 * @return null if there's no issue
	 * 
	 */
	String validate();

	/**
	 * 
	 */
	void selectionChanged();

	/**
	 * @param argumentComposite
	 * @param font
	 */
	void create(Composite argumentComposite, Font font);

	/**
	 * 
	 */
	void dispose();
	
}

class MultiMatcherCustomFilterArgumentUI implements ICustomFilterArgumentUI {

	Shell shell;
	FilterCopy filter;
	protected Button argumentsCaseSensitive;
	protected Button argumentsRegularExpresion;
	protected Text arguments;
	protected DateTime argumentsDate;
	protected Combo argumentsBoolean;
	protected Label argumentsLabel;
	protected Label description;
	protected ContentAssistCommandAdapter fContentAssistField;
	protected Combo multiKey;
	protected Combo multiOperator;
	protected Composite multiArgumentComposite;
	protected Composite conditionComposite;
	protected Composite descriptionComposite;
	protected Composite stringArgumentComposite;
	protected Composite stringTextArgumentComposite;
	protected Composite attributeStringArgumentComposite;
	protected Class intiantiatedKeyOperatorType = null;
	protected TreeMap/* <String, String>*/ valueCache = new TreeMap();
	protected boolean initializationComplete = false;
	protected FilterEditDialog dialog;
	protected Label dummyLabel1;
	protected Label dummyLabel2;
	
	/**
	 * @param dialog
	 * @param parentShell
	 * @param filter
	 */
	public MultiMatcherCustomFilterArgumentUI(FilterEditDialog dialog, Shell parentShell,
			FilterCopy filter) {
		this.shell = parentShell;
		this.dialog = dialog;
		this.filter = filter;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.ide.dialogs.ICustomFilterArgumentUI#getID()
	 */
	@Override
	public Object getID() {
		return FileInfoAttributesMatcher.ID;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.ide.dialogs.ICustomFilterArgumentUI#dispose()
	 */
	@Override
	public void dispose() {
		Widget list[] = new Widget[] {multiKey, multiOperator, multiArgumentComposite, stringArgumentComposite, stringTextArgumentComposite, arguments, argumentsLabel, argumentsCaseSensitive, argumentsRegularExpresion, attributeStringArgumentComposite, description, conditionComposite, descriptionComposite, dummyLabel1, dummyLabel2};
		for (int i = 0; i < list.length; i++) {
			if (list[i] != null) {
				list[i].dispose();
			}
		}
		multiKey = null;
		multiOperator = null;
		multiArgumentComposite = null;
		arguments = null;
		argumentsLabel = null;
		fContentAssistField = null;
		intiantiatedKeyOperatorType = null;
		stringArgumentComposite = null;
		stringTextArgumentComposite = null;
		argumentsCaseSensitive = null;
		argumentsRegularExpresion = null;
		attributeStringArgumentComposite = null;
		description = null;
		conditionComposite = null;
		descriptionComposite = null;
		dummyLabel1 = null;
		dummyLabel2 = null;
		initializationComplete = false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.ide.dialogs.ICustomFilterArgumentUI#create(org.eclipse.swt.widgets.Composite, org.eclipse.swt.graphics.Font)
	 */
	@Override
	public void create(Composite argumentComposite, Font font) {
		shell = argumentComposite.getShell();
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		argumentComposite.setLayout(layout);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		argumentComposite.setLayoutData(data);
		argumentComposite.setFont(font);
		
		conditionComposite = new Composite(argumentComposite, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 0;
		conditionComposite.setLayout(layout);
		conditionComposite.setFont(font);
		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		conditionComposite.setLayoutData(data);

		createCustomArgumentsArea(font, conditionComposite);
		
		descriptionComposite = new Composite(argumentComposite, SWT.NONE);
		layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginWidth = dialog.getHorizontalDLUsToPixel(IDialogConstants.HORIZONTAL_MARGIN);
		layout.marginHeight = dialog.getVerticalDLUsToPixel(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = 0;
		descriptionComposite.setLayout(layout);
		descriptionComposite.setFont(font);
		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		descriptionComposite.setLayoutData(data);
		
		createDescriptionArea(font, descriptionComposite);
		initializationComplete = true;
	}
	
	/**
	 * @param font
	 * @param composite
	 */
	private void createDescriptionArea(Font font, Composite composite) {
		GridData data;
		description = new Label(composite, SWT.LEFT | SWT.WRAP);
		data = new GridData(SWT.FILL, SWT.BEGINNING, true, true);
		data.horizontalSpan = 3;
		description.setLayoutData(data);
		description.setFont(font);
		setupDescriptionText(null);
	}

	private void setupDescriptionText(String errorString) {
		if (description != null) {
			if (errorString != null) {
				// take only the first line of the error string
				BufferedReader reader = new BufferedReader(new StringReader(errorString));
				try {
					String tmp = reader.readLine();
					if (tmp != null)
						errorString = tmp;
				} catch (IOException e) {
				}

				description.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_RED));
				description.setText(errorString);
			} else {
				description.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_BLACK));
				String selectedKey = MultiMatcherLocalization.getMultiMatcherKey(multiKey.getText());
				String selectedOperator = MultiMatcherLocalization.getMultiMatcherKey(multiOperator.getText());
				Class selectedKeyOperatorType = FileInfoAttributesMatcher.getTypeForKey(selectedKey, selectedOperator);
				description.setText(new String());
				if (selectedKeyOperatorType.equals(String.class)) {
					if (!argumentsRegularExpresion.getSelection())
						description.setText(NLS.bind(
								IDEWorkbenchMessages.ResourceFilterPage_multiMatcher_Matcher, null));
				}
				if (selectedKeyOperatorType.equals(Integer.class)) {
					if (selectedKey.equals(FileInfoAttributesMatcher.KEY_LAST_MODIFIED) || selectedKey.equals(FileInfoAttributesMatcher.KEY_CREATED))
						description.setText(NLS.bind(
								IDEWorkbenchMessages.ResourceFilterPage_multiMatcher_TimeInterval, null));
					else
					description.setText(NLS.bind(
							IDEWorkbenchMessages.ResourceFilterPage_multiMatcher_FileLength, null));
				}
			}
			shell.layout(true, true);
		}
	}
	
	private void createCustomArgumentsArea(Font font, Composite composite) {
		GridData data;

		multiArgumentComposite = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		layout.marginWidth = dialog.getHorizontalDLUsToPixel(IDialogConstants.HORIZONTAL_MARGIN);
		layout.horizontalSpacing = dialog.getHorizontalDLUsToPixel(IDialogConstants.HORIZONTAL_SPACING);
		layout.marginTop = dialog.getVerticalDLUsToPixel(IDialogConstants.VERTICAL_SPACING);
		layout.marginHeight = 0;
		multiArgumentComposite.setLayout(layout);
		multiArgumentComposite.setFont(font);
		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		multiArgumentComposite.setLayoutData(data);
		
		multiKey = new Combo(multiArgumentComposite, SWT.READ_ONLY);
		multiKey.setItems(getMultiMatcherKeys());
		data = new GridData(SWT.LEFT, SWT.TOP, false, false);
		multiKey.setLayoutData(data);
		multiKey.setFont(font);
		multiKey.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setupMultiOperatorAndField(true);
				storeMultiSelection();
			}
		});
		
		// calculate max combo width
		ArrayList allOperators = new ArrayList();
		String[] keys = getMultiMatcherKeys();
		for (int i = 0; i < keys.length; i++) {
			allOperators.addAll(Arrays.asList(getLocalOperatorsForKey(MultiMatcherLocalization.getMultiMatcherKey(keys[i]))));
		}
		Combo tmp = new Combo(multiArgumentComposite, SWT.READ_ONLY);
		tmp.setItems((String []) allOperators.toArray(new String[0]));
		int maxWidth = tmp.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		tmp.dispose();

		multiOperator = new Combo(multiArgumentComposite, SWT.READ_ONLY);
		data = new GridData(SWT.LEFT, SWT.TOP, false, false);
		data.widthHint = maxWidth;
		multiOperator.setLayoutData(data);
		multiOperator.setFont(font);
		multiOperator.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setupMultiOperatorAndField(false);
				storeMultiSelection();
			}
		});
		
		FileInfoAttributesMatcher.Argument argument = FileInfoAttributesMatcher.decodeArguments((String) filter.getArguments());
		String local = MultiMatcherLocalization.getLocalMultiMatcherKey(argument.key);
		int index = multiKey.indexOf(local);
		if (index != -1)
			multiKey.select(index);
		else
			multiKey.select(0);
		
		setupMultiOperatorAndField(true);
	}

	private void setupMultiOperatorAndField(boolean updateOperator) {
		boolean isUsingRegularExpression = false;
		String selectedKey = MultiMatcherLocalization.getMultiMatcherKey(multiKey.getText());
		if (updateOperator) {
			String[] operators = getLocalOperatorsForKey(selectedKey);
			multiOperator.setItems(operators);
			FileInfoAttributesMatcher.Argument argument = FileInfoAttributesMatcher.decodeArguments((String) filter.getArguments());
			String local = MultiMatcherLocalization.getLocalMultiMatcherKey(argument.operator);
			int index = multiOperator.indexOf(local);
			if (index != -1)
				multiOperator.select(index);
			else
				multiOperator.select(0);
		}
		String selectedOperator = MultiMatcherLocalization.getMultiMatcherKey(multiOperator.getText());
		
		Class selectedKeyOperatorType = FileInfoAttributesMatcher.getTypeForKey(selectedKey, selectedOperator);
		
		if (intiantiatedKeyOperatorType != null) {
			if (arguments != null) {
				arguments.dispose();
				arguments = null;
			}
			if (attributeStringArgumentComposite != null) {
				attributeStringArgumentComposite.dispose();
				attributeStringArgumentComposite = null;
			}
			if (stringArgumentComposite != null) {
				stringArgumentComposite.dispose();
				stringArgumentComposite = null;
			}
			if (argumentsBoolean != null) {
				argumentsBoolean.dispose();
				argumentsBoolean = null;
			}
			if (argumentsDate != null) {
				argumentsDate.dispose();
				argumentsDate = null;
			}
			if (argumentsRegularExpresion != null) {
				argumentsRegularExpresion.dispose();
				argumentsRegularExpresion = null;
			}
			if (argumentsCaseSensitive != null) {
				argumentsCaseSensitive.dispose();
				argumentsCaseSensitive = null;
			}
			if (dummyLabel1 != null) {
				dummyLabel1.dispose();
				dummyLabel1 = null;
			}
			if (dummyLabel2 != null) {
				dummyLabel2.dispose();
				dummyLabel2 = null;
			}
			fContentAssistField = null;
			FileInfoAttributesMatcher.Argument argument = FileInfoAttributesMatcher.decodeArguments((String) filter.getArguments());
			valueCache.put(intiantiatedKeyOperatorType.getName(), argument.pattern);
			argument.pattern = (String) valueCache.get(selectedKeyOperatorType.getName());
			if (argument.pattern == null)
				argument.pattern = new String();
			filter.setArguments(FileInfoAttributesMatcher.encodeArguments(argument));
		}

		if (selectedKeyOperatorType.equals(String.class)) {
	
			arguments = new Text(multiArgumentComposite, SWT.SINGLE | SWT.BORDER);
			GridData data= new GridData(SWT.FILL, SWT.FILL, true, false);
			data.widthHint = 150;
			arguments.setLayoutData(data);
			arguments.setFont(multiArgumentComposite.getFont());
			arguments.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					validateInputText();
				}
			});

			dummyLabel1 = new Label(multiArgumentComposite, SWT.NONE);
			data = new GridData(SWT.LEFT, SWT.CENTER, true, true);
			dummyLabel1.setText(new String());
			data.horizontalSpan = 1;
			dummyLabel1.setLayoutData(data);

			dummyLabel2 = new Label(multiArgumentComposite, SWT.NONE);
			data = new GridData(SWT.LEFT, SWT.CENTER, true, true);
			dummyLabel2.setText(new String());
			data.horizontalSpan = 1;
			dummyLabel2.setLayoutData(data);

			stringArgumentComposite = new Composite(multiArgumentComposite, SWT.NONE);
			
			GridLayout layout = new GridLayout();
			layout.numColumns = 2;
			layout.marginWidth = 0;
			layout.marginTop = dialog.getVerticalDLUsToPixel(IDialogConstants.VERTICAL_SPACING) / 2;
			layout.marginHeight = 0;
			layout.marginBottom = 0;
			stringArgumentComposite.setLayout(layout);
			data = new GridData(SWT.FILL, SWT.CENTER, true, true);
			data.horizontalSpan = 1;
			stringArgumentComposite.setLayoutData(data);
			stringArgumentComposite.setFont(multiArgumentComposite.getFont());

			argumentsCaseSensitive = new Button(stringArgumentComposite, SWT.CHECK);
			argumentsCaseSensitive.setText(NLS.bind(
					IDEWorkbenchMessages.ResourceFilterPage_caseSensitive, null));
			data = new GridData(SWT.LEFT, SWT.CENTER, false, false);
			argumentsCaseSensitive.setLayoutData(data);
			argumentsCaseSensitive.setFont(multiArgumentComposite.getFont());

			argumentsRegularExpresion = new Button(stringArgumentComposite, SWT.CHECK);
			argumentsRegularExpresion.setText(NLS.bind(
					IDEWorkbenchMessages.ResourceFilterPage_regularExpression, null));
			data = new GridData(SWT.LEFT, SWT.CENTER, false, false);
			data.minimumWidth = 100;
			argumentsRegularExpresion.setLayoutData(data);
			argumentsRegularExpresion.setFont(multiArgumentComposite.getFont());

			if (filter.hasStringArguments()) {
				FileInfoAttributesMatcher.Argument argument = FileInfoAttributesMatcher.decodeArguments((String) filter.getArguments());
				arguments.setText(argument.pattern);
				isUsingRegularExpression = argument.regularExpression;
				argumentsCaseSensitive.setSelection(argument.caseSensitive);
				argumentsRegularExpresion.setSelection(argument.regularExpression);
			}

			arguments.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					storeMultiSelection();
				}
			});
			argumentsRegularExpresion.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					setupDescriptionText(null);
					storeMultiSelection();
					if (fContentAssistField != null)
						fContentAssistField.setEnabled(argumentsRegularExpresion.getSelection());
				}
			});
			argumentsCaseSensitive.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					storeMultiSelection();
				}
			});

			TextContentAdapter contentAdapter= new TextContentAdapter();
			FindReplaceDocumentAdapterContentProposalProvider findProposer= new FindReplaceDocumentAdapterContentProposalProvider(true);
			fContentAssistField= new ContentAssistCommandAdapter(
					arguments,
					contentAdapter,
					findProposer,
					null,
					new char[] {'\\', '[', '('},
					true);
		}
		if (selectedKeyOperatorType.equals(Integer.class)) {
			GridData data;
			arguments = new Text(multiArgumentComposite, SWT.SINGLE | SWT.BORDER);
			data= new GridData(SWT.FILL, SWT.FILL, true, false);
			data.widthHint = 150;
			arguments.setLayoutData(data);
			arguments.setFont(multiArgumentComposite.getFont());
			arguments.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					validateInputText();
				}
			});

			if (filter.hasStringArguments()) {
				FileInfoAttributesMatcher.Argument argument = FileInfoAttributesMatcher.decodeArguments((String) filter.getArguments());
				if (selectedKey.equals(FileInfoAttributesMatcher.KEY_LAST_MODIFIED) || selectedKey.equals(FileInfoAttributesMatcher.KEY_CREATED))
					arguments.setText(convertToEditableTimeInterval(argument.pattern));
				else
					arguments.setText(convertToEditableLength(argument.pattern));
			}
			
			arguments.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					storeMultiSelection();
				}
			});
		}
		if (selectedKeyOperatorType.equals(Date.class)) {
			GridData data;
			argumentsDate = new DateTime(multiArgumentComposite, SWT.DATE | SWT.MEDIUM | SWT.BORDER);
			data= new GridData(SWT.FILL, SWT.FILL, true, false);
			argumentsDate.setLayoutData(data);
			argumentsDate.setFont(multiArgumentComposite.getFont());
			argumentsDate.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					storeMultiSelection();
				}
			});
			if (filter.hasStringArguments()) {
				FileInfoAttributesMatcher.Argument argument = FileInfoAttributesMatcher.decodeArguments((String) filter.getArguments());
				Date date;
				Calendar calendar = Calendar.getInstance();
				try {
					date = new Date(Long.parseLong(argument.pattern));
					calendar.setTime(date);
				} catch (NumberFormatException e1) {
					date = new Date();
					calendar.setTime(date);
					argument.pattern = Long.toString(calendar.getTimeInMillis());
				}
				argumentsDate.setDay(calendar.get(Calendar.DAY_OF_MONTH));
				argumentsDate.setMonth(calendar.get(Calendar.MONTH));
				argumentsDate.setYear(calendar.get(Calendar.YEAR));
			}
		}
		if (selectedKeyOperatorType.equals(Boolean.class)) {
			GridData data;
			argumentsBoolean = new Combo(multiArgumentComposite, SWT.READ_ONLY);
			data = new GridData(SWT.FILL, SWT.TOP, true, false);
			argumentsBoolean.setLayoutData(data);
			argumentsBoolean.setFont(multiArgumentComposite.getFont());
			argumentsBoolean.setItems(new String[] {MultiMatcherLocalization.getLocalMultiMatcherKey(Boolean.TRUE.toString()), MultiMatcherLocalization.getLocalMultiMatcherKey(Boolean.FALSE.toString())});
			argumentsBoolean.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					storeMultiSelection();
				}
			});
			if (filter.hasStringArguments()) {
				FileInfoAttributesMatcher.Argument argument = FileInfoAttributesMatcher.decodeArguments((String) filter.getArguments());
				if (argument.pattern.length() == 0)
					argumentsBoolean.select(0);
				else
					argumentsBoolean.select(Boolean.valueOf(argument.pattern).booleanValue() ? 0:1);
			}
		}
		intiantiatedKeyOperatorType = selectedKeyOperatorType;
		
		if (fContentAssistField != null)
			fContentAssistField.setEnabled(isUsingRegularExpression);

		shell.layout(true, true);
		if (initializationComplete) {
			Point size = shell.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			Point shellSize = shell.getSize();
			size.x = Math.max(size.x, shellSize.x);
			size.y = Math.max(size.y, shellSize.y);
			if ((size.x > shellSize.x) || (size.y > shellSize.y))
				shell.setSize(size);
		}
		shell.redraw();
		setupDescriptionText(null);
	}

	private String[] timeIntervalPrefixes = {"s", "m", "h", "d"};    //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$ //$NON-NLS-4$
	private double[] timeIntervalScale = {60, 60, 24};

	private String convertToEditableTimeInterval(String string) {
		if (string.length() == 0)
			return string;
		long value;
		try {
			value = Long.parseLong(string);
		} catch (NumberFormatException e) {
			value = 0;
		}
		if (value == 0)
			return Long.toString(0);
		for (int i = 0; i < timeIntervalPrefixes.length - 1; i++) {
			if (value % timeIntervalScale[i] != 0)
				return Long.toString(value) + timeIntervalPrefixes[i];
			value /= timeIntervalScale[i];
		}
		return Long.toString(value) + timeIntervalPrefixes[timeIntervalPrefixes.length - 1];
	}

	private String convertFromEditableTimeInterval(String string) {
		if (string.length() == 0)
			return string;
		for (int i = 1; i < timeIntervalPrefixes.length; i++) {
			if (string.endsWith(timeIntervalPrefixes[i])) {
				long value = Long.parseLong(string.substring(0, string.length() - 1));
				for (int j = 0; j < i; j++)
					value *= timeIntervalScale[j];
				return Long.toString(value);
			}
		}
		// seems equivalent to "return string", but it throws an exception if the string doesn't contain a valid number
		return Long.toString(Long.parseLong(string));
	}


	private String[] lengthPrefixes = {new String(), "k", "m", "g"};    //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$

	// converts "32768" to "32k"
	private String convertToEditableLength(String string) {
		if (string.length() == 0)
			return string;
		long value;
		try {
			value = Long.parseLong(string);
		} catch (NumberFormatException e) {
			value = 0;
		}
		if (value == 0)
			return Long.toString(0);
		for (int i = 0; i < lengthPrefixes.length; i++) {
			if (value % 1024 != 0)
				return Long.toString(value) + lengthPrefixes[i];
			if ((i + 1) < lengthPrefixes.length)
				value /= 1024;
		}
		return Long.toString(value) + lengthPrefixes[lengthPrefixes.length - 1];
	}
	
	// converts "32k" to "32768"
	private String convertFromEditableLength(String string) throws NumberFormatException {
		if (string.length() == 0)
			return string;
		for (int i = 1; i < lengthPrefixes.length; i++) {
			if (string.endsWith(lengthPrefixes[i])) {
				long value = Long.parseLong(string.substring(0, string.length() - 1));
				value *= Math.pow(1024, i);
				return Long.toString(value);
			}
		}
		// seems equivalent to "return string", but it throws an exception if the string doesn't contain a valid number
		return Long.toString(Long.parseLong(string));
	}
	
	private void storeMultiSelection() {
		if (intiantiatedKeyOperatorType != null) {
			String selectedKey = MultiMatcherLocalization.getMultiMatcherKey(multiKey.getText());
			String selectedOperator = MultiMatcherLocalization.getMultiMatcherKey(multiOperator.getText());
	
			FileInfoAttributesMatcher.Argument argument = new FileInfoAttributesMatcher.Argument();
			argument.key = selectedKey;
			argument.operator = selectedOperator;
			
			if (intiantiatedKeyOperatorType.equals(Date.class) && argumentsDate != null) {
				Calendar calendar = Calendar.getInstance();
				calendar.set(argumentsDate.getYear(), argumentsDate.getMonth(), argumentsDate.getDay());
				argument.pattern = Long.toString(calendar.getTimeInMillis());
			}
			if (intiantiatedKeyOperatorType.equals(String.class) && arguments != null) {
				argument.pattern = arguments.getText();
				if (argumentsRegularExpresion != null)
					argument.regularExpression = argumentsRegularExpresion.getSelection();
				if (argumentsCaseSensitive != null)
					argument.caseSensitive = argumentsCaseSensitive.getSelection();
			}
			if (intiantiatedKeyOperatorType.equals(Integer.class) && arguments != null) {
				try {
					if (selectedKey.equals(FileInfoAttributesMatcher.KEY_LAST_MODIFIED) || selectedKey.equals(FileInfoAttributesMatcher.KEY_CREATED))
						argument.pattern = convertFromEditableTimeInterval(arguments.getText());
					else
						argument.pattern = convertFromEditableLength(arguments.getText());
				} catch (NumberFormatException e) {
					argument.pattern = arguments.getText();
				}
			}
			if (intiantiatedKeyOperatorType.equals(Boolean.class) && argumentsBoolean != null)
				argument.pattern = MultiMatcherLocalization.getMultiMatcherKey(argumentsBoolean.getText());
			String encodedArgument = FileInfoAttributesMatcher.encodeArguments(argument);
			FilterTypeUtil.setValue(filter, FilterTypeUtil.ARGUMENTS, encodedArgument);
		}
	}

	private String[] getLocalOperatorsForKey(String key) {
		String [] operators = FileInfoAttributesMatcher.getOperatorsForKey(key);
		String[] result = new String[operators.length];
		for (int i = 0; i < operators.length; i++)
			result[i] = MultiMatcherLocalization.getLocalMultiMatcherKey(operators[i]);
		return result;
	}

	private String[] getMultiMatcherKeys() {
		ArrayList list = new ArrayList();
		list.add(MultiMatcherLocalization.getLocalMultiMatcherKey(FileInfoAttributesMatcher.KEY_NAME));
		list.add(MultiMatcherLocalization.getLocalMultiMatcherKey(FileInfoAttributesMatcher.KEY_PROPJECT_RELATIVE_PATH));
		list.add(MultiMatcherLocalization.getLocalMultiMatcherKey(FileInfoAttributesMatcher.KEY_LOCATION));
		list.add(MultiMatcherLocalization.getLocalMultiMatcherKey(FileInfoAttributesMatcher.KEY_LAST_MODIFIED));
		if (FileInfoAttributesMatcher.supportCreatedKey())
			list.add(MultiMatcherLocalization.getLocalMultiMatcherKey(FileInfoAttributesMatcher.KEY_CREATED));
		list.add(MultiMatcherLocalization.getLocalMultiMatcherKey(FileInfoAttributesMatcher.KEY_LENGTH));
		list.add(MultiMatcherLocalization.getLocalMultiMatcherKey(FileInfoAttributesMatcher.KEY_IS_READONLY));
		if (!Platform.getOS().equals(Platform.OS_WIN32))
			list.add(MultiMatcherLocalization.getLocalMultiMatcherKey(FileInfoAttributesMatcher.KEY_IS_SYMLINK));
		return (String []) list.toArray(new String[0]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.ide.dialogs.ICustomFilterArgumentUI#selectionChanged()
	 */
	@Override
	public void selectionChanged() {
	}

	void validateInputText() {
		setupDescriptionText(validate());
		dialog.updateFinishControls();
	}

	@Override
	public String validate() {
		String message = null;
		if (intiantiatedKeyOperatorType != null) {
			String selectedKey = MultiMatcherLocalization.getMultiMatcherKey(multiKey.getText());
			String selectedOperator = MultiMatcherLocalization.getMultiMatcherKey(multiOperator.getText());
	
			FileInfoAttributesMatcher.Argument argument = new FileInfoAttributesMatcher.Argument();
			argument.key = selectedKey;
			argument.operator = selectedOperator;
			
			if (intiantiatedKeyOperatorType.equals(Date.class) && argumentsDate != null) {
			}
			if (intiantiatedKeyOperatorType.equals(String.class) && arguments != null) {
				argument.pattern = arguments.getText();
				if (argumentsRegularExpresion != null)
					argument.regularExpression = argumentsRegularExpresion.getSelection();
				if (argumentsCaseSensitive != null)
					argument.caseSensitive = argumentsCaseSensitive.getSelection();
				String encodedArgument = FileInfoAttributesMatcher.encodeArguments(argument);
				FilterCopy copy = new FilterCopy(filter);
				FilterTypeUtil.setValue(copy, FilterTypeUtil.ARGUMENTS, encodedArgument);

				IFilterMatcherDescriptor desc = dialog.getResource().getWorkspace().getFilterMatcherDescriptor(copy.getId());
				if (desc != null) {
					try {
						AbstractFileInfoMatcher matcher = ((FilterDescriptor) desc).createFilter();
						matcher.initialize(dialog.getResource().getProject(), copy.getArguments());
					} catch (CoreException e) {
							message = e.getMessage();
					}
				}
			}
			if (intiantiatedKeyOperatorType.equals(Integer.class) && arguments != null) {
				if (selectedKey.equals(FileInfoAttributesMatcher.KEY_LAST_MODIFIED) || selectedKey.equals(FileInfoAttributesMatcher.KEY_CREATED)) {
					try {
						convertFromEditableTimeInterval(arguments.getText());
					} catch (NumberFormatException e) {
						message = NLS.bind(
								IDEWorkbenchMessages.ResourceFilterPage_multiMatcher_InvalidTimeInterval, arguments.getText());
					}
				}
				else {
					try {
						convertFromEditableLength(arguments.getText());
					} catch (NumberFormatException e) {
						message = NLS.bind(
								IDEWorkbenchMessages.ResourceFilterPage_multiMatcher_InvalidFileLength, arguments.getText());
					}
				}
			}
			if (intiantiatedKeyOperatorType.equals(Boolean.class) && argumentsBoolean != null) {
				
			}
		}
		return message;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.ide.dialogs.ICustomFilterArgumentUI#formatStyledText(org.eclipse.ui.internal.ide.dialogs.FilterCopy, org.eclipse.jface.viewers.StyledString.Styler, org.eclipse.jface.viewers.StyledString.Styler)
	 */
	@Override
	public StyledString formatStyledText(FilterCopy filter,
			Styler fPlainStyler, Styler fBoldStyler) {
		return new StyledString(formatMultiMatcherArgument(filter), fPlainStyler);
	}

	private String formatMultiMatcherArgument(FilterCopy filter) {
		String argumentString = (String) filter.getArguments();
		FileInfoAttributesMatcher.Argument argument = FileInfoAttributesMatcher.decodeArguments(argumentString);
		
		StringBuffer builder = new StringBuffer();
		builder.append(MultiMatcherLocalization.getLocalMultiMatcherKey(argument.key));
		builder.append(' ');
		builder.append(MultiMatcherLocalization.getLocalMultiMatcherKey(argument.operator));
		builder.append(' ');
		Class type = FileInfoAttributesMatcher.getTypeForKey(argument.key, argument.operator);
		if (type.equals(String.class))
			builder.append(argument.pattern);
		if (type.equals(Boolean.class))
			builder.append(MultiMatcherLocalization.getLocalMultiMatcherKey(argument.pattern));
		if (type.equals(Integer.class)) {
			if (argument.key.equals(FileInfoAttributesMatcher.KEY_LAST_MODIFIED) || argument.key.equals(FileInfoAttributesMatcher.KEY_CREATED))
				builder.append(convertToEditableTimeInterval(argument.pattern));
			else
				builder.append(convertToEditableLength(argument.pattern));
		}
		if (type.equals(Date.class))
			builder.append(DateFormat.getDateInstance().format(new Date(Long.parseLong(argument.pattern))));
		
		return builder.toString();
	}
}

class DefaultCustomFilterArgumentUI implements ICustomFilterArgumentUI {

	protected Shell shell;
	protected FilterCopy filter;
	protected Text arguments;
	protected Label argumentsLabel;
	protected Label description;
	protected ContentAssistCommandAdapter fContentAssistField;
	protected FilterEditDialog dialog;

	public static final String REGEX_FILTER_ID = "org.eclipse.core.resources.regexFilterMatcher"; //$NON-NLS-1$

	/**
	 * @param dialog
	 * @param parentShell
	 * @param filter
	 */
	public DefaultCustomFilterArgumentUI(FilterEditDialog dialog, Shell parentShell, FilterCopy filter) {
		this.shell = parentShell;
		this.dialog = dialog;
		this.filter = filter;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.ide.dialogs.ICustomFilterArgumentUI#getID()
	 */
	@Override
	public Object getID() {
		return new String();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.ide.dialogs.ICustomFilterArgumentUI#dispose()
	 */
	@Override
	public void dispose() {
		Widget list[] = new Widget[] {arguments, argumentsLabel, description};
		for (int i = 0; i < list.length; i++) {
			if (list[i] != null) {
				list[i].dispose();
			}
		}
		arguments = null;
		argumentsLabel = null;
		fContentAssistField = null;
		description = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.ide.dialogs.ICustomFilterArgumentUI#create(org.eclipse.swt.widgets.Composite, org.eclipse.swt.graphics.Font)
	 */
	@Override
	public void create(Composite argumentComposite, Font font) {
		shell = argumentComposite.getShell();
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		layout.marginWidth = dialog.getVerticalDLUsToPixel(IDialogConstants.HORIZONTAL_MARGIN);
		argumentComposite.setLayout(layout);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		argumentComposite.setLayoutData(data);
		argumentComposite.setFont(font);
		if (filter.hasStringArguments())
			createArgumentsArea(font, argumentComposite);

		createDescriptionArea(font, argumentComposite);

		if (fContentAssistField != null)
			fContentAssistField.setEnabled(filter.getId().equals(REGEX_FILTER_ID));
		argumentComposite.layout(true);
	}
	
	private void createArgumentsArea(Font font, Composite composite) {
		GridData data;
		argumentsLabel = addLabel(composite, NLS.bind(
				IDEWorkbenchMessages.ResourceFilterPage_columnFilterPattern,
				null));
		arguments = new Text(composite, SWT.SINGLE | SWT.BORDER);
		data = new GridData(SWT.FILL, SWT.CENTER, true, false);
		arguments.setLayoutData(data);
		arguments.setFont(font);
		arguments.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				FilterTypeUtil.setValue(filter, FilterTypeUtil.ARGUMENTS,
						arguments.getText());
			}
		});
		if (filter.hasStringArguments())
			arguments.setText((String) FilterTypeUtil.getValue(filter,
					FilterTypeUtil.ARGUMENTS));
		arguments.setEnabled(filter.hasStringArguments());
		setArgumentLabelEnabled();

		TextContentAdapter contentAdapter= new TextContentAdapter();
		FindReplaceDocumentAdapterContentProposalProvider findProposer= new FindReplaceDocumentAdapterContentProposalProvider(true);
		fContentAssistField= new ContentAssistCommandAdapter(
				arguments,
				contentAdapter,
				findProposer,
				null,
				new char[] {'\\', '[', '('},
				true);
	}

	private void setArgumentLabelEnabled() {
		if (argumentsLabel != null) {
			Color color = argumentsLabel.getDisplay().getSystemColor(
					filter.hasStringArguments() ? SWT.COLOR_BLACK : SWT.COLOR_GRAY);
			argumentsLabel.setForeground(color);
		}
	}

	Label addLabel(Composite composite, String text) {
		String delimiter = ":"; //$NON-NLS-1$

		Font font = composite.getFont();
		Label label = new Label(composite, SWT.LEFT);
		label.setText(text + delimiter);
		GridData data = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		label.setLayoutData(data);
		label.setFont(font);
		return label;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.ide.dialogs.ICustomFilterArgumentUI#selectionChanged()
	 */
	@Override
	public void selectionChanged() {
		if (arguments != null)
			arguments.setEnabled(filter.hasStringArguments());
		setArgumentLabelEnabled();
		if (fContentAssistField != null)
			fContentAssistField.setEnabled(filter.getId().equals(REGEX_FILTER_ID));
		description.setText(FilterTypeUtil
				.getDescriptor(filter.getId()).getDescription());
	}

	/**
	 * @param font
	 * @param composite
	 */
	private void createDescriptionArea(Font font, Composite composite) {
		GridData data;
		description = new Label(composite, SWT.LEFT | SWT.WRAP);
		description.setText(FilterTypeUtil.getDescriptor(filter.getId())
				.getDescription());
		data = new GridData(SWT.FILL, SWT.BEGINNING, true, true);
		data.widthHint = 300;
		data.heightHint = 40;
		data.horizontalSpan = 2;
		description.setLayoutData(data);
		description.setFont(font);
	}

	@Override
	public String validate(){
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.ide.dialogs.ICustomFilterArgumentUI#formatStyledText(org.eclipse.ui.internal.ide.dialogs.FilterCopy, org.eclipse.jface.viewers.StyledString.Styler, org.eclipse.jface.viewers.StyledString.Styler)
	 */
	@Override
	public StyledString formatStyledText(FilterCopy filter,
			Styler fPlainStyler, Styler fBoldStyler) {
		return new StyledString(filter.getArguments() != null ? filter
					.getArguments().toString() : new String(), fPlainStyler);
	}
}

class MultiMatcherLocalization {
	
	static String[][] multiMatcherKey = {
			{FileInfoAttributesMatcher.KEY_NAME, IDEWorkbenchMessages.ResourceFilterPage_multiKeyName},
			{FileInfoAttributesMatcher.KEY_PROPJECT_RELATIVE_PATH, IDEWorkbenchMessages.ResourceFilterPage_multiKeyProjectRelativePath},
			{FileInfoAttributesMatcher.KEY_LOCATION, IDEWorkbenchMessages.ResourceFilterPage_multiKeyLocation},
			{FileInfoAttributesMatcher.KEY_LAST_MODIFIED, IDEWorkbenchMessages.ResourceFilterPage_multiKeyLastModified},
			{FileInfoAttributesMatcher.KEY_CREATED, IDEWorkbenchMessages.ResourceFilterPage_multiKeyCreated},
			{FileInfoAttributesMatcher.KEY_LENGTH, IDEWorkbenchMessages.ResourceFilterPage_multiKeyLength},
			{FileInfoAttributesMatcher.KEY_IS_READONLY, IDEWorkbenchMessages.ResourceFilterPage_multiKeyReadOnly},
			{FileInfoAttributesMatcher.KEY_IS_SYMLINK, IDEWorkbenchMessages.ResourceFilterPage_multiKeySymLink},
			{FileInfoAttributesMatcher.OPERATOR_AFTER, IDEWorkbenchMessages.ResourceFilterPage_multiAfter},
			{FileInfoAttributesMatcher.OPERATOR_BEFORE, IDEWorkbenchMessages.ResourceFilterPage_multiBefore},
			{FileInfoAttributesMatcher.OPERATOR_EQUALS, IDEWorkbenchMessages.ResourceFilterPage_multiEquals},
			{FileInfoAttributesMatcher.OPERATOR_MATCHES, IDEWorkbenchMessages.ResourceFilterPage_multiMatches},
			{FileInfoAttributesMatcher.OPERATOR_LARGER_THAN, IDEWorkbenchMessages.ResourceFilterPage_multiLargerThan},
			{FileInfoAttributesMatcher.OPERATOR_SMALLER_THAN, IDEWorkbenchMessages.ResourceFilterPage_multiSmallerThan},
			{FileInfoAttributesMatcher.OPERATOR_WITHIN, IDEWorkbenchMessages.ResourceFilterPage_multiWithin},
			{Boolean.TRUE.toString(), IDEWorkbenchMessages.ResourceFilterPage_true},
			{Boolean.FALSE.toString(), IDEWorkbenchMessages.ResourceFilterPage_false}
	};
	
	static public String getLocalMultiMatcherKey(String key) {
		for (int i = 0; i < multiMatcherKey.length; i++) {
			if (multiMatcherKey[i][0].equals(key))
				return multiMatcherKey[i][1];
		}
		return null;
	}
	
	static public String getMultiMatcherKey(String local) {
		for (int i = 0; i < multiMatcherKey.length; i++) {
			if (multiMatcherKey[i][1].equals(local))
				return multiMatcherKey[i][0];
		}
		return null;
	}
}