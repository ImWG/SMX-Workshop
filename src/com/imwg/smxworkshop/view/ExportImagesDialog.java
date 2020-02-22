package com.imwg.smxworkshop.view;

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JColorChooser;

import com.imwg.smxworkshop.sprite.Sprite;
import com.imwg.smxworkshop.sprite.SpriteIO;

public class ExportImagesDialog extends PropDialog {

	private static final long serialVersionUID = 4977620204371008885L;
	private MainFrame mainFrame;
	private Choice imageModeChoice, anchorModeChoice;
	private NumberField rowsText, columnsText, paddingText;
	private Checkbox outlineCheckbox, smudgeCheckbox, csvCheckbox;
	private Button backgroundButton;
	
	static final int[] IMAGE_MODES = {
		SpriteIO.IMAGE_MODE_AUTO,
		SpriteIO.IMAGE_MODE_BYALPHA,
		SpriteIO.IMAGE_MODE_AUTO | SpriteIO.IMAGE_MODE_BACKGROUND_MASK,
		SpriteIO.IMAGE_MODE_SEPARATESHADOW,
		SpriteIO.IMAGE_MODE_SEPARATESHADOW | SpriteIO.IMAGE_MODE_BACKGROUND_MASK,
		SpriteIO.IMAGE_MODE_SHADOWONLY,
		SpriteIO.IMAGE_MODE_SHADOWONLY | SpriteIO.IMAGE_MODE_BACKGROUND_MASK,
		};
	
	static public Map<String, Integer> settings = new HashMap<String, Integer>(){
		private static final long serialVersionUID = -7849877117228654727L;
		{
			put("imageMode", SpriteIO.IMAGE_MODE_AUTO);
			put("anchorMode", SpriteIO.ANCHOR_MODE_ALIGN);
			put("rows", 1);
			put("columns", 1);
			put("padding", 0);
			put("backgroundColor", 0xffff00ff);
		}
	};
	
	public ExportImagesDialog(MainFrame frame) {
		super(frame, ExportImagesDialog.class);
		this.mainFrame = frame;
		
		setBounds();
		this.loadDefaultEvents();
		
		this.addLabel("Label.imageMode");
		this.addLabel("Label.anchorMode");
		this.addLabel("Label.size");
		this.addLabel("Label.padding");
		this.addLabel("Label.background");
		
		imageModeChoice = this.addChoice("Choice.imageMode");
		anchorModeChoice = this.addChoice("Choice.anchorMode");
		this.add(rowsText = new NumberField(true), "TextField.rows");
		this.add(columnsText = new NumberField(true), "TextField.columns");
		this.add(paddingText = new NumberField(true), "TextField.padding");
		outlineCheckbox = this.addCheckbox("Checkbox.outline");
		smudgeCheckbox = this.addCheckbox("Checkbox.smudge");
		csvCheckbox = this.addCheckbox("Checkbox.csv");
		
		rowsText.setRange(1, null);
		columnsText.setRange(1, null);
		paddingText.setRange(0, null);
		
		this.addButton("Button.autoStrip").addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				Sprite sprite = mainFrame.getSprite();
				int count = sprite.getFrameCount();
				int rows = (int) Math.ceil(Math.sqrt(count));
				int columns = (int) Math.ceil(count / (double) rows);
				rowsText.setText(rows);
				columnsText.setText(columns);
			}
		});
		
		backgroundButton = this.addButton("Button.background");
		backgroundButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				Color c = JColorChooser.showDialog(ExportImagesDialog.this, 
						backgroundButton.getLabel(),
						backgroundButton.getBackground());
				if (c != null)
					backgroundButton.setBackground(c);
			}
		});
				
		// SET VALUES
		int imageMode = settings.get("imageMode");
		outlineCheckbox.setState((imageMode & SpriteIO.IMAGE_MODE_OUTLINE_MASK) != 0);
		smudgeCheckbox.setState((imageMode & SpriteIO.IMAGE_MODE_SMUDGE_MASK) != 0);
		imageMode &= SpriteIO.IMAGE_MODE_MASK | SpriteIO.IMAGE_MODE_BACKGROUND_MASK;
		for (int i=0; i<IMAGE_MODES.length; ++i){
			if (IMAGE_MODES[i] == imageMode){
				imageModeChoice.select(i);
				break;
			}
		}
		
		int anchorMode = settings.get("anchorMode");
		csvCheckbox.setState((anchorMode & SpriteIO.ANCHOR_MODE_CSV_MASK) != 0);
		anchorModeChoice.select(anchorMode & SpriteIO.ANCHOR_MODE_MASK);
				
		rowsText.setText(settings.get("rows"));
		columnsText.setText(settings.get("columns"));
		paddingText.setText(settings.get("padding"));
		backgroundButton.setBackground(new Color(settings.get("backgroundColor")));
		
	}

	@Override
	public void onConfirmed() {
		int imageMode = IMAGE_MODES[imageModeChoice.getSelectedIndex()];
		if (outlineCheckbox.getState())
			imageMode |= SpriteIO.IMAGE_MODE_OUTLINE_MASK;
		if (smudgeCheckbox.getState())
			imageMode |= SpriteIO.IMAGE_MODE_SMUDGE_MASK;
		
		int anchorMode = anchorModeChoice.getSelectedIndex();
		if (csvCheckbox.getState())
			anchorMode |= SpriteIO.ANCHOR_MODE_CSV_MASK;
		
		settings.put("imageMode", imageMode);
		settings.put("anchorMode", anchorMode);
		
		settings.put("rows", rowsText.getInteger());
		settings.put("columns", columnsText.getInteger());
		settings.put("padding", paddingText.getInteger());
		settings.put("backgroundColor", backgroundButton.getBackground().getRGB());
	}

}
