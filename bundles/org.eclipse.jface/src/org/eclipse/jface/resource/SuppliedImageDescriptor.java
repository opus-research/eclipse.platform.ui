/*******************************************************************************
 * Copyright (c) 2015 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Sergey Grant (Google) - initial implementation
 ******************************************************************************/

package org.eclipse.jface.resource;

import java.util.function.Supplier;

import org.eclipse.swt.graphics.ImageData;

class SuppliedImageDescriptor extends ImageDescriptor {

	private Supplier<ImageData> supplier;
	private ImageData imageData;

	SuppliedImageDescriptor(Supplier<ImageData> supplier) {
		this.supplier = supplier;
	}

	@Override
	public ImageData getImageData() {
		if (imageData == null) {
			imageData = supplier.get();
		}
		return imageData;
	}
}
