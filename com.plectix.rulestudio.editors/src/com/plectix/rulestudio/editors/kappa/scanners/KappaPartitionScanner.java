package com.plectix.rulestudio.editors.kappa.scanners;

import org.eclipse.jface.text.rules.*;

public class KappaPartitionScanner extends RuleBasedPartitionScanner {
	public final static String KAPPA_COMMENT = "__kappa_comment";
	public final static String LABEL = "__label_tag";
	public final static String INIT_EXPRESSION = "__init_expression";
	public final static String OBS_EXPRESSION = "__obs_expression";
	public final static String STORY_EXPRESSION = "__story_expression";
	public final static String MODIFY_EXPRESSION = "__modify_expression";

	public KappaPartitionScanner() {

		IToken kappaComment = new Token(KAPPA_COMMENT);
		IToken kappaLabel = new Token(LABEL);
		IToken kappaInit = new Token(INIT_EXPRESSION);
		IToken kappaObs = new Token(OBS_EXPRESSION);
		IToken kappaModify = new Token(MODIFY_EXPRESSION);
		IToken kappaStory = new Token(STORY_EXPRESSION);

		IPredicateRule[] rules = new IPredicateRule[6];

		rules[0] = new SingleLineRule("'", "\n", kappaLabel, '\\', true, true);
		rules[1] = new SingleLineRule("#", "\n", kappaComment, '\\', true, true);
		rules[2] = new SingleLineRule("%init:", "\n", kappaInit, '\\', true, true);
		rules[3] = new SingleLineRule("%obs:", "\n", kappaObs, '\\', true, true);
		rules[4] = new SingleLineRule("%mod:", "\n", kappaModify, '\\', true, true);
		rules[5] = new SingleLineRule("%story:", "\n", kappaStory, '\\', true, true);

		setPredicateRules(rules);
	}
}
