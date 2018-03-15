package org.eclipse.ui.plugin;

import java.util.HashSet;
import java.util.Set;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 * An image manager provides convenient means to handle images for plug-ins that
 * don't have a bundle activator. It is similar to the facilities in
 * {@link AbstractUIPlugin}.
 * <p>
 * Consuming plug-ins should create one subclass per plug-in and register their
 * images in the implementation of <code>initializeImageRegistry</code>. The
 * keys for the registered images should be defined as public constants in that
 * subclass.
 * </p>
 *
 *
 */
public abstract class AbstractImageManager {

	static private final Set<Class<? extends AbstractImageManager>> classes = new HashSet<>();

	protected AbstractImageManager() {
		// ensure that we don't run in to concurrency issues when different threads
		// create instances (at the same time)
		if (Display.getCurrent() == null) {
			throw new IllegalStateException("Image Manager can only be used in the UI thread"); //$NON-NLS-1$
		}
		// ensure that every consumer (subclass) only creates one instance of it's image
		// manager. This ensures that we don't register the same images multiple times.
		Class<? extends AbstractImageManager> currentClass = getClass();
		if ("org.eclipse.ui.plugin.UiPluginImageManager".equals(currentClass.getName())) { //$NON-NLS-1$
			// AbstractUIPlugin instances all use instances of UiPluginImageManager. So we
			// have multiple instance for this class.
			return;
		}
		if (classes.contains(currentClass)) {
			throw new IllegalStateException(
					"Don't create multiple instances for the same image manager class. As this would load the same images multiple times."); //$NON-NLS-1$
		}
		classes.add(currentClass);
	}

	/**
	 * The registry for all graphic images; <code>null</code> if not yet
	 * initialized.
	 */
	private ImageRegistry imageRegistry = null;

	/**
	 * Returns a new image registry. The registry will be used to manage images
	 * which are frequently used. The implementation of this method creates an empty
	 * registry.
	 *
	 * @return ImageRegistry the resulting registry.
	 * @see #getImageRegistry
	 */
	private ImageRegistry createImageRegistry() {

		//If we are in the UI Thread use that
		if (Display.getCurrent() != null) {
			return new ImageRegistry(Display.getCurrent());
		}

		if (PlatformUI.isWorkbenchRunning()) {
			return new ImageRegistry(PlatformUI.getWorkbench().getDisplay());
		}

		//Invalid thread access if it is not the UI Thread
		//and the workbench is not created.
		throw new SWTError(SWT.ERROR_THREAD_INVALID_ACCESS);
	}

	/**
	 * Initializes an image registry with images which are frequently used by the
	 * consumer.
	 * <p>
	 * The image registry contains the images / image descriptors that are very
	 * frequently used. Since many OSs have a severe limit on the number of images
	 * that can be in memory at any given time, consumers should only register image
	 * descriptors. If consumers register images directly they should keep a small
	 * number of images in the registry.
	 * </p>
	 * <p>
	 * Implementors should create a JFace {@link ImageDescriptor} for each
	 * frequently used image. The descriptors describe how to create/find the image
	 * should it be needed. The image described by the descriptor is not actually
	 * allocated until someone retrieves it.
	 * </p>
	 * <p>
	 * Subclasses have to override this method to fill the image registry. This
	 * method is called at the first access to the {@link ImageRegistry}
	 * </p>
	 *
	 * @param reg
	 *            the registry to initialize
	 *
	 * @see #getImageRegistry
	 */
	protected abstract void initializeImageRegistry(ImageRegistry reg);

	/**
	 * Returns the image registry.
	 * <p>
	 * The image registry contains the images that are very frequently used and so
	 * need to be globally shared within the plug-in. Since many OSes have a severe
	 * limit on the number of images that can be in memory at any given time,
	 * consumers should only keep a small number of images in their registry.
	 * <p>
	 * Subclasses should implement <code>initializeImageRegistry</code> if they have
	 * custom graphic images to load.
	 * </p>
	 * <p>
	 * Subclasses may override this method but are not expected to.
	 * </p>
	 *
	 * @return the image registry
	 */
	public ImageRegistry getImageRegistry() {
		if (this.imageRegistry == null) {
			this.imageRegistry = createImageRegistry();
			initializeImageRegistry(this.imageRegistry);
		}
		return this.imageRegistry;
	}

	/**
	 * Returns the image for the given key.
	 *
	 * @param key
	 * @return the image
	 */
	public Image getImage(String key) {
		return this.getImageRegistry().get(key);
	}

	/**
	 * Returns the {@link ImageDescriptor} for the given key.
	 *
	 * @param key
	 * @return the image descriptor
	 */
	public ImageDescriptor getImageDescriptor(String key) {
		return this.getImageRegistry().getDescriptor(key);
	}

	/**
	 * Disposes all the images handled by this instance. Consumer should call this
	 * to free resources when no longer needed (e.g. when the corresponding bundle
	 * is stopped).
	 */
	public void dispose() {
		if (this.imageRegistry != null) {
			this.imageRegistry.dispose();
		}
		this.imageRegistry = null;
	}

	/**
	 * Puts a JFace {@link ImageDescriptor} into the image registry.
	 *
	 * @param pluginId
	 *            The Id of the plug-in that contains the image file
	 * @param key
	 *            The key that is used to address the image. Don't use the filename
	 *            as the key as filenames may change but keys should kept stable
	 * @param fileName
	 *            The image file
	 */
	protected void registerImage(String pluginId, String key, String fileName) {

		ImageRegistry registry = getImageRegistry();

		ImageDescriptor desc = AbstractUIPlugin.imageDescriptorFromPlugin(pluginId, fileName);

		if (registry.get(key) != null) {
			throw new IllegalStateException("duplicate imageId in image registry."); //$NON-NLS-1$
		}
		registry.put(key, desc);
	}

}
