/**
 * Copyright (c) 2008, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *      IBM Corporation - initial API and implementation
 */
package org.eclipse.e4.ui.model.application.ui.basic;

import org.eclipse.e4.ui.model.application.ui.MGenericTile;
import org.eclipse.e4.ui.model.application.ui.MUILabel;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Dialog</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * <p>
 * A subclass of Window representing a dialog.
 * </p>
 * @since 1.1
 * @noimplement This interface is not intended to be implemented by clients.
 * @noreference  This model element is still provisional.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.MDialog#getX <em>X</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.MDialog#getY <em>Y</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.MDialog#getWidth <em>Width</em>}</li>
 *   <li>{@link org.eclipse.e4.ui.model.application.ui.basic.MDialog#getHeight <em>Height</em>}</li>
 * </ul>
 *
 * @model
 * @generated
 */
public interface MDialog extends MUILabel, MGenericTile<MPartSashContainerElement>, MFrameElement {

	/**
	 * Returns the value of the '<em><b>X</b></em>' attribute.
	 * The default value is <code>"-2147483648"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * The 'X' position of this window
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>X</em>' attribute.
	 * @see #setX(int)
	 * @model default="-2147483648"
	 * @generated
	 */
	int getX();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.basic.MDialog#getX <em>X</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>X</em>' attribute.
	 * @see #getX()
	 * @generated
	 */
	void setX(int value);

	/**
	 * Returns the value of the '<em><b>Y</b></em>' attribute.
	 * The default value is <code>"-2147483648"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * The 'Y' position of this window
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Y</em>' attribute.
	 * @see #setY(int)
	 * @model default="-2147483648"
	 * @generated
	 */
	int getY();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.basic.MDialog#getY <em>Y</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Y</em>' attribute.
	 * @see #getY()
	 * @generated
	 */
	void setY(int value);

	/**
	 * Returns the value of the '<em><b>Width</b></em>' attribute.
	 * The default value is <code>"-1"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * The width of this window
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Width</em>' attribute.
	 * @see #setWidth(int)
	 * @model default="-1"
	 * @generated
	 */
	int getWidth();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.basic.MDialog#getWidth <em>Width</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Width</em>' attribute.
	 * @see #getWidth()
	 * @generated
	 */
	void setWidth(int value);

	/**
	 * Returns the value of the '<em><b>Height</b></em>' attribute.
	 * The default value is <code>"-1"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * <p>
	 * The heigfht of this window
	 * </p>
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Height</em>' attribute.
	 * @see #setHeight(int)
	 * @model default="-1"
	 * @generated
	 */
	int getHeight();

	/**
	 * Sets the value of the '{@link org.eclipse.e4.ui.model.application.ui.basic.MDialog#getHeight <em>Height</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Height</em>' attribute.
	 * @see #getHeight()
	 * @generated
	 */
	void setHeight(int value);
} // MDialog
