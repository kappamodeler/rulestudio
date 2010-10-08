package com.plectix.rulestudio.editors.builders;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.plectix.rulestudio.core.usagedata.Action;
import com.plectix.rulestudio.core.usagedata.UsageDataCollector;
import com.plectix.rulestudio.editors.Activator;
import com.plectix.rulestudio.editors.view.model.AgentObject;
import com.plectix.rulestudio.editors.view.model.KappaModelObject;
import com.plectix.rulestudio.editors.view.model.OutlineObject;
import com.plectix.rulestudio.editors.view.model.OutlineType;
import com.plectix.rulestudio.editors.view.model.RuleObject;
import com.plectix.rulestudio.editors.view.model.SiteObject;

/**
 * This is the main class that performs the Kappa syntax validation
 * 
 * @author bbuffone
 *
 */
public class KappaSyntaxParser {
	
	private KappaModelObject	_kappaModel = new KappaModelObject();
	
	private int 	_characterOffset = 0;
	private int 	_lineCount = 0;
	private boolean _bReportErrors = true;
	private IResource 	_fileBeingValidated = null;
	private CheckSemantics check;
	private boolean _doSemantics = true;
	
	//Contains all the named item defined in the obs statements
	private Set<String> _obsDefines = new HashSet<String>();
	
	/*
	 * CONSTRUCTOR
	 */
	public KappaSyntaxParser(boolean bReportErrors){
		_bReportErrors = bReportErrors;
		check = new CheckSemantics(this);
	}

	public KappaModelObject getKappaModel(){
		return _kappaModel;
	}
	
