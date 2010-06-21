package com.plectix.rulestudio.views.jobs;


import org.apache.commons.cli.ParseException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.progress.UIJob;

import com.plectix.rulestudio.core.Activator;
import com.plectix.rulestudio.views.simulator.RsLiveData;
import com.plectix.rulestudio.views.utils.JSimInterface;
import com.plectix.simulator.controller.SimulationService;
import com.plectix.simulator.controller.SimulatorCallable;
import com.plectix.simulator.controller.SimulatorCallableListener;
import com.plectix.simulator.controller.SimulatorInputData;
import com.plectix.simulator.controller.SimulatorStatusInterface;
import com.plectix.simulator.simulator.SimulatorCommandLine;
import com.plectix.simulator.streaming.LiveData;

public class ControlEmbedded implements Controller{

	private long simulationJobID = -1;
	
	private int graphUpdatePeriod = 1000;
	
	private SimulationService simulationService = null;
	
	private RunSimData myData = null;
	
	private ConsolePrintStream console = null;
	
	protected Job readFinal;
	
	protected int exitCount = 10;
	
	protected IProgressMonitor simMonitor;
	protected Throwable simError = null;			// for sim exception
	protected int doneSoFar = 0;
	
	
	public ControlEmbedded() {
		simulationService = JSimInterface.getSimService();
	}
	
	/**
	 * @param data Data sink for simulation
	 * @param inFile	Model input file
	 * @param pts Number of points to collect
	 * @param dur duration (seconds or events)
	 * @param type true -> time, false -> event
	 * @param output otput file
	 */

	public IStatus startSimulation(final IProgressMonitor pMonitor, RunSimData data, String[] args) {
		simMonitor = pMonitor;
		myData = data;

		try {
			
			UIJob setupChart = new UIJob("Setup Charts") {

				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					try {
						console = new ConsolePrintStream(myData);
					} catch (Exception ex) {
						ex.printStackTrace();
						return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
								"Error configuring chart", ex);
					}
					return Status.OK_STATUS;
				}
				
			};
			setupChart.setSystem(true);
			setupChart.setUser(false);
			setupChart.schedule();
			setupChart.join();					// wait to finish
			IStatus result = setupChart.getResult();
			if (result == null || result.getSeverity() == IStatus.ERROR) {
				return result;					// failed
			}

			SimulatorCommandLine commandLine = null;
			try {
				commandLine = new SimulatorCommandLine(args);
			} catch (ParseException parseException) {
				simMonitor.done();
				console.println("Error configuring simulation.");
				parseException.printStackTrace(console);
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error starting simulation", parseException);
			}
			
			simulationJobID = simulationService.submit(new SimulatorInputData(commandLine.getSimulationArguments(), console), new SimulatorCallableListener() {		

				public void finished(SimulatorCallable simulatorCallable) {
					doneSoFar = 100;
					simError = simulatorCallable.getSimulatorExitReport().getException();
					simMonitor.done();
					return;
				}
			});

			final Job updateTask = new Job("Update Data") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						checkStatus();
						if (doneSoFar == 100 || simMonitor.isCanceled()) {
							stopSimulation();
						}
						
						if (doneSoFar < 100) {
							this.schedule(graphUpdatePeriod);
						}
					} catch (Exception e) {
						return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error running simulator.", e);
					}
					return Status.OK_STATUS;
				}
				
			};
			updateTask.setSystem(true);
			updateTask.setUser(false);
			updateTask.schedule(graphUpdatePeriod);

		} catch (Exception exception) {
			if (simulationJobID >= 0) {
				try {
					if (simulationService.cancel(simulationJobID, true, true)) {
						simulationJobID = -1;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			doneSoFar = 100;
			simMonitor.done();
			console.println("Error configuring simulation.");
			exception.printStackTrace(console);
			console.close();
			return Status.CANCEL_STATUS;
		}
		do {
			try {
				synchronized (ControlEmbedded.this) {
					ControlEmbedded.this.wait(1000);
				}
			} catch (InterruptedException in) {
				in.printStackTrace();
			}
		} while (doneSoFar < 100 && !simMonitor.isCanceled() && simError == null);
		
		try {
			stopSimulation();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (simMonitor != null)
			simMonitor.done();
		
		if (simError != null) {
			simError.printStackTrace(console);
		}
		
		if (console != null) {
			console.close();
			console = null;
		}

		return Status.OK_STATUS;
	}
	
	/**
	 * This method is called when "Stop" button is called
	 * @throws Exception 
	 */
	private void stopSimulation() throws Exception {
		if (simulationJobID >= 0) {
			if (simulationService.cancel(simulationJobID, true, true)) {
				simulationJobID = -1;
				simMonitor.done();
			}
		}
	}

	private void checkStatus() throws Exception {

		SimulatorStatusInterface simulatorStatus = simulationService.getSimulatorStatus(simulationJobID);
		boolean done = false;
		int progress = -1;
		
		LiveData liveData = simulationService.getSimulatorLiveData(simulationJobID);
		
		if (simulatorStatus == null) {
			done = true;
			progress = 100;

		} else {
			if (simulatorStatus.getProgress() == 1.0) {
				done = true;
				progress = 100;
			} else {
				progress = (int) (100.0 * simulatorStatus.getProgress());
			}
		}
		
		// Feed live data to the charts
		if (liveData != null) {
			myData.addLiveData(new RsLiveData(liveData));
		}
		if (progress > doneSoFar) {
			simMonitor.worked(progress - doneSoFar);
			doneSoFar = progress;
		}
		
		if (done && simError == null) {
			simMonitor.done();
			doneSoFar = 100;					// mark complete
			synchronized (ControlEmbedded.this) {
				ControlEmbedded.this.notify();
			}
		} else {
			if (progress > doneSoFar) {
				simMonitor.worked(progress - doneSoFar);
				doneSoFar = progress;
			}
		}
	}
	
	public Throwable getError() {
		return simError;
	}
}
