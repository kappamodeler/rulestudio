package com.plectix.rulestudio.core.usagedata;

public class TallyActionProcessor extends ActionProcessor {
	private static final int NUMBER_OF_FIELDS = 4;
	
	private static final String TAG = "tally";
		
	private long count = 0;
	private long startTime = -1;
	private long endTime = -1;

	public TallyActionProcessor() {
		super(TAG, NUMBER_OF_FIELDS);
	}

	public void reset() {
		count = 0;
		startTime = -1;
		endTime = -1;
	}
	
	public void readFromString(String string) {
		String[] fields = getFields(string);
		if (fields == null) { 
			return;
		}
		
	    int fieldNo= 1;
		
		count = Long.parseLong(fields[fieldNo++]);
		startTime = Long.parseLong(fields[fieldNo++]);
		endTime = Long.parseLong(fields[fieldNo++]);
	}
	
	public String toString() {
		if (count == 0) {
			return ActionProcessor.NONE_STRING;
		}
		
		return TAG + ActionProcessor.FIELD_SEPARATOR
		     + count + ActionProcessor.FIELD_SEPARATOR 
		     + startTime + ActionProcessor.FIELD_SEPARATOR 
		     + endTime;
	}

	@Override
	public String toVerboseString() {
		StringBuffer verboseString = new StringBuffer(TAG + " -> [Count = " + count);
		if (count > 0) {
			verboseString.append(" from '" + getDateString(startTime) + "' to '" + getDateString(endTime) + "'"); 
		}
		verboseString.append("]");
		return verboseString.toString();
	}
	
	public void add(String label, long actionTime, boolean start) {
		if (startTime < 0) {
			startTime = actionTime;
		}
		endTime = actionTime;
		count++;
	}

	public boolean doesNeedLabel() {
		return false;
	}

	public boolean isOneTime() {
		return true;
	}
}
