/*******************************************************************************
 * Copyright (c) 2009, 2010 Ovidio Mallo and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ovidio Mallo - initial API and implementation (bug 248877)
 *     Simon Scholz <simon.scholz@vogella.com> - Bug 468903
 ******************************************************************************/

package org.eclipse.jface.databinding.dialog;

import org.eclipse.core.databinding.ValidationStatusProvider;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IMessageProvider;

/**
 * Standard implementation of the {@link IValidationMessageProvider} interface.
 *
 * @since 1.4
 */
public class ValidationMessageProvider implements IValidationMessageProvider {

	private String initalMessage;
	private int initalType;

	/**
	 * Default constructor.
	 */
	public ValidationMessageProvider() {
		initalMessage = null;
		initalType = IMessageProvider.NONE;
	}

	/**
	 * Creates a ValidationMessageProvider, which gets the initial values of the
	 * given {@link IMessageProvider}, in order to reset to those values, if the
	 * validation status equals {@link IStatus#isOK()}.
	 *
	 * @param messageProvider
	 *            {@link IMessageProvider} where an initial message can be
	 *            obtained.
	 * @since 1.7
	 */
	public ValidationMessageProvider(IMessageProvider messageProvider) {
		this(messageProvider.getMessage(), messageProvider.getMessageType());
	}

	/**
	 * Creates a ValidationMessageProvider, which resets the message to the
	 * given initial values, if the validation status equals
	 * {@link IStatus#isOK()}.
	 *
	 * @param initalMessage
	 * @param initalType
	 *
	 * @since 1.7
	 */
	public ValidationMessageProvider(String initalMessage, int initalType) {
		this.initalMessage = initalMessage;
		this.initalType = initalType;
	}

	/**
	 * Returns the {@link IStatus#getMessage() message} of the
	 * <code>IStatus</code> contained in the provided
	 * <code>validationStatusProvider</code> as is or <code>null</code> if the
	 * <code>validationStatusProvider</code> is itself <code>null</code>.
	 */
	@Override
	public String getMessage(ValidationStatusProvider statusProvider) {
		if (statusProvider != null) {
			IStatus status = (IStatus) statusProvider.getValidationStatus()
					.getValue();
			if (initalMessage != null && status.isOK()) {
				return initalMessage;
			}
			return status.getMessage();
		}
		return null;
	}

	/**
	 * Returns the message type defined in {@link IMessageProvider} which
	 * naturally maps to the {@link IStatus#getSeverity()} of the
	 * <code>IStatus</code> contained in the provided
	 * <code>validationStatusProvider</code>.
	 */
	@Override
	public int getMessageType(ValidationStatusProvider statusProvider) {
		if (statusProvider == null) {
			return IMessageProvider.NONE;
		}

		IStatus status = (IStatus) statusProvider.getValidationStatus()
				.getValue();
		int severity = status.getSeverity();
		switch (severity) {
		case IStatus.OK:
			return initalType;
		case IStatus.CANCEL:
			return IMessageProvider.NONE;
		case IStatus.INFO:
			return IMessageProvider.INFORMATION;
		case IStatus.WARNING:
			return IMessageProvider.WARNING;
		case IStatus.ERROR:
			return IMessageProvider.ERROR;
		default:
			Assert.isTrue(false, "incomplete switch statement"); //$NON-NLS-1$
			return -1; // unreachable
		}
	}
}
