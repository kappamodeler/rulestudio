package com.plectix.rulestudio.editors.kappa.completionprocessors;

import java.util.ArrayList;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

public class PrecentCompletionProcessor implements IContentAssistProcessor {

	private final IContextInformation[] NO_CONTEXTS = new IContextInformation[0];
	private final char[] PROPOSAL_ACTIVATION_CHARS = new char[] { '%' };
	private final static String[] choices = new String[] {"%init:", "%mod:", "%obs:", "%story:"};
	private final static String[] labels = new String[] {"%init: number * agent list", "%mod:", "%obs: rule or agent list", "%story: rule label"};

	private ContentAssistant master = null;
	
	public PrecentCompletionProcessor(ContentAssistant ca) {
		master = ca;
	}
	
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
			int offset) {

		BuildCaList bl = new BuildCaList(viewer, offset);
		String prefix = bl.getLine();
		
        ArrayList<ICompletionProposal> result = new ArrayList<ICompletionProposal>();
		master.setStatusMessage("Kappa Directive");
        for (int index = 0; index < choices.length; index++){
        	if (choices[index].startsWith(prefix) == true){
	        	result.add(new CompletionProposal(choices[index].substring(prefix.length()), offset, 
			         0, choices[index].length() - prefix.length(), null, labels[index], null, null));
        	}
        }

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
