package com.plectix.rulestudio.editors.view;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

import com.plectix.rulestudio.editors.view.model.OutlineObject;

/**
 * This class connects the displayed information of the outline view
 * to the kappa model objects. We simply delegate to the model objects.
 * 
 * @author bbuffone
 *
 */
public class OutlineLabelProvider implements ILabelProvider {

	public Image getImage(Object object) {
		if (object instanceof OutlineObject)
			return ((OutlineObject)object).getImage();
		return null;
	}

	public String getText(Object object) {
		if (object instanceof OutlineObject)
			return ((OutlineObject)object)._label;
		return "";
	}

	public void addListener(ILabelProviderListener listener) {
	}

	public void dispose() {
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
	}

}
