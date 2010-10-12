package com.plectix.rulestudio.editors.actions;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

import com.plectix.rulestudio.editors.Activator;
import com.plectix.rulestudio.editors.wizards.ExportWizard;

public class ExportToCellucidate implements IEditorActionDelegate {

	protected 	IFileEditorInput		_fileEditorInput = null;
	
	public void setActiveEditor(IAction action, IEditorPart targetEditor) {
		if (targetEditor == null) return;
		if (targetEditor.getEditorInput() instanceof IFileEditorInput){
			_fileEditorInput = (IFileEditorInput)targetEditor.getEditorInput();
		}
	}

	/**
	 * Good template for opening up a wizard.
	 */
	public void run(IAction action) {
		IFile			file = _fileEditorInput.getFile();
		
		try {
			IMarker[] errors = file.findMarkers(Activator.MARKER_TYPE, true, IResource.DEPTH_INFINITE);
			if (errors != null && errors.length > 0) {
				Shell parent = Display.getCurrent().getActiveShell();
				MessageDialog.openInformation(parent, "Upload to RuleBase", "You must fix the errors and warnings in your Kappa file before uploading it to RuleBase.");
				return;
			}
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
		ExportWizard	wizard = new ExportWizard();
		
		wizard.init(null, new StructuredSelection(file));
		
		WizardDialog 			dlg = new WizardDialog(Display.getCurrent().getActiveShell(), (IWizard)wizard);	
		int						iResult = dlg.open();
		if (iResult == WizardDialog.OK){
			if (wizard.getOpenBrowser() == true && wizard.getError() == false){				
				try {
					PlatformUI.getWorkbench().getBrowserSupport().
									getExternalBrowser().openURL(new URL(wizard.getKappaUrl()));
				} catch (PartInitException e) {
					e.printStackTrace();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}else if (wizard.getError() == false){
				MessageDialog.openInformation(Display.getCurrent().getActiveShell(), 
					"RuleStudio - Upload to Cellicudate", "Upload to RuleBase was Successful. You can go online and view it now.");
			}else{
				MessageDialog.openInformation(Display.getCurrent().getActiveShell(), 
						"RuleStudio - Failed to Upload", "Errors occurred during the process of loading your model to RuleBase.");
			}
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

}
