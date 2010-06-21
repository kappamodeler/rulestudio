package com.plectix.rulestudio.editors.builders;

import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

import com.plectix.rulestudio.editors.Activator;

/**
 * This class implements the build functionality for the kappa syntax checking. 
 * Kappa files will be validate on either a full or partial build.
 * 
 * @author bbuffone
 *
 */
public class KappaBuilder extends IncrementalProjectBuilder {

	/**
	 * The id of the builder has to be the "<plugin-id>.<builder-id>"
	 */
	public final static String ID = Activator.PLUGIN_ID+".KappaBuilder";
	public final static String KAPPA_FILE_EXTENSION = "ka";
	
	/* this is an eclise api */
	@SuppressWarnings("unchecked")
	protected IProject[] build(int kind, Map arg1, IProgressMonitor monitor)
		throws CoreException {
		
		if (kind == IncrementalProjectBuilder.FULL_BUILD) {
			fullBuild(monitor);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
			}
		}
		
		return null;
	}
		
	/**
	* Called when a full build needs to be done. I know I have work to
	* do so just build all the files needed.
	* 
	* @param monitor
	*/
	private void fullBuild(IProgressMonitor monitor){
		addResource(getProject(), monitor);
	}
	
	/**
	 * This method performs a half recursive loop through the project and validate all the file
	 * files in the project.
	 * @param resource
	 */
	private void addResource(IResource resource, IProgressMonitor monitor){
		if (resource.getType() == IResource.FOLDER ||
			resource.getType() == IResource.PROJECT){
			try {
				IResource[] resources = ((IContainer)resource).members();
				for (int index = 0; index < resources.length; index++){
					addResource(resources[index], monitor);
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}else if (resource.getType() == IResource.FILE &&
				  KAPPA_FILE_EXTENSION.equalsIgnoreCase(resource.getFileExtension()) == true){
			//check to see if I should validate this.
			validateResource((IFile)resource, monitor, new KappaSyntaxParser(true));
		}

	}
		
	/**
	* Called when the there is an incremental build kicked off.
	* 
	* @param delta
	* @param monitor
	* @throws CoreException
	*/
	private void incrementalBuild(IResourceDelta delta, final IProgressMonitor monitor) throws CoreException{
		
		delta.accept(new IResourceDeltaVisitor(){	
			public boolean visit(IResourceDelta resourceDelta) throws CoreException {
				IResource	resource = resourceDelta.getResource();
				if (resource.getType() == IResource.FILE &&
					KAPPA_FILE_EXTENSION.equalsIgnoreCase(resource.getFileExtension()) == true){
					//check to see if I should validate this.
					if (resource.exists() == true){
						validateResource((IFile)resource, monitor, new KappaSyntaxParser(true));
					}

				}
				return true;			
			}
		});
		
	}
	
	/**
	 * Hand of the file to the syntax validator class to check.
	 * 
	 * @param file
	 * @param monitor
	 * @param kappaSyntax
	 */
	private void validateResource(IFile file, IProgressMonitor monitor, KappaSyntaxParser kappaSyntax){	
		try{
			//Delete the old markers
			//TODO look at this code is seems infinite loop
			file.deleteMarkers(Activator.MARKER_TYPE, true, IResource.DEPTH_INFINITE);

			monitor.subTask("Validating  " + file.getProjectRelativePath().toString());
			kappaSyntax.validateFile(file);
			monitor.worked(1);
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	
	
}
