package com.plectix.rulestudio.editors.kappa.scanners;

import java.util.Iterator;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

/**
 * This rule will look for all the agent names in the document.
 * 
 * 'MKP.ERK@T{99736}' MKP(s), ERK(T~p) -> MKP(s!1), ERK(T~p!1) @ 1.0
 * 
 * %init: 50 * (Grb2(SH2~u,SH3n!1,SH3c), SoS(P!1,GEF,S~u))
 * 
 * @author bbuffone
 *
 */
public class InitAgentRule extends WordRule{

	private static class WordDetector implements IWordDetector{
		public boolean isWordPart(char c) {
			return c != '('  && 
				   c != '<' && c != '@' &&
				   c != ',' && c != '~' &&
				   c != ')' && c != '*';
		}

		public boolean isWordStart(char c) {
			return Character.isLetterOrDigit(c);
		}
		
	}
	
	private static WordDetector _detector = new WordDetector();
	private StringBuffer _fBuffer= new StringBuffer();

	public InitAgentRule(IToken defaultToken) {
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
						&& (fDetector.isWordPart((char) c)));
				scanner.unread();

				//Changed to not have the substring because it doesn't
				//allow for single character agent names
				_fBuffer = new StringBuffer(_fBuffer); //.substring(1));
				String buffer= _fBuffer.toString();
				IToken token= (IToken)fWords.get(buffer);
				
				if (buffer.trim().length() != 0 && (char) c == '('){
					//This is an agent.
					if (token == null) {
						Iterator iter= fWords.keySet().iterator();
						while (iter.hasNext()) {
							String key= (String)iter.next();
							if(buffer.equalsIgnoreCase(key)) {
								token= (IToken)fWords.get(key);
								break;
							}
						}
					}

					if (token != null){
						return token;
					}
					if (fDefaultToken.isUndefined()){
						unreadBuffer(scanner);
					}
					return fDefaultToken;
				}
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
