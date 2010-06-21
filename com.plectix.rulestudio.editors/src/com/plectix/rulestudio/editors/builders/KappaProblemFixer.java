package com.plectix.rulestudio.editors.builders;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;

/**
 * Class supplies the list of all possible fixes.
 * 
 * @author bbuffone
 *
 */
public class KappaProblemFixer implements IMarkerResolutionGenerator  {

	public IMarkerResolution[] getResolutions(IMarker marker) {
		
		IMarkerResolution[] resolvers = new IMarkerResolution[1];
		resolvers[0] = new KappaFix();
		return resolvers;
	}

}
