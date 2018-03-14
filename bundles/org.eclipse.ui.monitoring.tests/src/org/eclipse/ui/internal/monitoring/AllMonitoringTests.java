/*******************************************************************************
 * Copyright (C) 2014, Google Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Steve Foreman (Google) - initial API and implementation
 *     Marcus Eng (Google)
 *******************************************************************************/
package org.eclipse.ui.internal.monitoring;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test suite for {@code org.eclipse.ui.monitoring} plug-in.
 * {@link EventLoopMonitorThreadManualJUnitPluginTests} is excluded by default from this
 * suite due to its reliance on the performance of the machine running the tests.
 */
public class AllMonitoringTests {
	public static Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(EventLoopMonitorThreadTests.class);
		suite.addTestSuite(FilterHandlerTests.class);
		suite.addTestSuite(DefaultLoggerTest.class);
		return suite;
	}
}
