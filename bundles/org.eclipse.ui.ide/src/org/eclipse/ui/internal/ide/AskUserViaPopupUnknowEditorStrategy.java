package org.eclipse.ui.internal.ide;

import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.EditorSelectionDialog;
import org.eclipse.ui.ide.IUnknownEditorStrategy;

/**
 * @since 3.12
 *
 */
public class AskUserViaPopupUnknowEditorStrategy implements IUnknownEditorStrategy {

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * org.eclipse.ui.ide.IUnknownEditorStrategy#getEditorDescriptor(java.lang.
	 * String, org.eclipse.ui.IEditorRegistry)
	 */
	@Override
	public IEditorDescriptor getEditorDescriptor(String fileName, IEditorRegistry editorRegistry) {
		EditorSelectionDialog dialog = new EditorSelectionDialog(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		dialog.setFileName(fileName);
		dialog.setBlockOnOpen(true);
		dialog.open();
		return dialog.getSelectedEditor();
	}

}
