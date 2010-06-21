package encrypt.build;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class EncryptJarFile {
	
	public EncryptJarFile() {
		
	}
	
	public static void buildJar(File in, File out) {
		try {
			ZipInputStream zipIn = new ZipInputStream(new FileInputStream(in));
			ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(out));
			zipOut.setMethod(ZipOutputStream.STORED);
			ZipEntry entry = null;
			while ((entry = zipIn.getNextEntry()) != null) {
				zipOut.putNextEntry(entry);
				if (entry.getName().endsWith(".class"))
					FileEncryption.encrypt(zipIn, zipOut);
				else {
					byte[] buf = new byte[512];
					int len = 0;
					do {
						len = zipIn.read(buf);
						if (len > 0)
							zipOut.write(buf, 0, len);
					} while (len > 0);
				}
				zipIn.closeEntry();
				zipOut.closeEntry();
			}
			zipIn.close();
			zipOut.close();		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public final static void main(String[] args) {
		if (args.length != 2) {
			System.err.println("Run from to");
			System.exit(1);
		}
		
		File in = new File(args[0]);
		if (!in.exists()) {
			System.err.println("Input file " + args[0] + " is missing.");
			System.exit(1);
		}
		
		File out = new File(args[1]);
		if (out.exists()) {
			if (!out.delete()) {
				System.err.println("Cannot delete " + args[1]);
				System.exit(1);
			}
		}
		
		System.out.println("Encrypting " + in.getAbsolutePath() + " to "
				+ out.getAbsolutePath());
		try {
			EncryptJarFile.buildJar(in, out);
		} catch (Throwable th) {
			System.err.println("Error encrypting jar file.");
			System.exit(1);
		}
		
		System.out.println("Successfully completed");
		System.exit(0);			
	}

}
