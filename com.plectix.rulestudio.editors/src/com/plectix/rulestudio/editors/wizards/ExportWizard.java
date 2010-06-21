package com.plectix.rulestudio.editors.wizards;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IExportWizard;
import org.eclipse.ui.IWorkbench;
import org.w3c.dom.Document;

import com.plectix.rulestudio.core.xml.XmlUtils;

public class ExportWizard extends Wizard implements IExportWizard {

	private ExportPage _exportPage = null;
	private IStructuredSelection _selection = null;
	private String 	_kappaUrl = "";
	private boolean	_bOpenBrowser = false;
	private boolean _bError = false;
	private String 	_apiServerURL = "";
	
	public ExportWizard(){
		setNeedsProgressMonitor(true);
	}

	public String getKappaUrl(){
		return _kappaUrl;
	}
	
	public boolean getOpenBrowser(){
		return _bOpenBrowser;
	}
	
	public boolean getError(){
		return _bError;
	}
	
	public void addPages(){
  		IFile file = (IFile)_selection.getFirstElement();			  		

  		_exportPage = new ExportPage(file.getName());
		_exportPage.setTitle("Export Information");
		_exportPage.setDescription("Supply the required information below before uploading.");
		
		setWindowTitle("Upload Kappa File");
		
		addPage(_exportPage);
		super.addPages();
	}

