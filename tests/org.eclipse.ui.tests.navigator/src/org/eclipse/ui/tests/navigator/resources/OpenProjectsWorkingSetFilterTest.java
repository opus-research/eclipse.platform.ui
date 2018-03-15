/*******************************************************************************
 * Copyright (c) 2017 Conrad Groth and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *******************************************************************************/
package org.eclipse.ui.tests.navigator.resources;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.internal.WorkingSet;
import org.eclipse.ui.navigator.resources.OpenProjectsWorkingSetFilter;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class OpenProjectsWorkingSetFilterTest {

	OpenProjectsWorkingSetFilter filter = new OpenProjectsWorkingSetFilter();
	private IProject openProject, closedProject;

	@Before
	public void setup() throws CoreException {
		IProgressMonitor monitor = new NullProgressMonitor();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		openProject = root.getProject("open");
		openProject.create(monitor);
		openProject.open(monitor);
		closedProject = root.getProject("closed");
		closedProject.create(monitor);
	}

	@After
	public void cleanUp() throws CoreException {
		IProgressMonitor monitor = new NullProgressMonitor();
		openProject.delete(true, monitor);
		closedProject.delete(true, monitor);
	}

	@Test
	public void testOnlyWorkingSetsAreFiltered() {
		assertTrue(filter.select(null, null, openProject));
	}

	@Test
	public void testWorkingSetWithOpenProjectIsntFiltered() throws Exception {
		WorkingSet allOpen = new WorkingSet("setA", "setA", new IAdaptable[] { openProject });
		assertTrue(filter.select(null, null, allOpen));
	}

	@Test
	public void testWorkingSetWithClosedProjectIsFiltered() throws Exception {
		WorkingSet allClosed = new WorkingSet("setA", "setA", new IAdaptable[] { closedProject });
		assertFalse(filter.select(null, null, allClosed));
	}

	@Test
	public void testWorkingSetWithOpenAndClosedProjectsIsntFiltered() throws Exception {
		WorkingSet openAndClosed = new WorkingSet("setA", "setA", new IAdaptable[] { openProject, closedProject });
		assertTrue(filter.select(null, null, openAndClosed));
	}
}
