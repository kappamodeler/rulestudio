package com.plectix.rulestudio.views.actions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

import com.plectix.rulestudio.core.usagedata.Action;
import com.plectix.rulestudio.core.usagedata.UsageDataCollector;
import com.plectix.rulestudio.editors.kappa.KappaEditor;
import com.plectix.rulestudio.views.Activator;
import com.plectix.rulestudio.views.jobs.Controller;
import com.plectix.rulestudio.views.jobs.JsimJobFactory;
import com.plectix.rulestudio.views.simulator.RsLiveData;
import com.plectix.rulestudio.views.simulator.XML2LiveData;
import com.plectix.rulestudio.views.story.StoryData;
import com.plectix.rulestudio.views.story.StoryRunDialog;

public class RunStory implements IEditorActionDelegate {

	protected 	IFileEditorInput		_fileEditorInput = null;
	static SimpleDateFormat simDate = new SimpleDateFormat("yyyyMMddHHmm");
	protected IEditorPart _target;
	
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		_target = targetEditor;
		if (targetEditor == null) return;
		if (targetEditor.getEditorInput() instanceof IFileEditorInput){
			_fileEditorInput = (IFileEditorInput)targetEditor.getEditorInput();
		}
	}

	/**
	 * Good template for opening up a wizard.
	 */
	public void run(IAction action) {
		if (_target == null || _fileEditorInput == null)
			return;
		final IFile file = _fileEditorInput.getFile();
		Display display = Display.getCurrent();
		if (display == null)
			return;
		Shell shell = display.getActiveShell();
		if (shell == null)
			return;

		final StoryRunDialog srd = new StoryRunDialog(shell);
		int ret = srd.open();
		if (ret == IDialogConstants.OK_ID) { // start pressed

			String fileContents = null;
			if (_target instanceof KappaEditor) {
				fileContents = ((KappaEditor) _target).getContent();
			}
			final String fileLocation = file.getRawLocation().toOSString();
			String outTemp;
			int index = fileLocation.lastIndexOf('.');
			if (index == -1)
				outTemp = fileLocation;
			else
				outTemp = fileLocation.substring(0, index);
			String dateStr = simDate.format(new Date());
			String output = outTemp + dateStr + ".story";
			File outputFile = new File(output);
			for (char ch = 'a'; outputFile.exists(); ++ch) {
				output = outTemp + dateStr + ch + ".story";
				outputFile = new File(output);
			}

			final String outName = output;
			final File outFile = outputFile;

			final StoryData data = new StoryData();
			try {
				final Controller ct = JsimJobFactory.INSTANCE.getController();

				IWorkbenchPage page = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage();
				IEditorDescriptor desc = PlatformUI.getWorkbench()
						.getEditorRegistry().getDefaultEditor("foo.story");
				page.openEditor(data, desc.getId());

				final String kappa = fileContents;
				final Controller con = ct;
				final String[] args = makeCommandLineArguments(fileLocation, outName, srd);
				Job doSim = new Job("Run Stories") {

					@Override
					protected IStatus run(IProgressMonitor monitor) {
						try {
							monitor.beginTask("Build Stories", 100);
							IStatus ret = con.startSimulation(monitor, data,
									args);
							IContainer parent = file.getParent();
							addKappaFile(outName, kappa);
							try {
								parent.refreshLocal(IResource.DEPTH_INFINITE,
										null);
							} catch (CoreException e) {
								e.printStackTrace();
							}
							
							UIJob updateChart = new UIJob("Update Chart") {

								@Override
								public IStatus runInUIThread(
										IProgressMonitor monitor) {
									try {
										if (outFile.exists()) {
											RsLiveData liveData = XML2LiveData.getLiveData(outName);
											data.addLiveData(liveData);
										}
									} catch (Throwable th) {
										th.printStackTrace();
										return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
												"Error updating Simulator Charts", th);
									}
									return Status.OK_STATUS;
								}
								
							};
							updateChart.setSystem(true);
							updateChart.setUser(false);
							updateChart.schedule();
							return ret;
						} catch (Exception ex) {
							ex.printStackTrace();
							return new Status(IStatus.ERROR,
									Activator.PLUGIN_ID,
									"Error starting simulation", ex);

						}
					}

				};
				UsageDataCollector.getInstance().addTimeSpanAction(
						Action.SIMULATOR_JSIM_RUN_SIMULATION, true);
				doSim.setUser(true);
				doSim.schedule();
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}

		return;
	}

	private String[] makeCommandLineArguments(String file, String outName, StoryRunDialog srd) {
		ArrayList<String> argList = new ArrayList<String>();
		argList.add("--storify");
		argList.add(file);
		argList.add(srd.getType());
		argList.add(srd.getLength());
		argList.add("--xml-session-name");
		argList.add(outName);
		argList.add("--iteration");
		argList.add(srd.getIterations());
		String rs = srd.getRescale();
		if (rs.length() > 0) {
			argList.add("--rescale");
			argList.add(rs);
		}
		String seed = srd.getSeed();
		if (seed.length() > 0) {
			argList.add("--seed");
			argList.add(seed);
		}
		for (String comp: srd.getCompression()) {
			argList.add(comp);
		}
		
		return argList.toArray(new String[argList.size()]);
	}

	private void addKappaFile(String output, String contents) {
		File out = new File (output);
		if (out.exists()) {
			try {
				File dir = out.getParentFile();
				File temp = File.createTempFile("kappa", "storyout", dir);
				BufferedReader bfr = new BufferedReader(new FileReader(out));
				PrintWriter bw = new PrintWriter(new FileWriter(temp));
				String line = null;
				while ((line = bfr.readLine()) != null) {
					if (line.contains("</SimplxSession>")) {
						bw.print("\t\t<MODEL><![CDATA[");
						bw.print(contents);
						bw.println("]]>");
						bw.println("\t\t</MODEL>");
					}
					bw.println(line);
				}
				bfr.close();
				bw.close();
				if (out.delete()) {
					temp.renameTo(out);
				}				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}
	
}
