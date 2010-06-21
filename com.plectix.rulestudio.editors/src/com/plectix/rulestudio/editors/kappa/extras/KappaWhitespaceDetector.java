package com.plectix.rulestudio.editors.kappa.extras;

import org.eclipse.jface.text.rules.IWhitespaceDetector;

public class KappaWhitespaceDetector implements IWhitespaceDetector {

	public boolean isWhitespace(char c) {
		return (c == ' ' || c == '\t' || c == '\n' || c == '\r' || c == '\\');
	}
}
