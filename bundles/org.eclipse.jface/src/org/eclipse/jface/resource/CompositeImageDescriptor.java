/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Wahlbrink - fix for bug 341702 - incorrect mixing of images with alpha channel
 *******************************************************************************/
package org.eclipse.jface.resource;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageDataProvider;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;

/**
 * Abstract base class for image descriptors that synthesize an image from other
 * images in order to simulate the effect of custom drawing. For example, this
 * could be used to superimpose a red bar dexter symbol across an image to
 * indicate that something was disallowed.
 * <p>
 * Subclasses must implement {@link #getSize()} and {@link #drawImage(ImageDataProvider, int, int)}.
 * Little or no work happens until the image descriptor's image is
 * actually requested by a call to <code>createImage</code> (or to
 * <code>getImageData</code> directly).
 * </p>
 */
public abstract class CompositeImageDescriptor extends ImageDescriptor {

	/**
	 * The image data for this composite image.
	 */
	private ImageData imageData;

	/**
	 * The zoom level for this composite image.
	 */
	private int compositeZoom;

	/**
	 * Constructs an uninitialized composite image.
	 */
	protected CompositeImageDescriptor() {
	}

	/**
	 * Draw the composite images.
	 * <p>
	 * Subclasses must implement this framework method to paint images within
	 * the given bounds using one or more calls to the <code>drawImage</code>
	 * framework method.
	 * </p>
	 *
	 * @param width
	 *            the width
	 * @param height
	 *            the height
	 * @see #drawImage(ImageDataProvider, int, int)
	 */
	protected abstract void drawCompositeImage(int width, int height);

//	* @deprecated Use {@link #drawCompositeImage(int, int, int)} instead.
//	*/
//	@Deprecated
//	protected void drawCompositeImage(int width, int height) {
//		throw new IllegalStateException("This method must not be called"); //$NON-NLS-1$ TODO:
//		// JFace
//		// project
//		// settings
//		// are
//		// brain-damaged.
//	}
//
//	/**
//	 * Draw the composite images.
//	 * <p>
//	 * Subclasses must implement this framework method to paint images within
//	 * the given bounds using one or more calls to the <code>drawImage</code>
//	 * framework method.
//	 * </p>
//	 *
//	 * @param width
//	 *            the width
//	 * @param height
//	 *            the height
//	 * @param zoom
//	 *            the zoom level
//	 * @since 3.13
//	 */
//	protected void drawCompositeImage(int width, int height, int zoom) {
//		if (zoom == 100) {
//			drawCompositeImage(width, height);
//		} else {
//			throw new IllegalStateException(this.getClass().getName() + " must override this method."); //$NON-NLS-1$ TODO
//		}
//	}

	/**
	 * Draws the given source image data into this composite image at the given
	 * position.
	 * <p>
	 * Call this internal framework method to superimpose another image atop
	 * this composite image.
	 * </p>
	 *
	 * @param src
	 *            the source image data
	 * @param ox
	 *            the x position
	 * @param oy
	 *            the y position
	 * @deprecated Use {@link #drawImage(ImageDataProvider, int, int)} instead.
	 */
	@Deprecated
	final protected void drawImage(ImageData src, int ox, int oy) {
		if (src == null) { // wrong hack for https://bugs.eclipse.org/372956 , kept for compatibility with broken client code
			return;
		}
		drawImage(getUnzoomedImageDataProvider(src), ox, oy);
	}

