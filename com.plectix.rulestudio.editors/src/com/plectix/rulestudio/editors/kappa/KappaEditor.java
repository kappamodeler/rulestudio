package com.plectix.rulestudio.editors.kappa;

import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

import com.plectix.rulestudio.core.usagedata.Action;
import com.plectix.rulestudio.core.usagedata.UsageDataCollector;
import com.plectix.rulestudio.editors.kappa.extras.ColorManager;
import com.plectix.rulestudio.editors.view.EditorOutlinePage;

public class KappaEditor extends TextEditor {

	public final static String SYNTAX_CHANGES = "SYNTAX_CHANGES";
	public final static String LOC_MARKER = "com.plectix.rulestudio.editors.annotations";
	private ColorManager 		_colorManager = null;
	private EditorOutlinePage 	_outlinePage = null;
	private KappaDocumentProvider _documentProvider = null;
	
	/*
	 * CONSTUCTOR
	 */
	public KappaEditor() {
		super();
		
		_colorManager = new ColorManager();
		setSourceViewerConfiguration(new KappaConfiguration(_colorManager));
		_documentProvider = new KappaDocumentProvider(this);
		setDocumentProvider(_documentProvider);
		
		UsageDataCollector.getInstance().addOneTimeAction(Action.EDITOR_KAPPA_FILE_OPEN);
		
		//This makes many things not work, annotations, colors
		//setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}
	
	@Override
	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		
		if (input instanceof FileEditorInput) {
			String name = input.getName();
			UsageDataCollector.getInstance().addOneTimeActionWithLabel(Action.WORKSPACE_KAPPA_FILE, name);
		}

		//close is page.
		if (_documentProvider.pleaseClose() == true){
			final IEditorPart part = this;
			Display.getCurrent().asyncExec(new Runnable(){
				public void run(){
					IWorkbenchPage page = PlatformUI.getWorkbench().
						getActiveWorkbenchWindow().
						getActivePage();
						page.closeEditor(part, false);
				}
			});
		}
	}

	public void dispose() {
		_colorManager.dispose();
		super.dispose();
		UsageDataCollector.getInstance().addOneTimeAction(Action.EDITOR_KAPPA_FILE_CLOSE);
	}
	
	/* eclise api */
	@Override
	@SuppressWarnings("unchecked")
	public Object getAdapter(Class required){
	    if (IContentOutlinePage.class.equals(required)){
	        if (_outlinePage == null){
	            _outlinePage = new EditorOutlinePage(this);
	            if (getEditorInput() != null){
	                _outlinePage.setInput(getEditorInput());
	            }
	        }
	        return _outlinePage;
	    }
	    return super.getAdapter(required);
	}

	protected void doRestoreState(IMemento memento) {
		super.doRestoreState(memento);
		if (_outlinePage != null){
			_outlinePage.refresh();
		}
	}

	public void doSave(IProgressMonitor progressMonitor) {
		super.doSave(progressMonitor);
		if (_outlinePage != null){
			_outlinePage.refresh();
		}
	}

	public void doSaveAs() {
		super.doSaveAs();
		if (_outlinePage != null){
			_outlinePage.refresh();
		}
	}
	
	@Override
	protected void editorSaved() {
		super.editorSaved();
		UsageDataCollector.getInstance().addOneTimeAction(Action.EDITOR_KAPPA_FILE_SAVE);
	}
	
	/**
	 * This method will select/highlight the text that is passed as a parameter.
	 * If the text can't be found nothing happens.
	 * 
	 * @param label - the word highlight.
	 * @param search - the value that will be searched for in the document.
	 * This is different because we will search for states "~state" 
	 */
	@SuppressWarnings("unchecked")		// eclipse api
	public void highlightKeyword(String label, String search){
		
		if (search == null || search.length() == 0) return;
		
		IDocumentProvider 	documentProvider= getDocumentProvider();
		if (documentProvider == null) return;

		String 				content = getSourceViewer().getDocument().get();	
		int 				intPos = content.indexOf(search);
		IAnnotationModel 	annotationModel = documentProvider.getAnnotationModel(getEditorInput());
		
		if (intPos != -1){
			
			//highlight and select the first occurrence in the document.
			setHighlightRange(intPos, search.length(), false);
			showHighlightRangeOnly(false);
			this.selectAndReveal(intPos, search.length());
			
			//clear all the old annotations.
			Iterator iterator = annotationModel.getAnnotationIterator();
			while (iterator.hasNext() == true){
				Annotation anno = (Annotation)iterator.next();
				if (anno.getType().equals(LOC_MARKER)) {
					annotationModel.removeAnnotation(anno);
				}
			}
				
			//loop over the content and highlight all the occurrences of the selected label.
			while ((intPos = content.indexOf(search, (intPos + search.length()))) != -1){		
				
				//Need to adjust if this is a state;
				intPos = search.startsWith("~") ? intPos + 1 : intPos;
				
				annotationModel.addAnnotation(new Annotation(LOC_MARKER, 
									true, "Location of " + search), 
									new Position(intPos, search.length()));
			}

		}else{
			resetHighlightRange();
		}

	}
	
	public String getContent() {
		IDocumentProvider 	documentProvider= getDocumentProvider();
		if (documentProvider == null) 
			return "";

		String content = getSourceViewer().getDocument().get();	
		return content;
	}
	
	public void highlightAndMoveTo(int offset, int len) {
		setHighlightRange(offset, len, false);
		showHighlightRangeOnly(false);
		this.selectAndReveal(offset, len);

	}
	
	public IAnnotationModel getAnnotationModel() {
		IDocumentProvider 	documentProvider= getDocumentProvider();
		if (documentProvider == null) 
			return null;

		return documentProvider.getAnnotationModel(getEditorInput());

	}
	
	public IFile getFile() {
		IFileEditorInput 	input = (IFileEditorInput)getEditorInput();
		return input.getFile();

	}

	/**
	 * Find rule text within a file.  The input has no spaces while the editor may have
	 * spaces in it.  
	 * @param toFind Rule text to find.
	 * 
	 */
	public boolean highlightRuleText(String toFind) {
		if (toFind == null || toFind.length() == 0)
			return false;
		
		IDocumentProvider 	documentProvider= getDocumentProvider();
		if (documentProvider == null) 
			return false;

		boolean ret = false;
		String content = getSourceViewer().getDocument().get();	
		char start = toFind.charAt(0);
		int from = 0;
		for (int offset = 0; offset!= -1; offset = content.indexOf('\n', from)) {
			from = content.indexOf(start, offset);
			if (from == -1)
				break;					// done
			ret = ret || findInstance(content, from, toFind);
		}
		return ret;
	}
		
	private boolean findInstance(String content, int from, String toFind) {
		int fromNl = content.lastIndexOf('\n', from);
		int fromComment = content.lastIndexOf('#', from);
		if (fromComment > fromNl) { // in a comment
			return false;
		}
		int to = from + 1;
		boolean inBond = false;
		int index = 1;
		while (index < toFind.length() && to < content.length()) {
			char matchCh = toFind.charAt(index);
			char ch = content.charAt(to++);
			if (inBond && (ch == ',' || ch == ')')) {
				inBond = false;
			}
			if (ch == '<')
				continue;				// ignore <-> rule
			if (ch == matchCh || inBond) {
				++index;
				if (ch == '!')
					inBond = true;
			} else if (Character.isWhitespace(ch) || ch == '\\') {
				;
			} else {
				return false;
			}
		}
		if (index == toFind.length()) {
			highlightAndMoveTo(from, to - from);
			return true;
		}
		return false;
	}
	
}
