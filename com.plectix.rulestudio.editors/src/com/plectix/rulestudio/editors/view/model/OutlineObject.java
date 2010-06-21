package com.plectix.rulestudio.editors.view.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;


/**
 * This object contains the basic logic for controlling the view of data 
 * in an outline view. Also this class is used by the validator to hold
 * the informations.
 * 
 * @author bbuffone
 *
 */
public class OutlineObject {

	public final static Image MODEL_IMAGE = ImageDescriptor.createFromFile(OutlineObject.class, 
												"model.png").createImage();
	public final static Image RULE_IMAGE = ImageDescriptor.createFromFile(OutlineObject.class, 
												"rule.png").createImage();
	public final static Image AGENT_IMAGE = ImageDescriptor.createFromFile(OutlineObject.class, 
												"agent.png").createImage();	
	public final static Image LEAF_IMAGE = ImageDescriptor.createFromFile(OutlineObject.class, 
												"leaf.gif").createImage();
	public final static Image SIM_IMAGE = ImageDescriptor.createFromFile(OutlineObject.class, 
											"simulation.png").createImage();

	private final static OutlineObject[] empty = new OutlineObject[0];
	protected OutlineObject			_parent = null;
	public String					_label = "";
	public String					_searchString = "";
	public List<OutlineObject>  	_children = new ArrayList<OutlineObject>();
	public String					_hashCode = "";
	private String					_rep = null;			// string representation
	private OutlineType				_type = null;
	private int 					_offset = 0;			// file offset for 2nd pass
	private int						_line;					// line for 2nd pass

	public OutlineObject(OutlineObject parent, String label, String searchString, OutlineType type){
		_label = label;
		_parent = parent;
		_searchString = searchString;
		_type = type;
				
		//Create the hashCode.
		_hashCode = parent != null ? parent._hashCode : "";
		_hashCode += "/"+_label;
		_hashCode += parent != null ? String.valueOf(parent._children.size()) : "";
	}
	
	public OutlineObject addChild(OutlineObject outlineObject){
		_children.add(outlineObject);
		return outlineObject;
	}
	
	public OutlineObject addChild(int index, OutlineObject outlineObject) {
		_children.add(index, outlineObject);
		return outlineObject;
	}
	
	public OutlineObject[] getChildren(){
		if (_children.size() == 0){
			return empty;
		}else{
			OutlineObject[] children = new OutlineObject[_children.size()];
			_children.toArray(children);
			return children;
		}
	}
	
	public Image getImage(){
		return _type.getImage();
	}
	
	public String getLabel(){
		return _label;
	}
	
	public String getSearch(){
		return _searchString;
	}
	
	public OutlineObject getParent(){
		return _parent;
	}
	
	public int size() {
		return _children.size();
	}
	
	public String getName() {
		return _label;
	}
	
	public void setLoc(int offset, int line) {
		_offset = offset;
		_line = line;
	}
	
	public int getOffset() {
		return _offset;
	}
	
	public int getLine() {
		return _line;
	}
	
	/**
	 * For the supplied label parameter return the child that has the 
	 * same label.
	 * 
	 * @param label
	 * @return
	 */
	public OutlineObject findChildByLabel(String label){
		Object[]			children = getChildren();
		if (children == null) return null;
		
		for (int index = 0; index < children.length; index++){
			if (((OutlineObject)children[index]).getLabel().equals(label) == true){
				return (OutlineObject)children[index];
			}
		}
		return null;
	}
	
	/**
	 * Returns true if the supplied label is already a child of the object.
	 * 
	 * @param label
	 * @return
	 */
	public boolean containsObject(String label){
		return findChildByLabel(label) != null;
	}
	
	public boolean equals(Object compare){
		if (compare instanceof OutlineObject){
			return ((OutlineObject)compare)._hashCode.equals(_hashCode);
		}
		return false;
	}
	
	/* 
	 * Use the rep so the agent completion strings are not recomputed every time.
	 */
	public String getRep() {
		return _rep;
	}
	
	public void setRep(String rep) {
		_rep = rep;
	}
	
	public AgentObject addAgent(KappaModelObject model, String agent) {
		AgentObject object = model.findAgentObject(agent);
		if (object == null) {
			object = model.addAgent(agent);
		}
		AgentObject agentObject = new AgentObject(this, agent, agent + "(", OutlineType.AGENT, object);
		addChild(agentObject);
		return agentObject;
	}

}
