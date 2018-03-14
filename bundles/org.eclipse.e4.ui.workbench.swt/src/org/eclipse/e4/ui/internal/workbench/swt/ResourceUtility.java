/*******************************************************************************
 * Copyright (c) 2008, 2010 BestSolution.at and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
 *     Boris Bokowski, IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.ui.internal.workbench.swt;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.e4.ui.workbench.swt.util.ISWTResourceUtilities;
import org.eclipse.emf.common.util.URI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class ResourceUtility implements ISWTResourceUtilities {

	public interface UrlSchemeHandler {
		public String getScheme();

		public URI getPlatformURI(URI uri);
	}

	private Map<String, UrlSchemeHandler> schemeHandler = new HashMap<>();

	public ResourceUtility() {
		super();

		try {
			BundleContext context = WorkbenchSWTActivator.getDefault().getContext();
			Collection<ServiceReference<UrlSchemeHandler>> serviceReferences = context
					.getServiceReferences(UrlSchemeHandler.class, null);
			for (ServiceReference<UrlSchemeHandler> ref : serviceReferences) {
				UrlSchemeHandler service = context.getService(ref);
				schemeHandler.put(service.getScheme(), service);
			}
		} catch (InvalidSyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public ImageDescriptor imageDescriptorFromURI(URI iconPath) {
		if (!iconPath.scheme().equals("platform")) {
			UrlSchemeHandler urlSchemeHandler = schemeHandler.get(iconPath.scheme());
			if (urlSchemeHandler != null) {
				iconPath = urlSchemeHandler.getPlatformURI(iconPath);
			}
		}

		try {
			return ImageDescriptor.createFromURL(new URL(iconPath.toString()));
		} catch (MalformedURLException e) {
			System.err.println("iconURI \"" + iconPath.toString()
					+ "\" is invalid, no image will be shown");
			return null;
		}
	}

	@Override
	public Image adornImage(Image toAdorn, Image adornment) {
		if (toAdorn == null)
			return null;
		if (adornment == null)
			return toAdorn;
		Rectangle adornmentSize = adornment.getBounds();

		Image adornedImage = new Image(toAdorn.getDevice(), 16, 16);
		GC gc = new GC(adornedImage);
		gc.drawImage(toAdorn, 0, 0);
		// For now assume top-right
		gc.drawImage(adornment, 16 - adornmentSize.width, 0);
		gc.dispose();

		return adornedImage;
	}
}
