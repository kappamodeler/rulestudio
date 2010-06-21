package com.plectix.rulestudio.core.usagedata;

public enum StorageConnector {
	
	PREFERENCES_STORE {
		@Override
		public void load() {
			PreferenceStoreConnector.loadActionsFromStore();
		}
		@Override
		public void save() {
			PreferenceStoreConnector.saveActionsToStore();
		}
	},
	
	PROPERTIES_FILE {
		@Override
		public void load() {
			PropertiesConnector.loadActionsFromProperties();
		}

		@Override
		public void save() {
			PropertiesConnector.saveActionsToProperties();
		}
	},
	;
	
	abstract public void load();
	
	abstract public void save();

}