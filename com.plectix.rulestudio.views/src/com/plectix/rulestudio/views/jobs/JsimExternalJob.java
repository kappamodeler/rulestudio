package com.plectix.rulestudio.views.jobs;

import java.io.ByteArrayOutputStream;
import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.plectix.rulestudio.views.Activator;

/**
 * Class is used on a Mac os that is not using 1.6
 * @author bbuffone
 *
 */
public class JsimExternalJob implements JsimJob {

	private final static int UPDATE_TIME = 1000;			// 1 second
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
	}

	private static String[] execCommand = new String[]{
		MAC_OS_X_JAVA_EXECUTABLE,
		"-cp",
		JSIM_CLASSPATH,
		"com.plectix.rulestudio.views.utils.JSimInterface",
	};
	
	private OutputPollster pollster = null;
	private OutputPollster inPoll = null;
	
	/**
	 * This class handles displaying errors from the process
	 * error stream.
	 * @author bbuffone
	 *
	 */
	class OutputPollster extends java.lang.Thread{
		private java.io.InputStream _is;
		private ByteArrayOutputStream _output;
		
		OutputPollster(java.io.InputStream is) {
			_is = is;
			_output = new ByteArrayOutputStream();
		}

		public String getOutput(){
			return _output.toString();
		}
		public String getErrorSummary(){
			String result = _output.toString();
			int offset = result.indexOf("\n", 100);
			if (offset > 0) {
				return result.substring(0, offset);
			}
			return result;
		}
		public void run(){
			byte buf[] = new byte[128];
			int len = 0;
			try {
				while ((len = _is.read(buf)) != -1){
					_output.write(buf, 0, len);
				}
			}catch (java.io.IOException ex){
				ex.printStackTrace();
			}
		}
	}//end OutputPollster definition 
	
	public JsimExternalJob() {
	}
	
	
	/**
	 * This methods will use the embedded simulator library to create the
	 * compression map. 
	 * 
	 * @param monitor
	 * @param fileLocation
	 * @param outputLocation
	 * @return
	 */
	public IStatus startJob(final IProgressMonitor monitor, String[] argList) {
		String pluginDirectory = Activator.getDefault().getPluginLocation();
		
		//The simulator needs to run in JVM 1.6
		if (new File(execCommand[0]).exists() == false){
			String version = System.getProperty("java.version");
			if (version.startsWith("1.6")) {
				String javaLoc = System.getProperty("java.home") + File.separatorChar + "bin" + File.separatorChar + "java";
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
		
		String[] args = new String[execCommand.length + argList.length];
		System.arraycopy(execCommand, 0, args, 0, execCommand.length);
		System.arraycopy(argList, 0, args, execCommand.length, argList.length);
		
		monitor.setTaskName("Running external job.");
		
		try {
			final Process process = Runtime.getRuntime().exec(args, new String[]{}, new File(pluginDirectory));
			
			if (process != null){
				
				pollster = new OutputPollster(process.getErrorStream());
				pollster.start();

				inPoll = new OutputPollster(process.getInputStream());
				inPoll.start();
				
				final Job waitForCancel = new Job("Wait for cancel") {

					@Override
					protected IStatus run(IProgressMonitor myMonitor) {
						if (monitor == null || monitor.isCanceled()) {
							/* OutputStream proc = process.getOutputStream();
							try {
								proc.write((int)'q');
								proc.flush();
							} catch (IOException e) {
								// ignore because proc may be done.
							}
							*/
							System.out.println("Destroy the process");
							process.destroy();
							monitor.done();
						} else {
							this.schedule(UPDATE_TIME);
						}
						return Status.OK_STATUS;
					}
					
				};
				waitForCancel.setSystem(true);
				waitForCancel.setUser(false);
				waitForCancel.schedule(UPDATE_TIME);
						


				monitor.setTaskName("Waiting for the job to finish...");
				int exitValue;
				try {
					exitValue = process.waitFor();
				} catch (InterruptedException e) {
					exitValue = process.exitValue();
					e.printStackTrace();
				}
					
				if (exitValue == 0){
					return Status.OK_STATUS;
				} else {
					return Status.CANCEL_STATUS;
				}
			}else{
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
						"Failed to start the validation process.");
			}
		} catch (Throwable exception) {
			return new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
					exception.getLocalizedMessage(), exception);
		}
	}

	public String getConsole() {
		if (inPoll != null)
			return inPoll.getOutput();
		else
			return "";
	}
	
	public Throwable getError() {
		String msg = pollster.getErrorSummary();
		return new Exception(msg);
	}
	
	public boolean isExternal() {
		return true;
	}
	
}
