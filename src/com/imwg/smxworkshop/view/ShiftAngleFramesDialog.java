package com.imwg.smxworkshop.view;


public class ShiftAngleFramesDialog extends PropDialog {
	
	private static final long serialVersionUID = -7909241564133744695L;
	private NumberField anglesBox, radialOffsetBox, tangentOffsetBox;
	
	public MainFrame mainFrame;
	static public int angles = 8, radialOffset = 0, tangentOffset = 1;
	public boolean confirmed = false;
	
	
	public ShiftAngleFramesDialog(MainFrame frame){
		super(frame, ShiftAngleFramesDialog.class);
		this.mainFrame = frame;
		
		setBounds();
		loadDefaultEvents();
		
		this.add(anglesBox = new NumberField(true), "TextField.angles");
		this.add(radialOffsetBox = new NumberField(true), "TextField.radial");
		this.add(tangentOffsetBox = new NumberField(true), "TextField.tangent");
		
		this.addLabel("Label.angles");
		this.addLabel("Label.radial");
		this.addLabel("Label.tangent");
		this.addLabel("Label.note");

		anglesBox.setText(angles);
		radialOffsetBox.setText(radialOffset);
		tangentOffsetBox.setText(tangentOffset);
		
	}
	
	@Override
	public void onConfirmed() {
		angles = anglesBox.getInteger();
		radialOffset = radialOffsetBox.getInteger();
		tangentOffset = tangentOffsetBox.getInteger();
	}

}