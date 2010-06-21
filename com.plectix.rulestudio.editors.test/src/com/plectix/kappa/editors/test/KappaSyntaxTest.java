/**
 * 
 */
package com.plectix.kappa.editors.test;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;
import java.util.StringTokenizer;

import junit.framework.Assert;

import org.eclipse.core.resources.IMarker;
import org.junit.Test;

import com.plectix.kappa.editors.builders.KappaSyntaxParser;

/**
 * @author bill
 *
 */
public class KappaSyntaxTest {
	
	String[] kappaBadList = {
			"%init; FF(x)",
			"%init \\\n more",
			"  %init error",
			"%init:",
			"%other: this is wrong",
			"a(s)->a(s!_)",
			"a(s)->a(s?)",
			"a(),b(s?)->a(),b(s)",
			"a(s!_) <-> a(s)",
			"B(y~a~e)->B(y~a~e)",
			"agent(x),B(y~a?) -> agent(x!1),B(y~a!1)",
			"Aww(x) -> Aww(x?)",
			"A(x!_) <-> A(x)",
			"A(x) -> A(x!_)",
			"A(x!_),B(y) <-> A(x!1),B(y!1)",
			"A(x),B(y),C(v),D(x) -> A(x!1),B(y!1),C(v!1),D(x!1)",
			"A(x),B(y),C(v),D(x) -> A(x!1),B(y!1),C(v!1),D(x)",
			"A(x~u) -> A(x)",
			"A(x) -> A(x~u)",
			"A(x~u),B(x) -> A(x!1),B(x!1)",
			"A(x!_) -> A(x~a)",
			"A(x~u!_) -> A(x!_)",
			"A(x?) -> A(x~u?)",
			"A(x~a) -> A(~u)",
			"A(~a) -> A(x~u)",
			
			"A(x,y~a) -> A(x,~u)",
			"A(x~a),B(x) <-> A(~u!1),B(x)",
			"a(s1!1),b(s1)->",
			"->a(s1!1),b(s1)",
			"a(s1!1),b(s1!1),c(s1!1)->",
			"->a(s1!1),b(s1!1),c(s1!1)",
			"a(s1!1,s2!1)->",
			"->a(s1!1,s2!1)",
			"a(s1,s2,s3)->a(s1,s2)",
			"a(s)->a(s!_)",
			"a(s)->a(s?)",
			"a(),b(s?)->a(),b(s)",
			"a(s1,s2)-> a(s1!1,s2!1)",
			"a(s)->a(s!_)",
			"a(s)->a(s?)",
			"a(s!_) <-> a(s)",
			"B(y~a~e)->B(y~a~e)",

			"a(s1,s2)-> a(s1!1,s2!1)",
			"a(s)->a(s!_)",
			"a(s)->a(s?)",
			"a(s!_) <-> a(s)",
			"B(y~a~e)->B(y~a~e)",

			// INVALID agents
			"%init: 1 * a(s1,s1)",
			"%init: 1 * abcdefghijklmnopqrstu()",
			"%init: 1 * a(abcdefghijklmnopqrstu)",
			"%init: 1 * a(s~abcdefghijklmnopqrstu)",
			"%init: 1 * (s)",
			"%init: 1 * a(,s)",
			"%init: 1 * &()",
			"%init: 1 * a#()",
			"%init: 1 * a$()",
			"%init: 1 * ^a()",
			"%init: 1 * -a()",
			"%init: 1 * _a()",
			"%init: 1 * a(s%)",
			"%init: 1 * a(s*)",
			"%init: 1 * a(s@)",
			"%init: 1 * a(^1)",
			"%init: 1 * a(-b)",
			"%init: 1 * a(_c)",
			"%init: 1 * a(s~^)",
			"%init: 1 * a(s~-)",
			"%init: 1 * a(s~_)",
			"%init: 1 * a(s1!1),b(s1)",
			"%init: 1 * a(s1!1),b(s1!1),c(s1!1)",
			"%init: 1 * a(s1!1,s2!1)",
	};


