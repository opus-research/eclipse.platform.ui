/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.e4.migration;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import javax.inject.Inject;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * @since 3.5
 *
 */
public class MementoReader {

	@Inject
	protected IMemento memento;

	protected String getString(String attribute) {
		return memento.getString(attribute);
	}

	/**
	 * @param attribute
	 * @return value or false when attribute not found
	 */
	protected boolean getBoolean(String attribute) {
		return Boolean.TRUE.equals(memento.getBoolean(attribute));
	}

	protected Integer getInteger(String attribute) {
		return memento.getInteger(attribute);
	}

	protected Float getFloat(String attribute) {
		return memento.getFloat(attribute);
	}

	protected boolean contains(String attribute) {
		return Arrays.asList(memento.getAttributeKeys()).contains(attribute);
	}

	protected IMemento[] getChildren(String tagName) {
		return memento.getChildren(tagName);
	}

	protected IMemento getChild(String tagName) {
		return memento.getChild(tagName);
	}

	@Override
	public String toString() {
		if (!(memento instanceof XMLMemento)) {
			return null;
		}
		StringWriter writer = new StringWriter();
		try {
			((XMLMemento) memento).save(writer);
		} catch (IOException e) {
			WorkbenchPlugin.log(e);
		}
		return writer.toString();
	}

}
