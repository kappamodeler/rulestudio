package com.plectix.rulestudio.views.influencemap;

import java.util.HashMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parse the data from the property list.  Since I did not have
 * a DTD I had to dump data to understand the structure of the file.
 * 
 * @author bill
 *
 */
public class MapParse extends DefaultHandler {
    private HashMap<String, String> list = null;
    private String first = null;

    public MapParse(HashMap<String, String> list) {
        super();
        this.list = list;
    }
    
    public String getFirst() {
    	return first;
    }
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts)
            throws SAXException {
        // System.out.println("URI: " + uri + ", LN: " + localName + ", QN: " + qName);
        if (qName.equalsIgnoreCase("Rule")) {
        	String name = atts.getValue("Name");
        	String id = atts.getValue("Id");
        	if (name != null && id != null) {
        		list.put(name, id);
        		if (first == null)
        			first = name;
        	}
        }
    }


}
