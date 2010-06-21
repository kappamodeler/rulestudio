package com.plectix.rulestudio.editors.view;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import com.plectix.rulestudio.editors.view.model.OutlineObject;

/**
 * This class providers the Kappa model elements to the Outline view.
 * 
 * @author bbuffone
 *
 */
public class OutlineContentProvider implements ITreeContentProvider {

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof OutlineObject){
			return ((OutlineObject)parentElement).getChildren();
		}
		return null;
	}

	public boolean hasChildren(Object element) {
		if (element instanceof OutlineObject){
			return getChildren(element) != null;
		}
		return false;
	}

	public Object getParent(Object element) {
		if (element instanceof OutlineObject){
			return ((OutlineObject)element).getParent();
		}
		return null;
	}

	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	/*
	 * NOT IMPLEMENTED
	 */
	public void dispose() {
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
	
}
