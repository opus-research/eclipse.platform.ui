/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.e4.ui.css.swt.resources;

import org.eclipse.swt.graphics.Resource;

public class VolatileResource <T extends Resource> {	
	private boolean valid;
	
	private T resource;
	
	public VolatileResource(T resource) {
		setResource(resource);
	}
	
	public T getResource() {
		return resource;
	}
	
	public void setResource(T resource) {
		this.resource = resource;
	}
	
	public void setValid(boolean valid) {
		this.valid = valid;
	}
	
	public boolean isValid() {
		return valid;
	}	
}