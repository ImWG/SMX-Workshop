package com.imwg.smxworkshop.view;

import java.awt.Canvas;
import java.awt.Checkbox;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Scrollbar;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;

import com.imwg.smxworkshop.model.FrameFilter;

public class ImageAdjustDialog extends PropDialog {

	private static final long serialVersionUID = -2726331033002687526L;
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
			brightBar = addLinkedBarAndField("Scrollbar.bright", "TextField.bright");
			contrastBar = addLinkedBarAndField("Scrollbar.contrast", "TextField.contrast");
			brightBar.addAdjustmentListener(listener);
			contrastBar.addAdjustmentListener(listener);
			
			brightBar.setValue((int)(brightness * 255));
			contrastBar.setValue((int)(contrast * 255));
			
		}else{
			setTitle(ViewConfig.getString("ImageAdjustDialog.alter"));
			hueBar = addLinkedBarAndField("Scrollbar.hue", "TextField.hue");
			saturateBar = addLinkedBarAndField("Scrollbar.saturate", "TextField.saturate");
			valueBar = addLinkedBarAndField("Scrollbar.value", "TextField.value");
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
	
	private Scrollbar addLinkedBarAndField(String barName, String fieldName){
		final Scrollbar bar = new Scrollbar(); 
		add(bar, barName);
		bar.setOrientation(Scrollbar.HORIZONTAL);
		bar.setMinimum(-255);
		bar.setMaximum(255);
		
		final NumberField field = new NumberField(true);
		add(field, fieldName);
		field.setRange(-255, 255);
		
		field.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println(".");
				bar.setValue(field.getInteger());
				for (AdjustmentListener listener: bar.getAdjustmentListeners()){
					listener.adjustmentValueChanged(null);
				}
			}
		});
		bar.addAdjustmentListener(new AdjustmentListener(){
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				field.setText(bar.getValue());
			}
		});
		
		return bar;
	}

	
}
