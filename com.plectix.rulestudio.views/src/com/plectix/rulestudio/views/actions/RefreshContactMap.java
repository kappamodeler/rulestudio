package com.plectix.rulestudio.views.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

import com.plectix.rulestudio.views.contactmap.ContactMapView;

public class RefreshContactMap implements IViewActionDelegate {

	private ContactMapView _contactMap = null;
	
	public void init(IViewPart view) {
		// TODO Auto-generated method stub
		if (view instanceof ContactMapView){
			_contactMap = (ContactMapView)view;
		}
	}

	public void run(IAction action) {
		// TODO Auto-generated method stub
		if (_contactMap != null)
			_contactMap.refresh();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub
	}

}
