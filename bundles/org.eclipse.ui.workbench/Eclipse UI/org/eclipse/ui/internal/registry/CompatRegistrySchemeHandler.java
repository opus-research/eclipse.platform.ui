package org.eclipse.ui.internal.registry;

import org.eclipse.e4.ui.internal.workbench.swt.ResourceUtility;
import org.eclipse.emf.common.util.URI;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.menus.MenuHelper;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;

/**
 * @since 3.5
 *
 */
public class CompatRegistrySchemeHandler implements ResourceUtility.UrlSchemeHandler {
	public static final String COMPAT_ICON_SOURCE = "compat-icon-source"; //$NON-NLS-1$

	public static final String createIconUri(String regType, String partId) {
		return COMPAT_ICON_SOURCE + ":/" + regType + "/" + partId; //$NON-NLS-1$//$NON-NLS-2$
	}

	@Override
	public String getScheme() {
		return COMPAT_ICON_SOURCE;
	}

	@Override
	public URI getPlatformURI(URI uri) {
		if ("view".equals(uri.segment(0))) { //$NON-NLS-1$
			IViewRegistry viewRegistry = WorkbenchPlugin.getDefault().getViewRegistry();
			IViewDescriptor view = viewRegistry.find(uri.segment(1));
			if (view instanceof ViewDescriptor) {
				return URI.createURI(MenuHelper.getIconURI(((ViewDescriptor) view).getConfigurationElement(),
						IWorkbenchRegistryConstants.ATT_ICON));
			}
		} else if ("editor".equals(uri.segment(0))) { //$NON-NLS-1$
			IEditorRegistry editorRegistry = WorkbenchPlugin.getDefault().getEditorRegistry();
			IEditorDescriptor editor = editorRegistry.findEditor(uri.segment(1));
			if (editor instanceof EditorDescriptor) {
				return URI.createURI(MenuHelper.getIconURI(((EditorDescriptor) editor).getConfigurationElement(),
						IWorkbenchRegistryConstants.ATT_ICON));
			}
		}
		return null;
	}

}
