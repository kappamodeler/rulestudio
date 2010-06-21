package com.plectix.rulestudio.views.jobs;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;

import com.plectix.rulestudio.core.license.ValidateException;
import com.plectix.rulestudio.views.Activator;
import com.plectix.rulestudio.views.simulator.RsLiveData;

public class ControlExternal implements Controller {

	private RunSimData simData;
	private ConsolePrintStream console = null;
	private ConsolePollster consolePoll = null;
	private ChartPollster chartPoll = null;
	protected Job readFinal;
	protected int exitCount = 10;
	protected int progress = 0;
	protected IProgressMonitor simMon = null;
	protected Throwable simError = null;
	protected boolean procDone = false;
	protected boolean outOfMemory = false;
	
	private OutputStream toJob = null;
	protected static boolean dumpSim = false;
	
	//The simulator needs to run in JVM 1.6
	private static final String MAC_OS_X_JAVA_EXECUTABLE = "/System/Library/Frameworks/JavaVM.framework/Versions/1.6/Commands/java";

	private static final String[] JSIM_JAR_FILES = new String[]{"jsim.jar","andariel-1.2.3.jar","commons-cli-1.1.jar",									
								"commons-logging-1.1.1.jar","jcommon-1.0.16.jar","jfreechart-1.0.13.jar","junit.jar",
								"log4j-1.2.15.jar", "spring-beans-2.0.2.jar","spring-context-2.0.2.jar",
								"spring-core-2.0.2.jar","xpp3_min-1.1.4c.jar","xstream-1.3.jar"};
	
	private static final String JSIM_CLASSPATH;
	static {
		StringBuffer classPath = new StringBuffer(".");
		classPath.append(File.pathSeparatorChar);
		classPath.append("bin");
		for (int index = 0; index < JSIM_JAR_FILES.length; index++){
			classPath.append(File.pathSeparatorChar);
			classPath.append("lib");
			classPath.append(File.separatorChar);
			classPath.append(JSIM_JAR_FILES[index]);
		}
		JSIM_CLASSPATH = classPath.toString(); 
		String dump = System.getProperty("dumpsim");
		if (dump != null)
			dumpSim = true;
	}

	private static String[] execCommand = new String[]{
		MAC_OS_X_JAVA_EXECUTABLE,
		"-Xmx1024M",
		"-cp",
		JSIM_CLASSPATH,
		"com.plectix.rulestudio.views.utils.JSimSimulator",
	};
	
	
	/**
	 * This class handles displaying errors from the process
	 * error stream.
	 *
	 */
	class ConsolePollster extends Job {
		private BufferedReader in;
		
		ConsolePollster(java.io.InputStream is) {
			super("Console Monitor");
			in = new BufferedReader(new InputStreamReader(is));
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				String line = null;
				do {
				line = in.readLine();
				if (line != null) {
					simData.addConsole(line);
					if (line.indexOf("java.lang.OutOfMemoryError") != -1) {
						outOfMemory = true;
					}
					if (dumpSim)
						System.out.println(line);
				}
				} while (line != null);
			}catch (IOException ex){
				simError = ex;
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Collecting simulation messages.", ex);
			}
			return Status.OK_STATUS;
		}
		
