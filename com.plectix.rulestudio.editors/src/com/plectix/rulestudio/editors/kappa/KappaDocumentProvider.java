package com.plectix.rulestudio.editors.kappa;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.FileEditorInput;

import com.plectix.rulestudio.core.wizards.resource.KappaFile;
import com.plectix.rulestudio.editors.Activator;
import com.plectix.rulestudio.editors.builders.KappaSyntaxParser;
import com.plectix.rulestudio.editors.kappa.scanners.KappaPartitionScanner;

public class KappaDocumentProvider extends FileDocumentProvider {

	protected IEditorPart _editorPart = null;
	private boolean 	_pleaseClose = false;
	private FixUpError _fixUp = null;
	
	public KappaDocumentProvider(IEditorPart editorPart){
		_editorPart = editorPart;
		_fixUp = new FixUpError(this);
	}
	
	public boolean pleaseClose(){
		return _pleaseClose;
	}
	
	protected IDocument createDocument(Object element) throws CoreException {
		IDocument document = super.createDocument(element);
		if (document != null) {
			IDocumentPartitioner partitioner =
				new FastPartitioner(
					new KappaPartitionScanner(),
					new String[] {
						KappaPartitionScanner.LABEL,
						KappaPartitionScanner.OBS_EXPRESSION,
						KappaPartitionScanner.INIT_EXPRESSION,
						KappaPartitionScanner.STORY_EXPRESSION,
						KappaPartitionScanner.MODIFY_EXPRESSION,
						KappaPartitionScanner.KAPPA_COMMENT });
			partitioner.connect(document);
			document.setDocumentPartitioner(partitioner);
			document.addDocumentListener(new KappaDocumentListener());
		}
		return document;
	}
	
	protected boolean setDocumentContent(IDocument document, IEditorInput editorInput, String encoding) throws CoreException {
		if (editorInput instanceof FileStoreEditorInput){	
			FileStoreEditorInput input = (FileStoreEditorInput)editorInput;
			InputStream contentStream = null;
			
			try {
				contentStream = input.getURI().toURL().openStream();
				setDocumentContent(document, contentStream, encoding);
			
				//We want to ask user if they want to import the file.
				String asked = Activator.getDefault().getPreferenceStore().getString(input.getURI().getPath());
				if ("true".equals(asked) == false){
					_pleaseClose = MessageDialog.openQuestion(Display.getCurrent().getActiveShell(), 
							"RuleStudio - Opening External File", 
							"You are opening a file external to your workspace, would you like to import it? " +
							"If you answer NO, the file will be open in read only move.");
					
					if (_pleaseClose == true){
						contentStream = input.getURI().toURL().openStream();
						importFile(contentStream, input.getURI());
					}

					//We want to track if we asked so we don't annoy the use with the same question.
					Activator.getDefault().getPreferenceStore().setValue(input.getURI().getPath(), "true");
				}else{
					//We want to track if we asked so we don't annoy the use with the same question.
					Activator.getDefault().getPreferenceStore().setValue(input.getURI().getPath(), "false");
				}
				

			} catch (IOException ex) {
				String message= (ex.getMessage() != null ? ex.getMessage() : ""); //$NON-NLS-1$
				IStatus s= new Status(IStatus.ERROR, PlatformUI.PLUGIN_ID, IStatus.OK, message, ex);
				throw new CoreException(s);
			} finally {
				try {
					contentStream.close();
				} catch (IOException e1) {
				}
			}
						
			return true;
		}
		return super.setDocumentContent(document, editorInput, encoding);
	}

