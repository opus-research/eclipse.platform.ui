/*******************************************************************************
 * Copyright (c) 2013 Markus Alexander Kuppe and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Markus Alexander Kuppe - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.di;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.inject.Qualifier;

/**
 * Parts can specify this annotation on one of the methods to tag it as a method
 * that is to be executed inside the UI thread. "save" operation. The method
 * implementation can then safely access the UI widgets iff the underlying UI
 * library restricts access to widgets to the UI thread. In case the underlying
 * UI library does not pose such restrictions, the behavior of this annotation
 * is undefined.
 * <p>
 * Method containing such arguments will be called on the UI thread
 * </p>
 * 
 * @since 1.0
 */
@Qualifier
@Target({ METHOD })
@Retention(RUNTIME)
@Documented
public @interface UIInject {

}
