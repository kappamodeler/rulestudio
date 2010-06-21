package com.plectix.rulestudio.core.license;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.plectix.rulestudio.core.Activator;

public class GetLicense extends WizardPage {

	private 	Text		_email = null;
	private 	Text		_key = null;
	
	protected GetLicense() {
		super("Get License Settings");
	}
	
	public String getEmail(){
		return _email.getText().trim();
	}
	
	public String getAPIKey(){
		return _key.getText().trim();
	}
	
	public void createControl(Composite parent) {

		Composite container = new Composite(parent, SWT.NULL);
		GridLayout gridLayout = new GridLayout(2, false);
		container.setLayout(gridLayout);

		Label label = new Label(container, SWT.NULL);
		label.setText("Cellucidate Login:");

		_email = new Text(container, SWT.BORDER);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		_email.setLayoutData(gridData);
		_email.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent arg0) {
				validate();

			}

		});

		label = new Label(container, SWT.NULL);
		label.setText("API Key:");

		_key = new Text(container, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		_key.setLayoutData(gridData);
		_key.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent arg0) {
				validate();

			}

		});

		init();
		setControl(container);
	}
	
	public void init(){
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		String		email = store.getString("cellucidate_email");
		String		key = store.getString("cellucidate_api_key");

		_email.setText(email);
		_key.setText(key);
		
		if (email.length() == 0)
			_email.setFocus();
		else if (key.length() == 0)
			_key.setFocus();
	}
	
	public void validate() {
		if (validateEmail() == true) {
			if (validateApiKey() == true) {
				setPageComplete(true);
				setErrorMessage(null);
			}
		}
	}
	
	public boolean validateEmail(){
		String text = _email.getText();
		if (text == null || text.trim().length() == 0){
			setPageComplete(false);
			setErrorMessage("Please enter your Cellucidate user name.");
			return false;
		}
		return true;
	}

	public boolean validateApiKey(){
		String text = _key.getText();
		if (text == null || text.trim().length() == 0){
			setPageComplete(false);
			setErrorMessage("Please enter your Cellucidate API key.");
			return false;
		}
		return true;
	}

}
