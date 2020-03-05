package com.imwg.smxworkshop.plugin.cnc;

import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Frame;

import com.imwg.smxworkshop.sprite.Sprite;
import com.imwg.smxworkshop.view.PropDialog;

class LoadSaveDialog extends PropDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8760347018030472126L;
	private Choice playerColorChoice;
	private Checkbox combineShadowCheckbox, savePlayerColorCheckbox, saveShadowCheckbox;
	private boolean mode;
	static int playerColor = Sprite.PLAYER_PALETTE_NONE;
	static boolean combineShadow = true;
	static boolean savePlayerColor = false;
	static boolean saveShadow = true;
	
	final static private int[] playerModes = new int[]{
		Sprite.PLAYER_PALETTE_NONE,
		Sprite.PLAYER_PALETTE_AOE,
		Sprite.PLAYER_PALETTE_AOK,
		Sprite.PLAYER_PALETTE_AOEDE,
		Sprite.PLAYER_PALETTE_DE,
	};
	
	public LoadSaveDialog(Frame owner, boolean load) {
		super(owner);
		loadExternalProperties(LoadSaveDialog.class);
		
		setBounds();
		loadDefaultEvents();
		
		if (mode = load){
			addLabel("Label.playerColor");
			playerColorChoice = addChoice("Choice.playerColor");
			combineShadowCheckbox = addCheckbox("Checkbox.combineShadow");
			for (int i = 0; i < playerModes.length; ++i){
				if (playerModes[i] == playerColor)
					playerColorChoice.select(i);
			}
			combineShadowCheckbox.setState(combineShadow);
		}else{
			savePlayerColorCheckbox = addCheckbox("Checkbox.savePlayerColor");
			saveShadowCheckbox = addCheckbox("Checkbox.saveShadow");
			savePlayerColorCheckbox.setState(savePlayerColor);
			saveShadowCheckbox.setState(saveShadow);
		}
	}
	
	@Override
	public void onConfirmed(){
		if (mode){
			playerColor = playerModes[playerColorChoice.getSelectedIndex()];
			combineShadow = combineShadowCheckbox.getState();
		}else{
			savePlayerColor = savePlayerColorCheckbox.getState();
			saveShadow = saveShadowCheckbox.getState();
		}
	}

}
