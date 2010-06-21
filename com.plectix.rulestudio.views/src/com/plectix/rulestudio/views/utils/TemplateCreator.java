package com.plectix.rulestudio.views.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/*
 * This class simply can load a file and write it back out to a 
 * file to give basic template creation.
 */
public class TemplateCreator {
	
	private StringBuffer 	_htmlFileTemplate = null;
	private String			_templateLocation = null;
	
	public TemplateCreator(){
	}
	
	/**
	 * The reason that we need to load and store a contact map file is to provide parameterization
	 * of the agent name.
	 * @throws IOException
	 */
	public void loadTemplate(String templateLocation) throws IOException{
		_templateLocation = templateLocation;

		//read the data from the file.
		File 			file = new File(_templateLocation);
		FileInputStream fileInputStream = new FileInputStream(file);
		byte[]			buffer = new byte[1024];
		_htmlFileTemplate = new StringBuffer();
		while (fileInputStream.read(buffer)>0){
			_htmlFileTemplate.append(new String(buffer));
			
			//This was added because on the mac I was getting
			//a corrupted data stream.
			buffer = new byte[1024];
		}
	}
	
	/**
	 * This method simply replace the replayKey with the replaceWith parameter and 
	 * writes the contents to the outputfile.
	 * 
	 * @param replaceKey
	 * @param replaceWith
	 * @param outputFile
	 * @throws IOException
	 */
	public void replaceAndWrite(String replaceKey, String replaceWith, String outputFile) throws IOException{
		//remove the unneeded stuff everything before the <ContactMap 
		//and after the </ContactMap>
		int intPos = _htmlFileTemplate.indexOf(replaceKey);
		String content = "";
		if (intPos != -1){
			content = _htmlFileTemplate.substring(0, intPos) + replaceWith + 
					  _htmlFileTemplate.substring(intPos + replaceKey.length());
		} else {
			content = _htmlFileTemplate.toString();
		}
				
		//Write the data out to file.
		FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
		fileOutputStream.write(content.getBytes());		
		fileOutputStream.close();		
	}

}
