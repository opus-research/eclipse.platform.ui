/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Simon Scholz <scholzsimon@vogella.com> - Bug 440367
 *******************************************************************************/
package org.eclipse.jface.dialogs;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * The abstract implementation of a selection dialog. It can be primed with
 * initial selections (<code>setInitialSelections</code>), and returns the final
 * selection (via <code>getResult</code>) after completion.
 * <p>
 * Clients may subclass this dialog to inherit its selection facilities.
 * </p>
 *
 * @param <T>
 *            which declares the type of the elements in the
 *            {@link SelectionDialog}.
 *
 * @since 3.11
 */
public abstract class SelectionDialog<T> extends TrayDialog {
	// the final collection of selected elements, or null if this dialog was
	// canceled
	private Collection<T> result;

	// a collection of the initially-selected elements
	private Collection<T> initialSelections;

	// title of dialog
	private String title;

	// message to show user
	private String message = ""; //$NON-NLS-1$

	// dialog bounds strategy (since 3.2)
	private int dialogBoundsStrategy = Dialog.DIALOG_PERSISTLOCATION
			| Dialog.DIALOG_PERSISTSIZE;

	// dialog settings for storing bounds (since 3.2)
	private IDialogSettings dialogBoundsSettings = null;

	/**
	 * Creates a dialog instance. Note that the dialog will have no visual
	 * representation (no widgets) until it is told to open.
	 *
	 * @param parentShell
	 *            the parent shell
	 */
	protected SelectionDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		if (title != null) {
			shell.setText(title);
		}
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
				true);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Creates the message area for this dialog.
	 * <p>
	 * This method is provided to allow subclasses to decide where the message
	 * will appear on the screen.
	 * </p>
	 *
	 * @param composite
	 *            the parent composite
	 * @return the message label
	 */
	protected Label createMessageArea(Composite composite) {
		Label label = new Label(composite, SWT.NONE);
		if (message != null) {
			label.setText(message);
		}
		label.setFont(composite.getFont());
		return label;
	}

	/**
	 * Returns the collection of initial element selections.
	 *
	 * @return Collection
	 */
	protected Collection<T> getInitialElementSelections() {
		if (null == initialSelections) {
			return Collections.emptyList();
		}
		return initialSelections;
	}

	/**
	 * Returns the message for this dialog.
	 *
	 * @return the message for this dialog
	 */
	protected String getMessage() {
		return message;
	}

	/**
	 * Returns the collection of selections made by the user.
	 *
	 * @return the array of selected elements, or <code>null</code> if no result
	 *         was set
	 */
	public Collection<T> getResult() {
		return result;
	}

	/**
	 * Sets the initial selection in this selection dialog to the given
	 * elements.
	 *
	 * @param selectedElements
	 *            the array of elements to select
	 */
	public void setInitialElementSelections(T... selectedElements) {
		initialSelections = Arrays.asList(selectedElements);
	}

	/**
	 * Sets the initial selection in this selection dialog to the given
	 * elements.
	 *
	 * @param selectedElements
	 *            the List of elements to select
	 */
	public void setInitialElementSelections(Collection<T> selectedElements) {
		initialSelections = selectedElements;
	}

	/**
	 * Sets the message for this dialog.
	 *
	 * @param message
	 *            the message
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * Set the selections made by the user, or <code>null</code> if the
	 * selection was canceled.
	 *
	 * @param newResult
	 *            list of selected elements, or <code>null</code> if Cancel was
	 *            pressed
	 */
	protected void setResult(Set<T> newResult) {
		if (newResult == null) {
			result = Collections.emptySet();
		} else {
			result = newResult;
		}
	}

	/**
	 * Set the selections made by the user, or <code>null</code> if the
	 * selection was canceled.
	 * <p>
	 * The selections may accessed using <code>getResult</code>.
	 * </p>
	 *
	 * @param newResult
	 *            - the new values
	 */
	protected void setResult(T... newResult) {
		if (newResult == null) {
			result = Collections.emptyList();
		} else {
			result = Arrays.asList(newResult);
		}
	}

	/**
	 * Sets the title for this dialog.
	 *
	 * @param title
	 *            the title
	 */
	public void setTitle(String title) {
		this.title = title;
	}

	/**
	 * Set the dialog settings that should be used to save the bounds of this
	 * dialog. This method is provided so that clients that directly use
	 * SelectionDialogs without subclassing them may specify how the bounds of
	 * the dialog are to be saved.
	 *
	 * @param settings
	 *            the {@link IDialogSettings} that should be used to store the
	 *            bounds of the dialog
	 *
	 * @param strategy
	 *            the integer constant specifying how the bounds are saved.
	 *            Specified using {@link Dialog#DIALOG_PERSISTLOCATION} and
	 *            {@link Dialog#DIALOG_PERSISTSIZE}.
	 *
	 * @since 3.2
	 *
	 * @see Dialog#getDialogBoundsStrategy()
	 * @see Dialog#getDialogBoundsSettings()
	 */
	public void setDialogBoundsSettings(IDialogSettings settings, int strategy) {
		dialogBoundsStrategy = strategy;
		dialogBoundsSettings = settings;
	}

	/**
	 * Gets the dialog settings that should be used for remembering the bounds
	 * of the dialog, according to the dialog bounds strategy. Overridden to
	 * provide the dialog settings that were set using
	 * {@link #setDialogBoundsSettings(IDialogSettings, int)}.
	 *
	 * @return the dialog settings used to store the dialog's location and/or
	 *         size, or <code>null</code> if the dialog's bounds should not be
	 *         stored.
	 *
	 * @since 3.2
	 *
	 * @see Dialog#getDialogBoundsStrategy()
	 * @see #setDialogBoundsSettings(IDialogSettings, int)
	 */
	@Override
	protected IDialogSettings getDialogBoundsSettings() {
		return dialogBoundsSettings;
	}

	/**
	 * Get the integer constant that describes the strategy for persisting the
	 * dialog bounds. Overridden to provide the dialog bounds strategy that was
	 * set using {@link #setDialogBoundsSettings(IDialogSettings, int)}.
	 *
	 * @return the constant describing the strategy for persisting the dialog
	 *         bounds.
	 *
	 * @since 3.2
	 * @see Dialog#DIALOG_PERSISTLOCATION
	 * @see Dialog#DIALOG_PERSISTSIZE
	 * @see Dialog#getDialogBoundsSettings()
	 * @see #setDialogBoundsSettings(IDialogSettings, int)
	 */
	@Override
	protected int getDialogBoundsStrategy() {
		return dialogBoundsStrategy;
	}

	/**
	 * @since 3.4
	 */
	@Override
	protected boolean isResizable() {
		return true;
	}
}
