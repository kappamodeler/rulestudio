package com.plectix.rulestudio.core.license;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.osgi.framework.Version;

import com.plectix.rulestudio.core.Activator;

public class GetLicenseWizard extends Wizard implements IWizard {

	private GetLicense _licensePage = null;
	private String 	_kappaUrl = "";
	private boolean _bError = false;
	private boolean _renewal;
	
	public GetLicenseWizard(boolean renewal){
		setNeedsProgressMonitor(true);
		this._renewal = renewal;
	}

	public String getKappaUrl(){
		return _kappaUrl;
	}
	
	public boolean getError(){
		return _bError;
	}
	
	@Override
	public boolean canFinish() {
		return _licensePage.isPageComplete();
	}
	
	public void addPages(){
  		_licensePage = new GetLicense();
		_licensePage.setTitle("Get License");
		_licensePage.setDescription("Use your User ID and API key to download your license.");
		
		setWindowTitle("Get License");
		
		addPage(_licensePage);
		super.addPages();
	}

	public boolean performFinish() {
		
		final String apiKey = _licensePage.getAPIKey();
		final String email = _licensePage.getEmail();
		final Version version = Activator.getDefault().getVersion();
		final String url = Activator.getDefault().getUrl();
		
		//Set the information as preferences
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setValue("cellucidate_email", email);
		store.setValue("cellucidate_api_key", apiKey);

		try {
			getContainer().run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) {

					monitor.beginTask("Getting License", 4);

					final LoadLicense loader = new LoadLicense();
					final String content = loader.waitForJobToFinish(monitor, url, email, apiKey, version);
					monitor.setTaskName("Checking the license.");
					monitor.worked(1);
					if (content != null)
						ValidateLicense.setLicense(content);
					else  if (loader.getError()){
			  			Display.getDefault().syncExec(new Runnable(){
			  				public void run(){
			  					int responseCode = loader.getResponseCode();
			  					if (responseCode == 401) {
			  						MessageDialog.openError(Display.getCurrent().getActiveShell(), 
			  	  							"RuleStudio - Invalid API Key", 
			  	  		  					"Invalid user name or API key.  Please check your Cellucidate account" +
			  	  		  					"\nto make sure you have the right user name and API key.");
			  					} else if (responseCode != 200){
			  						MessageDialog.openError(Display.getCurrent().getActiveShell(), 
			  							"RuleStudio - Server error Getting license", 
			  		  					"An HTTP error (" + responseCode + ") occured while trying to get your license." +
			  		  					"\nPlease contact support@cellucidate.com for help.");
			  					} else if (_renewal) {
			  						MessageDialog.openError(Display.getCurrent().getActiveShell(), 
			  	  							"RuleStudio - Renewal Error", 
			  	  		  					"Unable to renew your license for this client.  Please contact support@cellucidate.com for help.");
				
			  					} else {
			  						MessageDialog.openError(Display.getCurrent().getActiveShell(), 
			  	  							"RuleStudio - Error getting license", 
			  	  		  					"Unable to get license for this client.  Please contact support@cellucidate.com for help.");
				
			  					}
			  				}
			  			});
					}
				}
			});
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return true;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
	}
	
}
