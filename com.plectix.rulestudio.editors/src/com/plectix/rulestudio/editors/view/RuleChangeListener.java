/**
 * 
 */
package com.plectix.rulestudio.editors.view;

import com.plectix.rulestudio.editors.kappa.KappaEditor;

/**
 * Update a view based on a rule change.
 * 
 * @author bill
 *
 */
public interface RuleChangeListener {
	
	public void changeRule(KappaEditor editor, String ruleName, String ruleData);

}
