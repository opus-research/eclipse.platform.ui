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
package org.eclipse.e4.ui.model.application;

import java.util.List;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Lifecycle Aware</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * <p>
 * This class should be mixed into any UI element that should participate in Life Cycle event
 * handling.
 * </p>
 * @since 1.1
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.e4.ui.model.application.MLifecycleAware#getLifeCycleHandlers <em>Life Cycle Handlers</em>}</li>
 * </ul>
 * </p>
 *
 * @model interface="true" abstract="true"
 * @generated
 */
public interface MLifecycleAware {
	/**
	 * Returns the value of the '<em><b>Life Cycle Handlers</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.e4.ui.model.application.MLifecycleContribution}.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Life Cycle Handlers</em>' containment reference list isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Life Cycle Handlers</em>' containment reference list.
	 * @model containment="true"
	 * @generated
	 */
	List<MLifecycleContribution> getLifeCycleHandlers();

} // MLifecycleAware
