package com.plectix.rulestudio.editors.kappa.scanners;

import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.WhitespaceRule;

import com.plectix.rulestudio.editors.kappa.extras.ColorManager;
import com.plectix.rulestudio.editors.kappa.extras.KappaWhitespaceDetector;
import com.plectix.rulestudio.editors.kappa.extras.SyntaxColors;

public class KappaStoryScanner extends RuleBasedScanner {

	public KappaStoryScanner(ColorManager colorManager) {
		IToken label = SyntaxColors.LABEL_EXPRESSION.token(colorManager);
		IToken processor = SyntaxColors.STORY_EXPRESSION.token(colorManager);

		IRule[] rules = new IRule[3];

		// Add a rule for single quotes
		rules[0] = new LabelRule(label);
		
		// Add a rule for single quotes
		rules[1] = new SingleLineRule("%sto", "ry:", processor, '\\', true, true);
		
		// Add generic whitespace rule.
		rules[2] = new WhitespaceRule(new KappaWhitespaceDetector());

		setRules(rules);
	}
}
