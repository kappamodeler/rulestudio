package com.plectix.rulestudio.editors.kappa;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;

import com.plectix.rulestudio.editors.kappa.completionprocessors.AgentCompletionProcessor;
import com.plectix.rulestudio.editors.kappa.completionprocessors.InitCompletionProcessor;
import com.plectix.rulestudio.editors.kappa.completionprocessors.LabelCompletionProcessor;
import com.plectix.rulestudio.editors.kappa.completionprocessors.ObsCompletionProcessor;
import com.plectix.rulestudio.editors.kappa.extras.AnnotationHover;
import com.plectix.rulestudio.editors.kappa.extras.ColorManager;
import com.plectix.rulestudio.editors.kappa.extras.KappaDoubleClickStrategy;
import com.plectix.rulestudio.editors.kappa.extras.KappaTextHover;
import com.plectix.rulestudio.editors.kappa.extras.NonRuleBasedDamagerRepairer;
import com.plectix.rulestudio.editors.kappa.extras.SyntaxColors;
import com.plectix.rulestudio.editors.kappa.scanners.KappaInitScanner;
import com.plectix.rulestudio.editors.kappa.scanners.KappaModifyScanner;
import com.plectix.rulestudio.editors.kappa.scanners.KappaObsScanner;
import com.plectix.rulestudio.editors.kappa.scanners.KappaPartitionScanner;
import com.plectix.rulestudio.editors.kappa.scanners.KappaRuleScanner;
import com.plectix.rulestudio.editors.kappa.scanners.KappaStoryScanner;

/**
 * This class configures the functionality of the Editor.
 * We need to add the syntax coloring, hovers
 * 
 * @author bbuffone
 *
 */
public class KappaConfiguration extends SourceViewerConfiguration {
	
	private KappaDoubleClickStrategy doubleClickStrategy = null;
	private KappaRuleScanner labelScanner = null;
	private KappaInitScanner initScanner = null;
	private KappaObsScanner obsScanner = null;
	private KappaModifyScanner modifyScanner = null;
	private KappaStoryScanner storyScanner = null;
	private ColorManager colorManager = null;

