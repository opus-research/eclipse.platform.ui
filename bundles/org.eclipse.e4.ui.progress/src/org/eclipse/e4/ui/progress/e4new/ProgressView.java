package org.eclipse.e4.ui.progress.e4new;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.progress.internal.DetailedProgressViewer;
import org.eclipse.e4.ui.progress.internal.ProgressManager;
import org.eclipse.e4.ui.progress.internal.ProgressManagerUtil;
import org.eclipse.e4.ui.progress.internal.ProgressViewerContentProvider;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

public class ProgressView {

	DetailedProgressViewer viewer;

	@Inject
	ESelectionService selectionService;

	ISelectionChangedListener selectionListener;

	@PostConstruct
	public void createPartControl(Composite parent) {
		viewer = new DetailedProgressViewer(parent, SWT.MULTI | SWT.H_SCROLL);
		viewer.setComparator(ProgressManagerUtil.getProgressViewerComparator());

		viewer.getControl().setLayoutData(
		        new GridData(SWT.FILL, SWT.FILL, true, true));

//		helpSystem.setHelp(parent, IWorkbenchHelpContextIds.RESPONSIVE_UI);

		ProgressViewerContentProvider provider = new ProgressViewerContentProvider(
		        viewer, true, true);
		viewer.setContentProvider(provider);
		viewer.setInput(ProgressManager.getInstance());

		selectionListener = new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				if (selectionService != null)
					selectionService.setSelection(event.getSelection());
			}
		};
		viewer.addSelectionChangedListener(selectionListener);
	}

	@Focus
	public void setFocus() {
		if (viewer != null) {
			viewer.setFocus();
		}
	}
}
