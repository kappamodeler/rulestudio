package com.plectix.rulestudio.editors.kappa.extras;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class ColorManager {

	protected Map<RGB, Color> fColorTable = new HashMap<RGB, Color>(10);

	public void dispose() {
		Iterator<Color> e = fColorTable.values().iterator();
		while (e.hasNext() == true){
			((Color) e.next()).dispose();
		}
	}
	public Color getColor(RGB rgb) {
		Color 	color = (Color) fColorTable.get(rgb);
		if (color == null) {
			color = new Color(Display.getCurrent(), rgb);
			fColorTable.put(rgb, color);
		}
		return color;
	}
	
	public Color getColor(String rgbString){
		RGB		rgb = convertString2RGB(rgbString);		
		Color 	color = (Color) fColorTable.get(rgb);
		if (color == null) {
			color = new Color(Display.getCurrent(), rgb);
			fColorTable.put(rgb, color);
		}
		return color;
	}
	
	/**
	 * RGB(255,124,34) if the input string isn't in the correct
	 * format it will throw an exception.
	 * 
	 * TODO - make this more fault tolerant.
	 * 
	 * @param rgbString
	 * @return
	 */
	public RGB convertString2RGB(String rgbString){
		rgbString = rgbString.substring(0, rgbString.indexOf("(")); 
		rgbString = rgbString.substring(rgbString.indexOf(")"));
		String[] colors = rgbString.split(",");
		
		int r = Integer.parseInt(colors[0]);
		int g = Integer.parseInt(colors[1]);
		int b = Integer.parseInt(colors[2]);
		return new RGB(r, g, b);
	}
}
