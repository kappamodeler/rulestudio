package com.plectix.kappa.editors.test;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import com.plectix.kappa.editors.test.KappaSyntaxTest.KappaStream;
import com.plectix.kappa.views.utils.RunSimJob;

/*
 * Check each kappa line in a kappa file for errors using the jsim parser
 */

public class CheckKappaFile {
	
	public static void checkKappaLines(File in, File out, String path) throws Exception {
		RunSimJob job = RunSimJob.getInstance();
		job.setLocalPath(path);
		KappaStream ks = new KappaStream(in);
		File test = new File("test.ka");
		if (test.exists() && !test.delete()) {
			System.err.println("Cannot delete temp " + test.getAbsolutePath());
			return;
		}
		File output = new File("test.xml");
		if (output.exists() && !output.delete()) {
			System.err.println("Cannot delete output file " + output.getAbsolutePath());
		}
		PrintWriter write = new PrintWriter(new FileWriter(out));
		String line = null;
		while ((line = ks.nextKappa()) != null) {
			if (test.exists() && !test.delete()) {
				System.err.println("Cannot delete temp " + test.getAbsolutePath());
				return;
			}
			PrintWriter pw = new PrintWriter(new FileWriter(test));
			pw.println("# test file");
			if (line.indexOf("%") != -1) 
				pw.println("'rule' A(x) -> B(y)");
			pw.println(line);
			pw.close();
			
			String[] args = makeCommandLineArguments(test.getAbsolutePath(), output.getAbsolutePath());
			Exception exp = null;
			try {
				Object task = job.getTask(args);
				job.runTask(task);
				exp = job.getException(task);
			} catch (Exception ex) {
				exp = ex;
			}
			if (exp == null) {
				write.println("OK: " + line);
			} else {
				write.println("Error: " + line);
				write.println("Message: " + exp.getMessage());
			}
		}
		write.close();
		ks.close();
	}
	
	private static String[] makeCommandLineArguments(String kappaFilename, String outputFilename) {
		/*
		   c << '--no-build-influence-map'
	       c << '--no-compute-qualitative-compression'
	       c << '--no-compute-quantitative-compression'
	       c << '--no-do-compute-dag-refinement-relation' unless self.use_jsim
	       c << '--no-dump-iteration-number'
	       c << '--no-dump-rule-iteration'
	       c << '--no-enumerate-complexes'
	       c << '--no-compute-local-views' unless self.use_jsim
	       c << '--contact-map' if self.use_jsim
		 */
		
		return new String[]{
				"--no-build-influence-map", 
				"--no-compute-qualitative-compression", 
				"--no-compute-quantitative-compression", 
				"--no-dump-iteration-number", 
				"--no-dump-rule-iteration", 
				"--no-enumerate-complexes", 
				"--contact-map",
				kappaFilename,
				"--xml-session-name",
				outputFilename
		};
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 3) {
			System.out.println("check <kappa file><output file><lib path>");
			System.exit(-1);
		}
		
		try {
			checkKappaLines(new File(args[0]), new File(args[1]), args[2]);
			
		} catch (Throwable th) {
			th.printStackTrace();
		}

	}

}
