package com.plectix.rulestudio.branding.perspectives;

import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.intro.IIntroConstants;

/**
 * This class is used to create the welcome perspective for RuleStudio
 * 
 * @author bbuffone
 * 
 */
public class Welcome implements IPerspectiveFactory, IPerspectiveListener {

	public final static String ID = Welcome.class.getCanonicalName();
	public void createInitialLayout(IPageLayout layout) {
		// Get the editor area.
		layout.setEditorAreaVisible(false);
		
		PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.addPerspectiveListener(this);

	}

	/**
	 * When the spective is activated show the welcome view.  This isn't done by default.
	 */
	public void perspectiveActivated(IWorkbenchPage page, IPerspectiveDescriptor pers) {
		if (pers.getId() != null && pers.getId().equals(ID)) {
			PlatformUI.getWorkbench().getIntroManager().showIntro(null, false);
		}else{
		}
	}

	/**
	 * When the perspective is remove then close the welcome screen.
	 */
	public void perspectiveChanged(IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId) {
		// System.out.println(perspective.getId() + " " + changeId);
		if (perspective != null && perspective.getId().equals(ID)) {
			if (changeId != null && changeId.equals(IWorkbenchPage.CHANGE_VIEW_HIDE)) {
				PlatformUI.getWorkbench().getIntroManager().showIntro(null, false);
			}
		}else if (page != null){
			//TODO - This can cause an exception when closing.
	        IViewReference reference = page.findViewReference(IIntroConstants.INTRO_VIEW_ID);
            if (reference != null){
            	page.hideView(reference);
            }
		}
	}

}
