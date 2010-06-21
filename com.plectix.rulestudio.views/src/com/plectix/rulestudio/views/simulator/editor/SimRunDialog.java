package com.plectix.rulestudio.views.simulator.editor;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class SimRunDialog extends Dialog {
	
	final private static int SIM_TIME = 0;
	final private static int SIM_EVENT = 1;
	
	private Text duration;
	private static String lastDuration = "1000";
	
	private Text points;
	private static String lastPoints = "1000";
	
	private Combo type;
	private static int lastType = SIM_TIME;
	
	private Text rescale;
	private static String lastRescale = "";
	
	private Text seed;
	private static String lastSeed = "";
	
	public SimRunDialog(Shell parentShell) {
		super(parentShell);
	}

	public SimRunDialog(IShellProvider parentShell) {
		super(parentShell);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Shell shell = getShell();
		shell.setText("Simulation");
		Composite area = new Composite(parent, SWT.NONE);
		GridLayout gl = new GridLayout(3, false);
		area.setLayout(gl);
		
		Label run = new Label(area, SWT.NONE);
		run.setText("Run Simulation for");
		
		duration = new Text(area, SWT.SINGLE | SWT.BORDER | SWT.RIGHT);
		duration.setText(lastDuration);
		duration.setSize(70, SWT.DEFAULT);
		duration.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				String dur = duration.getText();
				boolean durOk = true;
				if (dur == null || dur.length() == 0) {
					durOk = false;
				} else {
					try {
						if (lastType == SIM_EVENT) {
							int value = Integer.parseInt(dur);
							durOk = value > 0;
						} else {
							double value = Double.parseDouble(dur);
							durOk = value > 0.0;
						}
					} catch (NumberFormatException ne) {
						durOk = false;
					}
				}
				if (durOk) {
					lastDuration = dur;
				} else {
					int offset = duration.getCaretPosition() - 1;
					duration.setText(lastDuration);
					duration.setSelection(offset, offset);
				}
			}
			
		});
		
		
		type = new Combo(area, SWT.DROP_DOWN | SWT.READ_ONLY);
		type.add("seconds");
		type.add("events");
		type.select(lastType);
		type.addSelectionListener(new SelectionListener() {
			
			public void widgetSelected(SelectionEvent e) {
				lastType = type.getSelectionIndex();
				if (lastType == SIM_EVENT) {		// times are ok, update if event
					String dur = duration.getText();
					try {
						Integer.parseInt(dur);
					} catch (NumberFormatException ne) {
						int in = dur.indexOf('.');
						if (in == -1) {
							lastDuration = "1";	
						} else {
							lastDuration = dur.substring(0, in);
						}
						duration.setText(lastDuration);
					}
				}
				
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				// ignore
			}
		});
		
		Label with = new Label(area, SWT.NONE);
		with.setText("with");
		GridData gd = new GridData();
		gd.horizontalAlignment = SWT.CENTER;
		with.setLayoutData(gd);
		
		points = new Text(area, SWT.SINGLE | SWT.BORDER | SWT.RIGHT);
		points.setText(lastPoints);
		points.setSize(70, SWT.DEFAULT);
		points.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				String pt = points.getText();
				boolean ptOk = true;
				if (pt == null || pt.length() == 0) {
					ptOk = false;
				} else {
					try {
						int value = Integer.parseInt(pt);
						ptOk = value > 0;
					} catch (NumberFormatException ne) {
						ptOk = false;
					}
				}
				if (ptOk) {
					lastPoints = pt;
				} else {
					int offset = points.getCaretPosition() - 1;
					points.setText(lastPoints);
					points.setSelection(offset, offset);
				}
			}
			
		});

		Label count = new Label(area, SWT.NONE);
		count.setText("data points");
		
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
		gd.horizontalSpan = 2;
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
		gd.horizontalSpan = 2;
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
	
	public String getDuration() {
		return lastDuration;
	}
	
	public String getPoints() {
		return lastPoints;
	}
	
	public String getType() {
		return (lastType == 0)?"--time":"--event";
	}
	
	public String getRescale() {
		return lastRescale;
	}
	
	public String getSeed() {
		return lastSeed;
	}
	
}
