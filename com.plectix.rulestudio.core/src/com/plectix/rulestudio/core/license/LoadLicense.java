package com.plectix.rulestudio.core.license;

import java.io.IOException;
import java.util.List;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.osgi.framework.Version;
import org.w3c.dom.Document;

import com.plectix.rulestudio.core.xml.XmlUtils;

public class LoadLicense {

	private boolean _bError = false;
	private int responseCode = 0;
	
	public LoadLicense(){
	}

	public boolean getError(){
		return _bError;
	}
	
	public int getResponseCode() {
		return responseCode;
	}
	
	
	/**
	 * Method will upload 
	 * 
	 */
	private String getLicense(IProgressMonitor monitor, String url, String email, String apiKey, String version) 
				throws IOException, CoreException{

		HttpClient client = createHttpClient(email, apiKey);
		
 		PostMethod method = new PostMethod(url + "/licenses.xml");

  		monitor.setTaskName("Requesting license.");
  		monitor.worked(1);

  		method.setParameter("license_type", "eclipse_plugin");
  		method.setParameter("version", version);
  		
  		/*
  		List<String> post = null;
  		for (int i= 0; i< post.size(); i = i+2) {
  			method.setParameter(post.get(i), post.get(i+1));
  		}
  		*/

  		monitor.setTaskName("Getting client license.");
  		monitor.worked(1);
  		
   		responseCode = client.executeMethod(method);
   		String status = null;
   		if (responseCode == 200)
   			status = parseUploadResponse(monitor, new String(method.getResponseBody()));
  		if (responseCode != 200 || _bError){
  			monitor.setTaskName("Error getting license.");
  			monitor.done();
  			_bError = true;
  			return null;
  		}
  		
  		return status;
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
			String state = XmlUtils.findString(document, "//state");
			if (state == null) {
				_bError = true;
				return null;
			} else if (!state.equals("active"))
				return null;
			else
				return XmlUtils.findString(document, "//license_key");
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
	public String waitForJobToFinish(IProgressMonitor monitor, String url, String email, String apiKey, Version version){
		String  status = null;
		String vString = Integer.toString(version.getMajor()) + '.' 
			+ Integer.toString(version.getMinor()) + '.'
			+ Integer.toString(version.getMicro());

		boolean first = true;
		int count = 20;
		do {
			try {
				if (first)
					first = false;
				else
					Thread.sleep(500);
				status = getLicense(monitor, url, email, apiKey, vString);
			} catch (Exception e) {
				e.printStackTrace();
			}		
		} while (status == null && !_bError && monitor.isCanceled() == false && --count > 0);

		monitor.worked(1);
		return status;
	}
	

}
