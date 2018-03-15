/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jface.viewers;

import java.util.Arrays;
import java.util.Objects;

import org.eclipse.jface.resource.CompositeImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;

/**
 * A <code>DecorationOverlayIcon</code> is an image descriptor that can be used
 * to overlay decoration images on to the 4 corner quadrants of a base image.
 * The four quadrants are {@link IDecoration#TOP_LEFT}, {@link IDecoration#TOP_RIGHT},
 * {@link IDecoration#BOTTOM_LEFT} and {@link IDecoration#BOTTOM_RIGHT}. Additionally,
 * the overlay can be used to provide an underlay corresponding to {@link IDecoration#UNDERLAY}.
 *
 * @since 3.3
 * @see IDecoration
 */
public class DecorationOverlayIcon extends CompositeImageDescriptor {

    // the base image
	private Image baseImage;
	private ImageDescriptor baseDescriptor;

    // the overlay images
    private ImageDescriptor[] overlays;

	/**
	 * Do not reference directly as it's initialized lazily use
	 * {@link #getBaseImageData()} instead.
	 */
	private ImageData baseImageData;

	/**
	 * The size. Do not reference directly as it may be initiated lazily, use
	 * {@link #getSize()} instead.
	 */
    private Point size;

    /**
     * Create the decoration overlay for the base image using the array of
     * provided overlays. The indices of the array correspond to the values
     * of the 5 overlay constants defined on {@link IDecoration}
     * ({@link IDecoration#TOP_LEFT}, {@link IDecoration#TOP_RIGHT},
     * {@link IDecoration#BOTTOM_LEFT}, {@link IDecoration#BOTTOM_RIGHT}
     * and{@link IDecoration#UNDERLAY}).
     *
     * @param baseImage the base image
     * @param overlaysArray the overlay images
     * @param sizeValue the size of the resulting image
     */
    public DecorationOverlayIcon(Image baseImage,
            ImageDescriptor[] overlaysArray, Point sizeValue) {
		this.baseImage = baseImage;
        this.overlays = overlaysArray;
        this.size = sizeValue;
    }

    /**
     * Create the decoration overlay for the base image using the array of
     * provided overlays. The indices of the array correspond to the values
     * of the 5 overlay constants defined on {@link IDecoration}
     * ({@link IDecoration#TOP_LEFT}, {@link IDecoration#TOP_RIGHT},
     * {@link IDecoration#BOTTOM_LEFT}, {@link IDecoration#BOTTOM_RIGHT}
     * and {@link IDecoration#UNDERLAY}).
     *
     * @param baseImage the base image
     * @param overlaysArray the overlay images
     */
    public DecorationOverlayIcon(Image baseImage, ImageDescriptor[] overlaysArray) {
    	this(baseImage, overlaysArray, new Point(baseImage.getBounds().width, baseImage.getBounds().height));
    }

    /**
     * Create a decoration overlay icon that will place the given overlay icon in
     * the given quadrant of the base image.
	 * @param baseImage the base image
	 * @param overlayImage the overlay image
	 * @param quadrant the quadrant (one of {@link IDecoration}
     * ({@link IDecoration#TOP_LEFT}, {@link IDecoration#TOP_RIGHT},
     * {@link IDecoration#BOTTOM_LEFT}, {@link IDecoration#BOTTOM_RIGHT}
     * or {@link IDecoration#UNDERLAY})
	 */
	public DecorationOverlayIcon(Image baseImage, ImageDescriptor overlayImage, int quadrant) {
		this(baseImage, createArrayFrom(overlayImage, quadrant));
	}

