package org.eclipse.e4.ui.progress.e4new;


import org.eclipse.e4.core.contexts.Active;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.progress.internal.JobsViewPreferenceDialog;
import org.eclipse.swt.widgets.Shell;

public class ShowPreferencesHandler {

	@Execute
	public void JobsViewPreferenceDialog(@Active Shell shell, PreferenceStore preferences) {
		new JobsViewPreferenceDialog(shell, preferences).open();
	}

}
