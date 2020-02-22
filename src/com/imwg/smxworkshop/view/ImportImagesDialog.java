package com.imwg.smxworkshop.view;

import java.awt.Canvas;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Graphics;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.imwg.smxworkshop.sprite.Palette;
import com.imwg.smxworkshop.sprite.Sprite;
import com.imwg.smxworkshop.sprite.SpriteIO;
import com.imwg.smxworkshop.sprite.SpritePreview;

public class ImportImagesDialog extends PropDialog {

	private static final long serialVersionUID = -6871989180535569813L;
	static Properties properties;
	
	private Choice paletteList;
	private Choice playerModeList;
	private Choice playerPaletteList, imageModeList;
	private NumberField cutOpaqueText;
	private NumberField playerText;
	private Checkbox autoCropCheckbox;
	private Canvas paletteCanvas;
	private Checkbox hsvModeCheckbox;
	private NumberField rowsText, columnsText;
	private Checkbox outlineCheckbox, smudgeCheckbox, csvCheckbox;
		
	private int[] palettes;
	
	final static public Map<String, Integer> settings = new HashMap<String, Integer>(){
		private static final long serialVersionUID = -7849877117228654727L;
		{
			put("palette", 0);
			put("imageMode", SpriteIO.IMAGE_MODE_AUTO);
			put("playerMode", Sprite.PLAYER_PALETTE_DE);
			put("playerPalette", 0);
			put("playerPaletteTolerance", 0);
			put("cutOpaqueTolerance", 0);
			put("autoCrop", 1);
			put("hsvMode", 0);
			put("rows", 1);
			put("columns", 1);
			put("csv", 0);
		}
	};
	
	public ImportImagesDialog(MainFrame frame) {
		super(frame, ImportImagesDialog.class);
		
		setBounds();
		loadDefaultEvents();
		
		paletteList = new Choice();
		palettes = new int[Palette.palettes.length];
		int paletteCount = 0;
		for (int i=0; i<Palette.palettes.length; ++i){
			if (Palette.palettes[i] != null){
				paletteList.add("#"+i+" "+Palette.paletteNames[i]);
				palettes[paletteCount++] = i;
			}
		}
		paletteList.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent e) {
				paletteCanvas.repaint();
			}
			
		});
		this.add(paletteList, "List.palette");
		
		playerModeList = this.addChoice("List.playerMode");
		playerModeList.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent e) {
				playerPaletteList.setEnabled(playerModeList.getSelectedIndex() != 0);
			}
		});
		
		playerPaletteList = this.addChoice("List.playerPalette");
		
		this.add(playerText = new NumberField(true), "TextField.playerPalette");
		this.add(cutOpaqueText = new NumberField(true), "TextField.cutOpaque");

		this.add(rowsText = new NumberField(true), "TextField.rows");
		this.add(columnsText = new NumberField(true), "TextField.columns");
		
		autoCropCheckbox = this.addCheckbox("Checkbox.autoCrop");
		hsvModeCheckbox = this.addCheckbox("Checkbox.hsvMode");
		outlineCheckbox = this.addCheckbox("Checkbox.outline");
		smudgeCheckbox = this.addCheckbox("Checkbox.smudge");
		csvCheckbox = this.addCheckbox("Checkbox.csv");
		
		imageModeList = this.addChoice("List.imageMode");
		
		paletteCanvas = new Canvas(){
			private static final long serialVersionUID = 1L;
			@Override
			public void paint(Graphics g){
				g.drawImage(SpritePreview.paletteImages[palettes[paletteList.getSelectedIndex()]],
						0, 0, getWidth(), getHeight(), null);
			}
		};
		this.add(paletteCanvas, "Canvas.palette");
		
		this.addLabel("Label.size");
		
		// SET VALUES
		int palette = settings.get("palette");
		for (int i=0; i<palettes.length; ++i){
			if (palettes[i] == palette){
				paletteList.select(i); break;
			}
		}
		
		playerModeList.select(settings.get("playerMode") + 1);
		playerPaletteList.select(settings.get("playerPalette"));
		
		autoCropCheckbox.setState(settings.get("autoCrop") > 0);
		hsvModeCheckbox.setState(settings.get("hsvMode") > 0);
		csvCheckbox.setState(settings.get("csv") > 0);
		
		playerText.setText(settings.get("playerPaletteTolerance"));
		cutOpaqueText.setText(settings.get("cutOpaqueTolerance"));
		rowsText.setText(settings.get("rows"));
		rowsText.setRange(1, null);
		columnsText.setText(settings.get("columns"));
		columnsText.setRange(1, null);
		
		int imageMode = settings.get("imageMode");
		outlineCheckbox.setState((imageMode & SpriteIO.IMAGE_MODE_OUTLINE_MASK) != 0);
		smudgeCheckbox.setState((imageMode & SpriteIO.IMAGE_MODE_SMUDGE_MASK) != 0);
		imageMode &= SpriteIO.IMAGE_MODE_MASK | SpriteIO.IMAGE_MODE_BACKGROUND_MASK;
		for (int i=0; i<ExportImagesDialog.IMAGE_MODES.length; ++i){
			if (ExportImagesDialog.IMAGE_MODES[i] == imageMode){
				imageModeList.select(i);
				break;
			}
		}
		
	}
	
	@Override
	public void onConfirmed() {
		settings.put("palette", palettes[paletteList.getSelectedIndex()]);
		settings.put("playerMode", playerModeList.getSelectedIndex() - 1);
		settings.put("playerPalette", playerPaletteList.getSelectedIndex());
		
		settings.put("playerPaletteTolerance", playerText.getInteger());
		settings.put("cutOpaqueTolerance", cutOpaqueText.getInteger());
		settings.put("rows", rowsText.getInteger());
		settings.put("columns", columnsText.getInteger());
		
		settings.put("autoCrop", autoCropCheckbox.getState() ? 1 : 0);
		settings.put("hsvMode", hsvModeCheckbox.getState() ? 1 : 0);
		settings.put("csv", csvCheckbox.getState() ? 1 : 0);
		
		
		int imageMode = ExportImagesDialog.IMAGE_MODES[imageModeList.getSelectedIndex()];
		if (outlineCheckbox.getState())
			imageMode |= SpriteIO.IMAGE_MODE_OUTLINE_MASK;
		if (smudgeCheckbox.getState())
			imageMode |= SpriteIO.IMAGE_MODE_SMUDGE_MASK;
		
		settings.put("imageMode", imageMode);

	}
	
}
