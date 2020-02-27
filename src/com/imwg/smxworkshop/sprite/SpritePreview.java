package com.imwg.smxworkshop.sprite;

import java.awt.image.BufferedImage;

import com.imwg.smxworkshop.sprite.Palette;
import com.imwg.smxworkshop.sprite.Sprite;
import com.imwg.smxworkshop.view.MainFrame;

public class SpritePreview {
	Sprite sprite;
	public int playerColorId = 0;
	private BufferedImage[] frameImages;
	
	volatile private boolean[] frameStatus = new boolean[0]; // Async
	private Thread currentLoadingThread = null;
	
	static public BufferedImage[] paletteImages;
	
	public Sprite getSprite(){
		return this.sprite;
	}
	public void setSprite(Sprite sprite){
		this.sprite = sprite;
	}
	
	public BufferedImage getFrameImage(int index, int type){
		return frameImages[index*4 + type];
	}
	
	public boolean getFrameStatus(int index){
		if (index >= frameStatus.length)
			return true;
		return frameStatus[index];
	}
	
	public void refresh(){
		if (sprite == null)
			return;
		
		int count = sprite.getFrameCount();
		frameImages = new BufferedImage[count*4];
		frameStatus = new boolean[count];
		
		if (currentLoadingThread != null)
			currentLoadingThread.interrupt();
		
		// Async for better feeling
		currentLoadingThread = new Thread(new Runnable(){
			
			public void run(){
				
				Palette ppal = Palette.getPlayerPalette(sprite.playerMode, playerColorId);
				
				for (int index=0; index<sprite.getFrameCount(); ++index){
					Sprite.Frame frame = sprite.getFrame(index);
					Palette pal = Palette.getPalette(frame.getPalette());

					// Draw Normal
					if (frame.getWidth(Sprite.DATA_IMAGE) > 0 && frame.getHeight(Sprite.DATA_IMAGE) > 0){
						
						BufferedImage im = new BufferedImage(frame.getWidth(Sprite.DATA_IMAGE),
								frame.getHeight(Sprite.DATA_IMAGE), BufferedImage.TYPE_INT_ARGB);
						for (int i=0; i<frame.getHeight(Sprite.DATA_IMAGE); ++i){
							for (int j=0; j<frame.getWidth(Sprite.DATA_IMAGE); ++j){
								int pixel = frame.getPixel(Sprite.DATA_IMAGE, j, i);
								if (pixel != Sprite.PIXEL_NULL){
									if (pixel >= Sprite.PIXEL_PLAYER_START){
										if (pixel < ppal.rgbs.length + Sprite.PIXEL_PLAYER_START)
											im.setRGB(j, i, 0xff000000 | ppal.rgbs[pixel - Sprite.PIXEL_PLAYER_START]);
									}else{
										if (pixel < pal.rgbs.length)
											im.setRGB(j, i, pal.rgbs[pixel]);
									}
								}
							}
						}
						frameImages[index*4+Sprite.DATA_IMAGE] = im;
					}else{
						frameImages[index*4+Sprite.DATA_IMAGE] = null;
					}
				
					// Draw Shadow
					if (frame.getWidth(Sprite.DATA_SHADOW) > 0 && frame.getHeight(Sprite.DATA_SHADOW) > 0){
						BufferedImage im = new BufferedImage(frame.getWidth(Sprite.DATA_SHADOW),
								frame.getHeight(Sprite.DATA_SHADOW), BufferedImage.TYPE_INT_ARGB);
						for (int i=0; i<frame.getHeight(Sprite.DATA_SHADOW); ++i){
							for (int j=0; j<frame.getWidth(Sprite.DATA_SHADOW); ++j){
								int pixel = frame.getPixel(Sprite.DATA_SHADOW, j, i);
								if (pixel != Sprite.PIXEL_NULL)
									im.setRGB(j, i, pixel << 24);
							}
						}
						frameImages[index*4+Sprite.DATA_SHADOW] = im;
					}else{
						frameImages[index*4+Sprite.DATA_SHADOW] = null;
					}
					
					// Draw Outline
					if (frame.getWidth(Sprite.DATA_OUTLINE) > 0 && frame.getHeight(Sprite.DATA_OUTLINE) > 0){
						BufferedImage im = new BufferedImage(frame.getWidth(Sprite.DATA_OUTLINE),
								frame.getHeight(Sprite.DATA_OUTLINE), BufferedImage.TYPE_INT_ARGB);
						for (int i=0; i<frame.getHeight(Sprite.DATA_OUTLINE); ++i){
							for (int j=0; j<frame.getWidth(Sprite.DATA_OUTLINE); ++j){
								int pixel = frame.getPixel(Sprite.DATA_OUTLINE, j, i);
								if (pixel != Sprite.PIXEL_NULL){
									im.setRGB(j, i, 0xffffffff);
								}
							}
						}
						frameImages[index*4+Sprite.DATA_OUTLINE] = im;
					}else{
						frameImages[index*4+Sprite.DATA_OUTLINE] = null;
					}
					
					// Draw Smudge
					if (frame.getWidth(Sprite.DATA_SMUDGE) > 0 && frame.getHeight(Sprite.DATA_SMUDGE) > 0){
						BufferedImage im = new BufferedImage(frame.getWidth(Sprite.DATA_SMUDGE),
								frame.getHeight(Sprite.DATA_SMUDGE), BufferedImage.TYPE_INT_ARGB);
						for (int i=0; i<frame.getHeight(Sprite.DATA_SMUDGE); ++i){
							for (int j=0; j<frame.getWidth(Sprite.DATA_SMUDGE); ++j){
								int pixel = frame.getPixel(Sprite.DATA_SMUDGE, j, i);
								if (pixel != Sprite.PIXEL_NULL){
									pixel = (pixel >> 2) & 0xff;
									im.setRGB(j, i, pixel << 24);
								}
							}
						}
						frameImages[index*4+Sprite.DATA_SMUDGE] = im;
					}else{
						frameImages[index*4+Sprite.DATA_SMUDGE] = null;
					}
					
					MainFrame.setProcessString(String.format("Painting %d/%d ...", index, sprite.getFrameCount()));
					frameStatus[index] = true;
					
				}
			}
			
		});
		currentLoadingThread.start();
		
	}
	
	static public void loadPaletteImages(){
		paletteImages = new BufferedImage[Palette.ORIGINAL_PALETTE_COUNT + Palette.getCustomPaletteCount()];
		for (int i=0; i<paletteImages.length; ++i){
			if (Palette.getPalette(i) != null){
				int[] rgbs = Palette.getPalette(i).rgbs;
				int size = 32;
				if (rgbs.length <= 256){
					size = 16;
				}
				BufferedImage paletteImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
				for (int j=0; j<rgbs.length; ++j){
					paletteImage.setRGB(j % size, j / size, rgbs[j]);
				}
				paletteImages[i] = paletteImage;
			}
		}
	}
	
	
}
