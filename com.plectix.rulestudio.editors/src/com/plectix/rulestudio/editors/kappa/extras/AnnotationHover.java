package com.plectix.rulestudio.editors.kappa.extras;

import java.util.Iterator;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHoverExtension;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.LineRange;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.MarkerAnnotation;

public class AnnotationHover implements IAnnotationHover, IAnnotationHoverExtension {

	public AnnotationHover(ISourceViewer sourceViewer){
		
	}
	
	public String getHoverInfo(ISourceViewer sourceViewer, int lineNumber) {
		return "Test";
	}

	public boolean canHandleMouseCursor() {
		return false;
	}

	public IInformationControlCreator getHoverControlCreator() {
		return new IInformationControlCreator(){

			public IInformationControl createInformationControl(Shell parent) {
				return new DefaultInformationControl(parent, ""){
					
				};
			}
			
		};
	}

	/**
	 * We need to supply the string that will be displayed in the tooltip
	 */
	public Object getHoverInfo(ISourceViewer sourceViewer,
			ILineRange lineRange, int visibleNumberOfLines) {
		IAnnotationModel model = sourceViewer.getAnnotationModel();
		Iterator 	iterator = model.getAnnotationIterator();
		String		displayText = null;
		while (iterator.hasNext() == true){
			Object 	anno = iterator.next();
			if (anno instanceof MarkerAnnotation){
				MarkerAnnotation 	annotation = (MarkerAnnotation)anno;
				try {
					String	location = (String)annotation.getMarker().getAttribute(IMarker.LOCATION);
					if (location != null && location.trim().length() > 0){
						int intPos = location.indexOf(",");
						int lineNumber = Integer.parseInt(location.substring(5, intPos));
						if (lineRange.getStartLine() == (lineNumber - 1)){
							if (displayText == null){
								displayText = annotation.getText();
							}else{
								if (displayText.startsWith("Multiple markers") == false){
									displayText = "Multiple markers at this line:<br/> - " + displayText;
								}
								displayText += "<br/> - " + annotation.getText();
							}
						}
					}else{
						Integer lineNumber = (Integer)annotation.getMarker().getAttribute(IMarker.LINE_NUMBER);
						if (lineRange.getStartLine() == (lineNumber.intValue() - 1)){
							displayText = annotation.getText();
							break;
						}
					}
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}			
		}
		return displayText;
	}

	public ILineRange getHoverLineRange(ISourceViewer viewer, int lineNumber) {
		return new LineRange(lineNumber, 1);
	}

}
