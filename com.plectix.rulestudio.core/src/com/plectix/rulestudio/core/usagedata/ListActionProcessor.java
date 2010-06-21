package com.plectix.rulestudio.core.usagedata;

import java.util.ArrayList;
import java.util.List;


public class ListActionProcessor extends ActionProcessor {
	private static final String TAG = "list";
	
	private List<LabelWithTimeStamp> labelWithTimeStamps = new ArrayList<LabelWithTimeStamp>();
	
	public ListActionProcessor() {
		super(TAG, -1);
	}

	public void reset() {
		labelWithTimeStamps = new ArrayList<LabelWithTimeStamp>();
	}
	
	public void readFromString(String string) {
		String[] fields = getFields(string);
		if (fields == null) { 
			return;
		}
		
	    int fieldNo= 1;

		labelWithTimeStamps = new ArrayList<LabelWithTimeStamp>();		
		while (fieldNo < fields.length) {
			labelWithTimeStamps.add(new LabelWithTimeStamp(fields[fieldNo], Long.parseLong(fields[fieldNo+1])));
			fieldNo = fieldNo + 2;
		}
	}
	
	public String toString() {
		if (labelWithTimeStamps.size() == 0) {
			return ActionProcessor.NONE_STRING;
		}
		
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(TAG + ActionProcessor.FIELD_SEPARATOR);
		
		for (LabelWithTimeStamp labelWithTimeStamp : labelWithTimeStamps) {
			stringBuffer.append(labelWithTimeStamp.getName() + ActionProcessor.FIELD_SEPARATOR + labelWithTimeStamp.getTime() + ActionProcessor.FIELD_SEPARATOR);
		}
		stringBuffer.delete(stringBuffer.length() - ActionProcessor.FIELD_SEPARATOR.length(), stringBuffer.length());
		
		return stringBuffer.toString();
	}

	@Override
	public String toVerboseString() {
		if (labelWithTimeStamps.size() == 0) {
			return ActionProcessor.NONE_STRING;
		}
		
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(TAG + " -> ");
		
		for (LabelWithTimeStamp labelWithTimeStamp : labelWithTimeStamps) {
			stringBuffer.append("[" + labelWithTimeStamp.getName() + " on '" + getDateString(labelWithTimeStamp.getTime()) + "']");
		}
		
		return stringBuffer.toString();
	}
	
	public void add(String label, long actionTime, boolean start) {
		if (label == null) {
			UsageDataCollector.getInstance().addOneTimeAction(Action.UDC_CALL_ERROR);
			// throw new RuntimeException("label is null.");
			label = "NULL";
		}
		
		if (labelWithTimeStamps.size() == 0
				|| !labelWithTimeStamps.get(labelWithTimeStamps.size()-1).getName().equals(label)) {
			labelWithTimeStamps.add(new LabelWithTimeStamp(label, actionTime));
		}
	}

	private static final class LabelWithTimeStamp {
		private final String name;
		private final long time;
		
		public LabelWithTimeStamp(String name, long time) {
			this.name = name;
			this.time = time;
		}

		public final String getName() {
			return name;
		}

		public final long getTime() {
			return time;
		}
	}

	public boolean doesNeedLabel() {
		return true;
	}

	public boolean isOneTime() {
		return true;
	}


}
