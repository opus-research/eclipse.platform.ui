/*******************************************************************************
 * Copyright (c) 2009, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Thibault Le Ouay <thibaultleouay@gmail.com> - Bug 448832
 ******************************************************************************/

package org.eclipse.e4.ui.tests.reconciler;

import org.eclipse.e4.ui.tests.reconciler.xml.XMLModelReconcilerTestSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({ 
		E4XMIResourceFactoryTest.class,
		XMLModelReconcilerTestSuite.class
		})
public class ModelReconcilerTestSuite {

}
