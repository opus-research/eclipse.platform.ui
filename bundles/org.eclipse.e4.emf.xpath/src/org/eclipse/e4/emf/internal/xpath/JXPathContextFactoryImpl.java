/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - adjustment to EObject
  *     Steven Spungin <steven@spungin.tv> - Bug 442821 - [e4.emf.xpath] allow factory to accept custom functions
******************************************************************************/
package org.eclipse.e4.emf.internal.xpath;

import java.util.List;

import org.eclipse.e4.emf.xpath.XPathContext;
import org.eclipse.e4.emf.xpath.XPathContextFactory;

/**
 * Factory creating context using JXPath
 * 
 * @param <Type>
 *            the object the XPath is created for
 */
public class JXPathContextFactoryImpl<Type> extends XPathContextFactory<Type> {

	private List<Class<?>> functions;
	private List<String> namespaces;

	public XPathContext newContext(XPathContext parentContext, Object contextBean) {
		JXPathContextImpl ret = new JXPathContextImpl(parentContext, contextBean);
		ret.addFunctions(functions, namespaces);
		return ret;
	}


	public XPathContext newContext(Type contextBean) {
		JXPathContextImpl ret = new JXPathContextImpl(contextBean);
		ret.addFunctions(functions, namespaces);
		return ret;
	}

	public void setFunctions(List<Class<?>> functions, List<String> namespaces) {
		this.functions = functions;
		this.namespaces = namespaces;
	}

}
