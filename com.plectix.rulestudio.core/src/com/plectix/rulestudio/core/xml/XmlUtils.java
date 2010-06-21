package com.plectix.rulestudio.core.xml;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;

/**
 * This is a simple class that encapsulates XML parsing and XPath
 * 
 * @author bbuffone
 *
 */
public class XmlUtils {

    private static XPath _xpath = XPathFactory.newInstance().newXPath(); 

    /**
     * Parse the supplied string into an xml document.
     * 
     * @param content
     * @return
     */
	public static Document parseString(String content){			
		try {			
			DocumentBuilderFactory 			documentBuilderFactory = DocumentBuilderFactory.newInstance();
			documentBuilderFactory.setNamespaceAware(true);
			DocumentBuilder 				documentBuilder = documentBuilderFactory.newDocumentBuilder(); 
			
			//parse the stream into a document
			return documentBuilder.parse(new ByteArrayInputStream(content.getBytes())); 
		} catch ( Exception ex ) {
			ex.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Use the supplied XPath statement find the data.
	 * 
	 */
	public static String findString(Document document, String xpath){
		try {
			return (String)_xpath.evaluate(xpath, document, XPathConstants.STRING);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
}
