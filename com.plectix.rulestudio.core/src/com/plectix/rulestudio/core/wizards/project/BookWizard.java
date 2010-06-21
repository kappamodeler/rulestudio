package com.plectix.rulestudio.core.wizards.project;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;

import com.plectix.rulestudio.core.perspectives.K3WorkbenchPerspective;
import com.plectix.rulestudio.core.project.BookProject;

public class BookWizard extends BasicNewProjectResourceWizard{
	
	public void addPages(){
		setWindowTitle("Create Folder");	
		super.addPages();
		
		WizardPage page = (WizardPage)getPages()[0];
		page.setTitle("Folder Name");
		page.setDescription("Enter in your folder information.");
		
	}
	
	@Override
	public IWizardPage getNextPage(IWizardPage page) {	
		if (page instanceof WizardNewProjectCreationPage){
			return null;
		}
		return super.getNextPage(page);
	}

	@Override
	public void createPageControls(Composite pageContainer) {
		super.createPageControls(pageContainer);

		//Change the project name label.
		WizardPage wizardPage = (WizardPage)getPages()[0];
		
		Composite control = (Composite)wizardPage.getControl();
		Control[] controls = control.getChildren();
		
		control = (Composite)controls[0];
		
		controls = control.getChildren();
		for (int index = 0; index < controls.length; index++){
			if (controls[index] instanceof Label){
				((Label)controls[index]).setText("Folder name:");
			}
		}
	}

	public boolean performFinish() {

		//Need to do this before the finish or else it will
		//throw and exception
		super.performFinish();		

		IProject 	project = getNewProject();
		
		//Add the Modernization Nature
		try {
			IProjectDescription description = project.getDescription();
			String[]			natureIds = description.getNatureIds();
			String[]		    newNatureIds = new String[natureIds.length + 1];
			for (int index = 0; index < natureIds.length; index++){
				newNatureIds[index] = natureIds[index];
			}
			newNatureIds[newNatureIds.length - 1] = BookProject.NATURE_ID;
			description.setNatureIds(newNatureIds);

			//Add the Kappa Validation builder.
			ICommand command = description.newCommand();
			command.setBuilderName("com.plectix.rulestudio.editors.KappaBuilder");
			description.setBuildSpec(new ICommand[]{command});
			
			project.setDescription(description, new NullProgressMonitor());			
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
				
		try {
			project.refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		K3WorkbenchPerspective.openPerspective();
		
		return true;
	}

}
