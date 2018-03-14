/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.dom.preference;

import static org.eclipse.e4.ui.css.swt.helpers.ThemeElementDefinitionHelper.escapeId;

import org.eclipse.e4.ui.css.core.dom.ElementAdapter;
import org.eclipse.e4.ui.css.core.engine.CSSEngine;
import org.eclipse.e4.ui.css.core.utils.ClassUtils;
import org.eclipse.e4.ui.internal.css.swt.preference.IPreferenceNodeOverridable;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class PreferenceNodeElement extends ElementAdapter {
	private String localName;

	private String namespaceURI;

	private String id;

	public PreferenceNodeElement(IPreferenceNodeOverridable nativeWidget,
			CSSEngine engine) {
		super(nativeWidget, engine);
	}

	@Override
	public NodeList getChildNodes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getNamespaceURI() {
		if (namespaceURI == null) {
			namespaceURI = ClassUtils.getPackageName(getNativeWidget()
					.getClass());
		}
		return namespaceURI;
	}

	@Override
	public Node getParentNode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCSSId() {
		if (id == null) {
			id = escapeId(((IPreferenceNodeOverridable) getNativeWidget())
					.getId());
		}
		return id;
	}

	@Override
	public String getCSSClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getCSSStyle() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getLocalName() {
		if (localName == null) {
			localName = ClassUtils.getSimpleName(getNativeWidget().getClass());
		}
		return localName;
	}

	@Override
	public String getAttribute(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

}
