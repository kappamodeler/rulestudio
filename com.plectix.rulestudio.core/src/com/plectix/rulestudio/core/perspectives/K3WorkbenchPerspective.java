package com.plectix.rulestudio.core.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class K3WorkbenchPerspective implements IPerspectiveFactory {

	public final static String ID = K3WorkbenchPerspective.class.getCanonicalName();
	
	public void createInitialLayout(IPageLayout layout) {
		// Get the editor area.
		String 			editorArea = layout.getEditorArea();
		IFolderLayout 	topLeft = layout.createFolder("topLeft", IPageLayout.LEFT, 0.20f, editorArea);
		topLeft.addView(IPageLayout.ID_RES_NAV);
		
		IFolderLayout 	bottomLeft = layout.createFolder("bottomLeft", IPageLayout.BOTTOM, 0.5f, "topLeft");
		bottomLeft.addView(IPageLayout.ID_OUTLINE);
		//topLeft.addView(IPageLayout.ID_PROJECT_EXPLORER);
		
		IFolderLayout 	bottom = layout.createFolder("bottom", 
				IPageLayout.BOTTOM, 0.66f, editorArea);

		bottom.addView("com.plectix.rulestudio.views.rulevisualizer.RuleVisualizerView");
		bottom.addView(IPageLayout.ID_PROBLEM_VIEW);
		bottom.addView(IPageLayout.ID_PROGRESS_VIEW);


		IFolderLayout 	right = layout.createFolder("right", IPageLayout.RIGHT, 0.6f, editorArea);
		right.addView("com.plectix.rulestudio.views.contactmap.ContactMapView");
		right.addView("com.plectix.rulestudio.views.influencemap.InfluenceMapView");
		right.addView("com.plectix.rulestudio.views.compression.CompressionView");
		right.addView("com.plectix.rulestudio.views.reachable.ReachableView");
		right.addView("com.plectix.rulestudio.views.compile.CompileView");
	}
	
	/**
	 * This method is called when ever anyone need to open the workbench perspective
	 */
	public static void openPerspective(){
		IWorkbench				workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow 		window = workbench.getActiveWorkbenchWindow();		
		IPerspectiveRegistry 	reg = workbench.getPerspectiveRegistry();
        window.getActivePage().setPerspective(reg.findPerspectiveWithId(ID));

	}

}
