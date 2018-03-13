package org.eclipse.e4.ui.internal.workbench.swt;

import org.eclipse.core.runtime.jobs.Job;

/**
 * @since 3.3
 * 
 */
public class InternalSaveable {

	private Job backgroundSaveJob;

	/* package */Job getBackgroundSaveJob() {
		return backgroundSaveJob;
	}

	/* package */void setBackgroundSaveJob(Job backgroundSaveJob) {
		this.backgroundSaveJob = backgroundSaveJob;
	}

	/* package */boolean isSavingInBackground() {
		Job saveJob = backgroundSaveJob;
		if (saveJob == null) {
			return false;
		}
		return (backgroundSaveJob.getState() & (Job.WAITING | Job.RUNNING)) != 0;
	}

}
