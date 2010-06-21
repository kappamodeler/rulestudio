package com.plectix.rulestudio.core.actions;

import org.eclipse.jface.action.Action;

import com.plectix.rulestudio.core.perspectives.K3WorkbenchPerspective;

public class OpenWorkbenchPerspective extends Action {

	public void run(){
		K3WorkbenchPerspective.openPerspective();
	}
		
}
