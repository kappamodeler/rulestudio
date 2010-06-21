package com.plectix.rulestudio.views.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

public interface Controller {

	/**
	 * @param view	Simulation view for this sim
	 * @param inFile	Model input file
	 * @param pts Number of points to collect
	 * @param dur duration (seconds or events)
	 * @param type time or event
	 * @param output otput file
	 */
	public IStatus startSimulation(IProgressMonitor monitor, RunSimData data, String[] args);
	
	public Throwable getError();
}
