package com.plectix.rulestudio.editors.builders;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;

/**
 * Class supplies the actual fixing of the problem.
 * 
 * @author bbuffone
 *
 */
public class KappaFix implements IMarkerResolution {

	public String getLabel() {
		return "Add a \' to the label";
	}

	public void run(IMarker marker) {
	}

}