			//VALID rules
	String[] kappaGoodList = {
			"a(s!_)->a(s)",
			"B(y~a?) -> B(y~b?)",
			"Aww(x?) ->",
			"Aww(x?) -> B(y)", 
			"A(x!_) -> A(x)",
			"A(x!_),B(y) -> A(x!1),B(y!1)",
			"A(x),B(y),C(v),D(x) -> A(x!1),B(y!1),C(v!2),D(x!2)",
			"A(x~u) -> A(x~p)",
			"A(x~u),B(x) -> A(x~a!1),B(x!1)",
			"A(x~u!_) -> A(x~a)",
			"A(x~u!_) -> A(x~a!_)",
			"A(x~u?) -> A(x~a?)",
			"A(x~a) -> A(x~u)",
			"A(x~a) -> A(x~u)",
			"A(x,y~a) -> A(x,y~u)",
			"A(x~a),B(x) <-> A(x~u!1),B(x!1)",
			"a(s1!1,s2~u),b(s1!1)->a(s1,s2~p),b(s1)",
			"a(s1,s2,s3)->a(s3,s2,s1)",
			"A(x~a) -> A(x~u) @ 6.345E-1",
			"A(x~a) -> A(x~u) @ 6.345E+1",
			"A(x~a) -> A(x~u) @ 6.345E1",
			"A(x~a) -> A(x~u) @ 6.345e-1",
			"A(x~a) -> A(x~u) @ 6E-1",
			// VALID
			"%init: FF(x)",
			"%init: 1 * a(s1,s2~u)",
			"%init: 1 * azAZ09-_^()",
			"%init: 1 * a(azAZ09-_^)",
			"%init: 1 * a(s~azAZ09)",
			"%init: 1 * a(s1!1,s2~u),b(s1!1)",
	};
	
	@Test
	public void testStrings() {
		KappaSyntaxParserMock parser = new KappaSyntaxParserMock(true);
		int errorCount = 0;
		int count = 0;
		
		for (String arg: kappaBadList) {
			++count;
			try {
				errorCount += parser.checkBadKappaLine(arg);
			} catch (Throwable th) {
				++errorCount;
				System.err.println("Exception thrown for \"" + arg + "\"");
				th.printStackTrace();
			}
		}
		
		for (String arg: kappaGoodList) {
			++count;
			try {
				errorCount += parser.checkGoodKappaLine(arg);
			} catch (Throwable th) {
				++errorCount;
				System.err.println("Exception thrown for \"" + arg + "\"");
				th.printStackTrace();
			}
		}
		
		if (errorCount > 0)
			System.out.println("Test Count: " + count + ", Error(s): " + errorCount);

		Assert.assertEquals(0, errorCount);
	}
	
