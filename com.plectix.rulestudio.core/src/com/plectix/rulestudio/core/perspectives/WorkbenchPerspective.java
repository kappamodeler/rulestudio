package com.plectix.rulestudio.core.perspectives;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class WorkbenchPerspective implements IPerspectiveFactory {

	public final static String ID = WorkbenchPerspective.class.getCanonicalName();
	
	public void createInitialLayout(IPageLayout layout) {
		// Get the editor area.
		String 			editorArea = layout.getEditorArea();
		IFolderLayout 	topLeft = layout.createFolder("topLeft", IPageLayout.LEFT, 0.25f, editorArea);
		topLeft.addView(IPageLayout.ID_RES_NAV);

		IFolderLayout 	right = layout.createFolder("right", IPageLayout.RIGHT, 0.66f, editorArea);
		
		right.addView(IPageLayout.ID_OUTLINE);

		IFolderLayout 	bottomRight = layout.createFolder("bottomRight", 
										IPageLayout.BOTTOM, 0.50f, "right");
		
		bottomRight.addView("com.plectix.rulestudio.views.contactmap.ContactMapView");
		bottomRight.addView("com.plectix.rulestudio.views.influencemap.InfluenceMapView");
		bottomRight.addView("com.plectix.rulestudio.views.compression.CompressionView");
		bottomRight.addView("com.plectix.rulestudio.views.reachable.ReachableView");
		bottomRight.addView("com.plectix.rulestudio.views.compile.CompileView");

		IFolderLayout 	bottom = layout.createFolder("bottom", 
										IPageLayout.BOTTOM, 0.66f, editorArea);

		bottom.addView("com.plectix.rulestudio.views.rulevisualizer.RuleVisualizerView");
		bottom.addView(IPageLayout.ID_PROBLEM_VIEW);	
		bottom.addView(IPageLayout.ID_PROGRESS_VIEW);
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
