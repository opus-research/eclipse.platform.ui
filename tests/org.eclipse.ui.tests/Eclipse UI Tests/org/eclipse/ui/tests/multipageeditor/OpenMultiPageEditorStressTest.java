package org.eclipse.ui.tests.multipageeditor;

import org.junit.runner.RunWith;

import junit.framework.Test;
import junit.framework.TestSuite;


@RunWith(org.junit.runners.AllTests.class)
public class OpenMultiPageEditorStressTest extends TestSuite {

	public static Test suite() {
		return new OpenMultiPageEditorStressTest();
	}

	public OpenMultiPageEditorStressTest() {
		for (int i = 0; i < 50; ++i) {
			addTestSuite(OpenMultiPageEditorTest.class);
		}
	}
}
