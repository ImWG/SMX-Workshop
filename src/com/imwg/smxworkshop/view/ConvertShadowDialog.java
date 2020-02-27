package com.imwg.smxworkshop.view;

import java.awt.Choice;
import java.util.Properties;

public class ConvertShadowDialog extends PropDialog {

	private static final long serialVersionUID = -2549626452278918124L;

	static Properties properties;
	
	private NumberField lowText;
	private NumberField highText;
	private Choice modeChoice, levelChoice;
	
	static public int mode = 1, levels = 0, low = 0, high = 255;
	
	public ConvertShadowDialog(MainFrame owner) {
		super(owner, ConvertShadowDialog.class);
		setBounds();
		loadDefaultEvents();
		
		modeChoice = addChoice("Choice.mode");
		levelChoice = addChoice("Choice.levels");
		add(lowText = new NumberField(true), "TextField.low");
		add(highText = new NumberField(true), "TextField.high");
		addLabel("Label.mode");
		addLabel("Label.levels");
		addLabel("Label.range");
		
		lowText.setRange(0, 255);
		highText.setRange(0, 255);
		
		setDataByFrame();
	}
	
	private void setDataByFrame(){
		modeChoice.select(mode);
		if (levels == -2)
			levelChoice.select(9);
		else
			levelChoice.select(levels);
		lowText.setText(low);
		highText.setText(high);	
	}
	
	@Override
	public void onConfirmed() {
		mode = modeChoice.getSelectedIndex();
		levels = levelChoice.getSelectedIndex();
		if (levels == 9)
			levels = -2;
		low = lowText.getInteger();
		high = highText.getInteger();	
	}

}
