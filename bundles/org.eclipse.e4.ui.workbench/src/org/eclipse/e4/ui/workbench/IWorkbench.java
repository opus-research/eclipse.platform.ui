/*******************************************************************************
 * Copyright (c) 2008, 2013 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     IBM Corporation - initial API and implementation
 *     René Brandstetter - Bug 404231 - resetPerspectiveModel() does not reset the perspective
 ******************************************************************************/
package org.eclipse.e4.ui.workbench;

import java.net.URI;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;

/**
 * A running instance of the workbench.
 * 
 * This instance is published through:
 * <ul>
 * <li>the {@link IEclipseContext} of the application</li>
 * <li>the OSGi-Service-Registry</lI>
 * </ul>
 * <b>It is possible that there are multiple active {@link IWorkbench} instances in one
 * OSGi-Instance</b>
 * 
 * @since 1.0
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface IWorkbench {
	/**
	 * The argument for whether the persisted state should be cleared on startup <br>
	 * <br>
	 * Value is: <code>clearPersistedState</code>
	 */
	public static final String CLEAR_PERSISTED_STATE = "clearPersistedState"; //$NON-NLS-1$
	/**
	 * The argument for the {@link URI} of the resources referenced from the application CSS file <br>
	 * <br>
	 * Value is: <code>applicationCSSResources</code>
	 * 
	 * @since 0.12.0
	 */
	public static final String CSS_RESOURCE_URI_ARG = "applicationCSSResources"; //$NON-NLS-1$
	/**
	 * The argument for the {@link URI} of the application CSS file <br>
	 * <br>
	 * Value is: <code>applicationCSS</code>
	 * 
	 * @since 0.12.0
	 */
	public static final String CSS_URI_ARG = "applicationCSS"; //$NON-NLS-1$
	/**
	 * The argument for the {@link URI} of the life-cycle manager <br>
	 * <br>
	 * Value is: <code>lifeCycleURI</code>
	 * 
	 * @since 0.12.0
	 */
	public static final String LIFE_CYCLE_URI_ARG = "lifeCycleURI"; //$NON-NLS-1$
	/**
	 * The argument for the resource handler to use <br>
	 * <br>
	 * Value is: <code>modelResourceHandler</code>
	 */
	public static final String MODEL_RESOURCE_HANDLER = "modelResourceHandler"; //$NON-NLS-1$
	/**
	 * The argument for whether the workbench should save and restore its state <br>
	 * <<br>
	 * Value is: <code>persistState</code>
	 */
	public static final String PERSIST_STATE = "persistState"; //$NON-NLS-1$
	/**
	 * The argument for the {@link URI} of the application presentation <br>
	 * <br>
	 * Value is: <code>presentationURI</code>
	 * 
	 * @since 0.12.0
	 */
	public static final String PRESENTATION_URI_ARG = "presentationURI"; //$NON-NLS-1$
	/**
	 * The argument for the {@link URI} of the applicaton.xmi file <br>
	 * <br>
	 * Value is: <code>applicationXMI</code>
	 * 
	 * @since 0.12.0
	 */
	public static final String XMI_URI_ARG = "applicationXMI"; //$NON-NLS-1$

	/**
	 * The argument for disabling the default perspective keeper.
	 * <p>
	 * The default perspective keeper will create a snippet of the perspective when it becomes
	 * opened for the first time. To disable the automatic snippet creation either provide this
	 * property as single commandline argument or as a branding or system property.
	 * </p>
	 * <p>
	 * Value is: {@value #USE_CUSTOM_PERSPECTIVE_KEEPER}
	 * </p>
	 * <p>
	 * Usage example:
	 * <ul>
	 * <li>as commandline argument: <code>-useCustomPerspectiveKeeper</code></li>
	 * <li>as system property: <code>-DuseCustomPerspectiveKeeper=true</code></li>
	 * <li>as a branding property under the <code>org.eclipse.core.runtime.products</code> extension
	 * point: <code>
	 * <pre>
	 * &lt;product application="org.eclipse.e4.ui.workbench.swt.E4Application" name="ExmapleApplication"&gt;
	 *   &lt;property name="appName" value="ExmapleApplication" /&gt;
	 *   &lt;/property&gt;
	 *   <i>&lt;!-- ... other properties ... --&gt;</i>
	 *   <b>&lt;property name="useCustomPerspectiveKeeper" value="true"&gt;
	 *   &lt;/property&gt;</b>
	 * &lt;/product&gt;
	 * </pre>
	 * </code></li>
	 * </ul>
	 * </p>
	 * 
	 * @since 1.1.0
	 */
	public static final String USE_CUSTOM_PERSPECTIVE_KEEPER = "useCustomPerspectiveKeeper"; //$NON-NLS-1$

	/**
	 * Close the workbench instance
	 * 
	 * @return <code>true</code> if the shutdown succeeds
	 */
	public boolean close();

	/**
	 * @return the application model driving the workbench
	 */
	public MApplication getApplication();

	/**
	 * @return unique id of the instance
	 */
	public String getId();

	/**
	 * restart the workbench
	 * 
	 * @return <code>false</code> if the restart is aborted
	 */
	public boolean restart();

}
