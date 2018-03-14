package org.eclipse.e4.ui.progress.e4new;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.swt.widgets.Shell;

public class ShowPreferencesHandler {

	@Inject
	Shell shell;

	@Execute
	public void showPreferences() {
		//new JobsViewPreferenceDialog(shell).open();
	}

}
