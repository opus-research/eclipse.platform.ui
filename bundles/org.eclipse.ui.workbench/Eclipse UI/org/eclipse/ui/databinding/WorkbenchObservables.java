/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matthew Hall - initial API and implementation
 *     Lars Vogel <Lars.Vogel@gmail.com> - Bug 440810
 *******************************************************************************/

package org.eclipse.ui.databinding;

import org.eclipse.core.databinding.observable.value.ComputedValue;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.WritableValue;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.services.IServiceLocator;

/**
 * Factory methods for creating observables for Workbench objects
 *
 * @since 3.5
 */
public class WorkbenchObservables {
	/**
	 * Returns an observable with values of the given target type. If the
	 * wrapped observable's value is of the target type, or can be adapted to
	 * the target type, this is taken as the value of the returned observable,
	 * otherwise <code>null</code>.
	 *
	 * @param master
	 *            the observable whose value should be adapted
	 * @param adapter
	 *            the target type
	 * @return an observable with values of the given type, or <code>null</code>
	 *         if the current value of the given observable does not adapt to
	 *         the target type
	 */
	public static <T> IObservableValue<T> observeDetailAdaptedValue(IObservableValue<?> master, Class<T> adapter) {
		return observeDetailAdaptedValue(master, adapter, Platform
				.getAdapterManager());
	}

	/**
	 * Returns an observable with values of the given target type. If the
	 * wrapped observable's value is of the target type, or can be adapted to
	 * the target type, this is taken as the value of the returned observable,
	 * otherwise <code>null</code>.
	 *
	 * @param master
	 *            the observable whose value should be adapted
	 * @param adapter
	 *            the target type
	 * @param adapterManager
	 *            the adapter manager used to adapt the master value
	 * @return an observable with values of the given type, or <code>null</code>
	 *         if the current value of the given observable does not adapt to
	 *         the target type
	 */
	static <T> IObservableValue<T> observeDetailAdaptedValue(IObservableValue<?> master, Class<T> adapter,
			IAdapterManager adapterManager) {
		return WorkbenchProperties.adaptedValue(adapter, adapterManager)
				.observeDetail(master);
	}

	/**
	 * Returns an observable value that tracks the post selection of a selection
	 * service obtained through the given service locator, and adapts the first
	 * element of that selection to the given target type.
	 * <p>
	 * This method can be used by view or editor implementers to tie into the
	 * selection service, for example as follows:
	 *
	 * <pre>
	 * IObservableValue&lt;IResource&gt; selection = WorkbenchObservables.observeAdaptedSingleSelection(getSite(),
	 * 		IResource.class);
	 * </pre>
	 *
	 * </p>
	 *
	 * @param locator
	 *            a service locator with an available {@link ISelectionService}
	 * @param targetType
	 *            the target type
	 * @return an observable value whose value type is the given target type
	 */
	public static <T> IObservableValue<T> observeAdaptedSingleSelection(IServiceLocator locator, Class<T> targetType) {
		ISelectionService selectionService = locator.getService(ISelectionService.class);
		Assert.isNotNull(selectionService);
		return WorkbenchProperties.singleSelection(null, true).value(
				WorkbenchProperties.adaptedValue(targetType)).observe(
				selectionService);
	}

	/**
	 * Returns an observable value that tracks the active workbench window for
	 * the given workbench.
	 *
	 * @param workbench
	 *            the workbench to get the observable for
	 * @return an observable value that tracks the active workbench window
	 * @since 3.110
	 */
	public static IObservableValue<IWorkbenchWindow> observeActiveWorkbenchWindow(IWorkbench workbench) {
		return new WritableValue<IWorkbenchWindow>(null, IWorkbenchWindow.class) {
			private final IWindowListener listener = new IWindowListener() {
				@Override
				public void windowActivated(IWorkbenchWindow window) {
					setValue(window);
				}

				@Override
				public void windowDeactivated(IWorkbenchWindow window) {
					if (window == doGetValue())
						setValue(null);
				}

				@Override
				public void windowClosed(IWorkbenchWindow window) {
				}

				@Override
				public void windowOpened(IWorkbenchWindow window) {
				}
			};

			@Override
			protected void firstListenerAdded() {
				workbench.addWindowListener(listener);
				setValue(workbench.getActiveWorkbenchWindow());
			}

			@Override
			protected void lastListenerRemoved() {
				workbench.removeWindowListener(listener);
			}
		};
	}

