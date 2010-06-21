package com.plectix.rulestudio.editors.kappa.completionprocessors;

import java.util.ArrayList;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

public class LabelCompletionProcessor implements IContentAssistProcessor {
	private final IContextInformation[] NO_CONTEXTS = new IContextInformation[0];
	private final char[] PROPOSAL_ACTIVATION_CHARS = new char[] { '\'' };
	private ICompletionProposal[] NO_COMPLETIONS = new ICompletionProposal[0];
	
	private ContentAssistant master = null;
	
	public LabelCompletionProcessor(ContentAssistant ca) {
		master = ca;
	}

	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
			int offset) {
		
		BuildCaList ba = new BuildCaList(viewer, offset);
		master.setStatusMessage("Rule Label");
		ArrayList<ICompletionProposal> ret = new ArrayList<ICompletionProposal>();
		ba.buildLabelList(ret);
		
		if (ret.size() == 0)
			return NO_COMPLETIONS;
		
		return (ICompletionProposal[]) ret.toArray(new ICompletionProposal[ret.size()]);
		
	}
	

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
