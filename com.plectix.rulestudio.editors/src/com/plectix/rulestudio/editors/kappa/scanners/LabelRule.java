package com.plectix.rulestudio.editors.kappa.scanners;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

/**
 * This rule will look for labels.
 * 
 * 'MKP.ERK@T{99736}'
 * 
 */
public class LabelRule extends WordRule{

	private static class WordDetector implements IWordDetector{
		public boolean isWordPart(char c) {
			return true;
		}

		public boolean isWordStart(char c) {
			return c == '\'';
		}
		
	}
	
	private static WordDetector _detector = new WordDetector();
	private StringBuffer _fBuffer= new StringBuffer();

	public LabelRule(IToken defaultToken) {
		super(_detector, defaultToken);
	}

	public IToken evaluate(ICharacterScanner scanner) {
		int c= scanner.read();
		if (c != ICharacterScanner.EOF && fDetector.isWordStart((char) c)) {
			if (fColumn == UNDEFINED || (fColumn == scanner.getColumn() - 1)) {

				_fBuffer.setLength(0);
				do {
					_fBuffer.append((char) c);
					c= scanner.read();
				} while (c != ICharacterScanner.EOF 
						&& fDetector.isWordPart((char) c) && (char)c != '\'');
				
				if (c != '\'') {
					unreadBuffer(scanner);
					return Token.UNDEFINED;
				}

				return fDefaultToken;
			}
		}

		scanner.unread();
		return Token.UNDEFINED;
	}
	
	/**
	 * Returns the characters in the buffer to the scanner.
	 *
	 * @param scanner the scanner to be used
	 */
	protected void unreadBuffer(ICharacterScanner scanner) {
		for (int i= _fBuffer.length() - 1; i >= 0; i--){
			scanner.unread();
		}
	}

}
