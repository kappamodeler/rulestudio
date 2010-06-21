package com.plectix.rulestudio.editors.kappa.scanners;

import java.util.Iterator;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordRule;

/**
 * This rule will look for all the agent names in a rule statement.
 * 
 * 'MKP.ERK@T{99736}' MKP(s), ERK(T~p) -> MKP(s!1), ERK(T~p!1) @ 1.0
 * 
 * @author bbuffone
 *
 */
public class RuleAgentRule extends WordRule{

	private static class WordDetector implements IWordDetector{
		public boolean isWordPart(char c) {
			return c != '(' && 
				   c != '<' && c != '@' &&
				   c != ',' && c != '~' &&
				   c != ')' && c != '\\';
		}

		public boolean isWordStart(char c) {
			return c == '\'' || c == ',' || c == '>' || 
				   c == '\r' || c == '\n';
		}
	}
	
	private static WordDetector _detector = new WordDetector();
	private StringBuffer _fBuffer= new StringBuffer();

	public RuleAgentRule(IToken defaultToken, boolean bInitStatement) {
		super(_detector, defaultToken);
	}

	public IToken evaluate(ICharacterScanner scanner) {
		//We need to back up to be able to detect the ' on the rule's label
		scanner.unread();
		int c = scanner.read();
		
		//We need to check for the start of the file.
		boolean bStartFile = false;
		if (c == -1){
			bStartFile = true;
		}
		if (bStartFile == true ||
			c != ICharacterScanner.EOF && 
			fDetector.isWordStart((char) c)) {
			c = scanner.read();
			if ((fColumn == UNDEFINED || (fColumn == scanner.getColumn() - 1)) &&
				fDetector.isWordPart((char) c)) {

				_fBuffer.setLength(0);
				do {
					_fBuffer.append((char) c);
					c = scanner.read();
				} while (c != ICharacterScanner.EOF 
						&& (fDetector.isWordPart((char) c)));
				scanner.unread();

				//Changed to not have the substring because it doesn't
				//allow for single character agent names
				_fBuffer = new StringBuffer(_fBuffer); //.substring(1));
				String buffer= _fBuffer.toString().trim();
				int intPos = buffer.indexOf('\\');
				if (intPos != -1){
					buffer = buffer.substring(intPos + 1);
				}
				IToken token= (IToken)fWords.get(buffer);
				
				if (buffer.length() != 0 && (char) c == '('){
					//This is an agent.
					if (token == null) {
						Iterator iter= fWords.keySet().iterator();
						while (iter.hasNext()) {
							String key= (String)iter.next();
							if(buffer.equalsIgnoreCase(key)) {
								token = (IToken)fWords.get(key);
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
		}else{
			c = scanner.read();
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
