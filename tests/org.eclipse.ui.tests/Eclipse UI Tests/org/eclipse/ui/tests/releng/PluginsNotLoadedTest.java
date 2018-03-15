package org.eclipse.ui.tests.releng;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Platform;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

/**
 * Tests whether the Java Editor forces the Search plug-in to be loaded (which
 * it should not).
 *
 * @since 3.1
 */

public class PluginsNotLoadedTest {

	private static String[] NOT_LOADED_BUNDLES= new String[] {
			"org.apache.xerces",
			"org.eclipse.jdt.astview",
			"org.eclipse.jdt.jeview",
			"org.eclipse.reftracker",
			"org.eclipse.swt.sleak",
			"org.eclipse.swt.spy",
			"com.jcraft.jsch",
			"javax.servlet",
			"javax.servlet.jsp",
			"org.apache.ant",
			"org.apache.commons.el",
			"org.apache.commons.logging",
			"org.apache.jasper",
			"org.apache.lucene",
			"org.apache.lucene.analysis",
			"org.eclipse.ant.core",
			"org.eclipse.ant.ui",
			"org.eclipse.compare", // caveat, see workaround for EGit in setUpTest below!
			"org.eclipse.core.commands",
			"org.eclipse.core.expressions.tests",
			"org.eclipse.core.filebuffers.tests",
			"org.eclipse.core.filesystem.win32.x86",
			"org.eclipse.core.resources.compatibility",
			"org.eclipse.core.resources.win32",
			"org.eclipse.core.runtime.compatibility.registry",
			"org.eclipse.debug.ui",
			"org.eclipse.equinox.http.jetty",
			"org.eclipse.equinox.http.registry",
			"org.eclipse.equinox.http.servlet",
			"org.eclipse.equinox.jsp.jasper",
			"org.eclipse.equinox.jsp.jasper.registry",
			"org.eclipse.help.base",
			"org.eclipse.help.ui",
			"org.eclipse.help.webapp",
			"org.eclipse.jdt",
			"org.eclipse.jdt.apt.core",
			"org.eclipse.jdt.apt.ui",
			"org.eclipse.jdt.compiler.apt",
			"org.eclipse.jdt.compiler.tool",
			"org.eclipse.jdt.debug",
			"org.eclipse.jdt.debug.ui",
			"org.eclipse.jdt.doc.isv",
			"org.eclipse.jdt.doc.user",
			"org.eclipse.jdt.junit",
			"org.eclipse.jdt.junit.runtime",
			"org.eclipse.jdt.junit4.runtime",
			"org.eclipse.jdt.ui.examples.javafamily",
			"org.eclipse.jdt.ui.examples.projects",
			"org.eclipse.jdt.ui.tests.refactoring",
			"org.eclipse.jface.databinding",
			"org.eclipse.jface.text",
			"org.eclipse.jface.text.tests",
			"org.eclipse.ltk.core.refactoring.tests",
			"org.eclipse.ltk.ui.refactoring.tests",
			"org.eclipse.osgi.services",
			"org.eclipse.pde",
			"org.eclipse.pde.build",
			"org.eclipse.pde.doc.user",
			"org.eclipse.pde.runtime",
			"org.eclipse.platform.doc.isv",
			"org.eclipse.platform.doc.user",
			"org.eclipse.sdk",
			"org.eclipse.sdk.tests",
			"org.eclipse.search",
			"org.eclipse.search.tests",
			"org.eclipse.swt",
			"org.eclipse.swt.win32.win32.x86",
			"org.eclipse.team.cvs.core",
			"org.eclipse.team.cvs.ssh",
			"org.eclipse.team.cvs.ssh2",
			"org.eclipse.team.cvs.ui",
			"org.eclipse.test.performance",
			"org.eclipse.test.performance.ui",
			"org.eclipse.test.performance.win32",
			"org.eclipse.text",
			"org.eclipse.text.tests",
			"org.eclipse.ui.cheatsheets",
			"org.eclipse.ui.console",
			"org.eclipse.ui.editors.tests",
			"org.eclipse.ui.examples.javaeditor",
			"org.eclipse.ui.examples.rcp.texteditor",
			"org.eclipse.ui.examples.recipeeditor",
			"org.eclipse.ui.externaltools",
// Bug 416915: Allow to run tests with tycho-surefire-plugin
//			"org.eclipse.ui.navigator",
//			"org.eclipse.ui.navigator.resources",
			"org.eclipse.ui.views.properties.tabbed",
			"org.eclipse.ui.win32",
			"org.eclipse.ui.workbench.compatibility",
			"org.eclipse.ui.workbench.texteditor.tests",
			"org.eclipse.update.core.win32",
			"org.eclipse.update.core.linux",
			"org.eclipse.update.ui",
			"org.junit",
			"org.junit4",
			"org.mortbay.jetty",
			"com.ibm.icu.source",
			"javax.servlet.jsp.source",
			"javax.servlet.source",
			"org.apache.ant.source",
			"org.apache.commons.el.source",
			"org.apache.commons.logging.source",
			"org.apache.jasper.source",
			"org.apache.lucene.analysis.source",
			"org.apache.lucene.source",
			"org.eclipse.core.boot",
			"org.eclipse.core.databinding.beans",
			"org.eclipse.cvs",
			"org.eclipse.cvs.source",
			"org.eclipse.equinox.launcher",
			"org.eclipse.equinox.launcher.win32.win32.x86",
			"org.eclipse.help.appserver",
			"org.eclipse.jdt.apt.pluggable.core",
			"org.eclipse.jdt.source",
			"org.eclipse.jsch.ui",
			"org.eclipse.osgi.util",
			"org.eclipse.pde.source",
			"org.eclipse.pde.ui.templates",
			"org.eclipse.platform",
			"org.eclipse.platform.source",
			"org.eclipse.platform.source.win32.win32.x86",
			"org.eclipse.rcp",
			"org.eclipse.rcp.source",
			"org.eclipse.rcp.source.win32.win32.x86",
			"org.eclipse.ui.browser",
			"org.junit.source",
			"org.mortbay.jetty.source"
		};

