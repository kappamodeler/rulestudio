package com.plectix.rulestudio.views.compression;

import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parse the data from the compression documents.
 * 
 * @author bill
 *
 */
public class CompressionParse extends DefaultHandler {
    private ArrayList<CompressionRule> orig = null;
    private CompressionData quantitative = null;
    private CompressionData qualitative = null;
    private CompressionData activeData = null;
    private boolean active = false;

    public CompressionParse() {
        super();
    }
    
    public ArrayList<CompressionRule> getList() {
    	return orig;
    }
    
    public CompressionData getQualitative() {
    	return qualitative;
    }
    
    public CompressionData getQuantitative() {
    	return quantitative;
    }
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts)
            throws SAXException {
        // System.out.println("URI: " + uri + ", LN: " + localName + ", QN: " + qName);
        if (qName.equalsIgnoreCase("RuleSet")) {
        	String name = atts.getValue("Name");
        	if (name != null) {
        		if (name.equals("Original")) {
        			if (orig == null) {
        				orig = new ArrayList<CompressionRule>();
        				active = true;
        			}
        		} else if (name.equalsIgnoreCase("Qualitative compression")) {
        			qualitative = new CompressionData();
        			activeData = qualitative;
        			active = true;
        		} else if (name.equalsIgnoreCase("Quantitative compression")) {
        			quantitative = new CompressionData();
        			activeData = quantitative;
        			active = true;
        		}
        	}
        } else if (active && qName.equalsIgnoreCase("Rule")) {
        	String name = atts.getValue("Name");
        	String id = atts.getValue("Id");
        	String rate = atts.getValue("ForwardRate");
        	String data = atts.getValue("Data");
        	CompressionRule rule = new CompressionRule(id, name, data, rate);
        	if (activeData != null) {
        		activeData.addRule(id, rule);
        	} else if (orig != null) {
        		orig.add(rule);
        	}
        } else if (active && orig != null && qName.equalsIgnoreCase("Association")) {
        	String from = atts.getValue("FromRule");
        	String to = atts.getValue("ToRule");
        	activeData.addAssoc(from, to);
        }
    }
    
    @Override
    public void endElement(String uri, String localName,
            String qName)
     throws SAXException {
    	if (qName.equalsIgnoreCase("RuleSet")) {
    		active = false;
    		activeData = null;
    	}
    }

}
