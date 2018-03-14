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
	public static interface State {
		int UNKNOWN = 0;

		int OVERRIDDEN = 1;

		int ADDED_BY_CSS = 2;

		int MODIFIED_BY_USER = 4;
	}

	private String id;

	private String label;

	private String description;

	private String formattedDescription;

	private String categoryId;

	private int state = State.UNKNOWN;

	private String overriddenLabel;

	public ThemeElementDefinition(String id, String label, String description, String categoryId) {
		this.id = id;
		this.label = label;
		this.description = description;
		this.categoryId = categoryId;
	}

	/**
	 * @return the id of this definition. Should not be <code>null</code>.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the label.
	 * 
	 * @return String
	 */
	public String getName() {
		return label;
	}

	public void setName(String label) {
		this.label = label;
		appendState(State.OVERRIDDEN);
	}

	/**
	 * Returns the description.
	 * 
	 * @return String or
	 * 
	 *         <pre>
	 * null
	 * </pre>
	 * 
	 *         .
	 */
	public String getDescription() {
		if (formattedDescription == null) {
			formattedDescription = isOverridden()? description + ' ' + getOverriddenLabel() : description;			
		} else if (!isOverridden() && formattedDescription.endsWith(getOverriddenLabel())) {
			formattedDescription = formattedDescription.substring(0, formattedDescription.length()
					- getOverriddenLabel().length() - 1);
		}
		return formattedDescription;
	}

	public void setDescription(String description) {
		this.description = description;
		formattedDescription = null;
		appendState(State.OVERRIDDEN);
	}

	/**
	 * Returns the categoryId.
	 * 
	 * @return String
	 */
	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
		appendState(State.OVERRIDDEN);
	}

	public int getState() {
		return state;
	}

	public void appendState(int state) {
		this.state |= state;
	}

	public void removeState(int state) {
		this.state &= ~state;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.e4.ui.css.swt.definition.IDefinitionOverridable#isOverriden()
	 */
	public boolean isOverridden() {
		return (state & State.OVERRIDDEN) != 0;
	}

	public boolean isAddedByCss() {
		return (state & State.ADDED_BY_CSS) != 0;
	}

	public boolean isModifiedByUser() {
		return (state & State.MODIFIED_BY_USER) != 0;
	}

	public String getOverriddenLabel() {
		if (overriddenLabel == null) {
			ResourceBundle resourceBundle = ResourceBundle.getBundle(Theme.class.getName());
			overriddenLabel = resourceBundle.getString("Overridden.by.css.label"); //$NON-NLS-1$
		}
		return overriddenLabel;
	}

	public void resetToDefaultValue() {
		state = State.UNKNOWN;
	}
}
