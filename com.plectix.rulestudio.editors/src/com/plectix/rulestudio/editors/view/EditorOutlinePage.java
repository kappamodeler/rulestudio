package com.plectix.rulestudio.editors.view;

import java.util.ArrayList;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import com.plectix.rulestudio.editors.builders.KappaSyntaxParser;
import com.plectix.rulestudio.editors.kappa.KappaEditor;
import com.plectix.rulestudio.editors.view.model.AgentObject;
import com.plectix.rulestudio.editors.view.model.OutlineObject;
import com.plectix.rulestudio.editors.view.model.RuleObject;

public class EditorOutlinePage extends ContentOutlinePage {

	private final static String RULE_EXT_ID = "com.plectix.rulestudio.editor.changerule";
	private final static String AGENT_EXT_ID = "com.plectix.rulestudio.editor.changeagent";
	public final static ImageDescriptor COLLAPSE_ALL = ImageDescriptor.createFromFile(EditorOutlinePage.class, 
												"collapseall.gif");

	public final static ImageDescriptor LINK_TO_RULE_VIS = ImageDescriptor.createFromFile(EditorOutlinePage.class, 
												"synced.gif");

	private OutlineContentProvider 	_outlineContentProvider = null;
	private OutlineLabelProvider	_outlineLabelProvider = null;

	private KappaEditor 	_editor = null;
	private IEditorInput	_editorInput = null;
	private OutlineObject	_kappaModel = null;
	private Action 			_linkToRuleVisualizer = null;
	private static ArrayList<String> ruleChangers = null;
	private static ArrayList<String> agentChangers = null;
	
	/*
	 * CONSTUCTOR
	 */
	public EditorOutlinePage(KappaEditor editor){
		_editor = editor;
		if (ruleChangers == null) {
			ruleChangers = new ArrayList<String>();
			processExtension(RULE_EXT_ID, ruleChangers);
		}
		if (agentChangers == null) {
			agentChangers = new ArrayList<String>();
			processExtension(AGENT_EXT_ID, agentChangers);
		}
	}
	
