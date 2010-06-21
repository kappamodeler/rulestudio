package com.plectix.rulestudio.editors.kappa.extras;

import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;

/*
 * This emun lists all the syntax colors and is used to create
 * the text attributes that are need to color the Kappa editor properly.
 * 
 * The color and state information is initialized in the Activator Class.
 */
public enum SyntaxColors {

	DEFAULT_BACKGROUND_COLOR("Background Color"),	
	KAPPA_COMMENT("Comment"),
	
	LABEL_EXPRESSION("Rule Label"),
	LABEL_EXPRESSION_DEFAULT("Rule Default"),
	
	AGENT("Agent"),
	
	INIT_EXPRESSION("Init Keyword"),
	INIT_EXPRESSION_DEFAULT("Init Statement Default"),

	OBS_EXPRESSION("Obs Keyword"),
	OBS_EXPRESSION_DEFAULT("Obs Statement Default"),

	MODIFY_EXPRESSION("Mod Keyword"),
	MODIFY_EXPRESSION_DEFAULT("Mod Statement Default"),

	STORY_EXPRESSION("Story Keyword"),
	STORY_EXPRESSION_DEFAULT("Story Statement Default"),
	
	DO_WORD("Do Keyword"),
	RIGHT_ARROW("Right Arrow"),
	DOUBLE_ARROW("Double Arrow"),
	AT_SYMBOL("At Symbol");
	
	private String _label = "";
	private RGB    _color = new RGB(0,0,0);
	private int    _state = SWT.NONE;
	
	private SyntaxColors(String label){
		_label = label;
	}

	public String label(){
		return _label;
	}
	
	public RGB color(){
		return _color;
	}

	public void color(RGB color){
		_color = color;
	}
	
	public int state(){
		return _state;
	}

	public void state(int state){
		_state = state;
	}

	/**
	 * Used as the key to look up the property in either the 
	 * preference store or the properties file.
	 * @return
	 */
	public String key(){
		return _label.replaceAll(" ", "-");
	}
	
	public TextAttribute textAttribute(ColorManager colorManager){
		return new TextAttribute(colorManager.getColor(_color), null, _state);
	}
	
	public Token token(ColorManager colorManager){
		return new Token(textAttribute(colorManager));
	}
	
	/**
	 * This will initialize the values of the Color and the style
	 * values
	 * 
	 * The property must be in the following format 
	 * 
	 * RGB(123,255,12) - NONE
	 * RGB(123,255,12) - BOLD | ITALIC | STRIKETHROUGH | UNDERLINE
	 * 
	 * @param value
	 */
	public void initialize(String value){
		int intPos = 0;
		_state = SWT.NONE;
		
		//Pull out the color
		intPos = value.indexOf("(");
		if (intPos != -1){
			value = value.substring(intPos + 1);
			intPos = value.indexOf(")");
			if (intPos != -1){
				String colorString = value.substring(0, intPos);
				String[] colors = colorString.split(",");
				if (colors.length == 3){
					_color = new RGB(intValue(colors[0].trim()), intValue(colors[1].trim()), intValue(colors[2].trim()));
				}
			}
		}
		
		//Pull out the states
		intPos = value.indexOf("-");
		if (intPos != -1){
			String stateString = value.substring(intPos + 1);
			String[] states = stateString.split("\\|");
			for (int index = 0; index < states.length; index++){
				if (states[index].trim().equals("BOLD") == true){
					_state |= SWT.BOLD;
				}else if (states[index].trim().equals("ITALIC") == true){
					_state |= SWT.ITALIC;
				}else if (states[index].trim().equals("STRIKETHROUGH") == true){
					_state |= TextAttribute.STRIKETHROUGH;
				}else if (states[index].trim().equals("UNDERLINE") == true){
					_state |= TextAttribute.UNDERLINE;
				}
			}
		}
		
	}

	/**
	 * Change the string into an integer and catch the exception.
	 * 
	 * @param value
	 * @return
	 */
	private int intValue(String value){
		try{
			return Integer.parseInt(value);
		}catch (NumberFormatException exception){
			exception.printStackTrace();
		}
		return 0;
	}
	
}