	/**
	 * This method is called by the KappaBuilder when a file changes has been detected.
	 * 
	 * @param file
	 * @param markerType
	 */
	public void validateFile(IFile file){
		InputStream inputStream = null;
		_fileBeingValidated = file;
		
		try {
			inputStream = file.getContents(true);
			CharacterSource src = new StreamCharSource(inputStream);
			validateCharacterStream(src, 0, 0);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (CoreException e) {
			e.printStackTrace();
		}finally{
			try {
				if (inputStream != null){
					inputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * This method is called by the KappaBuilder when a file changes has been detected.
	 * 
	 * @param file
	 * @param markerType
	 */
	public void validateInputStream(InputStream inputStream){
		CharacterSource src = new StreamCharSource(inputStream);
		try {
			validateCharacterStream(src, 0, 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return;
	}
	
	/**
	 * Called by the outline view when the open file menu opens
	 * is used.
	 * 
	 * @param uri
	 */
	public void validateURI(URI uri){
		InputStream inputStream = null;

		try {
			inputStream = uri.toURL().openStream();
			CharacterSource src = new StreamCharSource(inputStream);
			validateCharacterStream(src, 0, 0);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			try {
				if (inputStream != null){
					inputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void validateCharacterStream(CharacterSource input, int offset, int lineNum) throws IOException {
		
		UsageDataCollector.getInstance().addOneTimeAction(Action.EDITOR_KAPPA_SYNTAX_CHECK);
		
			StringBuffer 	kappaLine = new StringBuffer();
			
			//we need to keep track of the last three characters to
			//be able to handle the \\\r\n or \\\n  sequences
			//that mean to continue
			
			char			character = 0;
			char			lastCharacter = 0;
			int				characterOffset = offset;
			int				lastLineStart = offset;
			int				lineContinuations = 0;
			boolean			atStart = true;				// to trim white space
			
			_lineCount = lineNum;
			
			while ((character = input.nextChar()) > 0){
				characterOffset++;
				if (character == '\n'){
					_lineCount++;
					if (lastCharacter != '\\'){						
						_characterOffset = lastLineStart;
						_lineCount -= lineContinuations;
						
						//Parse this line of kappa
						validateKappaLine(kappaLine.toString());
						
						_lineCount += lineContinuations;
						lineContinuations = 0;
						lastLineStart = characterOffset;
						
						//Re-initialize the string buffer and 
						//line continuation count.
						kappaLine = new StringBuffer();
						atStart = true;
					} else {
						++lineContinuations;	// keep track of continuations
						if (!atStart)
							kappaLine.append('\n');
					}
				} else if (atStart && Character.isWhitespace(character)) {
					++lastLineStart;			// skip over leading ws
				} else {
					atStart = false;
					kappaLine.append((char)character);
				}
				
				//ignore \r for line continuation check
				if (character != '\r'){
					lastCharacter = (char)character;
				}
			}
			
			if (kappaLine.length() > 0) {
				_characterOffset = lastLineStart;
				_lineCount -= lineContinuations;
				
				//Parse the last partial line of kappa
				validateKappaLine(kappaLine.toString());
			}
			
			if (_doSemantics)
				check.finalCheck(_kappaModel);	
	}

	/**
	 * This method is called by the KappaBuilder when a file changes has been detected.
	 * 
	 * @param file
	 * @param markerType
	 */
	public void validateString(String kappaContents, boolean doSemantics){
		
		CharacterSource src = new StringSource(kappaContents);
		try {
			_doSemantics = doSemantics;
			validateCharacterStream(src, 0, 0);
		} catch (IOException e) {
			e.printStackTrace();		// should not happen
		} finally {
			_doSemantics = true;
		}
	}
	

	public void validateDocString(IFile file, int offset, String line, int lineNum) {
		_fileBeingValidated = file;
		CharacterSource src = new StringSource(line);
		try {
			_doSemantics = false;
			validateCharacterStream(src, offset, lineNum-2);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			_doSemantics = true;
		}
	}

	private void validateKappaLine(String kappaLine){
		if (kappaLine.startsWith("%") == true){
			validateDeclLine(kappaLine);
		}else if (kappaLine.startsWith("#") == true){
			//This is just a comment do nothing.
		}else if (kappaLine.length() != 0){
			validateRule(kappaLine);
		}
		
	}
	
	private void validateRule(String kappaLine) {
		int intPos = skipWhite(kappaLine, 0);
		if (intPos >= kappaLine.length() || kappaLine.charAt(intPos) == '#')
			return; // no content on line

		RuleObject rule = null;
		if (kappaLine.charAt(intPos) == '\'') {
			++intPos;
			int nextPos = skipLabel(kappaLine, intPos);
			if (nextPos == -1)
				return;
			String label = kappaLine.substring(intPos, nextPos);
			
			if (_kappaModel.containsObject(label)) {
				createMarker(intPos, nextPos, "Duplicate label", IMarker.SEVERITY_WARNING);
			}
			rule = _kappaModel.addLabelRule(label, _characterOffset, _lineCount);
			rule.setRuleContent(kappaLine);
			intPos = skipWhite(kappaLine, nextPos + 1);
		} else {
			String label;
			if (kappaLine.length() - intPos > 20) {
				label = kappaLine.substring(intPos, intPos + 20);
			} else {
				label = kappaLine.substring(intPos);
			}
			rule = _kappaModel.addRule(label, _characterOffset, _lineCount);
			rule.setRuleContent(kappaLine);
		}

		if (intPos >= kappaLine.length()) {
			createMarker(0, kappaLine.length(), "Missing rule body",
					IMarker.SEVERITY_ERROR);
			return;
		}

		boolean inParen = false;
		boolean hasAgent = false;
		int agentPos = intPos;
		char ch = kappaLine.charAt(intPos);
		if (ch != '-' && ch != '<') {
			if (ch == '(') {
				inParen = true;
				intPos = skipWhite(kappaLine, intPos + 1);
			}

			for (; intPos < kappaLine.length(); ++intPos) {
				intPos = validateAgent(kappaLine, intPos, false, rule.getLeft());
				if (intPos == -1)
					return;

				hasAgent = true;
				intPos = skipWhite(kappaLine, intPos);
				if (intPos < kappaLine.length()
						&& kappaLine.charAt(intPos) != ',')
					break;
			}

			ch = (intPos < kappaLine.length()) ? kappaLine.charAt(intPos) : 0;
			if (inParen) {
				if (ch != ')') {
					createMarker(agentPos, intPos,
							"Missing closing ) in agent list.",
							IMarker.SEVERITY_ERROR);
					return;
				} else if (!hasAgent) {
					createMarker(agentPos, intPos, "Missing agent in ().", IMarker.SEVERITY_ERROR);
				} else {
					intPos = skipWhite(kappaLine, intPos + 1);
					ch = (intPos < kappaLine.length()) ? kappaLine
							.charAt(intPos) : 0;
				}
			}

			if (ch == '{') {
				intPos = validateConstraint(kappaLine, intPos);
				if (intPos == -1)
					return;
				else {
					intPos = skipWhite(kappaLine, intPos);
					ch = (intPos < kappaLine.length()) ? kappaLine
							.charAt(intPos) : 0;
				}
			}
		}

		boolean singleDirection = true;
		if (ch == '-') {
			++intPos;
			if (intPos < kappaLine.length() && kappaLine.charAt(intPos) == '>') {
				rule.setDirection("->");
				intPos = skipWhite(kappaLine, intPos + 1);
			} else {
				createMarker(intPos - 1, intPos, "Invalid arrow in rule",
						IMarker.SEVERITY_ERROR);
			}
		} else if (ch == '<') {
			++intPos;
			if (intPos < kappaLine.length() - 1
					&& kappaLine.charAt(intPos) == '-'
					&& kappaLine.charAt(intPos + 1) == '>') {
				rule.setDirection("<->");
				intPos = skipWhite(kappaLine, intPos + 2);
				singleDirection = false;
				if (!hasAgent) {
					createMarker(agentPos, intPos, "Bidirectional rules must have agents in the left side.", IMarker.SEVERITY_ERROR);
					return;
				}
			} else {
				createMarker(intPos - 1, intPos, "Invalid arrow in rule",
						IMarker.SEVERITY_ERROR);
				return;
			}
		} else {
			createMarker(intPos - 1, intPos, "Missing arrow in rule",
					IMarker.SEVERITY_ERROR);
			return;
		}

		inParen = false;
		ch = (intPos < kappaLine.length()) ? kappaLine.charAt(intPos) : 0;
		if (ch == '(') {
			inParen = true;
			intPos = skipWhite(kappaLine, intPos+1);
			ch = (intPos < kappaLine.length()) ? kappaLine.charAt(intPos) : 0;
		}
		
		if (ch != '@') {
			agentPos = intPos;
			hasAgent = false;
			for (; intPos < kappaLine.length(); ++intPos) {
				intPos = validateAgent(kappaLine, intPos, false, rule
						.getRight());
				if (intPos == -1)
					return;

				hasAgent = true;
				intPos = skipWhite(kappaLine, intPos);
				if (intPos < kappaLine.length()
						&& kappaLine.charAt(intPos) != ',')
					break;
			}
		}
		
		if (inParen) {
			if (intPos >= kappaLine.length() || kappaLine.charAt(intPos) != ')') {
				createMarker(agentPos, intPos, "Missing closing ) in agent list.", IMarker.SEVERITY_ERROR);
				return;
			} else if (!hasAgent) {
				createMarker(agentPos, intPos, "Missing agent in () list.", IMarker.SEVERITY_ERROR);
				return;
			} else {
				intPos = skipWhite(kappaLine, intPos+1);
			}
		}
		
		ch = (intPos < kappaLine.length()) ? kappaLine.charAt(intPos) : 0;
		if (ch == '{') {
			intPos = validateConstraint(kappaLine, intPos);
			if (intPos == -1)
				return;
			else {
				intPos = skipWhite(kappaLine, intPos);
				ch = (intPos < kappaLine.length()) ? kappaLine
						.charAt(intPos) : 0;
			}
		}
		
		if (ch == '@') {
			int rPos = intPos;
			intPos = validateRate(kappaLine, intPos);
			if (intPos == -1)
				return;
			if (!singleDirection ) {
				ch = (intPos < kappaLine.length()) ? kappaLine.charAt(intPos) : 0;
				if (ch != ',') {
					createMarker(rPos, kappaLine.length(), "Missing second rate for bidirectional rule.", IMarker.SEVERITY_ERROR);
					return;
				}
				intPos = validateRate(kappaLine, intPos);
				if (intPos == -1)
					return;
			}
		}
		
		int nextPos = skipWhite(kappaLine, intPos);
		if (nextPos < kappaLine.length()) {
			createMarker(nextPos, kappaLine.length(), "Extraneous characters in rule.", IMarker.SEVERITY_ERROR);
			return;
		}
		rule.trim();
		
		check.checkRule(rule, singleDirection);

	}

	private int validateConstraint(String kappaLine, int cPos) {
		int intPos = skipWhite(kappaLine, cPos+1);
		boolean hasConstraint = false;
		
		for (; intPos < kappaLine.length();) {
			intPos = skipConid(kappaLine, intPos);
			if (intPos == -1) 
				return -1;
			intPos = skipWhite(kappaLine, intPos+1);
			if (intPos > kappaLine.length()-2) {
				createMarker(intPos, kappaLine.length(), "Missing operator in constraint", IMarker.SEVERITY_ERROR);
				return -1;
			}
			if (kappaLine.startsWith("==", intPos) || kappaLine.startsWith("<>", intPos)) {
				intPos = skipWhite(kappaLine, intPos+2);
				intPos = skipConid(kappaLine, intPos);
				if (intPos == -1)
					return -1;
				intPos = skipWhite(kappaLine, intPos+1);
			} else if (kappaLine.startsWith("//", intPos)) {
				int nextPos = skipWhite(kappaLine, intPos+2);
				intPos = skipId(kappaLine, nextPos);
				if (intPos == -1)
					return -1;
				if (intPos == nextPos) {
					createMarker(intPos, kappaLine.length(), "Missing value for constraint", IMarker.SEVERITY_ERROR);
					return -1;
				}
			} else {
				createMarker(intPos, kappaLine.length(), "Invalid constraint operator.", IMarker.SEVERITY_ERROR);
				return -1;
			}
			hasConstraint = true;
			intPos = skipWhite(kappaLine, intPos);
			if (intPos >= kappaLine.length() || kappaLine.charAt(intPos) != ',') {
				break;
			}
			intPos = skipWhite(kappaLine, intPos+1);
		}
		if (intPos >=kappaLine.length() || kappaLine.charAt(intPos) != '}') {
			if (intPos >= kappaLine.length())
				intPos = kappaLine.length()-1;
			createMarker(intPos, intPos+1, "Missing } to terminate constraint.", IMarker.SEVERITY_ERROR );
		}
		++intPos;					// skip over }
		if (!hasConstraint) {
			createMarker(cPos, intPos, "Missing constraint after '{' in rule.", IMarker.SEVERITY_ERROR);
			return -1;
		}
		return intPos;
	}

	private int skipConid(String kappaLine, int intPos) {
		if (intPos >= kappaLine.length()) {
			createMarker(intPos-1, intPos, "Missing $ID in constraint.", IMarker.SEVERITY_ERROR);
			return -1;
		}
		if (kappaLine.charAt(intPos) != '$') {
			createMarker(intPos, kappaLine.length(), "Missing $ for constraint id.", IMarker.SEVERITY_ERROR);
			return -1;
		}
		++intPos;
		int nextPos = getNumber(kappaLine, intPos);
		if (intPos == nextPos) {
			createMarker(intPos, kappaLine.length(), "Invalid constraint id.", IMarker.SEVERITY_ERROR);
			return -1;
		}
		return intPos;
	}

	/**
	 * Check the rates for a rule
	 * 
	 * @param kappaLine
	 * @param intPos
	 * @param singleDirection
	 * @return
	 */
	private int validateRate(String kappaLine, int rPos) {
		int intPos = skipWhite(kappaLine, rPos + 1);
		if (intPos == kappaLine.length()) {
			createMarker(rPos, kappaLine.length(), "Missing rate after @", IMarker.SEVERITY_ERROR);
			return -1;
		}
		
		if (kappaLine.startsWith("$INF", intPos)) {
			intPos += 4;		// so we can add 1
		} else if ((intPos = skipFloat(kappaLine, intPos)) == -1) {
			createMarker(rPos, kappaLine.length(), "Invalid rate in rule, must be integer, floating point number or $INF", IMarker.SEVERITY_ERROR);
			return -1;
		}
		
		return skipWhite(kappaLine, intPos);
	}

	private void validateDeclLine(String kappaLine) {
		// check for :
		if (kappaLine.startsWith("%init:") == true){
			validateInitLine(kappaLine);
		}else if (kappaLine.startsWith("%obs:") == true){
			validateObsLine(kappaLine);
		}else if (kappaLine.startsWith("%mod:") == true){
			validateModLine(kappaLine);
		}else if (kappaLine.startsWith("%story:") == true){
			validateStoryLine(kappaLine);
		}else if (kappaLine.length() != 0){
			int colon = kappaLine.indexOf(':');
			int ws = kappaLine.indexOf(" \t");
			if (ws == -1)
				ws = kappaLine.length();
			
			if (colon == -1 || colon >= ws) {
				createMarker(0, ws, "Invalid declaration, missing :", IMarker.SEVERITY_ERROR);
			} else {
				createMarker(0, kappaLine.length(), "Unknown declaration", IMarker.SEVERITY_ERROR);
			}
		}
		
	}

	
	/**
	 * Validate the %init statement.  Check for number, '*' and then an agent list
	 * 
	 * @param kappaLine
	 */

	private void validateInitLine(String kappaLine){

		int intPos = kappaLine.indexOf("%init:");
		int linePos = intPos;
		intPos += "%init:".length();				// skip over it
		
		intPos = skipWhite(kappaLine, intPos);
		if (intPos == kappaLine.length())
			return;						// just init is OK.
		
		int nextPos = getNumber(kappaLine, intPos);
		if (intPos != nextPos) {
			intPos = skipWhite(kappaLine, nextPos);
			if (intPos == kappaLine.length() || kappaLine.charAt(intPos) != '*') {
				createMarker(linePos, kappaLine.length(), "Missing * after number.",
						IMarker.SEVERITY_ERROR);
				return;
			}
			++intPos; // skip *
			intPos = skipWhite(kappaLine, intPos);
		}
		
		if (intPos < kappaLine.length() && kappaLine.charAt(intPos) == '*') {
			createMarker(intPos, kappaLine.length(), "Missing number in init.",
					IMarker.SEVERITY_ERROR);
			return;
		}
		boolean inParen = false;
		boolean hasAgent = false;
		int agentPos = intPos;
		if (intPos < kappaLine.length() && kappaLine.charAt(intPos) == '(') {
			inParen = true;
			++intPos;					// allow paren around agent list
		}
		
		OutlineObject outline = _kappaModel.addInit(kappaLine, _characterOffset, _lineCount);
		
		for (; intPos < kappaLine.length(); ++intPos) {
			intPos = validateAgent(kappaLine, intPos, true, outline);
			if (intPos == -1)
				return;					// skip on error
			
			hasAgent = true;
			intPos = skipWhite(kappaLine, intPos);
			if (intPos < kappaLine.length() && kappaLine.charAt(intPos) != ',') {
				break;
			}			
		}
		char ch = (intPos < kappaLine.length())?kappaLine.charAt(intPos):0;
		if (inParen) {
			if (ch != ')') {
				createMarker(agentPos, intPos, "Unclosed ()'s in agent list", IMarker.SEVERITY_ERROR);
				return;
			} else {
				++intPos;			// skip )
				intPos = skipWhite(kappaLine, intPos);
				ch = (intPos < kappaLine.length())?kappaLine.charAt(intPos):0;
			}
		}
		if (!hasAgent) {
			createMarker(agentPos, intPos, "No agents in init list", IMarker.SEVERITY_ERROR);
		} else if (ch != 0) {
			createMarker(intPos, kappaLine.length(), "Extra characters in agent list", IMarker.SEVERITY_ERROR);
		} else {
			check.checkAgentList(outline.getChildren());
		}
		return;

	}
	
	/**
	 * Validate an agent.  Return then next offset to parse in the line.
	 * 
	 * @param kappaLine
	 * @param intPos
	 * @param isInit  If true then do not allow !_ or ? bonds.
	 * @return
	 */

	private int validateAgent(String kappaLine, int intPos, boolean isInit,
			OutlineObject parent) {
		int start = skipWhite(kappaLine, intPos);
		int nextPos = skipId(kappaLine, start);
		if (nextPos == intPos) {
			createMarker(start, kappaLine.length(), "Missing or invalid agent name",
					IMarker.SEVERITY_ERROR);
			return -1;
		}

		String agent = kappaLine.substring(start, nextPos);
		AgentObject listAgent = parent.addAgent(_kappaModel, agent);
		listAgent.setStart(start);

		intPos = skipWhite(kappaLine, nextPos);
		if (intPos == kappaLine.length() || kappaLine.charAt(intPos) != '(') {
			if (kappaLine.indexOf('(', intPos) == -1)
				createMarker(start, intPos, "Missing ( after agent name",
					IMarker.SEVERITY_ERROR);
			else 
				createMarker(start, intPos, "Invalid agent name",
						IMarker.SEVERITY_ERROR);
			return -1;
		}
		++intPos; // skip (
		intPos = skipWhite(kappaLine, intPos);
		if (intPos < kappaLine.length() && kappaLine.charAt(intPos) != ')') {
			for (; intPos < kappaLine.length(); ++intPos) {
				nextPos = skipSite(kappaLine, intPos, start, isInit, listAgent);
				if (nextPos == -1) {
					return -1; // pass back error
				} else if (intPos == nextPos) {
					createMarker(start, intPos, "Missing site name in Agent.",
							IMarker.SEVERITY_ERROR);
					return -1;
				}
				intPos = skipWhite(kappaLine, nextPos);
				if (intPos >= kappaLine.length()
						|| kappaLine.charAt(intPos) != ',') {
					break;
				}
			}

		}
		if (intPos >= kappaLine.length() || kappaLine.charAt(intPos) != ')') {
			int errPos = kappaLine.indexOf(')', start);
			if (errPos == -1) {
				errPos = kappaLine.length();
				createMarker(start, errPos, "Missing closing ) for site list.",
						IMarker.SEVERITY_ERROR);
			} else {
				createMarker(start, errPos, "Invalid site list.",
						IMarker.SEVERITY_ERROR);

			}
			return -1;
		}

		listAgent.setEnd(intPos);
		listAgent.updateOutlineAgent();
		return ++intPos;
	}

	private int skipSite(String kappaLine, int intPos, int agentPos, boolean isInit, OutlineObject agent) {
		int start = skipWhite(kappaLine, intPos);
		intPos = skipId(kappaLine, start);
		if (intPos == start) {
			int errPos = kappaLine.indexOf(',', start);
			boolean missingName = errPos == intPos || kappaLine.charAt(intPos) == ')';
			if (errPos == -1 || errPos == intPos) {
				errPos = kappaLine.indexOf(')');
				if (errPos == -1) {
					errPos = kappaLine.length();
				}
			}
			if (start == errPos) {
				start = agentPos;
			}
			if (missingName) {
				createMarker(start, errPos, "Missing site name", IMarker.SEVERITY_ERROR);
			} else {
				createMarker(start, errPos, "Invalid site name", IMarker.SEVERITY_ERROR);
			}
			return -1;
		}
		String siteName = kappaLine.substring(start, intPos);
		SiteObject site = new SiteObject(agent, siteName);
		agent.addChild(site);
		intPos = skipWhite(kappaLine, intPos);
		if (intPos == kappaLine.length())
			return intPos;
		char ch = kappaLine.charAt(intPos);
		if (ch == '~') {				// state 
			intPos = skipWhite(kappaLine, ++intPos);
			int nextPos = skipMark(kappaLine, intPos);
			if (intPos == nextPos) {
				createMarker(start, intPos, "Invalid marker", IMarker.SEVERITY_ERROR);
				return -1;
			}
			String stateName = kappaLine.substring(intPos, nextPos);
			site.addChild(new OutlineObject(site, stateName, null, OutlineType.SITE));
			intPos = skipWhite(kappaLine, nextPos);
			if (intPos == kappaLine.length())
				return intPos;
			ch = kappaLine.charAt(intPos);
		}
		if (!isInit && ch == '?') {
			site.setWild(true);
			return intPos + 1;
		} else if (ch == '!') {
			++intPos;
			if (!isInit && intPos < kappaLine.length() && kappaLine.charAt(intPos) == '_') {
				site.setAny(true);
				++intPos;
			} else {
				int nextPos = getNumber(kappaLine, intPos);
				if (nextPos == intPos) {
					createMarker(start, intPos, "Missing site ID", IMarker.SEVERITY_ERROR);
					return -1;
				}
				int bNum = Integer.parseInt(kappaLine.substring(intPos, nextPos));
				site.setBond(bNum);
				intPos = nextPos;
			}
		}
		return intPos;
	}

	private int skipMark(String kappaLine, int intPos) {
		for (; intPos < kappaLine.length(); ++intPos) {
			if (!Character.isLetterOrDigit(kappaLine.charAt(intPos)))
				break;
		}
		return intPos;
	}

	private int skipId(String kappaLine, int intPos) {
		boolean first = true;
		for (; intPos < kappaLine.length(); ++intPos) {
			char ch = kappaLine.charAt(intPos);
			if (first) {
				if (!Character.isLetterOrDigit(ch)) {
					return intPos;
				}
				first = false;
			} else if (!(Character.isLetterOrDigit(ch) || ch == '_' || ch == '^' || ch == '-')) {
				return intPos;
			}
		}
		return intPos;
	}

	private int getNumber(String kappaLine, int intPos) {
		for ( ; intPos < kappaLine.length(); ++intPos) {
			if (!Character.isDigit(kappaLine.charAt(intPos)))
				return intPos;
		}
		return intPos;
	}

	private int skipWhite(String kappaLine, int intPos) {
		for (; intPos < kappaLine.length(); ++intPos) {
			char ch = kappaLine.charAt(intPos);
			if (!Character.isWhitespace(ch)) {
				if (ch == '#') {
					return kappaLine.length();
				} else if (ch == '\\') {
					int len = kappaLine.length();
					int more = 1;
					if (intPos + 1 < len && kappaLine.charAt(intPos+1) == '\r') {
						++more;
					}
					if (intPos + more < len && kappaLine.charAt(intPos+more) == '\n') {
						intPos += more + 1;
						continue;
					}
				}
				break;
			}
		}
		return intPos;
	}

	/**
	 * need to ensure that the %obs has the format
	 * %obs 'existing-rule-name'
	 * %obs 'new-rule-name' agent list
	 * @param kappaLine
	 */
	private void validateObsLine(String kappaLine) {
		int start = kappaLine.indexOf("%obs:");
		int intPos = start + "%obs:".length();
		intPos = skipWhite(kappaLine, intPos);
		if (intPos == kappaLine.length()) {
			createMarker(start, intPos, "%obs must have arguments", IMarker.SEVERITY_ERROR);
		}
		OutlineObject obs = null;
		String label = null;
		int labelFrom = intPos;
		int labelTo = 0;
		if (intPos < kappaLine.length() && kappaLine.charAt(intPos) == '\'') {
			++intPos; // skip '
			int labelPos = skipLabel(kappaLine, intPos);
			if (labelPos == -1)
				return;
			labelTo = labelPos;
			label = new String(kappaLine.substring(intPos, labelPos));
			intPos = skipWhite(kappaLine, labelPos + 1);
		}

		boolean hasAgent = intPos < kappaLine.length();
		if (label == null) {
			obs = _kappaModel.addObs(kappaLine, OutlineType.AGENT, _characterOffset, _lineCount);			
		} else if (!hasAgent) {
			obs = _kappaModel.addObs(kappaLine, OutlineType.RULE, _characterOffset, _lineCount);
			obs.addChild(new OutlineObject(obs, label, label, OutlineType.RULE));
		} else {
			obs = _kappaModel.addObs(kappaLine, OutlineType.AGENT, _characterOffset, _lineCount);
			obs.addChild(new OutlineObject(obs, label, label, OutlineType.LABEL));
		}

		if (hasAgent) {
			int agentStart = intPos;
			for (; true; ++intPos) {
				intPos = skipWhite(kappaLine, intPos);
				if (intPos == kappaLine.length()) {
					createMarker(agentStart, intPos, "Invalid agentlist", IMarker.SEVERITY_ERROR);
					return;
				}
				intPos = validateAgent(kappaLine, intPos, false, obs);
				if (intPos == -1)
					return;
				intPos = skipWhite(kappaLine, intPos);	// trailing commma is also an error
				if (intPos == kappaLine.length() || kappaLine.charAt(intPos) != ',') {
					break;
				}
			}
		}

		intPos = skipWhite(kappaLine, intPos);
		if (intPos < kappaLine.length()) {
			createMarker(intPos, kappaLine.length(),
					"Extra characters on %obs line", IMarker.SEVERITY_ERROR);
			return;
		}
		if (label != null) {
			boolean bResult = _kappaModel.containsObject(label);

			// Need to save the label declared in an obs
			if (!bResult && !hasAgent) {
				_obsDefines.add(label);
			}
			if (hasAgent && bResult) {
				createMarker(labelFrom, labelTo, "The label \"" + label
						+ "\" has already been defined as a Rule.",
						IMarker.SEVERITY_ERROR);
			}
		}
	}

	private int skipLabel(String kappaLine, int intPos) {
		int endLabel = kappaLine.indexOf('\'', intPos);
		if (endLabel == -1) {
			createMarker(intPos, kappaLine.length(), "Label missing close '", IMarker.SEVERITY_ERROR);
			return -1;
		}
		return endLabel;
	}

	private void validateModLine(String kappaLine) {
		int start = kappaLine.indexOf("%mod:");
		int intPos = start + "%mod:".length();
		intPos = skipWhite(kappaLine, intPos);
		int expPos = intPos;
		if (intPos < kappaLine.length() && kappaLine.charAt(intPos) == '$') {
			++intPos;
			if (intPos == kappaLine.length() || kappaLine.charAt(intPos) != 'T') {
				createMarker(expPos, intPos, "Time variable must be $T",
						IMarker.SEVERITY_ERROR);
				return;
			}
			intPos = skipWhite(kappaLine, ++intPos);
			if (intPos == kappaLine.length() || kappaLine.charAt(intPos) != '>') {
				createMarker(expPos, intPos, "Time operator must be >",
						IMarker.SEVERITY_ERROR);
				return;

			}
			intPos = skipWhite(kappaLine, intPos + 1);
			intPos = skipFloat(kappaLine, intPos);
			if (intPos == -1)
				return;
		} else if (checkCharSet(kappaLine, intPos, "[(0123456789")) {
			intPos = skipExpr(kappaLine, intPos, false);
			if (intPos == -1)
				return;
			intPos = skipWhite(kappaLine, intPos);
			if (!checkCharSet(kappaLine, intPos, "<>")) {
				createMarker(expPos, intPos,
						"Missing '<' or '>' for concentration expression",
						IMarker.SEVERITY_ERROR);
				return;
			}
			intPos = skipWhite(kappaLine, intPos + 1);
			intPos = skipExpr(kappaLine, intPos, false);
			if (intPos == -1)
				return;
		} else {
			if (intPos == kappaLine.length())
				intPos = start;
			createMarker(
					intPos,
					kappaLine.length(),
					"Must have time or concentraion expression in %mod statement",
					IMarker.SEVERITY_ERROR);
			return;
		}

		intPos = skipWhite(kappaLine, intPos);

		if (intPos >= kappaLine.length() - 1 || kappaLine.charAt(intPos) != 'd'
				|| kappaLine.charAt(intPos + 1) != 'o') {
			createMarker(expPos, kappaLine.length(),
					"Missing do in %mod statement", IMarker.SEVERITY_ERROR);
			return;
		}
		intPos = skipWhite(kappaLine, intPos + 2);

		if (intPos == kappaLine.length() || kappaLine.charAt(intPos) != '\'') {
			createMarker(expPos, intPos, "Missing label for assignment target",
					IMarker.SEVERITY_ERROR);
			return;
		}

		intPos = skipLabel(kappaLine, intPos + 1);
		if (intPos == -1)
			return;

		intPos = skipWhite(kappaLine, intPos + 1);
		if (intPos >= kappaLine.length() - 1 || kappaLine.charAt(intPos) != ':'
				|| kappaLine.charAt(intPos + 1) != '=') {
			createMarker(expPos, kappaLine.length(),
					"Missing := in %mod statement", IMarker.SEVERITY_ERROR);
			return;
		}
		intPos = skipWhite(kappaLine, intPos + 2);

		intPos = skipExpr(kappaLine, intPos, false);
		if (intPos == -1)
			return;

		intPos = skipWhite(kappaLine, intPos);
		if (intPos < kappaLine.length()) {
			createMarker(intPos, kappaLine.length(),
					"Extra characters in %mod statement",
					IMarker.SEVERITY_ERROR);
			return;
		}
		
		_kappaModel.addMod(kappaLine, _characterOffset, _lineCount);
		
		return;

	}
	
	private int skipExpr(String kappaLine, int intPos, boolean inParen) {
		int start = intPos;
		intPos = skipValue(kappaLine, intPos, start);
		if (intPos == -1) 
			return -1;
		
		intPos = skipWhite(kappaLine, intPos);
		while (checkCharSet(kappaLine, intPos, "+-*/")) {
			intPos = skipWhite(kappaLine, intPos+1);
			intPos = skipValue(kappaLine, intPos, start);
			if (intPos == -1)
				return -1;
			intPos = skipWhite(kappaLine, intPos);			
		}
		if (intPos < kappaLine.length() && kappaLine.charAt(intPos) == ')') {
			if (!inParen) {
				createMarker(start, intPos, "Extra ) in expression", IMarker.SEVERITY_ERROR);
				return -1;
			}
		}
			
		return intPos;
	}

	private int skipValue(String kappaLine, int intPos, int start) {
		if (intPos == kappaLine.length()) {
			if (start == intPos)
				start = 0;
			createMarker(start, intPos, "Incomplete expression", IMarker.SEVERITY_ERROR);
			return -1;
		}
		char ch = kappaLine.charAt(intPos);
		if (ch == '[') {
			intPos = skipWhite(kappaLine, intPos+1);
			if (intPos == kappaLine.length() || kappaLine.charAt(intPos) != '\'') {
				createMarker(start, intPos, "Invalid label in expression", IMarker.SEVERITY_ERROR);
				return -1;
			}
			intPos = skipLabel(kappaLine, intPos+1);
			if (intPos == -1)
				return -1;
			intPos = skipWhite(kappaLine, intPos+1);
			if (intPos == kappaLine.length() || kappaLine.charAt(intPos) != ']') {
				createMarker(start, intPos, "Missing close ] for label", IMarker.SEVERITY_ERROR);
				return -1;
			}
			++intPos;
		} else if (ch == '\'') {
			intPos = skipLabel(kappaLine, intPos+1);
			if (intPos == -1)
				return -1;
			++intPos;
		} else if (ch == '(') {
			intPos = skipWhite(kappaLine, intPos+1);
			intPos = skipExpr(kappaLine, intPos, true);
			if (intPos == -1)
				return -1;
			if (intPos == kappaLine.length() || kappaLine.charAt(intPos) != ')') {
				createMarker(start, intPos, "Missing ')' in expression.", IMarker.SEVERITY_ERROR);
			}
			++intPos;				// consume the )
		} else {
			intPos = skipFloat(kappaLine, intPos);
			if (intPos == -1)
				return -1;
		}
		return skipWhite(kappaLine, intPos);
	}

	private int skipFloat(String kappaLine, int intPos) {
		int start = intPos;
		while (intPos < kappaLine.length() && Character.isDigit(kappaLine.charAt(intPos))) {
			++intPos;
		}
		
		if (intPos < kappaLine.length() && kappaLine.charAt(intPos) == '.') {
			++intPos;
			while (intPos < kappaLine.length() && Character.isDigit(kappaLine.charAt(intPos))) {
				++intPos;
			}
		}
		
		if (checkCharSet(kappaLine, intPos, "eE")) {
			++intPos;
			if (checkCharSet(kappaLine, intPos, "+-")) {
				++intPos;
			}
			while (intPos < kappaLine.length() && Character.isDigit(kappaLine.charAt(intPos))) {
				++intPos;
			}
		}
		
		if (intPos < kappaLine.length()) {
			char ch = kappaLine.charAt(intPos);
			if (ch == '.' || Character.isLetter(ch)) {
				if (intPos == start) {
					intPos = kappaLine.length();
				}
				createMarker(start, intPos, "Invalid number", IMarker.SEVERITY_ERROR);
				return -1;
			}
		}
		if (start == intPos) {
			if (start == kappaLine.length()) {
				start = 0;
			}
			createMarker(start, kappaLine.length(), "Missing number", IMarker.SEVERITY_ERROR);
			return -1;
		}
		return intPos;
	}

	private boolean checkCharSet(String kappaLine, int intPos, String set) {
		if (intPos < kappaLine.length()) {
			if (set.indexOf(kappaLine.charAt(intPos)) != -1)
				return true;
		}
		return false;
	}

	private void validateStoryLine(String kappaLine) {
		int start = kappaLine.indexOf("%story:");
		int intPos = skipWhite(kappaLine, start + "%story:".length());
		String label = null;
		if (intPos < kappaLine.length() && kappaLine.charAt(intPos) == '\'') {
			++intPos;
			int nextPos = skipLabel(kappaLine, intPos);
			if (nextPos == -1)
				return;
			if (nextPos > intPos) {
				label = kappaLine.substring(intPos, nextPos);
			}
			intPos = nextPos + 1;
		}
		
		intPos = skipWhite(kappaLine, intPos);
		if (label == null) {
			createMarker(start, kappaLine.length(), "A story must have a label", IMarker.SEVERITY_ERROR);
		} else if (intPos != kappaLine.length()) {
			createMarker(intPos, kappaLine.length(), "Extra text in a story", IMarker.SEVERITY_ERROR);
		}
		
		OutlineObject story = _kappaModel.addStory(kappaLine, _characterOffset, _lineCount);
		story.addChild(new OutlineObject(story, label, label, OutlineType.RULE));
		return;
		
	}
	
	/*
	 * This method will create a marker in the problems view and an error indicator in the 
	 * text editor if we are reporting errors.
	 */
	protected void createMarker(int charStart, int charEnd, String reason, int severity){
		
		int charStartPos = _characterOffset + charStart;
		
		//The end character is not inclusive so we need to add an extra character.
		int charEndPos = _characterOffset + charEnd;			

		
		createInternalMarker(charStart, _lineCount, charStart, charStartPos, charEndPos, reason,
				severity);
	}
	
	public void createPostMarker(int offset, int line, int start, int end, String reason,
			int severity) {
		createInternalMarker(offset, line, start, start + offset, end + offset, reason, severity);
		
	}

	private void createInternalMarker(int charStart, int line, int charPos,
			int charStartPos, int charEndPos, String reason, int severity) {
		if (_bReportErrors == false)
			return;

		if (_fileBeingValidated != null) {
			try {
				IMarker marker = _fileBeingValidated
						.createMarker(Activator.MARKER_TYPE);

				HashMap<String, Object> attributes = new HashMap<String, Object>(
						4);
				attributes.put(IMarker.MESSAGE, reason);
				attributes.put(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
				attributes.put(IMarker.SEVERITY, severity);
				attributes.put(IMarker.TEXT, reason);
				attributes.put(IMarker.LOCATION, "Line " + line + ", Char "
						+ (charPos + 1));
				attributes.put(IMarker.CHAR_START, new Integer(charStartPos));
				attributes.put(IMarker.CHAR_END, new Integer(charEndPos));
				attributes.put(IMarker.LINE_NUMBER, line);

				marker.setAttributes(attributes);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Return a stream of characters from the current input source. 
	 * We use this class so we can share the same code for processing kappa
	 * input
	 * 
	 * @author bill
	 *
	 */
	
	private interface CharacterSource {
		
		public char nextChar() throws IOException;
	}
	
	private static class StreamCharSource implements CharacterSource {
		private InputStream in = null;
		
		protected StreamCharSource (InputStream strm) {
			in = strm;
		}
		
		public char nextChar() throws IOException {
			int streamChar = in.read();
			if (streamChar == -1)
				return 0;						// eof
			else
				return (char)streamChar;
		}
	}
	
	private static class StringSource implements CharacterSource {
		private String in;
		private int offset = 0;
		private boolean done = false;
		
		protected StringSource(String src) {
			in = src;
		}
		
		public char nextChar() {
			if (offset == in.length()) {
				if (!done) {
					done = true;
					return '\n';
				} else 
					return 0;
			} else
				return in.charAt(offset++);
		}
	}

}
