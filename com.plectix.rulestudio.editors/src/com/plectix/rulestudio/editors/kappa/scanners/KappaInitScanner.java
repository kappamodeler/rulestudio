package com.plectix.rulestudio.editors.kappa.scanners;

import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;

import com.plectix.rulestudio.editors.kappa.extras.ColorManager;
import com.plectix.rulestudio.editors.kappa.extras.KappaWhitespaceDetector;
import com.plectix.rulestudio.editors.kappa.extras.SyntaxColors;

public class KappaInitScanner extends RuleBasedScanner {

	/** 
	 * _lastToken is used to track the last token returned
	 * during evaluation.
	 */
	private IToken _lastToken = null;
	private IToken _agentToken = null;
	
	public KappaInitScanner(ColorManager colorManager) {
		IToken string = SyntaxColors.INIT_EXPRESSION.token(colorManager);

		IRule[] rules = new IRule[3];

		// Add a rule for single quotes
		rules[0] = new SingleLineRule("%in", "it:", string, '\\', true, true);

		_agentToken = SyntaxColors.AGENT.token(colorManager);
		rules[1] = new InitAgentRule(_agentToken);

		// Add generic whitespace rule.
		rules[2] = new WhitespaceRule(new KappaWhitespaceDetector());

		setRules(rules);
	}
	
	/*
	 * METHODS BELOW
	 * 
	 * Overriden to provide correct coloring of agent names.
	 */
	
	/*
	 * @see ITokenScanner#getTokenOffset()
	 * 
	 * if this is agent token that we are getting the offset
	 * for, increase the number by one to not get the "(" or ","
	 * colored. 
	 */
	/*
	public int getTokenOffset() {
		if (_lastToken == _agentToken){
			return fTokenOffset + 1;
		}else{
			return fTokenOffset;
		}
	}
*/
	/*
	 * @see ITokenScanner#nextToken()
	 * 
	 * We override the method to track the token returned.
	 */
	public IToken nextToken() {

		fTokenOffset= fOffset;
		fColumn= UNDEFINED;

		_lastToken = null;
		if (fRules != null) {
			for (int i= 0; i < fRules.length; i++) {
				_lastToken = (fRules[i].evaluate(this));
				if (!_lastToken.isUndefined())
					return _lastToken;
			}
		}

		if (read() == EOF)
			return Token.EOF;
		return fDefaultReturnToken;
	}

}