		public void close() {
			try {
				in.close();
			} catch (IOException e) {
				if (simError == null)
					simError = e;
			}
			return;
		}
	}

	/**
	 * This class handles displaying errors from the process
	 * error stream.
	 *
	 */
	private class ChartPollster extends Job {
		private ObjectInputStream in;
		
		ChartPollster(InputStream is) throws Exception {
			super("Read Simulation Results");
			in = new ObjectInputStream(is);
		}

		public IStatus run(IProgressMonitor monitor){
			try {
				for (;;) {
					Object obj = in.readObject();
					if (obj instanceof RsLiveData) {
						RsLiveData ld = (RsLiveData)obj;
						simData.addLiveData(ld);
						int soFar = ld.getProgress();
						if (soFar > progress) {
							simMon.worked(soFar - progress);
							progress = soFar;
						}
					} else if (obj == null){
						System.out.println("Null return from sim");
					} else {
						System.out.println("Class: " + obj.getClass().getCanonicalName() + " returned from sim");
					}
				}
			} catch (EOFException ex) {
				try {
					in.close();
					shutdown();
				} catch (Exception e) {
					simError = e;
				}
			}catch (Exception ex){
				simError = ex;
				if (console != null)
					ex.printStackTrace(console);
			}
			return Status.OK_STATUS;
		}
		
		public void close() {
			try {
				in.close();
			} catch (IOException e) {
				if (simError == null)
					simError = e;
			}
			return;
		}

	}

	
	public ControlExternal() {
	}
	
	public void shutdown() throws Exception {
		
		simMon.setCanceled(true);
		simMon.done();

		if (toJob != null) {
			toJob.write(1);
			toJob.flush();
			toJob.close();
			toJob = null;
		}
	}
	
	/**
	 * @param view	Simulation view for this sim
	 * @param inFile	Model input file
	 * @param pts Number of points to collect
	 * @param dur duration (seconds or events)
	 * @param type true -> time, false -> event
	 * @param output otput file
	 */
	public IStatus startSimulation(IProgressMonitor myMonitor, RunSimData data, String argList[]) {
		
		simMon = myMonitor;
		simData = data;
		
		// The simulator needs to run in JVM 1.6
		if (new File(execCommand[0]).exists() == false) {
			String version = System.getProperty("java.version");
			if (version.startsWith("1.6")) {
				String javaLoc = System.getProperty("java.home")
						+ File.separatorChar + "bin" + File.separatorChar
						+ "java";
				if (!new File(javaLoc).exists())
					javaLoc += ".exe";
				execCommand[0] = javaLoc;
			}
			if (new File(execCommand[0]).exists() == false) {
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
						"We have detected that we are not running inside of the 1.6 JVM. " +
						"We have tried to locate an external 1.6 JVM but was unable to.");
			}

		}

		try {
			IPreferenceStore store = com.plectix.rulestudio.core.Activator.getDefault().getPreferenceStore();
			store.setDefault("cellucidate.extMem", "1G");
			String extMem = store.getString("cellucidate.extMem");
			execCommand[1] = "-Xmx" + extMem;

			String[] args = new String[execCommand.length + argList.length];
			System.arraycopy(execCommand, 0, args, 0, execCommand.length);
			System.arraycopy(argList, 0, args, execCommand.length, argList.length);
			
			simMon.worked(0);
			
			try {
				String pluginDirectory = Activator.getDefault().getPluginLocation();

				final Process process = Runtime.getRuntime().exec(args, new String[]{}, new File(pluginDirectory));
				
				int ret = -1;
				if (process != null){
					
					consolePoll = new ConsolePollster(process.getErrorStream());
					consolePoll.setSystem(true);
					consolePoll.schedule();

					chartPoll = new ChartPollster(process.getInputStream());
					chartPoll.setSystem(true);
					chartPoll.schedule();

					final Job waitForCancel = new Job("Wait for cancel") {

						@Override
						protected IStatus run(IProgressMonitor monitor) {
							if (simMon.isCanceled()) {
								OutputStream proc = process.getOutputStream();
								try {
									proc.write((int)'q');
									proc.flush();
								} catch (IOException e) {
									// ignore because proc may be done.
								}
								simMon.done();
							}
							if (!procDone) 
								this.schedule(1000);
							return Status.OK_STATUS;
						}
						
					};
					waitForCancel.setSystem(true);
					waitForCancel.setUser(false);
					waitForCancel.schedule(2000);
							
					simMon.setTaskName("Running the simulation.");
					try {
						ret = process.waitFor();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}else if (ret == -1) {
					return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
							"Failed to start the simulator process.");
				} else if (ret == 1) {
					return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
							"Failed to start the simulator process.");
				} else if (ret == 2 || outOfMemory) {
					return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
							"Job ran out of memory, set external memory in preferences");
				}
			} catch (ValidateException rex) {
				return Status.CANCEL_STATUS;
			} catch (Throwable exception) {
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
						exception.getLocalizedMessage(), exception);
			}

		} catch (Exception exception) {
			console.println("Error configuring simulation.");
			exception.printStackTrace(console);
			simError = exception;
			try {
				shutdown();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} finally {
			procDone = true;
			if (simMon != null)
				simMon.done();
			
			if (consolePoll != null) {
				consolePoll.cancel();
				consolePoll.close();
			}
			if (chartPoll != null) {
				chartPoll.cancel();
				chartPoll.close();
			}
		}
		if (outOfMemory) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
					"Job ran out of memory, set external memory in preferences");
		}
		return Status.OK_STATUS;
	}
	
	/**
	 * This method is called when "Stop" button is called
	 * @throws Exception 
	 */
	public final void stopSimulation() throws Exception {
		shutdown();
	}

	public Throwable getError() {
		return simError;
	}


}
