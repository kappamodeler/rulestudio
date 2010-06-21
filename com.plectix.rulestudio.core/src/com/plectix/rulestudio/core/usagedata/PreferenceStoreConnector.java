package com.plectix.rulestudio.core.usagedata;

import org.eclipse.jface.preference.IPreferenceStore;

import com.plectix.rulestudio.core.Activator;

public class PreferenceStoreConnector {

	protected static final void loadActionsFromStore() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		
		Action[] actionList = Action.values();
		boolean dataFound = false;
		for (Action action : actionList) {
			String value = store.getString(action.getName());
			if (value != null && value.trim().length() != 0) {
				dataFound = true;
				action.getActionProcessor().readFromString(value);
			} 
		}
		
		if (dataFound) {
			/* We can get the checksum and check whether it is correct or not, if need be.
			String checksum = store.getString("udc.chksm");
			*/
		}
	}
	
	protected static final void saveActionsToStore() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		
		Action[] actionList = Action.values();
		for (Action action : actionList) {
			// No need to get the old value: String oldValue = store.getString(action.getName());
			String newValue = action.getActionProcessor().toString();
		    store.setValue(action.getName(), newValue);
			// No need to call firePropertyChangeEvent: store.firePropertyChangeEvent((action.getName(), oldValue, newValue);
		}
		
		/* We can also compute and add a checksum if need be:
		String checksum = null;
		store.setValue("udc.chksm", checksum);
		*/
	}
	
}
