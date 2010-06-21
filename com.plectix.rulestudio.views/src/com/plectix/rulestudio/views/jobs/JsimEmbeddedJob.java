package com.plectix.rulestudio.views.jobs;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.apache.commons.cli.ParseException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.plectix.rulestudio.core.Activator;
import com.plectix.rulestudio.views.utils.JSimInterface;
import com.plectix.simulator.controller.SimulationService;
import com.plectix.simulator.controller.SimulatorCallable;
import com.plectix.simulator.controller.SimulatorCallableListener;
import com.plectix.simulator.controller.SimulatorInputData;
import com.plectix.simulator.controller.SimulatorStatusInterface;
import com.plectix.simulator.simulator.SimulatorCommandLine;

public class JsimEmbeddedJob implements JsimJob{

	private long simulationJobID = -1;
	private SimulationService simulationService = null;
	private PrintStream console = null;	
	private ByteArrayOutputStream outStream = null;
	protected Job readFinal;
	
	protected IProgressMonitor simMonitor;
	protected Throwable simError = null;			// for sim exception
	protected int doneSoFar = 0;
	private int UPDATE_TIME = 1000;
	
	
	public JsimEmbeddedJob() {
		simulationService = JSimInterface.getSimService();
	}
	
	public void setConsole(PrintStream console) {
		this.console = console;
	}
	
	public IStatus startJob(final IProgressMonitor pMonitor, String[] args) {
		simMonitor = pMonitor;

		simMonitor.beginTask("Running the job.", 100);

		if (console == null) {
			outStream = new ByteArrayOutputStream();
			console = new PrintStream(outStream);
		}

		SimulatorCommandLine commandLine = null;
		try {
			commandLine = new SimulatorCommandLine(args);
		} catch (ParseException parseException) {
			simMonitor.done();
			console.println("Error configuring job.");
			parseException.printStackTrace(console);
			simError = parseException;
			return Status.CANCEL_STATUS;
		}

		try {

			simulationJobID = simulationService.submit(new SimulatorInputData(
					commandLine.getSimulationArguments(), console),
					new SimulatorCallableListener() {

						public void finished(SimulatorCallable simulatorCallable) {
							doneSoFar = 100;
							simError = simulatorCallable.getSimulatorExitReport().getException();
							simMonitor.done();
							return;
						}
					});

			final Job updateTask = new Job("Update Task") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {
					try {
						SimulatorStatusInterface simulatorStatus = simulationService
								.getSimulatorStatus(simulationJobID);
						if (simulatorStatus != null) {
							int progress = (int) (100.0 * simulatorStatus.getProgress());
							if (progress > doneSoFar) {
								simMonitor.worked(progress - doneSoFar);
								doneSoFar = progress;
							}
						}

						if (simMonitor.isCanceled()) {
							stopSimulation();
						} else if (doneSoFar < 100) {
							this.schedule(UPDATE_TIME);
						}
					} catch (Exception e) {
						return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
								"Error running job.", e);
					}
					return Status.OK_STATUS;
				}

			};
			updateTask.setSystem(true);
			updateTask.setUser(false);
			updateTask.schedule(UPDATE_TIME);

		} catch (Exception exception) {
			if (simulationJobID >= 0) {
				try {
					simulationService.cancel(simulationJobID, true, true);
				} catch (Exception e) {
					e.printStackTrace();
				}
				simulationJobID = -1;
			}
			simMonitor.done();
			console.println("Error configuring job.");
			exception.printStackTrace(console);
			console.close();
			simError = exception;
			return Status.CANCEL_STATUS;
		}
		do {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException in) {
				in.printStackTrace();
			}
		} while (doneSoFar < 100 && !simMonitor.isCanceled() && simError == null);
		if (simMonitor != null)
			simMonitor.done();

		if (simError != null) {
			return Status.CANCEL_STATUS;
		}

		return Status.OK_STATUS;
	}
	
	/**
	 * This method is called when "Stop" button is called
	 * @throws Exception 
	 */
	private void stopSimulation() throws Exception {
		if (simulationJobID >= 0) {
			simulationService.cancel(simulationJobID, true, true);	
		}
		simulationJobID = -1;
		simMonitor.done();

	}


	public String getConsole() {
		if (outStream != null) {
			return outStream.toString();
		}
		return "";
	}
	
	public Throwable getError() {
		return simError;
	}
	
	public boolean isExternal() {
		return false;
	}
}
