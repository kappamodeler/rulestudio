package com.plectix.rulestudio.core.usagedata;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;

public class PropertiesConnector {
	private static final String PROPERTIES_FILENAME = "actions.properties";
	
	private static final String COMMENT = "Plectix UDC Data";
	
	
	protected static final void loadActionsFromProperties() {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(PROPERTIES_FILENAME));
			
			Action[] actionList = Action.values();
			for (Action action : actionList) {
				String value = properties.getProperty(action.getName());
				if (value != null && value.trim().length() != 0) {
					action.getActionProcessor().readFromString(value.trim());
				} 
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected static final void saveActionsToProperties() {
		Properties properties = new Properties();
		
		Action[] actionList = Action.values();
		// let's save them in alphabetical order for easy reading...
		// Arrays.sort(actionList, ACTION_COMPARATOR);
		for (Action action : actionList) {
			properties.setProperty(action.getName(), action.getActionProcessor().toString());
		}
		
		try {
			properties.store(new BufferedOutputStream(new FileOutputStream(PROPERTIES_FILENAME)), COMMENT);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
