package com.plectix.kappa.editors.test;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.plectix.kappa.editors.test";

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
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
		
		//file:/C:/3rdpartysdk/plectix/com.plectix.kappa.views/
		int indexPos = location.indexOf("file:");
		if (indexPos != -1){
			location = location.substring(indexPos + 1);
		}
		return location;
	}


}
