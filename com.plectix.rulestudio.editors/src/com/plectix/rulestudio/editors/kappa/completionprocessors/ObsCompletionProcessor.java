package com.plectix.rulestudio.editors.kappa.completionprocessors;

import java.util.ArrayList;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

/**
 * This class display the list of possible options in the %obs lines when the user
 * types one of the following characters ":'"
 * 
 * @author bbuffone
 *
 */
public class ObsCompletionProcessor implements IContentAssistProcessor {
	private final IContextInformation[] NO_CONTEXTS = new IContextInformation[0];
	private final char[] PROPOSAL_ACTIVATION_CHARS = new char[] { ':', ',', '\'', '~', '(' };
	private ICompletionProposal[] NO_COMPLETIONS = new ICompletionProposal[0];
	
	private ContentAssistant master = null;
	
	public ObsCompletionProcessor(ContentAssistant ca) {
		master = ca;
	}
	
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer,
			int offset) {

		BuildCaList ba = new BuildCaList(viewer, offset);
		char activChar = ba.getActivationCharacter(PROPOSAL_ACTIVATION_CHARS);

		ArrayList<ICompletionProposal> ret = new ArrayList<ICompletionProposal>();
		if (activChar == ':') {
			master.setStatusMessage("Rule Label, Observable Label or Agent List");
			defaultList(ret, ba);
		} else if (activChar == '\'' && ba.isInLabel()) {
			master.setStatusMessage("Rule Label or Observable Label");
			ba.buildLabelList(ret);
		} else if ((activChar == '(' || activChar == ',') && ba.inAgent()){
			master.setStatusMessage("Add Site");
			ba.buildSiteList(ret);
		} else if (activChar == '~') {
			master.setStatusMessage("Add State");
			ba.buildStateList(ret);
		} else {
			String label = ba.getLabel();
			if (!(label.length() > 0 && ba.isRuleLabel(label))) {
				master.setStatusMessage("Agent List");
				ba.buildAgentNameList(ret, ba.getAgentPrefix());
			}
		}
		if (ret.size() == 0)
			return NO_COMPLETIONS;

		return (ICompletionProposal[]) ret.toArray(new ICompletionProposal[ret
				.size()]);

	}
	
	private void defaultList(ArrayList<ICompletionProposal> result, BuildCaList ba) {
		
		String prefix = ba.getAgentPrefix();
		if (prefix.length() == 0) {
			ba.addProposal(result, "", "'", "'rule label'");
			ba.addProposal(result, "", "'", "'obs label' agent list");
		}

        ba.buildAgentNameList(result, prefix);
		return;
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
