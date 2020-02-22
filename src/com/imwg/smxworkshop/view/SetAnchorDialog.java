package com.imwg.smxworkshop.view;

import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import com.imwg.smxworkshop.sprite.Sprite;

public class SetAnchorDialog extends PropDialog {
	
	private static final long serialVersionUID = -2900353909536939929L;
	private NumberField xBox, yBox;
	private Choice typeChoice;
	private Checkbox relativeCheckbox;
	
	public MainFrame mainFrame;
	public int x, y, type;
	public boolean relative;
	public boolean confirmed = false;
	
	
	public SetAnchorDialog(MainFrame frame){
		super(frame, SetAnchorDialog.class);
		this.mainFrame = frame;
		
		setBounds();
		loadDefaultEvents();
		
		this.add(xBox = new NumberField(true), "TextField.x");
		this.add(yBox = new NumberField(true), "TextField.y");
		
		this.addLabel("Label.x");
		this.addLabel("Label.y");

		relativeCheckbox = this.addCheckbox("Checkbox.relative");
		relativeCheckbox.setState(true);
		typeChoice = this.addChoice("Choice.type");
		
		xBox.setText(0);
		yBox.setText(0);
		
		relativeCheckbox.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent e) {
				setDataByFrame();
			}
		});
		
	}
	
	@Override
	public void onConfirmed() {
		x = xBox.getInteger();
		y = yBox.getInteger();
		relative = relativeCheckbox.getState();
		
		switch(typeChoice.getSelectedIndex()){
		case 0: type = -1; break;
		case 1: type = Sprite.DATA_IMAGE; break;
		case 2: type = Sprite.DATA_SHADOW; break;
		case 3: type = Sprite.DATA_OUTLINE; break;
		}
	}
	
	private void setDataByFrame(){
		Sprite.Frame spriteFrame = mainFrame.getSprite().getFrame(mainFrame.current);
		if (spriteFrame == null)
			return;
		
		int type = typeChoice.getSelectedIndex();
		boolean relative = relativeCheckbox.getState();
		
		if (relative){
			xBox.setText(0);
			yBox.setText(0);
			
		}else{
			switch(type){
			default: type = Sprite.DATA_IMAGE; break;
			case 2: type = Sprite.DATA_SHADOW; break;
			case 3: type = Sprite.DATA_OUTLINE;
			}
			
			if (spriteFrame != null){
				xBox.setText(spriteFrame.getAnchorX(type));
				yBox.setText(spriteFrame.getAnchorY(type));
			}
		}
	}

}