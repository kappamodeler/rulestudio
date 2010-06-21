package com.plectix.rulestudio.editors.kappa.scanners;

import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.WhitespaceRule;

import com.plectix.rulestudio.editors.kappa.extras.ColorManager;
import com.plectix.rulestudio.editors.kappa.extras.KappaWhitespaceDetector;
import com.plectix.rulestudio.editors.kappa.extras.SyntaxColors;

public class KappaObsScanner extends RuleBasedScanner {
	
	IToken labelTok;
	IToken obsTok;
	IToken ruleToken;

	public KappaObsScanner(ColorManager colorManager) {
		IToken label = SyntaxColors.LABEL_EXPRESSION.token(colorManager);
		IToken processor = SyntaxColors.OBS_EXPRESSION.token(colorManager);
		labelTok = label;
		obsTok = processor;
		ruleToken = SyntaxColors.AGENT.token(colorManager);
		IRule[] rules = new IRule[4];

		// Add a rule for single quotes
		rules[0] = new SingleLineRule("%ob", "s:", processor, '\\', true, true);
		rules[1] = new LabelRule(label);
		rules[2] = new InitAgentRule(ruleToken);

		// Add generic whitespace rule.
		rules[3] = new WhitespaceRule(new KappaWhitespaceDetector());

		setRules(rules);
	}
	
}
