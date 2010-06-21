package com.plectix.rulestudio.editors.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IEditorPart;

public class DisplayEditorHighlightsJob extends Job {

	public DisplayEditorHighlightsJob(IEditorPart targetEditor) {
		super("Display Editor Highlights");
	}

	protected IStatus run(IProgressMonitor monitor) {		
		monitor.beginTask("", 3);
		return Status.OK_STATUS;
	}
	
}
