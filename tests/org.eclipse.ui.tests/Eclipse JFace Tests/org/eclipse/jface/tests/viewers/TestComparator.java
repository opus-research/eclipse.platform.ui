/*******************************************************************************
 * Copyright (c) 2004, 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.tests.viewers;

import java.util.Comparator;

/**
 * @since 3.1
 */
public class TestComparator implements Comparator<Object> {

    public volatile int comparisons = 0;

    @Override
	public int compare(Object arg0, Object arg1) {
        comparisons++;

        return (arg0.toString()).compareTo(arg1.toString());
    }
}