	@Before
	public void setUpTest() {
		/*
		 * Since https://bugs.eclipse.org/484795 in EGit 4.2,
		 * org.eclipse.egit.ui/plugin.xml contributes: <extension
		 * point="org.eclipse.ui.services"> <sourceProvider
		 * provider="org.eclipse.egit.ui.internal.selection.RepositorySourceProvider">
		 * ... This activates the EGit UI bundle very early. Because of that, EGit's
		 * org.eclipse.egit.ui.team.MergeTool command's handler class is loaded, which
		 * in turn activates the org.eclipse.compare bundle on startup.
		 *
		 * org.eclipse.pde.ui also contributes a sourceProvider, that's why we don't
		 * test for it...
		 *
		 * Workaround is to remove org.eclipse.compare if EGit is present:
		 */
		if (Platform.getBundle("org.eclipse.egit.ui") != null) {
			addLoadedPlugIns("org.eclipse.compare");
		}
	}

	/**
	 * If a test suite uses this test and has other tests that cause plug-ins to be
	 * loaded then those need to be indicated here.
	 *
	 * @param loadedPlugins
	 *            plug-ins that are additionally loaded by the caller
	 * @since 3.5
	 */
	public static void addLoadedPlugIns(String... loadedPlugins) {
		Assert.isLegal(loadedPlugins != null);
		List<String> l = new ArrayList<>(Arrays.asList(NOT_LOADED_BUNDLES));
		l.removeAll(Arrays.asList(loadedPlugins));
		NOT_LOADED_BUNDLES = l.toArray(new String[0]);
	}

	public void _testPrintNotLoaded() {
		Bundle bundle = FrameworkUtil.getBundle(PluginsNotLoadedTest.class);
		Bundle[] bundles = bundle.getBundleContext().getBundles();
		for (Bundle b : bundles) {
			if (b.getState() != Bundle.ACTIVE) {
				System.out.println(b.getSymbolicName());
			}
		}
	}

	@Test
	public void testPluginsNotLoaded() {
		// _testPrintNotLoaded();
		int i = 0;
		StringBuffer buf = new StringBuffer();
		for (String element : NOT_LOADED_BUNDLES) {
			Bundle bundle = Platform.getBundle(element);

			if (element.contains("org.junit")) {
				System.out.println();
			}
			if (bundle == null) {
				// log bundles that cannot be found:
				buf.append("- not found: ");
				buf.append(NOT_LOADED_BUNDLES[i]);
				buf.append('\n');
			} else if (bundle.getState() == Bundle.ACTIVE) {
				buf.append("- ");
				buf.append(element);
				buf.append('\n');
			}
			i++;
		}
		assertTrue("Wrong bundles loaded:\n" + buf, buf.length() == 0);
	}
}
