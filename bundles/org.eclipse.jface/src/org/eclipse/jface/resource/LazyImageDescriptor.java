package org.eclipse.jface.resource;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.graphics.ImageData;
import org.osgi.framework.Bundle;

/**
 * ImageDescriptor with arbitrary deferred initialization policy. Policy passed
 * using the ImageDescriptorGenerator interface at object creation and called
 * during the first {@link #getImageData()} call.
 * 
 * @since 3.12
 *
 */
public class LazyImageDescriptor extends ImageDescriptor {

	/**
	 * Interface for ImageDescriptor lazy initialization code to be run once
	 * upon the first {@link #getImageData()} call.
	 * 
	 * @since 3.4
	 *
	 */
	public interface ImageDescriptorGenerator {
		/**
		 * @return ImageDescriptor to be lazily generated.
		 */
		ImageDescriptor generateImageDescriptor();
	}

	private ImageDescriptorGenerator generator;
	private ImageDescriptor imageDescriptor;

	/**
	 * @param generator
	 *
	 */
	public LazyImageDescriptor(ImageDescriptorGenerator generator) {
		this.generator = generator;
	}

	/**
	 * @param bundle
	 * @param path
	 * @param fallback
	 * @param fallbackPath
	 * @return LazyImageDescriptor
	 */
	public static LazyImageDescriptor initFromFileName(Bundle bundle, String path, Class<?> fallback, String fallbackPath) {
		return new LazyImageDescriptor(() -> {
			if (bundle != null) {
				URL url = FileLocator.find(bundle, new Path(path), null);
				if (url != null)
					return ImageDescriptor.createFromURL(url);
			}
			// If we failed then load from the backup file
			return ImageDescriptor.createFromFile(fallback, fallbackPath);
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.resource.ImageDescriptor#getImageData()
	 */
	@Override
	public ImageData getImageData() {
		if (imageDescriptor == null) {
			imageDescriptor = generator.generateImageDescriptor();
		}
		return imageDescriptor.getImageData();
	}

}
