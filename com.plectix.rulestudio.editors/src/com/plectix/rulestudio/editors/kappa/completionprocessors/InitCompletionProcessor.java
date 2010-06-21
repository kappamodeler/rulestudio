package com.plectix.rulestudio.editors.kappa.completionprocessors;

import java.util.ArrayList;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

public class InitCompletionProcessor implements IContentAssistProcessor {
	private final IContextInformation[] NO_CONTEXTS = new IContextInformation[0];
	private final char[] PROPOSAL_ACTIVATION_CHARS = new char[] { '*', ',', ')', '~', '!', '?' };
	private ICompletionProposal[] NO_COMPLETIONS = new ICompletionProposal[0];

	private ContentAssistant master = null;
	
	public InitCompletionProcessor(ContentAssistant ca) {
		master = ca;
	}
	
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
			int offset) {

		BuildCaList ba = new BuildCaList(viewer, offset);
        ArrayList<ICompletionProposal> result = new ArrayList<ICompletionProposal>();
        
		master.setStatusMessage("Agent List");
        String prefix = ba.getAgentPrefix();
        char ch = ba.getActivationCharacter(PROPOSAL_ACTIVATION_CHARS);
        if (ch == '*' || ch == ',') {
    		ba.buildFullAgentList(result, prefix);
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