	/**
	 * Draws the given source image data into this composite image at the given
	 * position.
	 * <p>
	 * Subclasses call this framework method to superimpose another image atop
	 * this composite image. This method must only be called within the dynamic
	 * scope of a call to {@link #drawCompositeImage(int, int)}.
	 * </p>
	 *
	 * @param srcProvider
	 *            the source image data provider
	 * @param ox
	 *            the x position
	 * @param oy
	 *            the y position
	 * @since 3.13
	 */
	final protected void drawImage(ImageDataProvider srcProvider, int ox, int oy) {
		ImageData dst = imageData;
		ImageData src = getZoomedImageData(srcProvider);

		PaletteData srcPalette = src.palette;
		ImageData srcMask = null;
		int alphaMask = 0, alphaShift = 0;
		if (src.maskData != null) {
			srcMask = src.getTransparencyMask ();
			if (src.depth == 32) {
				alphaMask = ~(srcPalette.redMask | srcPalette.greenMask | srcPalette.blueMask);
				while (alphaMask != 0 && ((alphaMask >>> alphaShift) & 1) == 0) alphaShift++;
			}
		}
		for (int srcY = 0, dstY = srcY + autoScaleUp(oy); srcY < src.height; srcY++, dstY++) {
			for (int srcX = 0, dstX = srcX + autoScaleUp(ox); srcX < src.width; srcX++, dstX++) {
				if (!(0 <= dstX && dstX < dst.width && 0 <= dstY && dstY < dst.height)) continue;
				int srcPixel = src.getPixel(srcX, srcY);
				int srcAlpha = 255;
				if (src.maskData != null) {
					if (src.depth == 32) {
						srcAlpha = (srcPixel & alphaMask) >>> alphaShift;
						if (srcAlpha == 0) {
							srcAlpha = srcMask.getPixel(srcX, srcY) != 0 ? 255 : 0;
						}
					} else {
						if (srcMask.getPixel(srcX, srcY) == 0) srcAlpha = 0;
					}
				} else if (src.transparentPixel != -1) {
					if (src.transparentPixel == srcPixel) srcAlpha = 0;
				} else if (src.alpha != -1) {
					srcAlpha = src.alpha;
				} else if (src.alphaData != null) {
					srcAlpha = src.getAlpha(srcX, srcY);
				}
				if (srcAlpha == 0) continue;
				int srcRed, srcGreen, srcBlue;
				if (srcPalette.isDirect) {
					srcRed = srcPixel & srcPalette.redMask;
					srcRed = (srcPalette.redShift < 0) ? srcRed >>> -srcPalette.redShift : srcRed << srcPalette.redShift;
					srcGreen = srcPixel & srcPalette.greenMask;
					srcGreen = (srcPalette.greenShift < 0) ? srcGreen >>> -srcPalette.greenShift : srcGreen << srcPalette.greenShift;
					srcBlue = srcPixel & srcPalette.blueMask;
					srcBlue = (srcPalette.blueShift < 0) ? srcBlue >>> -srcPalette.blueShift : srcBlue << srcPalette.blueShift;
				} else {
					RGB rgb = srcPalette.getRGB(srcPixel);
					srcRed = rgb.red;
					srcGreen = rgb.green;
					srcBlue = rgb.blue;
				}
				int dstRed, dstGreen, dstBlue, dstAlpha;
				if (srcAlpha == 255) {
					dstRed = srcRed;
					dstGreen = srcGreen;
					dstBlue= srcBlue;
					dstAlpha = srcAlpha;
				} else {
					int dstPixel = dst.getPixel(dstX, dstY);
					dstAlpha = dst.getAlpha(dstX, dstY);
					dstRed = (dstPixel & 0xFF) >>> 0;
					dstGreen = (dstPixel & 0xFF00) >>> 8;
					dstBlue = (dstPixel & 0xFF0000) >>> 16;
					if (dstAlpha == 255) { // simplified calculations for performance
						dstRed += (srcRed - dstRed) * srcAlpha / 255;
						dstGreen += (srcGreen - dstGreen) * srcAlpha / 255;
						dstBlue += (srcBlue - dstBlue) * srcAlpha / 255;
					} else {
						// See Porter T., Duff T. 1984. "Compositing Digital Images".
						// Computer Graphics 18 (3): 253-259.
						dstRed = srcRed * srcAlpha * 255 + dstRed * dstAlpha * (255 - srcAlpha);
						dstGreen = srcGreen * srcAlpha * 255 + dstGreen * dstAlpha * (255 - srcAlpha);
						dstBlue = srcBlue * srcAlpha * 255 + dstBlue * dstAlpha * (255 - srcAlpha);
						dstAlpha = srcAlpha * 255 + dstAlpha * (255 - srcAlpha);
						if (dstAlpha != 0) { // if both original alphas == 0, then all colors are 0
							dstRed /= dstAlpha;
							dstGreen /= dstAlpha;
							dstBlue /= dstAlpha;
							dstAlpha /= 255;
						}
					}
				}
				dst.setPixel(dstX, dstY, ((dstRed & 0xFF) << 0) | ((dstGreen & 0xFF) << 8) | ((dstBlue & 0xFF) << 16));
				dst.setAlpha(dstX, dstY, dstAlpha);
			}
		}
	}

	/**
	 * @deprecated Use {@link #getImageData(int)} instead.
	 */
	@Deprecated
	@Override
	public ImageData getImageData() {
		return getImageData(100);
	}