	/**
	 * Called when the user wants to import the file into the workspace.
	 * @param contentStream
	 * @param uri
	 */
	public void importFile(InputStream contentStream, URI uri){
		KappaFile	wizard = new KappaFile();	
		wizard.setInputStream(contentStream);
		String fileName = uri.getPath();
		int intPos = fileName.lastIndexOf("/");
		if (intPos != -1){
			fileName = fileName.substring(intPos + 1);
			intPos = fileName.indexOf(".");
			fileName = fileName.substring(0, intPos);
		}else{
			fileName = "";
		}
		wizard.setFileName(fileName);
		
		wizard.init(null, new StructuredSelection());	
		WizardDialog 			dlg = new WizardDialog(Display.getCurrent().getActiveShell(), 
													  (IWizard)wizard);	
		int						iResult = dlg.open();
		if (iResult == WizardDialog.OK){
			try {
				IFile file = wizard.getFile();
				FileEditorInput input = new FileEditorInput(file);
				IWorkbenchPage page = PlatformUI.getWorkbench().
							getActiveWorkbenchWindow().
							getActivePage();
				page.openEditor(input, KappaEditor.class.getCanonicalName());
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public class KappaDocumentListener implements IDocumentListener {

		public void documentAboutToBeChanged(DocumentEvent event) {
			_fixUp.updateMarkers(event);
		}

		public void documentChanged(DocumentEvent event) {
			_fixUp.addEvent(event);
			
		}
		
	}
	
	public static class FixUpError extends Job {
		private ConcurrentLinkedQueue<DocumentEvent> changes;
		private ArrayList<DocumentEvent> todo;
		private KappaEditor ke;
		
		private FixUpError(KappaDocumentProvider kdp) {
			super("Update Kappa Errors");
			changes = new ConcurrentLinkedQueue<DocumentEvent>();
			todo = new ArrayList<DocumentEvent>();
			this.ke = (KappaEditor)kdp._editorPart;
			setUser(false);
			setSystem(true);
		}
		
		protected void addEvent(DocumentEvent event) {
			changes.add(event);
			schedule();
		}
		
		protected void updateMarkers(DocumentEvent event) {
			int offset = event.getOffset();
			int len = event.getText().length() - event.getLength();
			IFile file = ke.getFile();
			String content = ke.getContent();
			int from = getStart(content, event.getOffset());
			int to = getEnd(content, event.getOffset() + event.getLength());
			try {
				IMarker[] markers = file.findMarkers(Activator.MARKER_TYPE,
						false, IResource.DEPTH_ZERO);
				for (IMarker mark : markers) {
					int start = mark.getAttribute(IMarker.CHAR_START,
							Integer.MAX_VALUE);
					int end = mark.getAttribute(IMarker.CHAR_END, -1);
					if (to > start && from <= end) {
						mark.delete();
					} else {
						if (start >= offset) {
							start += len;
							mark.setAttribute(IMarker.CHAR_START, start);
						}
						if (end >= offset) {
							end += len;
							mark.setAttribute(IMarker.CHAR_END, end);
						}
						if (end < start)
							mark.delete();
					}
				}
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			DocumentEvent event = changes.poll();
			if (event == null)
				return Status.OK_STATUS;
			todo.add(event);
			KappaSyntaxParser kp = new KappaSyntaxParser(true);
			IFile file = ke.getFile();
			while (todo.size() > 0) {
				event = todo.remove(0);
				String content = ke.getContent();
				int from = getStart(content, event.getOffset());
				int to = getEnd(content, event.getOffset()
						+ event.getText().length());
				int lineNum = getLineNumber(content, to);
				// removeMarker(from, to, content.length());
				kp.validateDocString(file, from, findContent(content, from, to),
						lineNum);
				while ((event = changes.poll()) != null) {
					if (event.getOffset() < from || event.getOffset() > to) {
						todo.add(event);
					}
				}
			}
			return Status.OK_STATUS;
		}
		
		private String findContent(String content, int from, int to) {
			if (from < 0)
				from = 0;
			if (to > content.length())
				to = content.length();
			
			return content.substring(from, to);
		}

		private int getEnd(String content, int offset) {
			for (; offset < content.length(); ++offset) {
				if (content.charAt(offset) == '\n') {
					return offset+1;
				} else if (content.charAt(offset) == '\\') {
					int more = 1;
					if (offset+1 < content.length() && content.charAt(offset+1) == '\r') {
						++more;
					}
					if (offset+more < content.length() && content.charAt(offset+more) == '\n') {
						offset += more;				// skip crlf
					}
				}
			}
			return offset;
		}

		private int getStart(String content, int offset) {
			if (offset >= content.length())
				offset = content.length() - 1;
			for (; offset > 0; --offset) {
				if (content.charAt(offset) == '\n') {
					int delta = 1;
					if (offset > 1 && content.charAt(offset-1) == '\r') {
						++delta;
					}
					if (offset > delta && content.charAt(offset-delta) == '\\') {
						offset -= delta;
						continue;
					}
					return offset+1;
				}
			}
			return 0;
		}
		
		private int getLineNumber(String content, int to) {
			int ret = 0;				// 0 index line
			int offset = 0;
			do {
				offset = content.indexOf('\n', offset+1);
				++ret;
			} while (offset != -1 && offset < to);
			return ret;
		}
	}

}