package com.plectix.rulestudio.core.wizards.project;

import java.io.File;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class ImportPage extends WizardPage {

	private 	Text		_fileName = null;
	private 	Table		_resources = null;
	private 	Button		_import = null;
	
	protected ImportPage() {
		super("Import Model Settings");
	}
	
	public boolean getImport(){
		return _import.getSelection();
	}
	
	public File[] getResources(){
		File[] resources = new File[_resources.getItemCount()];
		
		for (int index = 0; index < _resources.getItemCount(); index++){
			TableItem	tableItem = _resources.getItem(index);
			resources[index] = (File)tableItem.getData();
		}
		
		return resources;
	}

	public void createControl(Composite parent) {

		Composite 		container = new Composite(parent, SWT.NULL);
		GridLayout		gridLayout = new GridLayout(2, false);
		container.setLayout(gridLayout);

		Label	label = new Label(container, SWT.NULL);
		label.setText("Choose the Model to Import");
		GridData gridData = new GridData();
		gridData.horizontalSpan = 2;
		label.setLayoutData(gridData);
		
		_fileName = new Text(container, SWT.BORDER);
		gridData = new GridData(GridData.FILL_HORIZONTAL);
		_fileName.setLayoutData(gridData);
		_fileName.addModifyListener(new ModifyListener(){

			public void modifyText(ModifyEvent arg0) {
				validate();
				
			}
			
		});
		
		Button	browser = new Button(container, SWT.NULL);
		browser.setText("Browse...");
		browser.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent arg0) {}

			public void widgetSelected(SelectionEvent arg0) {
				FileDialog	dialog = new FileDialog(Display.getDefault().getActiveShell());
				dialog.setFilterExtensions(new String[]{"*.pbt", "*.*"});
				dialog.setFilterNames(new String[]{"Powser Builder Projects", "All Files"});
				String fileName = dialog.open();
				if (fileName != null && fileName.trim().length() != 0){
					_fileName.setText(fileName);
				}
			}
			
		});
		
		label = new Label(container, SWT.NULL);
		label.setText("Resources");
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		label.setLayoutData(gridData);		
		
		_resources = new Table(container, SWT.BORDER);
		gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 2;
		_resources.setLayoutData(gridData);
		_resources.setHeaderVisible(true);
		_resources.setLinesVisible(true);
		
		TableColumn	column = new TableColumn(_resources, SWT.NONE);
		column.setText("Resource");
		column.setWidth(300);
		
		column = new TableColumn(_resources, SWT.NONE);
		column.setText("Size");
		column.setWidth(100);

		_import = new Button(container, SWT.CHECK);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		_import.setText("Import resources into projects");
		_import.setLayoutData(gridData);

		setPageComplete(true);		
		setControl(container);
	}
	
	public void validate(){
		if (validateFilename() == true){
			setPageComplete(true);
			setErrorMessage(null);
		}
	}
	
	public boolean validateFilename(){
		String text = _fileName.getText();
		if (text == null || text.trim().length() == 0){
			File	file = new File(text);
			if (file.exists() == false){
				setPageComplete(false);
				setErrorMessage("Please file in a valid model name.");
				return false;
			}
		}
		return true;
	}
	
}
