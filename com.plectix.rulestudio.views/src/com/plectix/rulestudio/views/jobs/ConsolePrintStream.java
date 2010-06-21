package com.plectix.rulestudio.views.jobs;


import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.progress.UIJob;

public class ConsolePrintStream extends PrintStream {
	
	private String toPrint = null;
	private UIJob writeText = null;
	private ConcurrentLinkedQueue<String> input = new ConcurrentLinkedQueue<String>();
	
	public ConsolePrintStream(final RunSimData data) throws Exception {
		super(new ByteArrayOutputStream());
		
		writeText = new UIJob("Write Thread") {
			
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				while (!input.isEmpty()) {
					String line = input.remove();
					data.addConsole(line);
				}
				return Status.OK_STATUS;
			}
		};
		writeText.setSystem(true);
		writeText.setUser(false);
		super.close();
	}

	@Override
	public PrintStream append(char c) {
		printWhereAmI();
		return super.append(c);
	}

	@Override
	public PrintStream append(CharSequence csq, int start, int end) {
		printWhereAmI();
		return super.append(csq, start, end);
	}

	@Override
	public PrintStream append(CharSequence csq) {
		printWhereAmI();
		return super.append(csq);
	}

	@Override
	public boolean checkError() {
		printWhereAmI();
		return super.checkError();
	}

	@Override
	public void close() {
		super.close();
	}

	@Override
	public void flush() {
		printWhereAmI();
		super.flush();
	}

	@Override
	public PrintStream format(Locale l, String format, Object... args) {
		printWhereAmI();
		return super.format(l, format, args);
	}

	@Override
	public PrintStream format(String format, Object... args) {
		printWhereAmI();
		return super.format(format, args);
	}

	@Override
	public void print(boolean b) {
		printWhereAmI();
		super.print(b);
	}

	@Override
	public void print(char c) {
		printWhereAmI();
		super.print(c);
	}

	@Override
	public void print(char[] s) {
		printWhereAmI();
		super.print(s);
	}

	@Override
	public void print(double d) {
		print(Double.toString(d));
	}

	@Override
	public void print(float f) {
		printWhereAmI();
		super.print(f);
	}

	@Override
	public void print(int i) {
		printWhereAmI();
		super.print(i);
	}

	@Override
	public void print(long l) {
		printWhereAmI();
		super.print(l);
	}

	@Override
	public void print(Object obj) {
		printWhereAmI();
		super.print(obj);
	}

	@Override
	public void print(final String s) {
		synchronized (this) {
			if (toPrint == null)
				toPrint = s;
			else
				toPrint += s;
		}
	}

	@Override
	public PrintStream printf(Locale l, String format, Object... args) {
		printWhereAmI();
		return super.printf(l, format, args);
	}

	@Override
	public PrintStream printf(String format, Object... args) {
		printWhereAmI();
		return super.printf(format, args);
	}

	@Override
	public void println() {
		this.println("");  
	}

	@Override
	public void println(boolean x) {
		printWhereAmI();
		super.println(x);
	}

	@Override
	public void println(char x) {
		printWhereAmI();
		super.println(x);
	}

	@Override
	public void println(char[] x) {
		printWhereAmI();
		super.println(x);
	}

	@Override
	public void println(double x) {
		printWhereAmI();
		super.println(x);
	}

	@Override
	public void println(float x) {
		printWhereAmI();
		super.println(x);
	}

	@Override
	public void println(int x) {
		printWhereAmI();
		super.println(x);
	}

	@Override
	public void println(long x) {
		printWhereAmI();
		super.println(x);
	}

	@Override
	public void println(Object x) {
		if (x == null)
			println("null");
		else
			println(x.toString());
	}

	@Override
	public void println(String x) {
		String toOut = null;
		synchronized(this) {
		if (toPrint != null) {
			toOut = toPrint + x;
			toPrint = null;
		} else
			toOut = x;
		}
		input.add(toOut);
		writeText.schedule();
	}

	@Override
	protected void setError() {
		printWhereAmI();
		super.setError();
	}

	@Override
	public void write(byte[] buf, int off, int len) {
		this.print(new String(buf, off, len)); // super.write(buf, off, len);
	}

	@Override
	public void write(int b) {
		printWhereAmI();
		super.write(b);
	}
	
	public static void printWhereAmI() {
	    //create exception and write its stack trace to a String
	    StringWriter sw = new StringWriter();
	    PrintWriter pw = new PrintWriter(sw);
	    new Exception("printWhereAmI()").printStackTrace(pw);
	    pw.close();
	    String exceptionText = sw.toString();

	    //skip through first two "at ..."
	    for(int i = 0;i < 2; i++) {
	    	exceptionText = exceptionText.substring(exceptionText.indexOf("at ", 1));
	    }
	    
	    //clip off remaining stack trace
	    System.out.println("\n **printWhereAmI(): " + exceptionText.substring(0,exceptionText.indexOf("at ",1)));
	    
	    throw new RuntimeException("Please make sure this output is redirected to the textArea!");
	}

}