/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.themes;

import java.util.ResourceBundle;

/**
 * @since 3.5
 *
 */
public class ThemeElementDefinition {
	private boolean overridden;

	private boolean addedByCss;

	private String overriddenLabel;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.css.swt.definition.IDefinitionOverridable#isOverriden()
	 */
	public boolean isOverridden() {
		return overridden;
	}

	protected void setOverridden(boolean overridden) {
		this.overridden = overridden;
	}

	public boolean isAddedByCss() {
		return addedByCss;
	}

	public void setAddedByCss(boolean addedByCss) {
		this.addedByCss = addedByCss;
	}

	public String getOverriddenLabel() {
		if (overriddenLabel == null) {
			ResourceBundle resourceBundle = ResourceBundle.getBundle(Theme.class.getName());
			overriddenLabel = resourceBundle.getString("Overridden.by.css.label"); //$NON-NLS-1$
		}
		return overriddenLabel;
	}
}
