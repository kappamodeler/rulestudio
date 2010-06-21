package com.plectix.rulestudio.editors.preferencepages;

import org.eclipse.jface.preference.ColorSelector;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.plectix.rulestudio.editors.Activator;
import com.plectix.rulestudio.editors.kappa.KappaEditor;
import com.plectix.rulestudio.editors.kappa.extras.SyntaxColors;

public class SyntaxColoring extends PreferencePage implements IWorkbenchPreferencePage{

	private List	_syntaxListBox = null;
	
	private ColorSelector fSyntaxForegroundColorEditor;
	private Label fColorEditorLabel;
	private Button fBoldCheckBox;
	private Button fItalicCheckBox;
	private Button fStrikethroughCheckBox;
	private Button fUnderlineCheckBox;
	private SyntaxColors _selectedColor = null;

	protected Control createContents(Composite parent) {
		Composite 		container = new Composite(parent, SWT.NULL);	
		GridLayout 		layout = new GridLayout(2, true);
		container.setLayout(layout);
		
		Label label = new Label(container, SWT.NULL);
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		gridData.horizontalSpan = 2;
		label.setLayoutData(gridData);
		label.setText("Element:");
		
		_syntaxListBox = new List(container, SWT.BORDER);
		_syntaxListBox.setLayoutData(new GridData(GridData.FILL_BOTH));
		_syntaxListBox.addSelectionListener(new SelectionListener(){

			public void widgetDefaultSelected(SelectionEvent e) {}

			public void widgetSelected(SelectionEvent e) {
				int index = _syntaxListBox.getSelectionIndex();
				if (index >= 0){
					_selectedColor = (SyntaxColors)_syntaxListBox.getData(_syntaxListBox.getItem(index));
					updateSyntaxFields(_selectedColor);
				}
				
			}
			
		});
		
		createStyleSection(container);
		
		fillListBox();
		
		_syntaxListBox.select(0);
		updateSyntaxFields(_selectedColor);
		
		return container;
	}
	
	private void createStyleSection(Composite parent){
		Composite stylesComposite= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.numColumns= 2;
		stylesComposite.setLayout(layout);
		stylesComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		fColorEditorLabel= new Label(stylesComposite, SWT.LEFT);
		fColorEditorLabel.setText("Color:");
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent= 20;
		fColorEditorLabel.setLayoutData(gd);

		fSyntaxForegroundColorEditor= new ColorSelector(stylesComposite);
		Button foregroundColorButton= fSyntaxForegroundColorEditor.getButton();
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		foregroundColorButton.setLayoutData(gd);
		foregroundColorButton.addSelectionListener(new SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				if (_selectedColor != null){
					_selectedColor.color(fSyntaxForegroundColorEditor.getColorValue());
				}
			}		
		});

		fBoldCheckBox= new Button(stylesComposite, SWT.CHECK);
		fBoldCheckBox.setText("Bold");
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent= 20;
		gd.horizontalSpan= 2;
		fBoldCheckBox.setLayoutData(gd);
		fBoldCheckBox.addSelectionListener(new SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				if (_selectedColor != null){
					int state = 0;
					if (fBoldCheckBox.getSelection() == true){
						state = _selectedColor.state() | SWT.BOLD;
					}else{
						state = _selectedColor.state() & (~SWT.BOLD);
					}
					_selectedColor.state(state);
				}
			}		
		});

		fItalicCheckBox= new Button(stylesComposite, SWT.CHECK);
		fItalicCheckBox.setText("Italic");
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent= 20;
		gd.horizontalSpan= 2;
		fItalicCheckBox.setLayoutData(gd);
		fItalicCheckBox.addSelectionListener(new SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				if (_selectedColor != null){
					int state = 0;
					if (fItalicCheckBox.getSelection() == true){
						state = _selectedColor.state() | SWT.ITALIC;
					}else{
						int test = ~SWT.ITALIC;
						state = _selectedColor.state() & (~SWT.ITALIC);
					}
					_selectedColor.state(state);
				}
			}		
		});

		fStrikethroughCheckBox= new Button(stylesComposite, SWT.CHECK);
		fStrikethroughCheckBox.setText("Strikethrough");
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent= 20;
		gd.horizontalSpan= 2;
		fStrikethroughCheckBox.setLayoutData(gd);
		fStrikethroughCheckBox.addSelectionListener(new SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				if (_selectedColor != null){
					int state = 0;
					if (fStrikethroughCheckBox.getSelection() == true){
						state = _selectedColor.state() | TextAttribute.STRIKETHROUGH;
					}else{
						state = _selectedColor.state() & (~TextAttribute.STRIKETHROUGH);
					}
					_selectedColor.state(state);
				}
			}			
		});

		fUnderlineCheckBox= new Button(stylesComposite, SWT.CHECK);
		fUnderlineCheckBox.setText("Underline");
		gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent= 20;
		gd.horizontalSpan= 2;
		fUnderlineCheckBox.setLayoutData(gd);
		fUnderlineCheckBox.addSelectionListener(new SelectionListener(){
			public void widgetDefaultSelected(SelectionEvent e) {}
			public void widgetSelected(SelectionEvent e) {
				if (_selectedColor != null){
					int state = 0;
					if (fUnderlineCheckBox.getSelection() == true){
						state = _selectedColor.state() | TextAttribute.UNDERLINE;
					}else{
						state = _selectedColor.state() & (~TextAttribute.UNDERLINE);
					}
					_selectedColor.state(state);
				}
			}		
		});

	}
	
	public void init(IWorkbench arg0) {}
	
	/**
	 * Calculate and store the data before the 
	 */
	public boolean performOk() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		
		int count = _syntaxListBox.getItemCount();
		for (int index = 0; index < count; index++){
			SyntaxColors color = (SyntaxColors)_syntaxListBox.getData(_syntaxListBox.getItem(index));
			String value = "RGB(" + color.color().red + "," + color.color().green + "," + color.color().blue + 
							") - ";
			if ((color.state() & SWT.BOLD) != 0){
				value += " BOLD | ";
			}
			if ((color.state() & SWT.ITALIC) != 0){
				value += " ITALIC | ";
			}
			if ((color.state() & TextAttribute.UNDERLINE) != 0){
				value += " UNDERLINE | ";
			}
			if ((color.state() & TextAttribute.STRIKETHROUGH) != 0){
				value += " STRIKETHROUGH | ";
			}
			store.setValue(color.key(), value);
		}
		
		//Trigger the editors to redisplay.
		Activator.getDefault().getPreferenceStore().
					firePropertyChangeEvent(KappaEditor.SYNTAX_CHANGES, "Old Value", "New Value");
		
		return super.performOk();
	}
	
	private void fillListBox(){
		SyntaxColors[] colors = SyntaxColors.values();
		for (int index = 0; index < colors.length; index++){
			if (index == 0){
				_selectedColor = colors[index];
			}
			_syntaxListBox.add(colors[index].label());
			_syntaxListBox.setData(colors[index].label(), colors[index]);
		}
	}
	
	private void updateSyntaxFields(SyntaxColors color){
		fSyntaxForegroundColorEditor.setColorValue(color.color());
		fBoldCheckBox.setSelection((color.state() & SWT.BOLD) != 0);
		fItalicCheckBox.setSelection((color.state() & SWT.ITALIC) != 0);
		fStrikethroughCheckBox.setSelection((color.state() & TextAttribute.STRIKETHROUGH) != 0);
		fUnderlineCheckBox.setSelection((color.state() & TextAttribute.UNDERLINE) != 0);
	
	}

	
}