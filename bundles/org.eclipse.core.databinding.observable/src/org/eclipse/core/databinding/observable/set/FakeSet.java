/*******************************************************************************
 * Copyright (c) 2014 TwelveTone LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Steven Spungin - initial API and implementation,  Bug 432440
 *******************************************************************************/

package org.eclipse.core.databinding.observable.set;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * @since 1.4 This class implements the Set interface, but is actually an
 *        ArrayList. It is used to make a copy of an existing set to be passed
 *        into methods that require a Set interface. Items added must be checked
 *        by the caller to insure the set does not already contain them. (via
 *        hashcode, compare, etc).
 */
public class FakeSet extends ArrayList implements Set {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	ArrayList items = new ArrayList();

	/**
	 * @param items
	 */
	public FakeSet(Collection items) {
		addAll(items);
	}

}
