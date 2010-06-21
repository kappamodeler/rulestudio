package com.plectix.rulestudio.core.preferencepages;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.plectix.rulestudio.core.Activator;

public class Cellucidate extends PreferencePage implements IWorkbenchPreferencePage{

	protected Text 	_email = null;
	protected Text	_api = null;
	private boolean useExternal = true;
	protected Button _useExternal = null;
	protected Text _extMem = null;
	
	protected Control createContents(Composite parent) {
		Composite 		container = new Composite(parent, SWT.NULL);	
		GridLayout 		layout = new GridLayout(2, false);
		layout.verticalSpacing = 2;
		container.setLayout(layout);
		
		Label label = new Label(container, SWT.NULL);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = SWT.RIGHT;
		label.setLayoutData(gridData);
		label.setText("User Name:");
		
		_email = new Text(container, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		_email.setLayoutData(gridData);
		_email.addModifyListener(new ModifyListener(){

			public void modifyText(ModifyEvent arg0) {
				validate();				
			}
			
		});

		label = new Label(container, SWT.NULL);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.RIGHT;
		label.setLayoutData(gridData);
		label.setText("API Key:");
		
		_api = new Text(container, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		_api.setLayoutData(gridData);
		_api.addModifyListener(new ModifyListener(){

			public void modifyText(ModifyEvent arg0) {
				validate();				
			}
			
		});
		
		useExternal = checkExternal();
		
		if (useExternal) {
			_useExternal = new Button(container, SWT.CHECK);
			_useExternal.setText("Run the simulator in an external process.");
			gridData = new GridData();
			gridData.grabExcessHorizontalSpace = true;
			gridData.horizontalSpan = 2;
			_useExternal.setLayoutData(gridData);
			_useExternal.addSelectionListener(new SelectionListener() {

				public void widgetDefaultSelected(SelectionEvent e) {
					_extMem.setEnabled(_useExternal.getSelection());
					if (_useExternal.getSelection())
						validate();
				}

				public void widgetSelected(SelectionEvent e) {
					_extMem.setEnabled(_useExternal.getSelection());	
					if (_useExternal.getSelection())
						validate();
				}
				
			});
		}
		
		Label simMem = new Label(container, SWT.NONE);
		gridData = new GridData();
		gridData.horizontalAlignment = SWT.RIGHT;
		simMem.setLayoutData(gridData);
		simMem.setText("External Simulator Memory:");
		
		_extMem = new Text(container, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		_extMem.setLayoutData(gridData);
		_extMem.addModifyListener(new ModifyListener(){

			public void modifyText(ModifyEvent arg0) {
				validate();				
			}
			
		});
		
		
		init();
		return container;
	}
	
	private boolean checkExternal() {
		String version = System.getProperty("java.version");
		String forceExternal = System.getProperty("force.external");
		return forceExternal == null && version.startsWith("1.6");
	}

	public void init(IWorkbench arg0) {
	}
	private void init(){
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault("cellucidate_use_external", false);
		store.setDefault("cellucidate.extMem", "1G");
		String		email = store.getString("cellucidate_email");
		if (email != null && email.trim().length() > 0){
			_email.setText(email);
		}
		String		key = store.getString("cellucidate_api_key");
		if (key != null && key.trim().length() > 0){
			_api.setText(key);
		}
		
		if (_useExternal != null) {
			boolean ext = store.getBoolean("cellucidate_use_external");
			_useExternal.setSelection(ext);
			_extMem.setEnabled(ext);
		}
		
		String extMem = store.getString("cellucidate.extMem");
		_extMem.setText(extMem);
	}

	/**
	 * Calculate and store the data before the 
	 */
	public boolean performOk() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		String oldEmail = store.getString("cellucidate_email");
		String newEmail = _email.getText();
		if (!newEmail.equals(oldEmail)) {
			store.setValue("cellucidate_url", newEmail);
			store.firePropertyChangeEvent("cellucidate_email", oldEmail,
					newEmail);
		}

		String oldKey = store.getString("cellucidate_api_key");
		String newKey = _api.getText();
		if (!newKey.equals(oldKey)) {
			store.setValue("cellucidate_url", newKey);
			store.firePropertyChangeEvent("cellucidate_api_key", oldKey,
					newKey);
		}

		if (_useExternal != null) {
			boolean oldUse = store.getBoolean("cellucidate_use_external");
			boolean newUse = _useExternal.getSelection();
			if (oldUse != newUse) {
				store.setValue("cellucidate_use_external", newUse);
				store.firePropertyChangeEvent("cellucidate_use_external",
						oldUse, newUse);
			}
		}

		if (_extMem.isEnabled()) {
			String oldMem = store.getString("cellucidate.extMem");
			String newMem = _extMem.getText();
			if (!newMem.equals(oldMem)) {
				store.setValue("cellucidate.extMem", newMem);
				store.firePropertyChangeEvent("cellucidate.extMem", oldMem,
						newMem);
			}
		}
		
		return super.performOk();
	}
	
	/**
	 * Called when the url changes.
	 */
	private void validate(){
		String value = _email.getText();
		if (value == null || value.trim().length() == 0) {
			setErrorMessage("Please enter your user name.");
			return;
		}
		value = _api.getText();
		if (value == null || value.trim().length() == 0) {
			setErrorMessage("Please enter your API key.");
			return;
		}
		
		if (_extMem.isEnabled()) {
			value = _extMem.getText();
			if (value == null || value.trim().length() == 0) {
				setErrorMessage("Please enter the memory argument for the Simulator.");
				return;
			} else if (badMem(value.trim())) {
				setErrorMessage("Memory must be a number followed by K, M or G.");
				return;
			}
		}
		setErrorMessage(null);

	}

	private boolean badMem(String value) {
		int len = value.length();
		char units = value.charAt(len - 1);
		if ("kKmMgG".indexOf(units) == -1) {
			return true; 
		}
		String numStr = value.substring(0, len -1);
		try {
			Integer.parseInt(numStr);
		} catch (NumberFormatException ne) {
			return true;
		}
		return false;
	}

}