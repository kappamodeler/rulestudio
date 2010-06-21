package com.plectix.rulestudio.core.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

import com.plectix.rulestudio.core.wizards.project.BookWizard;

public class CreateBookWizard extends Action {

	/**
	 * This is called from the welcome perspective and will open the 
	 * kappa book wizard.
	 */
	public void run(){
		BookWizard		wizard = new BookWizard();
		wizard.init(PlatformUI.getWorkbench(), new StructuredSelection());
		
		WizardDialog 	dlg = new WizardDialog(Display.getCurrent().getActiveShell(), (IWizard)wizard);	
		dlg.open();
	}
	
}
