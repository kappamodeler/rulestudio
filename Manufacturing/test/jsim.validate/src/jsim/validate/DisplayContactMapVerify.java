package jsim.validate;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class DisplayContactMapVerify {
	
	public int runTests(String dName) {
		int count = 0;
		File dir =  new File(dName);
		
		File[] tests = dir.listFiles();
		for (File one: tests) {
			if (one.isFile() && one.getName().endsWith(".ka")) {
				System.out.println("Testing " + one.getName());
				String resName = one.getName() + ".result.xml";
				File res = new File(dir, resName);
				File out = new File(dir, one.getName() + ".xml");
				if (out.exists())
					if (!out.delete())
						System.err.println("Cannot delete " + out.getAbsolutePath());
				if (!runTest(one, out, res)) {
					System.err.println("Test failed for " + one.getName());
					++count;
				} else {
					if (!out.delete()) {
						System.err.println("Cannot delete " + out.getAbsolutePath());
					}
				}
			}
		}
		return count;
	}
	
	public boolean runTest(File kappa, File out, File result) {
		String[] args = makeCommandLineArguments(kappa.getAbsolutePath(), out.getAbsolutePath());
		try {
			com.SFT runTask = com.CLR.gSFT(args);
			runTask.run();
			Exception exception = runTask.get().getSER().getE();
			if (exception != null)
				throw exception;
			if (!result.exists())
				return false;
			
			return compareKappa(out, result);
		} catch (Throwable th) {
			// TODO Auto-generated catch block
			th.printStackTrace();
			return false;
		}
	}
	
	public static void main(String[] args) {
		if (args.length == 0) {
			System.err.println("validate <test dir>");
			System.exit(1);
		}
		DisplayContactMapVerify dc = new DisplayContactMapVerify();
		int errCount = dc.runTests(args[0]);
		System.out.println("Testing Complete");
		if (errCount == 0) {
			System.out.println("No errors found.");
			System.exit(0);
		}
		System.out.println(Integer.toString(errCount) + " error(s) found.");
		System.exit(1);
	}
	
	protected String[] makeCommandLineArguments(String kappaFilename, String outputFilename) {
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

	private boolean compareKappa(File f1, File f2) throws Exception {
		if (f1 == null || f2 == null)
			throw new NullPointerException("Cannot compare null files");
		
		String was = getContactMap(f1);
		String now = getContactMap(f2);
		
		return was.equals(now);
	}
		
	
	private String getContactMap(File file) throws Exception {
		BufferedReader br = new BufferedReader(new FileReader(file));
		StringBuffer content = new StringBuffer();
		String line = null;
		
		while ((line = br.readLine()) != null) {
			content.append(line);
			content.append("\n");
		}
		br.close();
		
		int offset = content.indexOf("<ContactMap");
		if (offset == -1)
			throw new Exception("Cannot find Contact Map");
		
		int end = content.indexOf("</ContactMap>", offset);
		
		return content.substring(offset, end + 13);
	}


}
