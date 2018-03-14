/*******************************************************************************
 * Copyright (c) 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Steven Spungin <steven@spungin.tv> - Bug 442821 - [e4.emf.xpath] allow factory to accept custom functions
 ******************************************************************************/
package org.eclipse.e4.emf.xpath;

import java.util.List;

import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.eclipse.e4.emf.internal.xpath.EObjectPointerFactory;
import org.eclipse.e4.emf.internal.xpath.JXPathContextFactoryImpl;
import org.eclipse.emf.ecore.EObject;

/**
 * Factory which creates an XPathContextFactory for {@link EObject}s
 * 
 * @since 1.0
 */
public class EcoreXPathContextFactory{

	static {
		JXPathContextReferenceImpl.addNodePointerFactory(new EObjectPointerFactory());
	}

	/**
	 * Create a new factory
	 * 
	 * @return the factory
	 */
	public static XPathContextFactory<EObject> newInstance() {
		return new JXPathContextFactoryImpl<EObject>();
	}

	public static XPathContextFactory<EObject> newInstance(List<Class<?>> functions, List<String> namespaces) {
		JXPathContextFactoryImpl<EObject> ret = new JXPathContextFactoryImpl<EObject>();
		ret.setFunctions(functions, namespaces);
		return ret;
	}

}