	/**
	 * Called when the outline is created. need to setup the
	 * data providers.
	 */
	public void createControl(Composite parent) {
		super.createControl(parent);
		
		_outlineContentProvider = new OutlineContentProvider();
		_outlineLabelProvider = new OutlineLabelProvider();
		this.getTreeViewer().setContentProvider(_outlineContentProvider);
		this.getTreeViewer().setLabelProvider(_outlineLabelProvider);	
		
		this.getTreeViewer().addPostSelectionChangedListener(
				new ISelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent event) {
						IStructuredSelection selection = (IStructuredSelection) event
								.getSelection();
						if (selection != null) {
							OutlineObject outlineObject = (OutlineObject) selection
									.getFirstElement();
							if (outlineObject != null) {
								// Hightlight the keyword selected
								_editor.highlightKeyword(outlineObject
										.getLabel(), outlineObject.getSearch());
							}

							// If this is a rule then update the rule editor
							// when the selection
							// changes to a new rule. Make sure that the user
							// also wants this to happen.
							if (outlineObject != null
									&& outlineObject instanceof RuleObject) {

								String rn = ((RuleObject) outlineObject)
										.getRuleName();
								String rd = ((RuleObject) outlineObject)
										.getRuleContent();

								for (String id : ruleChangers) {
									Object view = PlatformUI.getWorkbench()
											.getActiveWorkbenchWindow()
											.getActivePage().findView(id);
									if (view instanceof RuleChangeListener) {
										((RuleChangeListener) view).changeRule(
												_editor, rn, rd);
									}
								}
							} else if (outlineObject != null
									&& outlineObject instanceof AgentObject) {

								String name = outlineObject.getName();

								for (String id : agentChangers) {
									Object view = PlatformUI.getWorkbench()
											.getActiveWorkbenchWindow()
											.getActivePage().findView(id);
									if (view instanceof AgentChangeListener) {
										((AgentChangeListener) view).changeAgent(
												_editor, name);
									}
								}
							}
						}
					}
				});
		refresh();
	}
	
	public void setInput(IEditorInput input){
		_editorInput = input;
	}
	
	/**
	 * Called when we want to update the display.  This happens
	 * when the outline is created and when the editor is saved.
	 */
	public void refresh(){			
		//We don't want to update if the outline has been closed.
		if (getTreeViewer().getTree().isDisposed() == true) return;
		
		getTreeViewer().getTree().setRedraw(false);
		try{
			KappaSyntaxParser	kappaSyntax = new KappaSyntaxParser(false);
			
			//The Editor input is not always a FileEditorInput
			if (_editorInput instanceof FileEditorInput){
				kappaSyntax.validateFile(((FileEditorInput)_editorInput).getFile());
			}else if (_editorInput instanceof FileStoreEditorInput){
				FileStoreEditorInput input = (FileStoreEditorInput)_editorInput;
				kappaSyntax.validateURI(input.getURI());				
			}
			
			//Get the list of expanded elements
			Object[] previousExpanded = getTreeViewer().getExpandedElements(); 

			_kappaModel = kappaSyntax.getKappaModel();
			getTreeViewer().setInput(_kappaModel);
			
			OutlineObject[] children = _kappaModel.getChildren();
			if (previousExpanded != null && children != null){
				updateExpanded(children, previousExpanded);
			}
			
		}finally{
			this.getTreeViewer().getTree().setRedraw(true);
		}
	}
	
	/**
	 * We override the is method to add our actions to the toolbar and 
	 * menubars
	 */
	public void makeContributions(IMenuManager menuManager,
			IToolBarManager toolBarManager, IStatusLineManager statusLineManager) {

		Action collapseAll = new Action("Collapse All", COLLAPSE_ALL){

			public void run() {
				getTreeViewer().collapseAll();
			}
			
		};

		collapseAll.setToolTipText("Collapse all the items in the outline view.");
		toolBarManager.add(collapseAll);
		menuManager.add(collapseAll);
		
		_linkToRuleVisualizer = new Action("Link to Rule Visualizer", LINK_TO_RULE_VIS){

			public void run() {
			}
			
		};
		_linkToRuleVisualizer.setImageDescriptor(LINK_TO_RULE_VIS);
		_linkToRuleVisualizer.setChecked(true);
		collapseAll.setToolTipText("Changes in the outline view will update the Rule Visualizer.");
		toolBarManager.add(_linkToRuleVisualizer);
		menuManager.add(_linkToRuleVisualizer);

		super.makeContributions(menuManager, toolBarManager, statusLineManager);
	}

	//
	// PRIVATE METHODS
	//
	private void updateExpanded(OutlineObject[] currentItems, Object[] previousExpanded){
		for (int index = 0; index < currentItems.length; index++){
			
			//Update the expanded state.
			expand(currentItems[index], previousExpanded);
			
			OutlineObject[] children2 = (OutlineObject[])currentItems[index].getChildren();
			if (children2 != null){
				updateExpanded(children2, previousExpanded);
			}
		}
	}
	
	/**
	 * Check to the if the treeItem should be in the expanded state.
	 * 
	 * @param treeItem
	 * @param previousExpanded
	 */
	private void expand(OutlineObject treeItem, Object[] previousExpanded){
		for (int index = 0; index < previousExpanded.length; index++){
			if (treeItem.equals(previousExpanded[index]) == true){
				getTreeViewer().setExpandedState(treeItem, true);
			}
		}
	}
	
	private void processExtension(String id, ArrayList<String> listeners) {
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint(id);

		// check: Any <extension> tags for our extension-point?
		if (point != null) {
			IExtension[] extensions = point.getExtensions();

			for (int i = 0; i < extensions.length; i++) {
				IConfigurationElement[] ces = extensions[i].getConfigurationElements();

				for (int j = 0; j < ces.length; j++) {
					String vid = ces[j].getAttribute("viewid");
					if (vid != null)
						listeners.add(vid);
				}
			}
		}

	}
	
	
}
