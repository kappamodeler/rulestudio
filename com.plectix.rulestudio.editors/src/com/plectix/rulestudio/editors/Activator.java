package com.plectix.rulestudio.editors;

import java.util.Properties;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.plectix.rulestudio.editors.kappa.extras.SyntaxColors;
import com.plectix.rulestudio.editors.preferencepages.SyntaxColoring;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "com.plectix.rulestudio.editors";

	public final static String MARKER_TYPE = PLUGIN_ID+".kappaproblem";

	// The shared instance
	private static Activator plugin;
	
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

		IPreferenceStore store = getPreferenceStore();
		Properties defaultProps = new Properties();
		defaultProps.load(SyntaxColors.class.getResourceAsStream("DefaultColors.properties"));

		SyntaxColors[] colors = SyntaxColors.values();
		for (int index = 0; index < colors.length; index++){
			String 	key = colors[index].key();
			String	value = store.getString(key);
			if (value != null && value.trim().length() != 0){
				colors[index].initialize(value);
			}else if (defaultProps.get(key) != null){
				colors[index].initialize((String)defaultProps.get(key));
			}
		}

	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
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

}
