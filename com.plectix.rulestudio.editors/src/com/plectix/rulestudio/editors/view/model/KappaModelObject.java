package com.plectix.rulestudio.editors.view.model;

import java.util.ArrayList;

public class KappaModelObject extends OutlineObject {
	
	public final static int AGENTS_INDEX = 0;
	public final static int RULES_INDEX = 1;
	public final static int INIT_INDEX = 2;
	public final static int OBS_INDEX = 3;
	public final static int MOD_INDEX = 4;
	public final static int STORY_INDEX = 5;
	
	public final static String[] order = {
		"Agents",
		"Rules",
		"Initial Conditions",
		"Observables",
		"Perturbations",
		"Stories",
	};

	public KappaModelObject() {
		super(null, "Kappa Model", "", OutlineType.MODEL);
	}
	
	private OutlineObject getKappaSection(int index, boolean create) {
		OutlineObject what = findChildByLabel(order[index]);
		if (what == null && create) {
			// insert using the order specified
			int offset = index;
			OutlineObject[] lists = getChildren();
			if (lists.length == 0) {
				offset = 0;
			} else {
				for (int i = 0, lin = 0; i < order.length && i < index; ++i) {
					if (lists[lin]._label == order[i]) {
						if (++lin >= lists.length) {
							offset = lin;
							break;
						}
					} else {
						--offset;
					}
				}
			}
			what = addChild(offset, new OutlineObject(this, order[index], "",
					OutlineType.MODEL));
		}
		return what;
	}

	/*
	 * LABEL METHODS
	 */
	public boolean containsObject(String label){
		OutlineObject object = findRuleObject(label);
		return object != null;
	}

	public OutlineObject findRuleObject(String label){
		OutlineObject object = getKappaSection(RULES_INDEX, false);
		if (object != null) 
			object = object.findChildByLabel(label);
		return object;
	}
	
	public RuleObject addLabelRule(String label, int fileOffset, int line){
		OutlineObject parent = getKappaSection(RULES_INDEX, true);
		String labelString = "'" + label + "'";
		RuleObject object = new RuleObject(parent, label, labelString, labelString, OutlineType.RULE);
		parent.addChild(object);
		object.setLoc(fileOffset, line);
		return object;
	}
	
	public RuleObject addRule(String label, int fileOffset, int line){
		OutlineObject parent = getKappaSection(RULES_INDEX, true);
		RuleObject object = new RuleObject(parent, removeWs(label), label, null, OutlineType.RULE);
		parent.addChild(object);
		object.setLoc(fileOffset, line);
		return object;
	}
	
	private String removeWs(String line) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < line.length(); ++i) {
			if (!Character.isWhitespace(line.charAt(i))) {
				char ch = line.charAt(i);
				if (ch == '#')
					break;
				else if (ch == '\\') {
					int more = 1;
					if (i+more < line.length() && line.charAt(i+1) == '\r') {
						++more;
					}
					if (i+more < line.length() && line.charAt(i+more) == '\n') {
						i += more;
						continue;
					}
				}
				buf.append(ch);
			}
		}
		return buf.toString();
	}

	public OutlineObject getRules() {
		OutlineObject ret = getKappaSection(RULES_INDEX, false);
		return ret;
	}
	
	public ArrayList<String> getLabelList() {
		OutlineObject list = getKappaSection(RULES_INDEX, false);
		ArrayList<String> ret = new ArrayList<String>();
		if (list != null) {
			for (OutlineObject obj: list.getChildren()) {
				if (obj instanceof RuleObject) {
					String label = ((RuleObject)obj).getRuleLabel();
					if (label != null && label.length() > 0) 
						ret.add(label);
				}
			}
		}
		return ret;
	}

	/*
	 * AGENT METHODS
	 */
	public boolean containsAgent(String label){
		AgentObject object = findAgentObject(label);
		return object != null;
	}

	public AgentObject findAgentObject(String label){
		OutlineObject object = getKappaSection(AGENTS_INDEX, false);
		if (object != null) 
			object = object.findChildByLabel(label);
		return (AgentObject)object;
	}
	
	public AgentObject addAgent(String agent){
		OutlineObject parent = getKappaSection(AGENTS_INDEX, true);
		AgentObject object = new AgentObject(parent, agent, agent + "(", OutlineType.AGENT, null);
		parent.addChild(object);
		return object;
	}

	// Initial Conditions
	
	public OutlineObject addInit(String init, int offset, int line) {
		OutlineObject parent = getKappaSection(INIT_INDEX, true);
		OutlineObject object = new OutlineObject(parent, removeWs(init), init, OutlineType.INIT);
		parent.addChild(object);
		object.setLoc(offset, line);
		return object;
	}
	
	public OutlineObject getInits() {
		OutlineObject ret = getKappaSection(INIT_INDEX, false);
		return ret;
	}


	// Observable statements
	
	public OutlineObject addObs(String init, OutlineType type, int offset, int line) {
		OutlineObject parent = getKappaSection(OBS_INDEX, true);
		OutlineObject object = new OutlineObject(parent, removeWs(init), init, type);
		parent.addChild(object);
		object.setLoc(offset, line);
		return object;
	}

	public OutlineObject getObs() {
		OutlineObject ret = getKappaSection(OBS_INDEX, false);
		return ret;
	}


	// Perturbations
	
	public OutlineObject addMod(String init, int offset, int line) {
		OutlineObject parent = getKappaSection(MOD_INDEX, true);
		OutlineObject object = new OutlineObject(parent, removeWs(init), init, OutlineType.MOD);
		parent.addChild(object);
		object.setLoc(offset, line);
		return object;
	}
	
	public OutlineObject getMods() {
		OutlineObject ret = getKappaSection(MOD_INDEX, false);
		return ret;
	}

	
	// Stories
	
	public OutlineObject addStory(String init, int offset, int line) {
		OutlineObject parent = getKappaSection(STORY_INDEX, true);
		OutlineObject object = new OutlineObject(parent, removeWs(init), init, OutlineType.STORY);
		parent.addChild(object);
		object.setLoc(offset, line);
		return object;
	}
	
	public OutlineObject getStories() {
		OutlineObject ret = getKappaSection(STORY_INDEX, false);
		return ret;
	}


	
	

}
