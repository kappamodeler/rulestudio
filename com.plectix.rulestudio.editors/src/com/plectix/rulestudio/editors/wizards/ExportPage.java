package com.plectix.rulestudio.editors.wizards;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class ExportPage extends WizardPage {

	private		Text		_modelName = null;
	private 	Text		_bookName = null;
	private 	Text		_email = null;
	private 	Text		_key = null;
	private 	Button		_openBrowser = null;
	private 	String		_fileName = "";
	
	protected ExportPage(String fileName) {
		super("Upload Model");
		_fileName = fileName;
	}
	
	public boolean openBrowser(){
		return _openBrowser.getSelection();
	}
	
	public String getBookName(){
		return _bookName.getText();
	}
	
	public String getEmail(){
		return _email.getText();
	}
	
	public String getAPIKey(){
		return _key.getText();
	}
	
	public void createControl(Composite parent) {

		Composite 		container = new Composite(parent, SWT.NULL);
		GridLayout		gridLayout = new GridLayout(2, false);
		container.setLayout(gridLayout);

		Label	label = new Label(container, SWT.NULL);
		label.setText("Model:");
		
		 _modelName = new Text(container, SWT.BORDER);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		_modelName.setLayoutData(gridData);
		_modelName.addModifyListener(new ModifyListener(){

			public void modifyText(ModifyEvent arg0) {
				validate();				
			}
			
		});
		
		label = new Label(container, SWT.NULL);
		label.setText("New Book Name:");
		
		_bookName = new Text(container, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		_bookName.setLayoutData(gridData);
		_bookName.addModifyListener(new ModifyListener(){

			public void modifyText(ModifyEvent arg0) {
				validate();				
			}
			
		});
		
		label = new Label(container, SWT.NULL);
		label.setText("RuleBase Login:");
		
		_email = new Text(container, SWT.BORDER);
		 gridData = new GridData(GridData.FILL_HORIZONTAL);
		 _email.setLayoutData(gridData);
		 _email.addModifyListener(new ModifyListener(){

			public void modifyText(ModifyEvent arg0) {
				validate();
				
			}
			
		});
		 
		label = new Label(container, SWT.NULL);
		label.setText("API Key:");
			
		_key = new Text(container, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		_key.setLayoutData(gridData);
		_key.addModifyListener(new ModifyListener(){

				public void modifyText(ModifyEvent arg0) {
					validate();
					
				}
				
		});	
		
				
		_openBrowser = new Button(container, SWT.CHECK);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		_openBrowser.setText("Open a Web browser to Celludicate when finished");
		_openBrowser.setLayoutData(gridData);

		init();
		
		setPageComplete(true);		
		setControl(container);
	}
	
	public void init(){
		IPreferenceStore store = com.plectix.rulestudio.core.Activator.getDefault().getPreferenceStore();
		String		email = store.getString("cellucidate_email");
		String		oldAccountData = store.getString("cellucidate_api_key");
		boolean		openBrowser = store.getBoolean("cellucidate_open_browser");

		_email.setText(email);
		_key.setText(oldAccountData);
		_openBrowser.setSelection(openBrowser);
		_modelName.setText(_fileName);
		
		int intPos = _fileName.lastIndexOf(".");
		if (intPos != -1){
			_fileName = _fileName.substring(0, intPos);
		}
		_bookName.setText(_fileName);		
	}
	
	public void validate(){
		if (validateModelName() == true) {
		if (validateBookName() == true){
			if (validateEmail() == true){
				if (validateApiKey() == true){
					setPageComplete(true);
					setErrorMessage(null);
				}
			}
		}
		}
	}
	
	public boolean validateModelName(){
		String text = _modelName.getText();
		if (text == null || text.trim().length() == 0){
			setPageComplete(false);
			setErrorMessage("Please enter a name for the model.");
			return false;
		}
		return true;
	}

	public boolean validateBookName(){
		String text = _bookName.getText();
		if (text == null || text.trim().length() == 0){
			setPageComplete(false);
			setErrorMessage("Please enter a name for the book.");
			return false;
		}
		return true;
	}

	public boolean validateEmail(){
		String text = _email.getText();
		if (text == null || text.trim().length() == 0){
			setPageComplete(false);
			setErrorMessage("Please file in a valid email address.");
			return false;
		}
		return true;
	}

	public boolean validateApiKey(){
		String text = _bookName.getText();
		if (text == null || text.trim().length() == 0){
			setPageComplete(false);
			setErrorMessage("Please file in a valid API Key.");
			return false;
		}
		return true;
	}

}
