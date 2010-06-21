/**
 * 
 */
package encrypt.build;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 
 * Encrypt and decrypt zip files.
 * 
 * @author bill
 *
 */
public class EncryptedClassLoader extends ClassLoader{
	private ZipFile zip;
	
	public EncryptedClassLoader(File file) throws Exception {
		super();
		zip = new ZipFile(file);
	}
	
	public EncryptedClassLoader(File file, ClassLoader parent) throws Exception {
		super(parent);
		zip = new ZipFile(file);
	}
		
	public Class<?> findClass(String name) throws ClassNotFoundException {
		String eName = name.replace('.', '/') + ".class";
		Class<?> what = null;
		try {
			byte[] from = getEntry(eName);
			if (from == null)
				return super.findClass(name);
			what = defineClass(name, from, 0, from.length);
		} catch (Exception e) {
			ClassNotFoundException cnf = new ClassNotFoundException("Cannot find class entry for " + eName, e);
			throw cnf;
		}
		return what;
	}

	public byte[] getEntry(String eName)
			throws ClassNotFoundException, IOException, Exception {
		ZipEntry ent = zip.getEntry(eName);
		if (ent == null) {
			return null;
		}
		InputStream in = zip.getInputStream(ent);
		ByteArrayOutputStream bot = new ByteArrayOutputStream((int) ent.getSize());
		FileEncryption.decrypt(in, bot);
		byte[] from = bot.toByteArray();
		return from;
	}

}