	@Override
	public ImageData getImageData(int zoom) {
		if (!supportsZoomLevel(zoom)) {
			return null;
		}
		Point size = getSize();

		/* Create a 24 bit image data with alpha channel */
		imageData = new ImageData(scaleUp(size.x, zoom), scaleUp(size.y, zoom), 24,
				new PaletteData(0xFF, 0xFF00, 0xFF0000));
		imageData.alphaData = new byte[imageData.width * imageData.height];
		compositeZoom = zoom;

		drawCompositeImage(size.x, size.y);

		/* Detect minimum transparency */
		boolean transparency = false;
		byte[] alphaData = imageData.alphaData;
		for (int i = 0; i < alphaData.length; i++) {
			int alpha = alphaData[i] & 0xFF;
			if (!(alpha == 0 || alpha == 255)) {
				/* Full alpha channel transparency */
				return imageData;
			}
			if (!transparency && alpha == 0) transparency = true;
		}
		if (transparency) {
			/* Reduce to 1-bit alpha channel transparency */
			PaletteData palette = new PaletteData(new RGB[]{new RGB(0, 0, 0), new RGB(255, 255, 255)});
			ImageData mask = new ImageData(imageData.width, imageData.height, 1, palette);
			for (int y = 0; y < mask.height; y++) {
				for (int x = 0; x < mask.width; x++) {
					mask.setPixel(x, y, imageData.getAlpha(x, y) == 255 ? 1 : 0);
				}
			}
		} else {
			/* no transparency */
			imageData.alphaData = null;
		}
		return imageData;
	}


	/**
	 * Return the transparent pixel for the receiver.
	 * <strong>NOTE</strong> This value is not currently in use in the
	 * default implementation.
	 * @return int
	 * @since 3.3
	 */
	protected int getTransparentPixel() {
		return 0;
	}

	/**
	 * Return the size of this composite image.
	 * <p>
	 * Subclasses must implement this framework method.
	 * </p>
	 *
	 * @return the x and y size of the image expressed as a point object
	 */
	protected abstract Point getSize();

	/**
	 * @param imageData The imageData to set.
	 * @since 3.3
	 * @deprecated This method doesn't make sense and should never have been
	 *             made API.
	 */
	@Deprecated
	protected void setImageData(ImageData imageData) {
		this.imageData = imageData;
	}

	/**
	 * Returns whether the given zoom level is supported by this
	 * CompositeImageDescriptor.
	 *
	 * @param zoom
	 *            the zoom level
	 * @return whether the given zoom level is supported. Must return true for
	 *         {@code zoom == 100}.
	 * @since 3.13
	 */
	protected boolean supportsZoomLevel(int zoom) {
		// Only support integer zoom levels, because getZoomedImageData(..)
		// suffers from Bug 97506: [HiDPI] ImageData.scaledTo() should use a
		// better interpolation method.
		return zoom > 0 && zoom % 100 == 0;
	}

	private ImageData getZoomedImageData(ImageDataProvider srcProvider) {
		ImageData src = srcProvider.getImageData(compositeZoom);
		if (src == null) {
			ImageData src100 = srcProvider.getImageData(100);
			src = src100.scaledTo(autoScaleUp(src100.width), autoScaleUp(src100.height));
		}
		return src;
	}

	/**
	 * Returns the current zoom level.
	 * <p>
	 * This method must only be called within the dynamic scope of a call to
	 * {@link #drawCompositeImage(int, int)}.
	 * </p>
	 *
	 * @return The zoom level in % of the standard resolution (which is 1
	 *         physical monitor pixel == 1 SWT logical point). Typically 100,
	 *         150, or 200.
	 * @since 3.13
	 */
	protected int getZoomLevel() {
		return compositeZoom;
	}

	/**
	 * Converts a value in high-DPI pixels to the corresponding value in SWT points.
	 * <p>
	 * This method must only be called within the dynamic
	 * scope of a call to {@link #drawCompositeImage(int, int)}.
	 * </p>
	 *
	 * @param pixels a value in high-DPI pixels
	 * @return corresponding value in SWT points
	 * @since 3.13
	 */
	protected int autoScaleDown(int pixels) {
		// @see SWT's internal DPIUtil#autoScaleDown(int)
		if (compositeZoom == 100) {
			return pixels;
		}
		float scaleFactor = compositeZoom / 100f;
		return Math.round(pixels / scaleFactor);
	}

	/**
	 * Converts a value in SWT points to the corresponding value in high-DPI pixels.
	 * <p>
	 * This method must only be called within the dynamic
	 * scope of a call to {@link #drawCompositeImage(int, int)}.
	 * </p>
	 *
	 * @param points a value in SWT points
	 * @return corresponding value in high-DPI pixels
	 * @since 3.13
	 */
	protected int autoScaleUp(int points) {
		// @see SWT's internal DPIUtil#autoScaleUp(int)
		return scaleUp(points, compositeZoom);
	}

	private static int scaleUp(int points, int zoom) {
		if (zoom == 100) {
			return points;
		}
		float scaleFactor = zoom / 100f;
		return Math.round(points * scaleFactor);
	}

	private static ImageDataProvider getUnzoomedImageDataProvider(ImageData imageData) {
		return zoom -> zoom == 100 ? imageData : null;
	}
}