	/*
	 * The IMarker api uses unchecked maps.
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testFiles() {
		String loc = Activator.getDefault().getPluginLocation();
		File testDir = new File(new File(loc), "test");
		
		String[] files = testDir.list(new FilenameFilter() {

			@Override
			public boolean accept(File arg0, String arg1) {
				return arg1.endsWith(".ka");
			}
			
		});
		
		for (String name: files) {
			try {
				File input = new File(testDir, name);
				FileMock fm = new FileMock(input);
				KappaSyntaxParser parser = new KappaSyntaxParser(true);
				parser.validateFile(fm);
				
				IMarker[] errors = fm.findMarkers(null, true, 0);
				File report = new File(testDir, name + ".out");
				if (!report.exists()) {
					PrintWriter out = new PrintWriter(new FileWriter(report));
					out.println("# Created " + new Date().toString());
					out.println("# start\tend\tlevel\tmessage");
					for (IMarker mark: errors) {
						Map<String, Object> mMap = mark.getAttributes();
						String line = (String)(mMap.get(IMarker.LOCATION));
						out.print(line);
						out.print('\t');
						int start = (Integer)(mMap.get(IMarker.CHAR_START));
						out.print(start);
						out.print("\t");
						int end = (Integer)(mMap.get(IMarker.CHAR_END));
						out.print(end);
						out.print("\t");
						int level = (Integer)(mMap.get(IMarker.SEVERITY));
						out.print(getSeverityString(level));
						out.print("\t");
						out.println((String)(mMap.get(IMarker.MESSAGE)));
					}
					out.close();
				} else {
					ErrorStream es = new ErrorStream(report);
					es.nextError();
					for (IMarker mark: errors) {
						Map<String, Object> mMap = mark.getAttributes();
						int lNum = (Integer)mMap.get(IMarker.LINE_NUMBER);
						while (es.getLine() != -1 && lNum < es.getLine()) {
							String errMsg = String.format("Missing error: %s", es.toString());
							Assert.fail(errMsg);
							es.nextError();
						}
						if (lNum == es.getLine()) {
							int start = (Integer)mMap.get(IMarker.CHAR_START);
							int end = (Integer)mMap.get(IMarker.CHAR_END);
							String level = getSeverityString((Integer)mMap.get(IMarker.SEVERITY));
							String message = (String)mMap.get(IMarker.MESSAGE);
							if (start != es.getStart() || end != es.getEnd() || !level.equals(es.getLevel())
									|| !message.equals(es.getMessage())) {
								String errMsg = String.format("Error Changed.%nWas: %s%nNow: %s%n", 
										es.toString(), mark.toString());
								Assert.fail(errMsg);
							}
							es.nextError();
						} else {
							String errMsg = String.format("New error: %s", mark.toString());
							Assert.fail(errMsg);
						}	
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Assert.fail(e.getMessage());
			}
		}
		
	}
	
	private String getSeverityString(int level) {
		switch(level) {
		case IMarker.SEVERITY_ERROR:
			return ("Error");
		case IMarker.SEVERITY_WARNING:
			return ("Warning");
		case IMarker.SEVERITY_INFO:
			return ("Info");
		default:
			return ("Unknown:" + level);
		}
				
	}
	
	public static void main(String args[]) {
		if (args.length != 1) {
			System.out.println("dump <directory>");
			System.exit(1);
		}
		
		try {
			File dir = new File(args[0]);
			String[] files = dir.list(new FilenameFilter() {

				@Override
				public boolean accept(File arg0, String arg1) {
					return arg1.endsWith(".ka");
				}
				
			});
			
			for (String base: files) {
				File kappa = new File(dir, base);
				System.out.printf("Processing %s%n", kappa.getAbsoluteFile());
				File out = new File(dir, base + ".out");
				if (!out.exists()) {
					System.err.println(out.getAbsolutePath() + " does not exist.");
					continue;
				}
				File dump = new File(dir, base + ".dmp");
				if (dump.exists() && !dump.delete()) {
					System.err.println("Cannot delete old dump file" + dump.getAbsolutePath());
					continue;
				}
				KappaStream kList = new KappaStream(kappa);
				ErrorStream es = new ErrorStream(out);
				PrintWriter pwOut = new PrintWriter(new FileWriter(dump));
				pwOut.println("Test results for " + base);
				pwOut.println("Errors are described before the line in the kappa file.");
				pwOut.println("The *'s are the characters underlined for the message.\n");
				
				es.nextError();
				String kappaLine = null;
				while ((kappaLine = kList.nextKappa()) != null) {
					displayResults(es, kList, pwOut);
					pwOut.println(kappaLine);
					pwOut.println();
				}
				if (es.getLine() != -1) {
					do {
						pwOut.printf("Missing error for line %d%n", es.getLine());
					} while (es.nextError());
				}
				kList.close();
				es.close();
				pwOut.close();
			}

			
		} catch (Throwable th) {
			th.printStackTrace();
			System.exit(-1);
		}
		System.out.println("Done processing files.");
		System.exit(0);
	}
	
	private static void displayResults(ErrorStream es, KappaStream kList,
			PrintWriter pwOut) throws Exception {
		if (kList.getLineNumber() != es.getLine()) {
			pwOut.println("No error.");
		} else {
			do {
				pwOut.printf("Error: %s, Message %s%n", es.getLevel(),
						es.getMessage());
				int start = es.getStart() - kList.getOffset();
				int done = es.getEnd() - kList.getOffset();
				int offset = 0;
				StringBuilder sb = new StringBuilder();
				for (; offset < start; ++offset) {
					sb.append(' ');
				}
				for (; offset < done; ++offset) {
					sb.append('*');
				}
				pwOut.println(sb.toString());
				es.nextError();
			} while (es.getLine() == kList.getLineNumber());
		}
		return;
	}

	public static class KappaStream {
		private InputStream rd;
		private int lineNum = 1;
		private int curLine = 0;
		private boolean more = true;
		private int offset = 0;
		private int curOffset = 0;
		
		public KappaStream(File in) throws Exception {
			rd = new FileInputStream(in);
		}
		
		public String nextKappa() throws Exception {
			if (!more)
				return null;
			curLine = lineNum;
			curOffset = offset;
			StringBuffer kappa = new StringBuffer();
			int nextChar = 0;
			char lastChar = 0;
			
			while((nextChar = rd.read()) != -1) {
				char ch = (char)nextChar;
				if (ch == '\r') {
					continue;
				} else if (ch == '\n') {
					++lineNum;
					++offset;
					if (lastChar == '\\') {
						kappa.append(ch);
						lastChar = ch;
					} else {
						if (isValid(kappa))
							return kappa.toString();
						else {
							curLine = lineNum;
							curOffset = offset;
							kappa.delete(0, kappa.length());
						}
						
					}
				} else {
					++offset;
					kappa.append(ch);
				}
			}
			more = false;
			if (isValid(kappa)) {
				return kappa.toString();
			} else {
				return null;
			}
			
		}
		
		private boolean isValid(StringBuffer buf) {
			int i = 0;
			for (; i < buf.length() && Character.isWhitespace(buf.charAt(i)); ++i) {
				;
			}
			return i < buf.length() && buf.charAt(i) != '#';
		}
		
		public int getLineNumber() {
			return curLine;
		}
		
		public int getOffset() {
			return curOffset;
		}
		
		public void close() throws Exception {
			rd.close();
		}
	}
	
	public static class ErrorStream {
		private BufferedReader read = null;
		private int lineNum = 0;		// for the kappa file
		private int start = 0;
		private int end = 0;
		private String level = null;
		private String message = null;
		
		public ErrorStream(File in) throws Exception {
			read = new BufferedReader(new FileReader(in));
		}
		
		/* get the next record from a line like
		 * Line 13, Char 1	439	451	Warning	The any-bond (ie, "!_") can appear on both sides of a rule on the same sites. However, the only circumstance where it can appear only on one side of a rule is when it is on the left hand side of a non-reversible rule (ie, a rule with a forward arrow only).
		 * 
		 */
		public boolean nextError() throws Exception {
			if (lineNum == -1) {
				return false;
			}
			String line = null;
			while ((line = read.readLine()) != null) {
				if (line.length() == 0 || line.startsWith("#"))
					continue;
				StringTokenizer st = new StringTokenizer(line, ", ");
				String token = st.nextToken();
				token = st.nextToken();
				lineNum = Integer.parseInt(token);
				st.nextToken("\t");			// skip to start
				token = st.nextToken();
				start = Integer.parseInt(token);
				token = st.nextToken();
				end = Integer.parseInt(token);
				level = st.nextToken();
				message = st.nextToken();
				return true;
			}
			lineNum = -1;
			return false;				// done
		}
		
		public int getLine() {
			return lineNum;
		}
		
		public int getStart() {
			return start;
		}
		
		public int getEnd() {
			return end;
		}
		
		public String getLevel() {
			return level;
		}
		
		public String getMessage() {
			return message;
		}
		
		public void close() throws Exception {
			read.close();
		}
		
		public String toString() {
			if (lineNum == -1)
				return "No Error";
			else {
				return String.format("Line: %d Start: %d End: %d Level: %s Message: %s", 
						lineNum, start, end, level, message);
				
			}
		}
		
	}

}