	/**
	 * Returns an observable value that tracks the active workbench page for the
	 * given workbench window.
	 *
	 * @param window
	 *            the workbench window to get the observable for
	 * @return an observable value that tracks the active workbench page
	 * @since 3.110
	 */
	public static IObservableValue<IWorkbenchPage> observeActiveWorkbenchWindow(IWorkbenchWindow window) {
		return new WritableValue<IWorkbenchPage>(null, IWorkbenchPage.class) {
			private final IPageListener listener = new IPageListener() {
				@Override
				public void pageActivated(IWorkbenchPage page) {
					setValue(page);
				}

				@Override
				public void pageClosed(IWorkbenchPage page) {
					if (page == doGetValue())
						setValue(null);
				}

				@Override
				public void pageOpened(IWorkbenchPage page) {
				}
			};

			@Override
			protected void firstListenerAdded() {
				setValue(window.getActivePage());
				window.addPageListener(listener);
			}

			@Override
			protected void lastListenerRemoved() {
				window.removePageListener(listener);
			}
		};
	}

	/**
	 * Returns an observable value that tracks the active workbench part for the
	 * given part service.
	 *
	 * @param partService
	 *            the part service to get the observable for, e.g. a workbench
	 *            page
	 * @return an observable value that tracks the active workbench part
	 * @since 3.110
	 */
	public static IObservableValue<IWorkbenchPartReference> observeActivePart(IPartService partService) {
		return new WritableValue<IWorkbenchPartReference>(null, IWorkbenchPartReference.class) {
			private final IPartListener2 listener = new IPartListener2() {
				@Override
				public void partActivated(IWorkbenchPartReference partRef) {
					setValue(partRef);
				}

				@Override
				public void partDeactivated(IWorkbenchPartReference partRef) {
					if (partRef == doGetValue())
						setValue(null);
				}

				@Override
				public void partBroughtToTop(IWorkbenchPartReference partRef) {
				}

				@Override
				public void partClosed(IWorkbenchPartReference partRef) {
				}

				@Override
				public void partOpened(IWorkbenchPartReference partRef) {
				}

				@Override
				public void partHidden(IWorkbenchPartReference partRef) {
				}

				@Override
				public void partVisible(IWorkbenchPartReference partRef) {
				}

				@Override
				public void partInputChanged(IWorkbenchPartReference partRef) {
				}
			};

			@Override
			protected void firstListenerAdded() {
				setValue(partService.getActivePartReference());
				partService.addPartListener(listener);
			}

			@Override
			protected void lastListenerRemoved() {
				partService.removePartListener(listener);
			}
		};
	}

	/**
	 * Returns an observable value that tracks the active editor for the given
	 * part service.
	 *
	 * @param partService
	 *            the part service to get the observable for, e.g. a workbench
	 *            page
	 * @return an observable value that tracks the active editor
	 * @since 3.110
	 */
	public static IObservableValue<IEditorReference> observeActiveEditor(IPartService partService) {
		return new ComputedValue<IEditorReference>(IEditorReference.class) {
			final IObservableValue<IWorkbenchPartReference> partObservable = observeActivePart(partService);

			@Override
			protected IEditorReference calculate() {
				IWorkbenchPartReference value = partObservable.getValue();
				return value instanceof IEditorReference ? (IEditorReference) value : null;
			}

			@Override
			public synchronized void dispose() {
				partObservable.dispose();
				super.dispose();
			}
		};
	}
}
