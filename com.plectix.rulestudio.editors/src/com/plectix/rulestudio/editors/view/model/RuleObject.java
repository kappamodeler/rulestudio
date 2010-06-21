package com.plectix.rulestudio.editors.view.model;

public class RuleObject extends OutlineObject {

	public final static String LEFT_SIDE_STRING = "Left";
	public final static String RIGHT_SIDE_STRING = "Right";
	
	private String _ruleContent = "";
	private OutlineObject _direction = null;
	private OutlineObject _left = null;
	private OutlineObject _right = null;
	private String _ruleLabel = null;
	
	public RuleObject(OutlineObject parent, String name, String searchString, String label, OutlineType type) {
		super(parent, name, searchString, type);
		
		_ruleLabel = label;
		
		_left = new OutlineObject(this, LEFT_SIDE_STRING, "", OutlineType.RULE);
		addChild(_left);
		
		_direction = new OutlineObject(this, "->", "", OutlineType.ARROW);
		addChild(_direction);
		
		_right = new OutlineObject(this, RIGHT_SIDE_STRING, "", OutlineType.RULE);
		addChild(_right);
	}

	public void setRuleContent(String newValue){
		_ruleContent = newValue;
	}
	
	public OutlineObject getLeft() {
		return _left;
	}
	
	public OutlineObject getRight() {
		return _right;
	}
	
	public void trim() {
		if (_left.size() == 0)
			_children.remove(_left);
		if (_right.size() == 0)
			_children.remove(_right);
	}

	public String getRuleContent(){
		return _ruleContent;
	}
	
	public String getRuleName() {
		return getLabel();
	}
	
	public String getRuleLabel() {
		return _ruleLabel;
	}
	
	public void setDirection(String newValue){
		_direction._label = newValue;
	}

	public OutlineObject getDirection() {
		return _direction;
	}
	
	
}
