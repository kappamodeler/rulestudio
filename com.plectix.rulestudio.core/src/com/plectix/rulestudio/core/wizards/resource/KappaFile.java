package com.plectix.rulestudio.core.wizards.resource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.ide.DialogUtil;
import org.eclipse.ui.internal.wizards.newresource.ResourceMessages;
import org.eclipse.ui.wizards.newresource.BasicNewFileResourceWizard;

public class KappaFile extends BasicNewFileResourceWizard{

	private final static String DEFAULT_KAPPA_FILE_CONTENT = "#\r\n" + 
				"# Welcome to the RuleStudio Editor \r\n" + 
				"# \r\n" + 
				"# This editor will improve the speed of entering valid kappa by using auto completion and basic syntax checking as you write each line.\r\n" +  
				"# Auto completion of agents and rules will drop down when you are creating initial conditions, observables or rules that reuse agents in prior rules.\r\n" +  
				"# \r\n" + 
				"# Please send comments and feedback on our editor to support@cellucidate.com\r\n" + 
				"#\r\n\r\n\r\n\r\n";
	
	private InputStream _inputStream = null;
	private String _fileName = "";
	private IFile _file = null;
	
	public void addPages(){
		super.addPages();
		
		WizardNewFileCreationPage page = (WizardNewFileCreationPage)this.getPages()[0];
		page.setFileName(_fileName);
		page.setFileExtension("ka");		
	}
	
	public void setInputStream(InputStream inputStream){
		_inputStream = inputStream;
	}
	
	public void setFileName(String fileName){
		_fileName = fileName;
	}
	
	public IFile getFile(){
		return _file;
	}
	
	public boolean performFinish() {
		
		//Need to get the file and add the Kappa File default header.
		WizardNewFileCreationPage page = (WizardNewFileCreationPage)this.getPages()[0];
        _file = page.createNewFile();
        
		if (_file.exists() == true){
			try {
				//Set the content of the file.
				InputStream stream = null;
				if (_inputStream == null){
					stream = new ByteArrayInputStream(DEFAULT_KAPPA_FILE_CONTENT.getBytes());
				}else{
					 stream = _inputStream;
				}
				_file.setContents(stream, true, false, new NullProgressMonitor());
				
				//If this is just creating a new file and not importing, then open.
				//import will handle open it's self.
				if (_inputStream == null){
					selectAndReveal(_file);
					openEditor(_file);
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
				
		return true;
	}
	
	public void openEditor(IFile file){
        // Open editor on new file.
        IWorkbenchWindow dw = getWorkbench().getActiveWorkbenchWindow();
        try {
            if (dw != null) {
                IWorkbenchPage page = dw.getActivePage();
                if (page != null) {
                    IDE.openEditor(page, file, true);
                }
            }
        } catch (PartInitException e) {
            DialogUtil.openError(dw.getShell(), ResourceMessages.FileResource_errorMessage, 
                    e.getMessage(), e);
        }
	}

}
