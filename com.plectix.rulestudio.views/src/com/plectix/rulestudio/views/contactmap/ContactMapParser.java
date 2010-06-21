package com.plectix.rulestudio.views.contactmap;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Transform the xml file
 * 
 * @author bill
 *
 */
public class ContactMapParser extends DefaultHandler {
    private PrintWriter out = null;
    private boolean inRuleSet = false;
    private boolean inContactMap = false;
    private ArrayList<ArrayList<String>> ruleList = null;
    private String agent = null;

    public ContactMapParser(File outFile) throws Exception {
        super();
        out = new PrintWriter(new FileWriter(outFile));
        ruleList = new ArrayList<ArrayList<String>>();
    }
    
    public String getFirstAgent() {
    	return agent;
    }
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts)
            throws SAXException {
        // System.out.println("URI: " + uri + ", LN: " + localName + ", QN: " + qName);
    	if (inRuleSet) {
    		ArrayList<String> atList = new ArrayList<String>();
    		for (int i = 0; i < atts.getLength(); ++i) {
    			String name = atts.getQName(i);
    			if (name.equalsIgnoreCase("id"))
    				name = "ID";
    			String entry = name + "=\"" + atts.getValue(i) + "\"";
    			atList.add(entry);
    		}
    		ruleList.add(atList);
    	} else if (inContactMap) {
    		printNode(qName, atts);
    		if (qName.equals("Bond")) {			// wrap bonds
    			out.println(" <RuleSet Name=\"Mod\">");
    		} else if (agent == null && qName.equals("Agent")) {
    			agent = atts.getValue("Name");
    		}
    	} else if (qName.equalsIgnoreCase("RuleSet")) {
        	String name = atts.getValue("Name");
        	if ("ContactMap".equals(name)) {
        		inRuleSet = true;
        	}
        } else if (qName.equals("ContactMap")) {
        	printNode("ContactMap Ready=\"true\"", atts);
        	out.println("<RuleSet Name=\"Original\">");
        	for (ArrayList<String> atList: ruleList) {
        		out.print("<Rule");
        		for (String entry: atList){
        			out.print(" ");
        			out.print(entry);
        		}
        		out.println(">");
        		out.println("</Rule>");
        	}
        	out.println("</RuleSet>");
        	inContactMap = true;
        	}
        }

	private void printNode(String qName, Attributes atts) {
		out.print("<");
		out.print(qName);
		for (int i = 0; i < atts.getLength(); ++i) {
			out.print(" ");
			String name = atts.getQName(i);
			if (name.equalsIgnoreCase("id"))
				name = "ID";
			out.print(name);
			out.print("=\"");
			out.print(atts.getValue(i));
			out.print('"');
		}
		out.println(">");
		return;
    }
	
	@Override
	public void endElement(String uri, String localName, String qName)
     throws SAXException {
		if (inRuleSet && qName.equals("RuleSet")) {
			inRuleSet = false;
		} else if (inContactMap) {
			if ("Bond".equals(qName)) {
				out.println("</RuleSet>");
			}
			out.print("</");
			out.print(qName);
			out.println(">");
			
			if ("ContactMap".equals(qName)) {
				inContactMap = false;
			}
		}
	}
	
	@Override
	public void endDocument() {
		out.close();
	}


}
