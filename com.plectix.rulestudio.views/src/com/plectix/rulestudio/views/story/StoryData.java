
/**
 * 
 */
package com.plectix.rulestudio.views.story;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.progress.UIJob;

import com.plectix.rulestudio.views.simulator.RsLiveData;
import com.plectix.rulestudio.views.jobs.RunSimData;

/**
 * @author bill
 *
 */
public class StoryData implements IEditorInput, RunSimData {
	private RsLiveData lastData = null;
	private String console = null;
	private StoryEditor editor = null;

	public StoryData() {
	}
	
	public void addConsole(final String line) {
		if (editor != null) {
			UIJob updateCharts = new UIJob("Update Charts") {

				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					editor.writeConsole(line);
					return Status.OK_STATUS;
				}
				
			};
			updateCharts.setSystem(true);
			updateCharts.setUser(false);
			updateCharts.schedule();
		} else if (console == null) {
			console = line;
		} else {
			console += line + "\n";
		}
	}
	
	public String getConsole() {
		String ret = console;
		console = null;
		return ret;
	}
	
	public void addLiveData(final RsLiveData data) {
		//throw new IllegalStateException("Live data input invalid for Stories");
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#exists()
	 */
	public boolean exists() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getName()
	 */
	public String getName() {
		return "Story";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getPersistable()
	 * not persistable
	 */
	public IPersistableElement getPersistable() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getToolTipText()
	 */
	public String getToolTipText() {
		return "Stories Run";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		if (adapter.equals(StoryData.class))
			return this;
		else if (adapter.equals(RsLiveData.class))
			return lastData;
		else
			return null;
	}
	
	@Override
	public boolean equals(Object other) {
		return this == other;
	}

	public void setEditor(StoryEditor storyEditor) {
		editor = storyEditor;
	}

}
