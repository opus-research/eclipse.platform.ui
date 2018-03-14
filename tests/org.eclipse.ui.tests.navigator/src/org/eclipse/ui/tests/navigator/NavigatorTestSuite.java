/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Fair Issac Corp - bug 287103 - NCSLabelProvider does not properly handle overrides
 *
 *******************************************************************************/
package org.eclipse.ui.tests.navigator;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.ui.tests.navigator.cdt.CdtTest;
import org.eclipse.ui.tests.navigator.jst.JstPipelineTest;

public final class NavigatorTestSuite extends TestSuite {

	/**
	 * Returns the suite. This is required to use the JUnit Launcher.
	 */
	public static final Test suite() {
		return new NavigatorTestSuite();
	}

	/**
	 * Construct the test suite.
	 */
	public NavigatorTestSuite() {
		addTest(new TestSuite(InitialActivationTest.class));
		addTest(new TestSuite(ActionProviderTest.class));
		addTest(new TestSuite(ExtensionsTest.class));
		addTest(new TestSuite(FilterTest.class));
		addTest(WorkingSetTest.suite());
		addTest(new TestSuite(ActivityTest.class));
		addTest(new TestSuite(OpenTest.class));
		addTest(new TestSuite(INavigatorContentServiceTests.class));
		addTest(new TestSuite(ProgrammaticOpenTest.class));
		addTest(new TestSuite(PipelineTest.class));
		addTest(new TestSuite(PipelineChainTest.class));
		addTest(new TestSuite(JstPipelineTest.class));
		addTest(new TestSuite(LabelProviderTest.class));
		addTest(new TestSuite(SorterTest.class));
		addTest(new TestSuite(ViewerTest.class));
		addTest(new TestSuite(CdtTest.class));
		addTest(new TestSuite(M12Tests.class));
		addTest(new TestSuite(FirstClassM1Tests.class));
		addTest(new TestSuite(LinkHelperTest.class));
		addTest(new TestSuite(ResourceTransferTest.class));
		// addTest(new TestSuite(DnDTest.class));
		// Does not pass on all platforms see bug 264449
		//addTest(new TestSuite(PerformanceTest.class));
	}

}