	/**
	 * Create a decoration overlay icon that will place the given overlay icon
	 * in the given quadrant of the base image.
	 *
	 * @param baseImage
	 *            the base image
	 * @param overlayImage
	 *            the overlay image
	 * @param quadrant
	 *            the quadrant (one of {@link IDecoration}
	 *            ({@link IDecoration#TOP_LEFT}, {@link IDecoration#TOP_RIGHT},
	 *            {@link IDecoration#BOTTOM_LEFT},
	 *            {@link IDecoration#BOTTOM_RIGHT} or
	 *            {@link IDecoration#UNDERLAY})
	 * @since 3.13
	 */
	public DecorationOverlayIcon(ImageDescriptor baseImage, ImageDescriptor overlayImage, int quadrant) {
		this(null, createArrayFrom(overlayImage, quadrant), null);
		this.baseDescriptor = baseImage;
	}

	/**
	 * Convert the given image and quadrant into the proper input array.
	 * @param overlayImage the overlay image
	 * @param quadrant the quadrant
	 * @return an array with the given image in the proper quadrant
	 */
	private static ImageDescriptor[] createArrayFrom(
			ImageDescriptor overlayImage, int quadrant) {
		ImageDescriptor[] descs = new ImageDescriptor[] { null, null, null, null, null };
		descs[quadrant] = overlayImage;
		return descs;
	}

	/**
     * Draw the overlays for the receiver.
     * @param overlaysArray
     */
    private void drawOverlays(ImageDescriptor[] overlaysArray) {

        for (int i = 0; i < overlays.length; i++) {
            ImageDescriptor overlay = overlaysArray[i];
            if (overlay == null) {
				continue;
			}
            ImageData overlayData = overlay.getImageData();
            //Use the missing descriptor if it is not there.
            if (overlayData == null) {
				overlayData = ImageDescriptor.getMissingImageDescriptor()
                        .getImageData();
			}
            switch (i) {
            case IDecoration.TOP_LEFT:
                drawImage(overlayData, 0, 0);
                break;
            case IDecoration.TOP_RIGHT:
				drawImage(overlayData, getSize().x - overlayData.width, 0);
                break;
            case IDecoration.BOTTOM_LEFT:
				drawImage(overlayData, 0, getSize().y - overlayData.height);
                break;
            case IDecoration.BOTTOM_RIGHT:
				drawImage(overlayData, getSize().x - overlayData.width, getSize().y
                        - overlayData.height);
                break;
            }
        }
    }

    @Override
	public boolean equals(Object o) {
        if (!(o instanceof DecorationOverlayIcon)) {
			return false;
		}
        DecorationOverlayIcon other = (DecorationOverlayIcon) o;
		return Objects.equals(baseImage, other.baseImage) && Objects.equals(baseDescriptor, other.baseDescriptor)
                && Arrays.equals(overlays, other.overlays);
    }

    @Override
	public int hashCode() {
		int code = System.identityHashCode(baseImage);
		code ^= System.identityHashCode(baseDescriptor);
        for (int i = 0; i < overlays.length; i++) {
            if (overlays[i] != null) {
				code ^= overlays[i].hashCode();
			}
        }
        return code;
    }

    @Override
	protected void drawCompositeImage(int width, int height) {
    	if (overlays.length > IDecoration.UNDERLAY) {
	        ImageDescriptor underlay = overlays[IDecoration.UNDERLAY];
	        if (underlay != null) {
				drawImage(underlay.getImageData(), 0, 0);
			}
    	}
    	if (overlays.length > IDecoration.REPLACE && overlays[IDecoration.REPLACE] != null) {
    		drawImage(overlays[IDecoration.REPLACE].getImageData(), 0, 0);
    	} else {
			drawImage(getBaseImageData(), 0, 0);
    	}
        drawOverlays(overlays);
    }

    @Override
	protected Point getSize() {
		if (size == null) {
			size = new Point(getBaseImageData().width, getBaseImageData().height);
		}
        return size;
    }

    @Override
	protected int getTransparentPixel() {
		return getBaseImageData().transparentPixel;
    }

	private ImageData getBaseImageData() {
		if (this.baseImageData == null) {
			if (this.baseImage != null) {
				this.baseImageData = this.baseImage.getImageData();
			} else if (this.baseDescriptor != null) {
				this.baseImageData = this.baseDescriptor.getImageData();
			}
		}
		return this.baseImageData;
	}

}
