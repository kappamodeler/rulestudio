package com.plectix.rulestudio.core.usagedata;

import java.util.HashSet;
import java.util.Set;

public class SetActionProcessor extends ActionProcessor {
	private static final String TAG = "set";
	
	private static final int ITEM_LIMIT = 30;
	
	private Set<String> items = new HashSet<String>();
	
	public SetActionProcessor() {
		super(TAG, -1);
	}

	@Override
	public void reset() {
		items = new HashSet<String>();
	}

	@Override
	public void readFromString(String string) {
		String[] fields = getFields(string);
		if (fields == null) { 
			return;
		}
		
	    items = new HashSet<String>();	
		for (int fieldNo= 1; fieldNo < fields.length; fieldNo++) {
			items.add(fields[fieldNo]);
		}
	}

	@Override
	public String toString() {
		if (items.size() == 0) {
			return ActionProcessor.NONE_STRING;
		}
		
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(TAG + ActionProcessor.FIELD_SEPARATOR);
		
		int count = 0;
		for (String text : items) {
			stringBuffer.append(text + ActionProcessor.FIELD_SEPARATOR);
			if (++count == ITEM_LIMIT) {
				break;
			}
		}
		stringBuffer.delete(stringBuffer.length() - ActionProcessor.FIELD_SEPARATOR.length(), stringBuffer.length());
		
		return stringBuffer.toString();
	}

	@Override
	public String toVerboseString() {
		if (items.size() == 0) {
			return ActionProcessor.NONE_STRING;
		}
		
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(TAG + " -> ");
		
		int count = 0;
		for (String text : items) {
			stringBuffer.append("[" + text + "]");
			if (++count == ITEM_LIMIT) {
				break;
			}
		}
		
		return stringBuffer.toString();
	}

	
	@Override
	public void add(String label, long actionTime, boolean start) {
		if (label == null) {
			UsageDataCollector.getInstance().addOneTimeAction(Action.UDC_CALL_ERROR);
			// throw new RuntimeException("label is null.");
			label = "NULL";
		}
		
		items.add(label);
	}


	@Override
	public boolean doesNeedLabel() {
		return true;
	}

	@Override
	public boolean isOneTime() {
		return true;
	}

}
