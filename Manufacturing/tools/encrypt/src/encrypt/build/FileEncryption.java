/*
 * FileEncryption.java
 * 
 * Created on Sep 7, 2007, 10:03:47 AM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package encrypt.build;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.Provider;
import java.security.Security;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author justin.schumacher
 */
public class FileEncryption {
    private static final byte[] keyBytes = new byte[] {-64,-57,-57,76,114,-108,8,-17,-113,-50,-3,83,-115,-89,-89,-8};

    private static final SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
    
    private FileEncryption() {
    }

    public static void encrypt(File in, File out) throws Exception {
        FileOutputStream fout = new FileOutputStream(out);
        FileInputStream fin = new FileInputStream(in);
        try {
            encrypt(fin, fout);
        } finally {
            fin.close();
            fout.close();
        }
    }
    
    public static void encrypt(InputStream in, OutputStream out) throws Exception {
        Cipher cipher = createCipher();
        cipher.init(Cipher.ENCRYPT_MODE, key);
        processData(in, out, cipher);
    }
    
    public static byte[] encrypt(byte[] in) throws Exception {
    	Cipher cipher = createCipher();
    	cipher.init(Cipher.ENCRYPT_MODE, key);
    	return cipher.doFinal(in);
    }
    
    public static byte[] decrypt(byte[] in) throws Exception {
    	Cipher cipher = createCipher();
    	cipher.init(Cipher.DECRYPT_MODE, key);
    	return cipher.doFinal(in);
    }
    
    public static void decrypt(File in, File out) throws Exception {
        FileOutputStream fout = new FileOutputStream(out);
        FileInputStream fin = new FileInputStream(in);
        try {
            decrypt(fin, fout);
        } finally {
            fin.close();
            fout.close();
        }
    }
    
    public static void decrypt(InputStream in, OutputStream out) throws Exception {
        Cipher cipher = createCipher();
        cipher.init(Cipher.DECRYPT_MODE, key);
        processData(in, out, cipher);
    }
    
    private static Cipher createCipher() throws Exception {
        /*KeyGenerator kgen = KeyGenerator.getInstance("AES");
        kgen.init(128); // 192 and 256 bits may not be available
        SecretKey skey = kgen.generateKey();
        byte[] raw = skey.getEncoded();
        System.out.print("{");
        for (byte b : raw) {
            System.out.print(b + ",");
        }
        System.out.print("}\n");*/
        Provider p = Security.getProvider("SunJCE");
        return Cipher.getInstance("AES", p);
    }
    
    private static void processData(InputStream in, OutputStream out, Cipher cipher) throws Exception {
        byte[] buf = new byte[1024];
        int numRead=0;
        while((numRead=in.read(buf)) != -1){
            out.write(cipher.update(buf, 0, numRead));
        }
        out.write(cipher.doFinal());
    }
}
