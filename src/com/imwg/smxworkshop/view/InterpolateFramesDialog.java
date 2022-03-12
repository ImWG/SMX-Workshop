package com.imwg.smxworkshop.view;

import java.awt.Checkbox;

public class InterpolateFramesDialog extends PropDialog {
	
	private static final long serialVersionUID = -6017667830943957311L;
	private NumberField anglesBox, framesBox;
	private Checkbox loopCheckbox;
	
	public MainFrame mainFrame;
	static public int angles = 8, frames = 1;
	static public boolean loop = true;
	public boolean confirmed = false;
	
	
	public InterpolateFramesDialog(MainFrame frame){
		super(frame, InterpolateFramesDialog.class);
		this.mainFrame = frame;
		
		setBounds();
		loadDefaultEvents();
		
		this.add(anglesBox = new NumberField(true), "TextField.angles");
		this.add(framesBox = new NumberField(true), "TextField.frames");
		
		this.addLabel("Label.angles");
		this.addLabel("Label.frames");

		loopCheckbox = this.addCheckbox("Checkbox.loop");
		loopCheckbox.setState(loop);
		
		anglesBox.setText(angles);
		framesBox.setText(frames);
		
	}
	
	@Override
	public void onConfirmed() {
		angles = anglesBox.getInteger();
		frames = framesBox.getInteger();
		loop = loopCheckbox.getState();
	}

}