	/*
	 * CONSTRUCTOR
	 */
	public KappaConfiguration(ColorManager colorManager) {
		this.colorManager = colorManager;
	}
	
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] {
			KappaPartitionScanner.LABEL,
			KappaPartitionScanner.KAPPA_COMMENT,
			KappaPartitionScanner.LABEL,
			KappaPartitionScanner.OBS_EXPRESSION,
			KappaPartitionScanner.INIT_EXPRESSION,
			KappaPartitionScanner.STORY_EXPRESSION,
			KappaPartitionScanner.MODIFY_EXPRESSION 
		};
	}
	
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
	    ContentAssistant ca = new ContentAssistant();
	    
	    ca.enableAutoActivation(true);
	    ca.setShowEmptyList(true);
	    ca.setAutoActivationDelay(0);
	    ca.setStatusLineVisible(true);
	    ca.setStatusMessage("Kappa Model");
	    
	    IContentAssistProcessor ruleCP = new LabelCompletionProcessor(ca);
	    IContentAssistProcessor agentCR = new AgentCompletionProcessor(ca);
	    IContentAssistProcessor obsCR = new ObsCompletionProcessor(ca);
	    IContentAssistProcessor initCR = new InitCompletionProcessor(ca);
	    ca.setContentAssistProcessor(ruleCP, KappaPartitionScanner.MODIFY_EXPRESSION);
	    ca.setContentAssistProcessor(obsCR, KappaPartitionScanner.OBS_EXPRESSION);
	    ca.setContentAssistProcessor(initCR, KappaPartitionScanner.INIT_EXPRESSION);
	    ca.setContentAssistProcessor(ruleCP, KappaPartitionScanner.STORY_EXPRESSION);	    
	    ca.setContentAssistProcessor(agentCR, IDocument.DEFAULT_CONTENT_TYPE);	    
	    ca.setContentAssistProcessor(agentCR, KappaPartitionScanner.LABEL);	    
  
	    ca.setInformationControlCreator(getInformationControlCreator(sourceViewer));
	    
	    return ca;
	}

	public ITextDoubleClickStrategy getDoubleClickStrategy(
						ISourceViewer sourceViewer, String contentType) {
		
		if (doubleClickStrategy == null){
			doubleClickStrategy = new KappaDoubleClickStrategy();
		}
		return doubleClickStrategy;
	}

	/*
	 * We currently aren't using the hover functionality.
	 */

 	public ITextHover getTextHover(ISourceViewer sourceViewer,
			String contentType, int stateMask) {
		return new KappaTextHover(sourceViewer, contentType, stateMask);
	}

	public ITextHover getTextHover(ISourceViewer sourceViewer,
			String contentType) {
		return super.getTextHover(sourceViewer, contentType);
	}
	
	/*
	 * @see SourceViewerConfiguration#getAnnotationHover(ISourceViewer)
	 */
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
		return new AnnotationHover(sourceViewer);
	}

	
	protected KappaRuleScanner getLabelScanner() {
		if (labelScanner == null) {
			labelScanner = new KappaRuleScanner(colorManager);
			labelScanner.setDefaultReturnToken(SyntaxColors.
								LABEL_EXPRESSION_DEFAULT.token(colorManager));
		}
		return labelScanner;
	}

	protected KappaInitScanner getInitScanner() {
		if (initScanner == null) {
			initScanner = new KappaInitScanner(colorManager);
			initScanner.setDefaultReturnToken(SyntaxColors.
								INIT_EXPRESSION_DEFAULT.token(colorManager));
		}
		return initScanner;
	}

	protected KappaModifyScanner getModifyScanner() {
		if (modifyScanner == null) {
			modifyScanner = new KappaModifyScanner(colorManager);
			modifyScanner.setDefaultReturnToken(SyntaxColors.
								MODIFY_EXPRESSION_DEFAULT.token(colorManager));
		}
		return modifyScanner;
	}

	protected KappaStoryScanner getStoryScanner() {
		if (storyScanner == null) {
			storyScanner = new KappaStoryScanner(colorManager);
			storyScanner.setDefaultReturnToken(SyntaxColors.
								STORY_EXPRESSION_DEFAULT.token(colorManager));
		}
		return storyScanner;
	}

	protected KappaObsScanner getObsScanner() {
		if (obsScanner == null) {
			obsScanner = new KappaObsScanner(colorManager);
			obsScanner.setDefaultReturnToken(SyntaxColors.
								OBS_EXPRESSION_DEFAULT.token(colorManager));
		}
		return obsScanner;
	}

	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();

		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getLabelScanner());
		reconciler.setDamager(dr, KappaPartitionScanner.LABEL);
		reconciler.setRepairer(dr, KappaPartitionScanner.LABEL);
		
		dr = new DefaultDamagerRepairer(getLabelScanner());
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		dr = new DefaultDamagerRepairer(getInitScanner());
		reconciler.setDamager(dr, KappaPartitionScanner.INIT_EXPRESSION);
		reconciler.setRepairer(dr, KappaPartitionScanner.INIT_EXPRESSION);
		
		dr = new DefaultDamagerRepairer(getModifyScanner());
		reconciler.setDamager(dr, KappaPartitionScanner.MODIFY_EXPRESSION);
		reconciler.setRepairer(dr, KappaPartitionScanner.MODIFY_EXPRESSION);
		
		dr = new DefaultDamagerRepairer(getObsScanner());
		reconciler.setDamager(dr, KappaPartitionScanner.OBS_EXPRESSION);
		reconciler.setRepairer(dr, KappaPartitionScanner.OBS_EXPRESSION);
		
		dr = new DefaultDamagerRepairer(getStoryScanner());
		reconciler.setDamager(dr, KappaPartitionScanner.STORY_EXPRESSION);
		reconciler.setRepairer(dr, KappaPartitionScanner.STORY_EXPRESSION);
		
		NonRuleBasedDamagerRepairer ndr = new NonRuleBasedDamagerRepairer(
							SyntaxColors.KAPPA_COMMENT.textAttribute(colorManager));
		reconciler.setDamager(ndr, KappaPartitionScanner.KAPPA_COMMENT);
		reconciler.setRepairer(ndr, KappaPartitionScanner.KAPPA_COMMENT);

		return reconciler;
	}

}