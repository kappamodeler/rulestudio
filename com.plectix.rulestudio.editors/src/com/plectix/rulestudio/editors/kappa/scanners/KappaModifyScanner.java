package com.plectix.rulestudio.editors.kappa.scanners;

import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.WhitespaceRule;

import com.plectix.rulestudio.editors.kappa.extras.ColorManager;
import com.plectix.rulestudio.editors.kappa.extras.KappaWhitespaceDetector;
import com.plectix.rulestudio.editors.kappa.extras.SyntaxColors;

public class KappaModifyScanner extends RuleBasedScanner {

	public KappaModifyScanner(ColorManager colorManager) {
		IToken label = SyntaxColors.LABEL_EXPRESSION.token(colorManager);
		IToken processor = SyntaxColors.MODIFY_EXPRESSION.token(colorManager);
		IToken doToken = SyntaxColors.DO_WORD.token(colorManager);
		IRule[] rules = new IRule[5];

		// Add a rule for single quotes
		rules[0] = new LabelRule(label);
		rules[1] = new SingleLineRule("%m", "od:", processor, '\\', true, true);
		rules[2] = new SingleLineRule(" d", "o ", doToken, '\\', true, true);
		rules[3] = new SingleLineRule(":", "=", doToken, '\\', true, true);	
		
		// Add generic whitespace rule.
		rules[4] = new WhitespaceRule(new KappaWhitespaceDetector());

		setRules(rules);
	}
}
