/**
 * 
 */
package com.plectix.rulestudio.views.compression;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class contains the data needed to analyze compresssion, independent of 
 * the type of compression.
 * 
 * @author bill
 *
 */
public class CompressionData {
    private HashMap<String, CompressionRule> compressed = null;
    private HashMap<String, String> assoc = null;
    private HashMap<String, ArrayList<String>> back = null;
    
    public CompressionData() {
    	compressed = new HashMap<String, CompressionRule>();
    	assoc = new HashMap<String, String>();
    	back = new HashMap<String, ArrayList<String>>();
    }
    
    public void addRule(String id, CompressionRule rule) {
    	compressed.put(id, rule);
    }
    
    public void addAssoc(String from, String to) {
    	assoc.put(from, to);
    	ArrayList<String> same = back.get(to);
    	if (same == null) {
    		same = new ArrayList<String>();
    		back.put(to, same);
    	}
    	same.add(from);
    }
 
	public ArrayList<CompressionRule> getUnreachable(ArrayList<CompressionRule> orig) {
		ArrayList<CompressionRule> result = new ArrayList<CompressionRule>();
		for (CompressionRule rule: orig) {
			String compId = assoc.get(rule.getId());
			CompressionRule comp = compressed.get(compId);
			if (comp != null) {
				String data = comp.getData();
				if (data.startsWith("Cannot be applied")) {
					result.add(rule);
				}
			}
		}
		return result;
	}
	
	public ArrayList<ArrayList<CompressionRule>> getImprovable(ArrayList<CompressionRule> orig) {
		ArrayList<ArrayList<CompressionRule>> result = new ArrayList<ArrayList<CompressionRule>>();
		CompressionRule[] working = orig.toArray(new CompressionRule[orig.size()]);
		for (int i = 0; i < working.length; ++i) {
			CompressionRule rule = working[i];
			if (rule != null) {
				String compId = assoc.get(rule.getId());
				CompressionRule comp = compressed.get(compId);
				ArrayList<String> extra = back.get(comp.getId());
				if (extra == null) {
					System.err.println("Missing reverse map for " + comp.getName());
					continue;
				}
				if (comp.getData().startsWith("Cannot be applied"))
					continue;				// ignore
				if (extra.size() > 1 || !comp.getData().equals(rule.getData())) {
					ArrayList<CompressionRule> clause = new ArrayList<CompressionRule>();
					clause.add(comp);
					for (String id: extra) {
						if (id.equals(rule.getId())) {
							clause.add(rule);
						} else {
							CompressionRule more = removeRule(working, id, i);
							clause.add(more);

						}
					}
					result.add(clause);
				}

			}
		}
	
		return result;
	}

	private CompressionRule removeRule(CompressionRule[] working,
			String id, int index) {
		for (; index < working.length; ++index) {
			CompressionRule rule = working[index];
			if (rule == null) {
				continue;				// already done
			} else if (id.equals(rule.getId())) {
				working[index] = null;
				return rule;
			}
		}
		throw new RuntimeException("Missing rule " + id + " in rule list.");
	}

}
