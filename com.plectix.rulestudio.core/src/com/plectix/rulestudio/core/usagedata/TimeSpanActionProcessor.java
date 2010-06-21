package com.plectix.rulestudio.core.usagedata;


public class TimeSpanActionProcessor extends ActionProcessor {
	private static final int NUMBER_OF_FIELDS = 9;
	
	private static final String TAG = "span";
	
	private long count = 0;
	private long errorCount = 0;
	
	private long startTime = -1;
	private long endTime = -1;

	private long min = Long.MAX_VALUE;
    private long max = Long.MIN_VALUE;
	private long sum = 0;
	
    private long lastStartTime = -1;
    
	public TimeSpanActionProcessor() {
		super(TAG, NUMBER_OF_FIELDS);
	}

	public void reset() {
		count = 0;
		errorCount = 0;
		
		startTime = lastStartTime;
		endTime = -1;

		min = Long.MAX_VALUE;
	    max = Long.MIN_VALUE;
		sum = 0;
		
	    // DO NOT RESET lastStartTime
	}
	
	public void readFromString(String string) {
		String[] fields = getFields(string);
		if (fields == null) { 
			return;
		}
		
	    int fieldNo= 1;
		
		count = Long.parseLong(fields[fieldNo++]);
		errorCount = Long.parseLong(fields[fieldNo++]);
		min  = Long.parseLong(fields[fieldNo++]);
		max  = Long.parseLong(fields[fieldNo++]);
		sum  = Long.parseLong(fields[fieldNo++]);		
		startTime = Long.parseLong(fields[fieldNo++]);
		endTime = Long.parseLong(fields[fieldNo++]);
		lastStartTime  = Long.parseLong(fields[fieldNo++]);		
	}
	
	public String toString() {
		return TAG + ActionProcessor.FIELD_SEPARATOR
		     + count + ActionProcessor.FIELD_SEPARATOR 
		     + errorCount + ActionProcessor.FIELD_SEPARATOR 
		     + min + ActionProcessor.FIELD_SEPARATOR 
		     + max + ActionProcessor.FIELD_SEPARATOR 
		     + sum + ActionProcessor.FIELD_SEPARATOR 
		     + startTime + ActionProcessor.FIELD_SEPARATOR 
		     + endTime + ActionProcessor.FIELD_SEPARATOR 
		     + lastStartTime;
	}
	

	@Override
	public String toVerboseString() {
		StringBuffer verboseString = new StringBuffer(TAG + " -> [Count = " + count + " ErrorCount = " + errorCount);
		if (count > 0) {
			verboseString.append(" min = " + (min/1000) + " max = " + (max/1000) + " mean = " + (sum/count/1000)); 
			verboseString.append(" from '" + getDateString(startTime) + "' to '" + getDateString(endTime) + "'"); 
		}
		verboseString.append("]");
		return verboseString.toString();
	}

	public void add(String label, long actionTime, boolean start) {
		if (start) {
			start(actionTime);
		} else {
			end(actionTime);
		}
	}
	
	public void start(long actionTime) {
		if (lastStartTime > 0) {
			// we didn't get an end() call! 
			errorCount++;
			lastStartTime = actionTime;
			return;
		}

		if (startTime < 0) {
			startTime = actionTime;
		}
		
		lastStartTime = actionTime;
	}
	
	public void end(long actionTime) {
		if (lastStartTime < 0) {
			// we didn't get a start() call!
			errorCount++;
			return;
		}

		endTime = actionTime;
		
		long timespan = actionTime - lastStartTime;
		min = Math.min(min, timespan);
		max = Math.max(max, timespan);
		sum = sum + timespan;
		count++;
		
		lastStartTime = -1;
	}

	public boolean doesNeedLabel() {
		return false;
	}

	public boolean isOneTime() {
		return false;
	}


}
