package com.plectix.rulestudio.views.reachable;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parse the data from the compression documents.
 * 
 * @author bill
 *
 */
public class ReachableParse extends DefaultHandler {
	private Reachable model;
	private static int NONE = 0;
	private static int IN_VIEW = 1;
	private static int IN_SUBVIEW = 2;
	private static int IN_SPECIES = 3;
	private int reading = NONE;

	
    public ReachableParse() {
        super();
        this.model = new Reachable();
    }
    
    public Reachable getReachable() {
    	return model;
    }
        
    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts)
            throws SAXException {
        // System.out.println("URI: " + uri + ", LN: " + localName + ", QN: " + qName);

        if (qName.equalsIgnoreCase("Reachables")) {
        	String name = atts.getValue("Name");
        	if (name != null) {
        		if (name.equals("Views")) {
        			reading = IN_VIEW;
        		} else if (name.equals("Subviews")) {
        			reading = IN_SUBVIEW;
        		} else if (name.equals("Species")) {
        			reading = IN_SPECIES;
        			model.startSpecies();
        		}
        	}
        } else if (qName.equalsIgnoreCase("Set")) {
        	if (reading == IN_VIEW) {
        		String agent = atts.getValue("Agent");
        		model.addViewAgent(agent);
        	}
        } else if (qName.equalsIgnoreCase("Tag")) {
        	if (reading == IN_SUBVIEW) {
        		String data = atts.getValue("Data");
        		model.addSubViewAgent(data);
        	}
        } else if (qName.equalsIgnoreCase("Entry")) {
        	if (reading != NONE) {
        		String data = atts.getValue("Data");
        		model.addEntry(data);
        	}
        }
    }
    
    @Override
    public void endElement(String uri, String localName,
            String qName)
     throws SAXException {
    	if (qName.equalsIgnoreCase("Reachables")) {
    		reading = NONE;
    	}
    }

}
