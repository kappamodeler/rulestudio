package com.plectix.rulestudio.views.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

public interface JsimJob {

	/**
	 * Start the jsim job.
	 * 
	 * @param monitor  The monitor used to track progress and cancel the job.
	 * @param args The jsim arguments for the command.
	 * @return  The return status OK or Cancel.  If cancel then use getError to see the error.
	 */
	public IStatus startJob(IProgressMonitor monitor, String[] args);
	
	/**
	 * Get the print stream for the job, or stdout for exteranl jobs.
	 * 
	 * @return The console.
	 */
	public String getConsole();
	
	/**
	 * Get the error associated with the task.  We do not return errors because Eclipse brings up
	 * the error dialog.
	 * 
	 * @return The error or null if none.
	 */
	public Throwable getError();
	
	/**
	 * Whether this job runs externally.
	 * 
	 * @return true if external, false if not.
	 */
	public boolean isExternal();

}
