package com.plectix.rulestudio.views;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.URL;
import java.util.Hashtable;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.http.jetty.JettyConfigurator;
import org.eclipse.equinox.http.jetty.JettyConstants;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.plectix.rulestudio.views";

	// The shared instance
	private static Activator plugin;

	private int _jettPortNumber = 0;
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		Bundle bundle = Platform.getBundle("org.eclipse.equinox.http.registry");
		if (bundle.getState() == Bundle.RESOLVED) {
			bundle.start(Bundle.START_TRANSIENT);
		} 

		//We use this method to get an open port, jetty will find one itself but it
		//is hard to get the port it is using. So we pick an open one ourselfs.
		_jettPortNumber = findUnusedPort();
		
		Hashtable<String, Object> dictionary = new Hashtable<String, Object>();
		dictionary.put(JettyConstants.HTTP_ENABLED, new Boolean(true));
		dictionary.put(JettyConstants.HTTP_PORT,  _jettPortNumber);
				
		try{
			//dictionary.put("http.port", new Boolean(true));
			JettyConfigurator.startServer(PLUGIN_ID + ".server", dictionary);				
		}catch (RuntimeException e){
			e.printStackTrace();
		}
		
	}
	
	public String getUrlPrefix(){
		return "http://localhost:" + _jettPortNumber + "/kappa-view/";
	}
	
	/**
	 * This will return an unused server socket.
	 * 
	 * @return
	 */
	public int findUnusedPort(){
		ServerSocket socket;
		int portNumber = 0;
		try {
			socket = new ServerSocket();
			socket.bind(null);
			portNumber = socket.getLocalPort();
			socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return portNumber;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		JettyConfigurator.stopServer(PLUGIN_ID + ".server");
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	/**
	 * Get the location of the plugin on disk.
	 * 
	 * @return
	 */
	public String getPluginLocation(){
		String 	location = "";
		URL 	url = FileLocator.find(getBundle(), new Path(""), null);
		
		try {
			url = FileLocator.resolve(url);
			location = url.getPath();
		}catch (IOException ioe){
			
		}
		
		//file:/C:/3rdpartysdk/plectix/com.plectix.rulestudio.views/
		int indexPos = location.indexOf("file:");
		if (indexPos != -1){
			location = location.substring(indexPos + 1);
		}
		return location;
	}
	
}
