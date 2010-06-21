package com.plectix.rulestudio.views.utils;

import org.apache.commons.cli.ParseException;
import org.apache.log4j.PropertyConfigurator;

import com.plectix.simulator.controller.SimulationService;
import com.plectix.simulator.controller.SimulatorCallable;
import com.plectix.simulator.controller.SimulatorCallableListener;
import com.plectix.simulator.controller.SimulatorInputData;
import com.plectix.simulator.simulator.DefaultSimulatorFactory;
import com.plectix.simulator.simulator.SimulatorCommandLine;
import com.plectix.simulator.simulator.ThreadLocalData;
import com.plectix.simulator.util.io.PlxLogger;

public class JSimInterface {
	
	private static final String LOG4J_PROPERTIES_FILENAME = "config/log4j.properties";
	private static boolean loggingInitialized = false;
	private PlxLogger LOGGER = ThreadLocalData.getLogger(JSimInterface.class);
	private long simulationJobID = -1;	
	private static SimulationService simService = null;
	
	public JSimInterface() {
		LOGGER = ThreadLocalData.getLogger(JSimInterface.class);
		getSimService();
	}
	
	public static SimulationService getSimService() {
		if (simService == null) {
			synchronized(JSimInterface.class) {
				if (simService == null)
					simService = new SimulationService(new DefaultSimulatorFactory());
			}
		}
		return simService;
	}
	
	/**
	 * @param view	Simulation view for this sim
	 * @param inFile	Model input file
	 * @param pts Number of points to collect
	 * @param dur duration (seconds or events)
	 * @param type true -> time, false -> event
	 * @param output otput file
	 */
	public final boolean startSimulation(String[] args) {

		try {
			SimulatorCommandLine commandLine = null;
			try {
				commandLine = new SimulatorCommandLine(args);
			} catch (ParseException pex) {
				pex.printStackTrace(System.err);
				return false;
			}
			simService = new SimulationService(new DefaultSimulatorFactory());
			simulationJobID = simService.submit(new SimulatorInputData(commandLine.getSimulationArguments(), System.out), new SimulatorCallableListener() {		

				public void finished(SimulatorCallable simulatorCallable) {
					Exception exception = simulatorCallable.getSimulatorExitReport().getException();
					if (exception == null) {
						System.exit(0);
					} else {
						LOGGER.error("Job exited with an Exception", exception);
						exception.printStackTrace(System.err);
						System.exit(1);
					}
				}
			});

		} catch (Exception exception) {
			if (simulationJobID >= 0) {
				try {
					if (simService.cancel(simulationJobID, true, true)) {
						simulationJobID = -1;
					}
				} catch (Exception e) {
					e.printStackTrace(System.err);
				}
			}
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
		if (simulationJobID >= 0) {
			if (simService.cancel(simulationJobID, true, true)) {
				simulationJobID = -1;
			}
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
			JSimInterface cj = new JSimInterface();
			if (cj.startSimulation(args)) {
				System.in.read(); // wait for quit
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
