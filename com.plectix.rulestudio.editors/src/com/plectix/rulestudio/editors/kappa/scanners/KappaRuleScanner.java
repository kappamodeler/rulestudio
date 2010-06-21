package com.plectix.rulestudio.editors.kappa.scanners;

import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;

import com.plectix.rulestudio.editors.kappa.extras.ColorManager;
import com.plectix.rulestudio.editors.kappa.extras.KappaWhitespaceDetector;
import com.plectix.rulestudio.editors.kappa.extras.SyntaxColors;

public class KappaRuleScanner extends RuleBasedScanner {

	/**
	 * Class helps find the at symbol in the rule.
	 * 
	 * @author bbuffone
	 *
	 */
	private static class AtDetector implements IWordDetector{
		public boolean isWordPart(char c) {
			return false;
		}

		public boolean isWordStart(char c) {
			return c == '@';
		}
	}
	
	private static class RightDetector implements IWordDetector{
		public boolean isWordPart(char c) {
			return c == '>';
		}

		public boolean isWordStart(char c) {
			return c == '-';
		}
	}
	
	
	public KappaRuleScanner(ColorManager colorManager) {		
		IToken label = SyntaxColors.LABEL_EXPRESSION.token(colorManager);

		IRule[] rules = new IRule[6];

		// Add a rule for single quotes
		rules[0] = new LabelRule(label);
		rules[1] = new RuleAgentRule(SyntaxColors.AGENT.token(colorManager), false);
		rules[2] = new WordRule(new RightDetector(), Token.UNDEFINED);
		rules[3] = new SingleLineRule("<-", ">", SyntaxColors.DOUBLE_ARROW.token(colorManager), '\\', false, false);
		rules[4] = new WordRule(new AtDetector(), SyntaxColors.AT_SYMBOL.token(colorManager));
		
		//Need to add right arrow as a word
		((WordRule)rules[2]).addWord("->", SyntaxColors.RIGHT_ARROW.token(colorManager));
		

		// Add generic whitespace rule.
		rules[5] = new WhitespaceRule(new KappaWhitespaceDetector());

		setRules(rules);
	}

}
