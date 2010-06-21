package com.plectix.rulestudio.views.utils;

import java.io.ObjectOutputStream;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.cli.ParseException;
import org.apache.log4j.PropertyConfigurator;

import com.plectix.rulestudio.views.simulator.RsLiveData;
import com.plectix.simulator.controller.SimulationService;
import com.plectix.simulator.controller.SimulatorCallable;
import com.plectix.simulator.controller.SimulatorCallableListener;
import com.plectix.simulator.controller.SimulatorInputData;
import com.plectix.simulator.controller.SimulatorStatusInterface;
import com.plectix.simulator.simulator.DefaultSimulatorFactory;
import com.plectix.simulator.simulator.SimulatorCommandLine;
import com.plectix.simulator.simulator.ThreadLocalData;
import com.plectix.simulator.streaming.LiveData;
import com.plectix.simulator.util.io.PlxLogger;

public class JSimSimulator {
	
	private static final String LOG4J_PROPERTIES_FILENAME = "config/log4j.properties";

	private static boolean loggingInitialized = false;
	
	private static final PlxLogger LOGGER = ThreadLocalData.getLogger(JSimSimulator.class);

	private Timer timer = null;
	
	private long simulationJobID = -1;
	
	private int graphUpdatePeriod = 1000;
	
	private SimulationService service = null;
	
	private ObjectOutputStream dataOut;
	
	private boolean isDone = false;
	
	private int doneSoFar = 0;
	
	public JSimSimulator() throws Exception {
		dataOut = new ObjectOutputStream(System.out);
	}
	
	public final boolean startSimulation(String[] args) {

		try {
			SimulatorCommandLine commandLine = null;
			try {
				commandLine = new SimulatorCommandLine(args);
			} catch (ParseException pex) {
				pex.printStackTrace(System.err);
				return false;
			}
			service = new SimulationService(new DefaultSimulatorFactory());
			simulationJobID = service.submit(new SimulatorInputData(commandLine.getSimulationArguments(), System.err), new SimulatorCallableListener() {		

				public void finished(SimulatorCallable simulatorCallable) {
					System.err.println("Finished");
					simulationJobID = -1;
					Exception exception = simulatorCallable.getSimulatorExitReport().getException();
					if (exception == null) {
						System.exit(0);
					} else {
						LOGGER.error("Simulator exited with an Exception", exception);
						System.exit(1);
					}
				}
			});

			timer = new Timer();
			timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					try {
						checkStatus();
					} catch (Exception e) {
						e.printStackTrace(System.err);
					}
				}

			}, 0, graphUpdatePeriod);

		} catch (Exception exception) {
			if (simulationJobID >= 0) {
				try {
					if (service.cancel(simulationJobID, true, true)) {
						simulationJobID = -1;
					}
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}
			}
			System.err.println("Error configuring simulation.");
			exception.printStackTrace(System.err);
			return false;
		}
		return true;
	}
	
	/**
	 * This method is called when "Stop" button is called
	 * @throws Exception 
	 */
	public final void stopSimulation() throws Exception {
		System.err.println("Stopping simulation");
		if (simulationJobID >= 0) {
			if (service.cancel(simulationJobID, true, true)) {
				simulationJobID = -1;
				timer.cancel();
				timer.purge();
			}
		}
	}

	private void checkStatus() throws Exception {
		SimulatorStatusInterface simulatorStatus = service.getSimulatorStatus(simulationJobID);
		LiveData liveData = service.getSimulatorLiveData(simulationJobID);
		
		int progress = 0;
		if (simulatorStatus == null) {
			timer.cancel();
			timer.purge();

		} else {
			if (isDone || simulatorStatus.getProgress() == 1.0) {
				stopSimulation();
			} else {
				progress = (int) (100.0 * simulatorStatus.getProgress());
				if (progress == 100) {
					stopSimulation();
				}
			}
			
		}
		
		// Feed live data to the charts
		if (liveData != null) {
			RsLiveData ld = new RsLiveData(liveData);
			ld.setProgress(progress);
			doneSoFar = progress;
			dataOut.writeObject(ld);
		} else if (progress > doneSoFar) {
			RsLiveData ld = new RsLiveData();
			ld.setProgress(progress);
			doneSoFar = progress;
			dataOut.writeObject(ld);
		}
	} 
	
	public static void initializeLogging() {
		if (loggingInitialized) {
			return;
		}
		
		// Initialize log4j
		PropertyConfigurator.configure(LOG4J_PROPERTIES_FILENAME);
		loggingInitialized = true;
	}

	
	public static void main(String args[]) {
		initializeLogging();
		try {
			System.err.println("Start sim");
			JSimSimulator cj = new JSimSimulator();
			if (cj.startSimulation(args)) {

				System.in.read(); // wait for quit
				cj.isDone = true;
				System.err.println("Simulation aborted");
				cj.stopSimulation();
			} else {
				System.exit(1);
			}
		} catch (OutOfMemoryError om) {
			om.printStackTrace(System.err);
			System.exit(2);
		} catch (Throwable e) {
			e.printStackTrace(System.err);
			System.exit(1);
		}
		// do the real exit based on the sim's finished process.
	}
}
