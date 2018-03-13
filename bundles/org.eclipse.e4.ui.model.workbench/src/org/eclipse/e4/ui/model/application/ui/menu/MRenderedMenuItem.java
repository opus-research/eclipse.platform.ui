/**
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *      IBM Corporation - initial API and implementation
 */
package org.eclipse.e4.ui.model.application.ui.menu;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Rendered Menu Item</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.menu.MRenderedMenuItem#getContributionItem <em>Contribution Item</em>}</li>
 * </ul>
 * </p>
 *
 * @model
 * @generated
 */
public interface MRenderedMenuItem extends MMenuItem {
	/**
	 * Returns the value of the '<em><b>Contribution Item</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Contribution Item</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Contribution Item</em>' attribute.
	 * @see #setContributionItem(Object)
	 * @model transient="true"
	 * @generated
	 */
	Object getContributionItem();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.menu.MRenderedMenuItem#getContributionItem <em>Contribution Item</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Contribution Item</em>' attribute.
	 * @see #getContributionItem()
	 * @generated
	 */
	void setContributionItem(Object value);

} // MRenderedMenuItem
