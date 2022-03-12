package com.imwg.smxworkshop.sprite;

import java.awt.image.BufferedImage;
import java.lang.ref.WeakReference;

import com.imwg.smxworkshop.sprite.Palette;
import com.imwg.smxworkshop.sprite.Sprite;
import com.imwg.smxworkshop.view.MainFrame;

public class SpritePreview {
	Sprite sprite;
	public int playerColorId = 0;
	private WeakReference<BufferedImage>[] frameImages; // Due to large memory occupy
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
		WeakReference<BufferedImage> ref = frameImages[index*4 + type]; 
		if (ref != null){
			if (ref.get() != null){
				return ref.get();
			}
		}
		this.createImage(index);
		return frameImages[index*4 + type].get();
	}
	
	public boolean getFrameStatus(int index){
		if (index >= frameStatus.length)
			return true;
		return frameStatus[index];
	}
	
	public void createImage(int index){
		Sprite.Frame frame = sprite.getFrame(index);
		Palette pal = Palette.getPalette(frame.getPalette());
		Palette ppal = Palette.getPlayerPalette(sprite.playerMode, playerColorId);

		// Draw Normal
		if (frame.getWidth(Sprite.DATA_IMAGE) > 0 && frame.getHeight(Sprite.DATA_IMAGE) > 0){
			
			BufferedImage im = new BufferedImage(frame.getWidth(Sprite.DATA_IMAGE),
					frame.getHeight(Sprite.DATA_IMAGE), BufferedImage.TYPE_INT_ARGB);
			for (int i=0; i<frame.getHeight(Sprite.DATA_IMAGE); ++i){
				for (int j=0; j<frame.getWidth(Sprite.DATA_IMAGE); ++j){
					int pixel = frame.getPixel(Sprite.DATA_IMAGE, j, i);
					if (pixel != Sprite.PIXEL_NULL){
						if (pixel >= Sprite.PIXEL_PLAYER_START){
							if (pixel < ppal.getColorCount()+ Sprite.PIXEL_PLAYER_START)
								im.setRGB(j, i, 0xff000000 | ppal.getColor(pixel - Sprite.PIXEL_PLAYER_START));
						}else{
							if (pixel < pal.getColorCount())
								im.setRGB(j, i, pal.getColor(pixel));
						}
					}
				}
			}
			frameImages[index*4+Sprite.DATA_IMAGE] = new WeakReference<BufferedImage>(im);
		}else{
			frameImages[index*4+Sprite.DATA_IMAGE] = new WeakReference<BufferedImage>(null);
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
			frameImages[index*4+Sprite.DATA_SHADOW] = new WeakReference<BufferedImage>(im);
		}else{
			frameImages[index*4+Sprite.DATA_SHADOW] = new WeakReference<BufferedImage>(null);
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
			frameImages[index*4+Sprite.DATA_OUTLINE] = new WeakReference<BufferedImage>(im);
		}else{
			frameImages[index*4+Sprite.DATA_OUTLINE] = new WeakReference<BufferedImage>(null);
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
			frameImages[index*4+Sprite.DATA_SMUDGE] = new WeakReference<BufferedImage>(im);
		}else{
			frameImages[index*4+Sprite.DATA_SMUDGE] = new WeakReference<BufferedImage>(null);
		}
	}
	
	public void refresh(){
		if (sprite == null)
			return;
		
		int count = sprite.getFrameCount();
		frameImages = new WeakReference[count*4];
		frameStatus = new boolean[count];
		
		if (currentLoadingThread != null)
			currentLoadingThread.interrupt();
		
		// Async for better feeling
		currentLoadingThread = new Thread(new Runnable(){
			
			public void run(){
				for (int index=0; index<sprite.getFrameCount(); ++index){
					MainFrame.setProcessString(String.format("Painting %d/%d ...", index, sprite.getFrameCount()));
					createImage(index);
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
