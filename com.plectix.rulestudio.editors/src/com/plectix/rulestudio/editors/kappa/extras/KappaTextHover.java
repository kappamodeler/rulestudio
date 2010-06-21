package com.plectix.rulestudio.editors.kappa.extras;

import org.eclipse.jface.text.AbstractInformationControl;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextHoverExtension2;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.Geometry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

public class KappaTextHover implements ITextHover, ITextHoverExtension, ITextHoverExtension2 {

	public KappaTextHover(ISourceViewer sourceViewer, String contentType,
			int stateMask) {
	}

	public KappaTextHover(ISourceViewer sourceViewer, String contentType) {
	}

	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		return "Model";
	}

	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		Point selection = textViewer.getSelectedRange();
		if (selection.x <= offset && offset < selection.x + selection.y)
			return new Region(selection.x, selection.y);
		return new Region(offset, 0);
	}

	public Object getHoverInfo2(final ITextViewer textViewer, final IRegion hoverRegion) {
		int 	offset = hoverRegion.getOffset();
		int 	length = hoverRegion.getLength();				
		String 	string = "";
		
		try {
			string = extractKappaLine(textViewer, offset, length);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
				
		return "<html><h1>" + string + "</h1></html>";
	}

	/*
	public Object getHoverInfo2(final ITextViewer textViewer, final IRegion hoverRegion) {
		Display.getDefault().syncExec(new Runnable() {
			public void run(){
				
				int 	offset = hoverRegion.getOffset();
				int 	length = hoverRegion.getLength();				
				String 	string = "";
				
				try {
					string = extractKappaLine(textViewer, offset, length);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
				
				RuleVisualizerView view = (RuleVisualizerView)PlatformUI.getWorkbench().
				getActiveWorkbenchWindow().getActivePage().findView(RuleVisualizerView.ID);

				if (string.trim().length() == 0){
					view.changeRule("No label line has been selected");	
				}else{					
					view.changeRule(string);	
				}
			}
		});

		return "Information in the Rule Visualizer has been updated";
	}
	*/

	private static class MyInformationControl extends AbstractInformationControl{

		private static Browser 	_browser = null;
		
		public MyInformationControl(Shell parent) {
			super(parent, true);
			create();
		}

		protected void createContent(Composite parent) {
			parent.setLayout(new FillLayout());
			_browser = new Browser(parent, SWT.NULL);
		}
		
		/*
		 * @see IInformationControl#setInformation(String)
		 */
		public void setInformation(String content) {
			System.out.println("setInformation = " + _browser.hashCode() + " - " + content);
			if (_browser != null){
				_browser.setText(content);
			}
		}
		
		/*
		 * @see org.eclipse.jface.text.AbstractInformationControl#computeTrim()
		 */
		public Rectangle computeTrim() {
			if (_browser != null){				
				return Geometry.add(new Rectangle(0,0,0,0), _browser.computeTrim(0, 0, 300, 200));
			}else{
				return new Rectangle(0,0,0,0);
			}
		}

		/*
		 * @see IInformationControlExtension#hasContents()
		 */
		public boolean hasContents() {
			if (_browser != null){
				System.out.println("hasContents = " + _browser.hashCode() + " - " + _browser.getText());
				return true; //_browser.getText().length() > 0;
			}else{
				return false;
			}
		}

		/*
		 * @see IInformationControl#computeSizeHint()
		 */
		public Point computeSizeHint() {
			// see: https://bugs.eclipse.org/bugs/show_bug.cgi?id=117602
			int widthHint= 300;
			return getShell().computeSize(widthHint, 200, true);
		}
		
		/*
		 * @see IInformationControl#setVisible(boolean)
		 */
		public void setVisible(boolean visible) {
			if (getShell().isVisible() == visible)
				return;

			getShell().setVisible(visible);
		}


	}
	
	public IInformationControlCreator getHoverControlCreator() {
		return new IInformationControlCreator(){

			public IInformationControl createInformationControl(Shell parent) {
				return new MyInformationControl(parent);
			}
			
		};
	}

	/**
	 * Pull out the full kappa line.  Need to find the starting lin
	 * return and the ending line return.
	 * 
	 * @param textViewer
	 * @param offset
	 * @param length
	 * @return
	 * @throws BadLocationException 
	 */
	private String extractKappaLine(ITextViewer textViewer, 
										int offset, int length) throws BadLocationException {
		String 	string = textViewer.getDocument().get();
		int		startPos = 0;
		int		endPos = string.length() - 1;
		
		//Find the starting line return for the line.
		for (int index = offset; index > 0; index--){
			if (string.charAt(index) == '\n'){
				startPos = index;
				break;
			}
		}

		//Find the ending line return for the line.
		for (int index = offset; index < string.length(); index++){
			if (string.charAt(index) == '\n'){
				endPos = index;
				break;
			}
		}
		if (startPos >= endPos){
			return "";
		}else{
			return textViewer.getDocument().get(startPos, (endPos-startPos));
		}
	}

}
