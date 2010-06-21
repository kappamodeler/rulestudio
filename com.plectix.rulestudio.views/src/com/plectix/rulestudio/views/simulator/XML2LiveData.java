package com.plectix.rulestudio.views.simulator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.xmlpull.mxp1.MXParser;

public class XML2LiveData {

	public static final RsLiveData getLiveData(final String xmlFilename) throws Exception {
		final List<Plot> plots = new ArrayList<Plot>();
		
		final MXParser mxParser = new MXParser();
		final BufferedReader reader = new BufferedReader(new FileReader(xmlFilename));
		mxParser.setInput(reader);
		RsLiveData ret = new RsLiveData();
		StringBuffer log = new StringBuffer();

		int event = mxParser.next();
		while (event != MXParser.END_DOCUMENT) {
			if (event == MXParser.START_TAG) {
				if (mxParser.getName().equalsIgnoreCase("PLOT")) {
					String plotType = null;
					String plotName = null;
					for (int i= 0; i < mxParser.getAttributeCount(); i++) {
						String attributeName = mxParser.getAttributeName(i);
						if (attributeName.equalsIgnoreCase("TYPE")) {
							plotType = mxParser.getAttributeValue(i);
						} else if (attributeName.equalsIgnoreCase("TEXT")) {
							String name = mxParser.getAttributeValue(i);
							if (name.length() > 2 && name.charAt(0) == '[' && name.charAt(name.length()-1) == ']') {
								name = name.substring(1, name.length()-1);
							}
							plotName = name;
						} else {
							throw new RuntimeException("Unexpected attribute: " + attributeName);
						}
					}
					if (plotType == null || plotName == null) {
						throw new RuntimeException("plotType or plotName is null");
					}
					plots.add(new Plot(plotType, plotName));
				} else if (mxParser.getName().equalsIgnoreCase("CSV")) {
					mxParser.next();
					String csv = mxParser.getText();
					ArrayList<DataPoint> data = readCSV(csv, plots.size());
					ret.addPlots(plots.toArray(new Plot[]{}), data);
					ret.setCSV(csv);
				} else if (mxParser.getName().equalsIgnoreCase("MODEL")) {
					mxParser.next();
					ret.setModel(mxParser.getText());
				} else if (mxParser.getName().equalsIgnoreCase("ENTRY")) {
					String msg = null;
					boolean isInfo = false;
					for (int i= 0; i < mxParser.getAttributeCount(); i++) {
						String attributeName = mxParser.getAttributeName(i);
						if (attributeName.equalsIgnoreCase("MESSAGE")) {
							msg = mxParser.getAttributeValue(i);
						} else if (attributeName.equalsIgnoreCase("TYPE")) {
							isInfo = mxParser.getAttributeValue(i).equalsIgnoreCase("INFO");
						}
					}
					if (isInfo && msg != null) {
						log.append(msg);
						log.append("\n");
					}
					
				}
			}
			
			event = mxParser.next();
		}
		reader.close();
		
		if (log.length() > 0) {
			ret.setLog(log.toString());
		}
		return ret;
	}
	
	private static final ArrayList<DataPoint> readCSV(final String text, final int numberOfPlots) throws IOException {
		final BufferedReader reader = new BufferedReader(new StringReader(text));
		final ArrayList<DataPoint> data = new ArrayList<DataPoint>();

		String line = reader.readLine();
		while (line != null) {
			line = line.trim();
			if (line.length() > 0) { // skip over empty lines
				final String[] fields = line.split(",");
				if (fields.length != numberOfPlots+1) {
					throw new RuntimeException("Unexpected number of columns in the CSV section: " + fields.length + " != " + (numberOfPlots+1));
				} 
				final double time = Double.parseDouble(fields[0]);
				final double[] values = new double[numberOfPlots];
				for (int i= 0; i< numberOfPlots; i++) {
					values[i] = Double.parseDouble(fields[i+1]);
				}
				data.add(new DataPoint(time, values));
			}
			line = reader.readLine();
		}
		
		return data;
	}

	public static final void main(String args[]) {
		try {
			Display display = new Display();
			Shell shell = new Shell(display);
			shell.setSize(200, 200);
			shell.open();
			ChartContainer cc = new ChartContainer(null, "try it");
			RsLiveData liveData = getLiveData(args[0]);
			cc.updateLiveDataChart(liveData);
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}

 