	public boolean performFinish() {
		
		final String bookName = _exportPage.getBookName();
		final String apiKey = _exportPage.getAPIKey();
		final String email = _exportPage.getEmail();
		_bOpenBrowser = _exportPage.openBrowser();
		
		//Set the information as preferences
		IPreferenceStore store = com.plectix.rulestudio.core.Activator.getDefault().getPreferenceStore();
		store.setValue("cellucidate_email", email);
		store.setValue("cellucidate_api_key", apiKey);
		store.setValue("cellucidate_open_browser", _bOpenBrowser);
		_apiServerURL = com.plectix.rulestudio.core.Activator.getDefault().getUrl();
		
		try {
			getContainer().run(true, true, new IRunnableWithProgress() {
			  	public void run(IProgressMonitor monitor){
			  		
			  		IFile file = (IFile)_selection.getFirstElement();			  		
			  		monitor.beginTask("Uploading Model to Cellucidate", 6);
			  					  		
			  		try {				  			
			  			String content = uploadKappa(monitor, email, apiKey,  bookName, file);
			  			if (content != null && _bError == false){
				  			String jobId = parseUploadResponse(monitor, content);
					  		if (jobId != null && _bError == false){
					  			boolean bJobFinishedSuccessfully = waitForJobToFinish(monitor, email, apiKey, jobId);
					  			if (bJobFinishedSuccessfully == true && _bError == false){
					  				_kappaUrl = findBookUrl(monitor, email, apiKey, jobId);
					  			}
					  		}
			  			}
				  		monitor.setTaskName("Testing if the upload worked.");
				  		monitor.worked(1);
				  		
					} catch (HttpException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} catch (CoreException e) {
						e.printStackTrace();
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
	
	String fileToString(IFile file) throws IOException, CoreException {			
        StringBuilder 	sb = new StringBuilder();
        byte[] 			cbuf = new byte[10000];
        int 			numRead;
        InputStream		inputStream = file.getContents();
        while (true) {
            numRead = inputStream.read(cbuf, 0, 10000);
            if (numRead<0) break;
            sb.append(new String(cbuf), 0, numRead);
        }
        inputStream.close();
        return sb.toString();
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		_selection = selection;
	}
	
	/**
	 * Method will upload 
	 * 
	 */
	private String uploadKappa(IProgressMonitor monitor, String email, String apiKey, 
				String bookName, IFile file) 
				throws IOException, CoreException{

		HttpClient client = createHttpClient(email, apiKey);
		
  		PostMethod method = new PostMethod(_apiServerURL + "/kappa_import.xml");

  		monitor.setTaskName("Encoding model Content.");
  		monitor.worked(1);

  		method.setParameter("book_name", bookName);
  		method.setParameter("kappa", fileToString(file));

  		monitor.setTaskName("Uploading model to Cellucidate");
  		monitor.worked(1);
  		
   		final int	 responseCode = client.executeMethod(method);
  		if (responseCode != 200){
  			
  			//We need to put the ui thread be into the right mode
  			Display.getDefault().syncExec(new Runnable(){
  				public void run(){
  					MessageDialog.openError(Display.getCurrent().getActiveShell(), 
  							"RuleStudio - Upload model Error", 
  		  					"An HTTP error (" + responseCode + ") occured while trying uploading the model file." +
  		  					"\r\nThis is a result of the server being down or your credentials being incorrect.");
  		  	 					
  				}
  			});
  			_bError = true;
  			return null;
  		}
  		
  		return new String(method.getResponseBody());
	}
	
	/**
	 * This method create a HttpClient that can be used to communicate with the 
	 * api server. The security credentials are applied to the http client.
	 * 
	 * @param email
	 * @param apiKey
	 * @return
	 */
	private HttpClient createHttpClient(String email, String apiKey){
  		HttpClient client = new HttpClient();			  		
  		client.getParams().setAuthenticationPreemptive(true);
  		Credentials defaultcreds = new UsernamePasswordCredentials(email, apiKey);
  		client.getState().setCredentials(AuthScope.ANY, defaultcreds);			  		

  		return client;
	}
	
	/**
	 * Given the content we need to pull out the job id. The content should be in
	 * the following form.
	 * 
	 * <job id="1">
	 * 	<id>8</id>
	 * </job>
	 * 
	 * @param monitor
	 * @param content
	 * @return
	 */
	private String parseUploadResponse(IProgressMonitor monitor, String content){
		Document document = XmlUtils.parseString(content);
		if (document == null){
  			_bError = true;
			return null;
		}else{
			return XmlUtils.findString(document, "//job/@id");
		}
	}
	
	/**
	 * We need to loop until job has finished, there is an error or the user cancels.
	 * 
	 * @param monitor
	 * @param email
	 * @param apiKey
	 * @param jobId
	 * @return
	 */
	private boolean waitForJobToFinish(IProgressMonitor monitor, String email, String apiKey, String jobId){
		boolean bFinished = checkJobStatus(monitor, email, apiKey, jobId);
		while (bFinished == false && monitor.isCanceled() == false){
			try {
				Thread.sleep(500);
				bFinished = checkJobStatus(monitor, email, apiKey, jobId);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}		
		}

		monitor.worked(1);
		return bFinished;
	}
	
	/**
	 * This 
	 * 
	 * @param monitor
	 * @param email
	 * @param apiKey
	 * @param jobId
	 * @return
	 */
	private boolean checkJobStatus(IProgressMonitor monitor, 
						String email, String apiKey, String jobId){

		HttpClient client = createHttpClient(email, apiKey);
	
		GetMethod method = new GetMethod(_apiServerURL + "/jobs/"+ jobId + ".xml");

		monitor.setTaskName("Checking job (" + jobId + ") status.");
		
		try{
			final int	 responseCode = client.executeMethod(method);
			
			if (responseCode != 200){
				
				//We need to put the ui thread be into the right mode
				Display.getDefault().syncExec(new Runnable(){
					public void run(){
						MessageDialog.openError(Display.getCurrent().getActiveShell(), 
								"RuleStudio - Error Checking Job Status", 
			  					"An HTTP error (" + responseCode + ") occured while trying check the job status." +
			  					"\r\nThis is a result of the server being down or your credentials being incorrect.");
			  	 					
					}
				});
	  			_bError = true;
				return false;
				
			}
			String content = new String(method.getResponseBody());
			return isJobFinished(monitor, content);
		}catch (IOException exception){
			exception.printStackTrace();
		}
		
		return false;
	}
	
	/**
	 * Seeing if the job is finished.  Need to look at the content and parse
	 * out the content.
	 * 
	 * The content should look like this. We will set the monitor status with
	 * the progress of the job.
	 * 
	 * <job>
	 *    <id>16</id>
	 *    <status>succeeded</status>
	 *    <progress>99.0</progress>
	 *    <error></error>
	 * </job>
	 * 
	 */
	private boolean isJobFinished(IProgressMonitor monitor, String content){
		String status = "";
		String percentComplete = "";
		
		Document document = XmlUtils.parseString(content);
		if (document == null){
  			_bError = true;
			return false;
		}else{
			status = XmlUtils.findString(document, "//status");
			percentComplete = XmlUtils.findString(document, "//progress");
		}

		monitor.setTaskName("Job is " + percentComplete + "% finished.");
		if ("failed".equals(status) == true){
			_bError = true;
			final String error = XmlUtils.findString(document, "//error");
			Display.getDefault().syncExec(new Runnable(){
				public void run(){
					MessageDialog.openError(Display.getCurrent().getActiveShell(), 
							"RuleStudio - Error Uploading Model", 
		  					"An error uploading your model to Cellucidate." +
		  					"\n" + error);
		  	 					
				}
			});
		}
		return "failed".equals(status) || "succeeded".equals(status);
	}
	
	/**
	 * This is called once the job has been successfuly completed. We need
	 * to get the status information which contains the url to the book.
	 * 
	 * @param monitor
	 * @param email
	 * @param apiKey
	 * @param jobId
	 * @return
	 */
	private String findBookUrl(IProgressMonitor monitor, String email, String apiKey, String jobId){
		HttpClient client = createHttpClient(email, apiKey);
		
		GetMethod method = new GetMethod(_apiServerURL + "/jobs/"+ jobId + "/results");

		monitor.setTaskName("Looking up the book url for job (" + jobId + ").");
		monitor.worked(1);
		
		try{
			final int	 responseCode = client.executeMethod(method);
			
			if (responseCode != 200){
				
				//We need to put the ui thread be into the right mode
				Display.getDefault().syncExec(new Runnable(){
					public void run(){
						MessageDialog.openError(Display.getCurrent().getActiveShell(), 
								"RuleStudio - Error Checking Job Status", 
			  					"An HTTP error (" + responseCode + ") occured while trying check the job status." +
			  					"\r\nThis is a result of the server being down or your credentials being incorrect.");
			  	 					
					}
				});
	  			_bError = true;
				return null;
				
			}
			String content = new String(method.getResponseBody());
			return parseBookUrl(content);
		}catch (IOException exception){
			exception.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * This method parses the status response and will pullout the url.
	 * 
	 * Not sure of the content format has of yet but it is probably something
	 * like the following.
	 * 
	 * <element>
	 *    <book-name></book-name>
	 *    <url></url>
	 * </element>
	 * 
	 * @param content
	 * @return
	 */
	private String parseBookUrl(String content){
		Document document = XmlUtils.parseString(content);
		if (document == null){
  			_bError = true;
			return null;
		}else{
			//This book name should match are book.
			String bookName = XmlUtils.findString(document, "//book-name");
			
			String url = XmlUtils.findString(document, "//url");

			//This fixes and error in the response from the api call
			//the url has "http//" instead of "http://"
			if (url.startsWith("http://http://")){
				url = url.substring("http://".length());		
			}

			return url;
		}
	}

}
