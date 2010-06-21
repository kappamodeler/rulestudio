package com.plectix.kappa.editors.test;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;

import com.plectix.kappa.editors.builders.KappaSyntaxParser;

public class KappaSyntaxParserMock extends KappaSyntaxParser {
	private List<KappaMarker> markers;
	private int testNum = 0;

	public KappaSyntaxParserMock(boolean bReportErrors) {
		super(bReportErrors);
		testNum = 0;
	}
	
	public void setup() {
		++testNum;
		markers = new ArrayList<KappaMarker>();
	}
	
	public List<KappaMarker> getMarkers() {
		return markers;
	}
	
	public int checkKappaLine(String input, boolean isBad) {
		setup();
		validateString(input);
		List<KappaMarker> errors = getMarkers();

		if (!isBad) { // no error
			if (errors.size() > 0) {
				return reportError(input, "Extraneous error reported");
			}
		} else {
			if (errors.size() == 0)
				return reportError(input, "Missed Error");
			KappaMarker mark = errors.get(0);
			String result = mark.checkMarker(0, input.length(), IMarker.SEVERITY_ERROR);
			if (result != null) {
				return reportError(input, result);
			}
		}
		return 0;
	}
	
	
	private int reportError(String input, String result) {
		System.err.println("Test for \"" + input + "\" failed: " + result);
		return 1;
	}

	protected void createMarker(int charStart, int charEnd, String reason, int severity){
		KappaMarker mark = new KappaMarker(charStart, charEnd, severity, reason);
		markers.add(mark);
	}

	public static class KappaMarker {
		private int start;
		private int end;
		private String severity;
		private String message;
		
		public KappaMarker(int start, int end, int severity, String message) {
			this.start = start;
			this.end = end;
			this.severity = levelName(severity);
			this.message = message;
			
		}
		
		private String levelName(int level) {
			switch (level) {
			case IMarker.SEVERITY_ERROR:
				return "Error";
			case IMarker.SEVERITY_WARNING:
				return "Warning";
			case IMarker.SEVERITY_INFO:
				return "Info";
			default:
				return "Other";					
			}
		}
		
		public String checkMarker(int from, int to, int level) {
			if (from != start) {
				return "Error start at " + start + ", should be " + from;
			} else if (to != end) {
				return "Error end at " + end + ", should be " + to;
			} else if (!levelName(level).equals(severity)) {
				return "Error level is " + severity + ", should be " + level;
			} else {
				return null;
			}
		}
		
		public int getStart() {
			return start;
		}
		
		public int getEnd() {
			return end;
		}
		
		public String getLevel() {
			return severity;
		}
		
		public String getError() {
			return message;
		}

	}

	public int checkBadKappaLine(String arg) {
		return checkKappaLine(arg, true);
	}

	public int checkGoodKappaLine(String arg) {
		return checkKappaLine(arg, false);
	}

}
