package com.plectix.rulestudio.views.story;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class StoryRunDialog extends Dialog {
	
	private final static String TIME = "--time";
	private final static String EVENT = "--event";
		
	private final static String[] NO_COMP = new String[] { "--no-compress-stories" };
	private final static String[] WEAK_COMP = new String[] { "--compress-stories", "--no-use-strong-compression" };
	private final static String[] STRONG_COMP = new String[] { "--compress-stories", "--use-strong-compression" };
	
	private Text iterations;
	private static String lastIterations = "10";
	
	private Text length;
	private static String lastLength = "1000";
	
	private Combo type;
	private static int lastType = 0;
	
	private Combo compression;
	private static int lastCompression = 0;
	
	private Text rescale;
	private static String lastRescale = "";
	
	private Text seed;
	private static String lastSeed = "";
	
	public StoryRunDialog(Shell parentShell) {
		super(parentShell);
	}

	public StoryRunDialog(IShellProvider parentShell) {
		super(parentShell);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Shell shell = getShell();
		shell.setText("Story");
		Composite area = new Composite(parent, SWT.NONE);
		GridLayout gl = new GridLayout(2, false);
		area.setLayout(gl);
		
		Label run = new Label(area, SWT.NONE);
		run.setText("Iterations: ");
		GridData gd = new GridData();
		gd.horizontalAlignment = SWT.RIGHT;
		run.setLayoutData(gd);

		
		iterations = new Text(area, SWT.SINGLE | SWT.BORDER | SWT.RIGHT);
		iterations.setText(lastIterations);
		iterations.setSize(70, SWT.DEFAULT);
		iterations.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				String it = iterations.getText();
				boolean itOk = true;
				if (it == null || it.length() == 0) {
					itOk = false;
				} else {
					try {
						int value = Integer.parseInt(it);
						itOk = value > 0;
					} catch (NumberFormatException ne) {
						itOk = false;
					}
				}
				if (itOk) {
					lastIterations = it;
				} else {
					int offset = iterations.getCaretPosition()-1;
					iterations.setText(lastIterations);
					iterations.setSelection(offset, offset);
				}
			}
			
		});
		
		length = new Text(area, SWT.SINGLE | SWT.BORDER | SWT.RIGHT);
		length.setText(lastLength);
		length.setSize(70, SWT.DEFAULT);
		gd = new GridData();
		gd.horizontalAlignment = SWT.RIGHT;
		length.setLayoutData(gd);
		length.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				String lg = length.getText();
				boolean lgOk = true;
				if (lg.length() == 0) {
					lgOk = false;
				} else {
					try {
						double value = Double.parseDouble(lg);
						lgOk = value > 0.0;
					} catch (NumberFormatException ne) {
						lgOk = false;
					}
				}
				if (lgOk) {
					lastLength = lg;
				} else {
					int offset = length.getCaretPosition()-1;
					length.setText(lastLength);
					length.setSelection(offset, offset);
				}
			}
			
		});
		
		
		type = new Combo(area, SWT.DROP_DOWN | SWT.READ_ONLY);
		type.add("seconds");
		type.add("events");
		type.select(lastType);
		
		Label with = new Label(area, SWT.NONE);
		with.setText("Compression: ");
		gd = new GridData();
		gd.horizontalAlignment = SWT.RIGHT;
		with.setLayoutData(gd);
		
		compression = new Combo(area, SWT.DROP_DOWN | SWT.READ_ONLY);
		compression.add("Strong");
		compression.add("Weak");
		compression.add("None");
		compression.select(lastCompression);
		
		
		Label rLabel = new Label(area, SWT.NONE);
		rLabel.setText("Rescale: ");
		gd = new GridData();
		gd.horizontalAlignment = SWT.RIGHT;
		rLabel.setLayoutData(gd);

		rescale = new Text(area, SWT.SINGLE | SWT.BORDER | SWT.RIGHT);
		rescale.setText(lastRescale);
		rescale.setSize(70, SWT.DEFAULT);
		gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		rescale.setLayoutData(gd);
		rescale.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				String rs = rescale.getText();
				boolean rsOk = true;
				if (rs.length() > 0) {
					try {
						double value = Double.parseDouble(rs);
						rsOk = value > 0;
					} catch (NumberFormatException ne) {
						rsOk = false;
					}
				}
				if (rsOk) {
					lastRescale = rs;
				} else {
					int offset = rescale.getCaretPosition() - 1;
					rescale.setText(lastRescale);
					rescale.setSelection(offset, offset);
				}
			}
			
		});

		
		Label sdLabel = new Label(area, SWT.NONE);
		sdLabel.setText("Random Seed: ");
		gd = new GridData();
		gd.horizontalAlignment = SWT.RIGHT;
		sdLabel.setLayoutData(gd);

		seed = new Text(area, SWT.SINGLE | SWT.BORDER | SWT.RIGHT);
		seed.setText(lastSeed);
		seed.setSize(70, SWT.DEFAULT);
		gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		seed.setLayoutData(gd);
		seed.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				String sd = seed.getText();
				boolean sdOk = true;
				if (sd.length() > 0) {
					try {
						int value = Integer.parseInt(sd);
						sdOk = value > 0;
					} catch (NumberFormatException ne) {
						sdOk = false;
					}
				}
				if (sdOk) {
					lastSeed = sd;
				} else {
					int offset = seed.getCaretPosition() - 1;
					seed.setText(lastSeed);
					seed.setSelection(offset, offset);
				}
			}
			
		});

		
		return area;	
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "Start", true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}
	
	public String getIterations() {
		return lastIterations;
	}
	
	public String getType() {
		return (lastType == 0)?TIME:EVENT;
	}
	
	public String getLength() {
		return lastLength;
	}
	
	public String[] getCompression() {
		switch(lastCompression) {
		case 0:
			return NO_COMP;
		case 1:
			return WEAK_COMP;
		case 2:
			return STRONG_COMP;
		}
		return null;
	}
	
	public String getRescale() {
		return lastRescale;
	}
	
	public String getSeed() {
		return lastSeed;
	}
	
}
