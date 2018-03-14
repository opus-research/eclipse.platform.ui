package org.eclipse.e4.ui.progress.e4new;

import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.progress.internal.FinishedJobs;

public class ClearAllHandler {

	@Execute
	public static void clearAll() {
		FinishedJobs.getInstance().clearAll();
	}


}
