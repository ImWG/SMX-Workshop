package com.imwg.smxworkshop.view;

import java.awt.Checkbox;

public class TrimAngleFramesDialog extends PropDialog {
	
	private static final long serialVersionUID = -7692180444639374728L;
	private NumberField anglesBox, startBox, framesBox;
	private Checkbox removeCheckbox;
	
	public MainFrame mainFrame;
	static public int angles = 8, startFrame = 0, frames = 1;
	static public boolean removeSelected = true;
	public boolean confirmed = false;
	
	
	public TrimAngleFramesDialog(MainFrame frame){
		super(frame, TrimAngleFramesDialog.class);
		this.mainFrame = frame;
		
		setBounds();
		loadDefaultEvents();
		
		this.add(anglesBox = new NumberField(true), "TextField.angles");
		this.add(startBox = new NumberField(true), "TextField.start");
		this.add(framesBox = new NumberField(true), "TextField.frames");
		
		this.addLabel("Label.angles");
		this.addLabel("Label.start");
		this.addLabel("Label.frames");

		removeCheckbox = this.addCheckbox("Checkbox.remove");
		removeCheckbox.setState(removeSelected);
		
		anglesBox.setText(angles);
		startBox.setText(startFrame);
		framesBox.setText(frames);
		
	}
	
	@Override
	public void onConfirmed() {
		angles = anglesBox.getInteger();
		startFrame = startBox.getInteger();
		frames = framesBox.getInteger();
		removeSelected = removeCheckbox.getState();
	}

}