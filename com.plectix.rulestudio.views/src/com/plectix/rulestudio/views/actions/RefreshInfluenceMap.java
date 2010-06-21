package com.plectix.rulestudio.views.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

import com.plectix.rulestudio.views.influencemap.InfluenceMapView;

public class RefreshInfluenceMap implements IViewActionDelegate {

	private InfluenceMapView _influenceMap = null;
	
	public void init(IViewPart view) {
		// TODO Auto-generated method stub
		if (view instanceof InfluenceMapView){
			_influenceMap = (InfluenceMapView)view;
		}
	}

	public void run(IAction action) {
		// TODO Auto-generated method stub
		if (_influenceMap != null)
			_influenceMap.refresh();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

}
