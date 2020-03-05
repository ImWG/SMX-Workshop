package com.imwg.smxworkshop.view;

import java.awt.Canvas;
import java.awt.Checkbox;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Scrollbar;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;

import com.imwg.smxworkshop.model.FrameFilter;

public class ImageAdjustDialog extends PropDialog {

	private Scrollbar brightBar, contrastBar;
	private Scrollbar hueBar, saturateBar, valueBar;
	private BufferedImage sourceImage, adjustedImage;
	private Checkbox tintCheckbox, normalCheckbox, playerCheckbox;
	private Canvas previewCanvas;
	private boolean brightnessMode;
	private double scaleRate;
	
	static public double brightness, contrast, hue, saturation, value;
	static public boolean tint, normal = true, player = true;
	
	public ImageAdjustDialog(Frame owner, boolean brightnessMode) {
		super(owner, ImageAdjustDialog.class);
		setBounds();
		loadDefaultEvents();
		
		previewCanvas = new BufferedCanvas(){
			private static final long serialVersionUID = -4551224243328558750L;
			@Override
			public void paint(Graphics g){
				g.drawImage(adjustedImage, 
						(getWidth() - adjustedImage.getWidth()) / 2,
						(getHeight() - adjustedImage.getHeight()) / 2,
						null);
			}
		};
		add(previewCanvas, "Canvas.preview");
		
		AdjustmentListener listener = new AdjustmentListener(){
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				refreshAdjustedImage();
				previewCanvas.repaint();
			}
		};
		ItemListener itemListener = new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent e) {
				refreshAdjustedImage();
				previewCanvas.repaint();
			}
		};
		
		normalCheckbox = this.addCheckbox("Checkbox.normal");
		playerCheckbox = this.addCheckbox("Checkbox.player");
		normalCheckbox.setState(normal);
		playerCheckbox.setState(player);
		
		this.brightnessMode = brightnessMode;
		if (brightnessMode){
			add(brightBar = new Scrollbar(), "Scrollbar.bright");
			brightBar.setOrientation(Scrollbar.HORIZONTAL);
			brightBar.setMinimum(-255);
			brightBar.setMaximum(255);
			add(contrastBar = new Scrollbar(), "Scrollbar.contrast");
			contrastBar.setOrientation(Scrollbar.HORIZONTAL);
			contrastBar.setMinimum(-255);
			contrastBar.setMaximum(255);
			brightBar.addAdjustmentListener(listener);
			contrastBar.addAdjustmentListener(listener);
			
			brightBar.setValue((int)(brightness * 255));
			contrastBar.setValue((int)(contrast * 255));
			
		}else{
			setTitle(ViewConfig.getString("ImageAdjustDialog.alter"));
			add(hueBar = new Scrollbar(), "Scrollbar.hue");
			hueBar.setOrientation(Scrollbar.HORIZONTAL);
			hueBar.setMinimum(-255);
			hueBar.setMaximum(255);
			add(saturateBar = new Scrollbar(), "Scrollbar.saturate");
			saturateBar.setOrientation(Scrollbar.HORIZONTAL);
			saturateBar.setMinimum(-255);
			saturateBar.setMaximum(255);
			add(valueBar = new Scrollbar(), "Scrollbar.value");
			valueBar.setOrientation(Scrollbar.HORIZONTAL);
			valueBar.setMinimum(-255);
			valueBar.setMaximum(255);
			hueBar.addAdjustmentListener(listener);
			saturateBar.addAdjustmentListener(listener);
			valueBar.addAdjustmentListener(listener);
			tintCheckbox = addCheckbox("Checkbox.tint");
			tintCheckbox.addItemListener(itemListener);
			
			hueBar.setValue((int)(hue * 255));
			saturateBar.setValue((int)(saturation * 255));
			valueBar.setValue((int)(value * 255));
			tintCheckbox.setState(tint);
		}
		
	}
	
	public void setPreviewImage(BufferedImage image){
		sourceImage = image;
		int width = sourceImage.getWidth();
		int height = sourceImage.getHeight();
		scaleRate = Math.min(1, Math.min(
				(double) previewCanvas.getWidth() / width,
				(double) previewCanvas.getHeight() / height));
		adjustedImage = new BufferedImage(
				(int) (width * scaleRate), (int) (height * scaleRate),
				BufferedImage.TYPE_INT_ARGB);
		
		refreshAdjustedImage();
		previewCanvas.repaint();
	}
	
	private void refreshAdjustedImage(){
		final int width = adjustedImage.getWidth();
		final int height = adjustedImage.getHeight();
		
		if (brightnessMode){
			final double brightness = brightBar.getValue() / 255.0;
			final double contrast = contrastBar.getValue() / 255.0;
			
			for (int y = 0; y < height; ++y){
				int y1 = (int) (y / scaleRate);
				for (int x = 0; x < width; ++x){
					int x1 = (int) (x / scaleRate);
					adjustedImage.setRGB(x, y, 
							FrameFilter.adjustColorBrightness(sourceImage.getRGB(x1, y1), brightness, contrast));
				}
			}
			
		}else{
			final double hue = hueBar.getValue() / 255.0;
			final double saturate = saturateBar.getValue() / 255.0;
			final double value = valueBar.getValue() / 255.0;
			
			if (tintCheckbox.getState())
				for (int y = 0; y < height; ++y){
					int y1 = (int) (y / scaleRate);
					for (int x = 0; x < width; ++x){
						int x1 = (int) (x / scaleRate);
						adjustedImage.setRGB(x, y, 
								FrameFilter.adjustColorTint(sourceImage.getRGB(x1, y1), hue, saturate, value));
					}
				}
			else
				for (int y = 0; y < height; ++y){
					int y1 = (int) (y / scaleRate);
					for (int x = 0; x < width; ++x){
						int x1 = (int) (x / scaleRate);
						adjustedImage.setRGB(x, y, 
								FrameFilter.adjustColorHue(sourceImage.getRGB(x1, y1), hue, saturate, value));
					}
				}
			
		}
	}
	
	@Override
	public void onConfirmed(){
		if (brightnessMode){
			brightness = (double) brightBar.getValue() / 255;
			contrast = (double) contrastBar.getValue() / 255;
			
		}else{
			hue = (double) hueBar.getValue() / 255;
			saturation = (double) saturateBar.getValue() / 255;
			value = (double) valueBar.getValue() / 255;
			tint = tintCheckbox.getState();
			
		}
		normal = normalCheckbox.getState();
		player = playerCheckbox.getState();
	}

	
}
