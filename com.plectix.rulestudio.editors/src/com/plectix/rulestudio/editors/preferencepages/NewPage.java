package com.plectix.rulestudio.editors.preferencepages;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.plectix.rulestudio.editors.Activator;

public class NewPage extends PreferencePage implements IWorkbenchPreferencePage{


	protected Control createContents(Composite parent) {
		Composite 		container = new Composite(parent, SWT.NULL);	
		GridLayout 		layout = new GridLayout(2, true);
		container.setLayout(layout);
		
		return container;
	}
		
	public void init(IWorkbench arg0) {}
	
	/**
	 * Calculate and store the data before the 
	 */
	public boolean performOk() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		return super.performOk();
	}
	
	
}