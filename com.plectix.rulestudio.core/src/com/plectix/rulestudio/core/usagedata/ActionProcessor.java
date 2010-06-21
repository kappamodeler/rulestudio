package com.plectix.rulestudio.core.usagedata;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

public abstract class ActionProcessor {

	protected static final String FIELD_SEPARATOR = "|";
	
	protected static final String NONE_STRING = "none";
	
	protected static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	private String tag = null;
	
	private int numberOfFields = Integer.MAX_VALUE;
	
	/**
	 * Constructor
	 * 
	 * @param tag
	 * @param numberOfFields
	 */
	public ActionProcessor(String tag, int numberOfFields) {
		if (tag == null) {
			throw new RuntimeException("ActionProcessor Tag can not be null!");
		}
		this.tag = tag;
		this.numberOfFields = numberOfFields;
	}
	
	abstract public void readFromString(String string);
	
	abstract public String toString();
	
	abstract public String toVerboseString();
	
	abstract public void add(String label, long actionTime, boolean start);

	abstract public void reset();

	abstract public boolean isOneTime();

	abstract public boolean doesNeedLabel();
	
	protected final String getDateString(long time) {
		return DATE_FORMAT.format(new Date(time)); 
	}
	
	protected final String[] getFields(String string) {
		if (string.equals(ActionProcessor.NONE_STRING)) {
			return null;
		}
		
		StringTokenizer stringTokenizer = new StringTokenizer(string, ActionProcessor.FIELD_SEPARATOR);
		
		String[] fields = new String[stringTokenizer.countTokens()];
		
		if (numberOfFields > 0) {
			if (fields.length != numberOfFields) {
				UsageDataCollector.getInstance().addOneTimeAction(Action.UDC_READ_ERROR);
				// throw new RuntimeException("Unexpected number of fields: " + fields.length);
				return null;
			}
		}

		int fieldNo= 0;
	    while (stringTokenizer.hasMoreTokens()) {
	    	fields[fieldNo++] = stringTokenizer.nextToken();
	    }
	    
		String tagInString = fields[0];
		if (!tagInString.equals(tag)) {
			UsageDataCollector.getInstance().addOneTimeAction(Action.UDC_READ_ERROR);
			// throw new RuntimeException("Unexpected tag: " + tagInString + " is not " + tag);
			return null;
		}
	    
	    return fields;
	}
}
