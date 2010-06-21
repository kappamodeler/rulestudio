package com.plectix.rulestudio.editors.kappa.completionprocessors;

import java.util.ArrayList;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

import com.plectix.rulestudio.core.usagedata.Action;
import com.plectix.rulestudio.core.usagedata.UsageDataCollector;

/**
 * This class display the list of possible options in the %init lines when the user
 * types one of the following characters "( , ~"
 * 
 * @author bbuffone
 *
 */
public class AgentCompletionProcessor implements IContentAssistProcessor {
	private final IContextInformation[] NO_CONTEXTS = new IContextInformation[0];
	private final char[] PROPOSAL_ACTIVATION_CHARS = new char[] { '(', ',', '~', '%', '\'', '\n', '>', '*', '{', '}', '@', '!', '?' };
	private ICompletionProposal[] NO_COMPLETIONS = new ICompletionProposal[0];

	private ContentAssistant master = null;
	
	public AgentCompletionProcessor(ContentAssistant ca) {
		master = ca;
	}
	
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
										int offset) {
		
		BuildCaList bcl = new BuildCaList(viewer, offset);
		ArrayList<ICompletionProposal> result = new ArrayList<ICompletionProposal>();
		char activChar = bcl.getActivationCharacter(PROPOSAL_ACTIVATION_CHARS);
    
        //Look through all the agent objects and add them to the completion processor.
        // OutlineObject agentsObject = parser.getKappaModel().findChildByLabel("Agents");
		
        if (activChar == '\n') {
        	master.setStatusMessage("Kappa Model Statement");
        	String prefix = bcl.getAgentPrefix();
        	if (prefix.length() == 0) {
        		bcl.buildEmptyList(result);
        		bcl.buildPrecentCompletionProposals(result);
        	}
        	bcl.buildAgentNameList(result, prefix);
		} else if ((activChar == '(' || activChar == ',') && bcl.inAgent()){
			master.setStatusMessage("Add Site");
			bcl.buildSiteList(result);
		} else if (activChar == '~'){
        	master.setStatusMessage("Add State");
			UsageDataCollector.getInstance().addOneTimeAction(Action.EDITOR_KAPPA_AUTOCOMPLETE_SITE);
			bcl.buildStateList(result);
		}else if (activChar == '%'){
			master.setStatusMessage("Kappa Directive");
			bcl.buildPrecentCompletionProposals(result);
		} else if (activChar == '\'' && bcl.isInLabel() || "{}@!?".indexOf(activChar) != -1) {
			return NO_COMPLETIONS;
		} else {
			master.setStatusMessage("Agent List");
			UsageDataCollector.getInstance().addOneTimeAction(Action.EDITOR_KAPPA_AUTOCOMPLETE_AGENT);
			String prefix = bcl.getAgentPrefix();
			if (activChar == '>') {
				String right = bcl.getRightSide();
				if (right.length() > 0) {
					bcl.addProposal(result, prefix, right, right);
				}
			}
			bcl.buildAgentNameList(result, prefix);
		}
        
		if (result.size() == 0)
			return NO_COMPLETIONS;
		
		return (ICompletionProposal[]) result.toArray(new ICompletionProposal[result.size()]);

		
	}
	


	
	/*
	 * IContentAssistProcessor METHODS
	 */
	public IContextInformation[] computeContextInformation(ITextViewer viewer,
			int offset) {
		return NO_CONTEXTS;
	}

	public char[] getContextInformationAutoActivationCharacters() {
		return PROPOSAL_ACTIVATION_CHARS;
	}

	public char[] getCompletionProposalAutoActivationCharacters() {
		return PROPOSAL_ACTIVATION_CHARS;
	}

	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

	public String getErrorMessage() {
		return null;
	}

}
