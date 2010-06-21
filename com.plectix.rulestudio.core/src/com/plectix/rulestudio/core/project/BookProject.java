package com.plectix.rulestudio.core.project;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;

import com.plectix.rulestudio.core.Activator;

public class BookProject implements IProjectNature {

	public final static String NATURE_ID = Activator.PLUGIN_ID + ".BookProject";

	public IProject 	_project = null;
	
	public BookProject(){
	}
	
	public void configure() throws CoreException {
	}

	public void deconfigure() throws CoreException {
	}

	public IProject getProject() {
		return _project;
	}

	public void setProject(IProject project) {
		_project = project;
	}
	
	public static boolean isModerizationProject(Object object) {
		IProject 	project = (IProject)object;
		boolean 	bFound = false;
		try {
			//See if this is a modernization projects
			String[] 	ids = project.getDescription().getNatureIds();
			for (int index = 0; index < ids.length; index++){
				if (BookProject.NATURE_ID.equals(ids[index]) == true){
					bFound = true;
					break;
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return bFound;
	}

}
