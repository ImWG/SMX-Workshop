package com.imwg.smxworkshop.model;

import java.io.File;

import com.imwg.smxworkshop.plugin.Plugin;
import com.imwg.smxworkshop.sprite.Palette;
import com.imwg.smxworkshop.sprite.SMXSprite;
import com.imwg.smxworkshop.sprite.Sprite;
import com.imwg.smxworkshop.sprite.SpriteIO;
import com.imwg.smxworkshop.sprite.SpritePreview;
import com.imwg.smxworkshop.view.AboutDialog;
import com.imwg.smxworkshop.view.MainFrame;
import com.imwg.smxworkshop.view.ViewConfig;

public class MainModel {

	MainFrame mainFrame;
	
	public MainModel(MainFrame mainFrame){
		this.mainFrame = mainFrame;
	}
	
	public static void main(String[] args){
		Configuration.loadConfig();
		ViewConfig.loadViewConfig();
		Palette.loadPalettes();
		SpritePreview.loadPaletteImages();
		new MainFrame().loadSprite(new SMXSprite());
		
	}
	
	public static void exit(){
		Configuration.saveConfig();
	}
		
	public void setLanguage(String action){
		int id = Integer.parseInt(action);
		Configuration.setLanguageId(id);
		ViewConfig.setStringTable(id);
		Sprite sprite = mainFrame.getSprite();
		mainFrame.dispose();
		new MainFrame().loadSprite(sprite);
	}

	
	public int saveSprite(Sprite sprite, File file){
		String name = file.getName(); 
		String[] ends = name.split("\\.");
		switch (ends[ends.length-1]){
		case "SLP":
			SpriteIO.saveSLPSprite(sprite, file.getAbsolutePath());
			break;
		case "SMX":	
		default:
			SpriteIO.saveSMXSprite(sprite, file.getAbsolutePath());
		}
		return 1;
	}
	
	
	public void flipFrames(Sprite sprite, int[] frameIndexes, int angle){
		for (int index : frameIndexes){
			sprite.getFrame(index).flip(angle);
		}
	}
	public void rotateFrames(Sprite sprite, int[] frameIndexes, int angle){
		for (int index : frameIndexes){
			sprite.getFrame(index).rotate(angle);
		}		
	}
	public void cropFrames(Sprite sprite, int[] frameIndexes){
		for (int index : frameIndexes){
			for (int TYPE : Sprite.DATA_TYPES)
				sprite.getFrame(index).crop(TYPE);
		}
	}
	public void scaleFrames(Sprite sprite, int[] frameIndexes,
			double xFactor, double yFactor, boolean interpolate) {
		if (interpolate){
			FrameFilter filter = new FrameFilter();
			for (int index : frameIndexes){
				Sprite.Frame frame = sprite.getFrame(index);
				Palette.getPalette(frame.getPalette()).setCacheMode(true);
			}
			for (int index : frameIndexes){
				Sprite.Frame frame = sprite.getFrame(index);
				filter.setFrame(frame);
				filter.scale(xFactor, yFactor, Palette.getPalette(frame.getPalette()),
						Palette.getPlayerPalette(sprite.playerMode, mainFrame.getPreview().playerColorId));
			}
			for (int index : frameIndexes){
				Sprite.Frame frame = sprite.getFrame(index);
				Palette.getPalette(frame.getPalette()).setCacheMode(false);
			}
		}else{
			for (int index : frameIndexes){
				sprite.getFrame(index).scale(xFactor, yFactor);
			}
		}
	}
	
	public void duplicateFrames(Sprite sprite, int[] frameIndexes){
		int last = frameIndexes[frameIndexes.length-1] + 1;
		for (int i=0; i<frameIndexes.length; ++i){
			Sprite.Frame frame = sprite.getFrame(frameIndexes[i]);
			sprite.insertFrame(last+i, sprite.createFrame(frame));
		}
	}
	public void deleteFrames(Sprite sprite, int[] frameIndexes){
		for (int i=frameIndexes.length-1; i>=0; --i){
			sprite.removeFrame(frameIndexes[i]);
		}
	}
	public void reverseFrames(Sprite sprite, int[] frameIndexes){
		for (int i=0; i<frameIndexes.length/2; ++i){
			sprite.swapFrames(frameIndexes[i], frameIndexes[frameIndexes.length-i-1]);
		}
	}
	public int shiftFrames(Sprite sprite, int[] frameIndexes, int offset){
		final int count = frameIndexes.length;
		if (count == 0){
			return 0;
		}else{
			Sprite.Frame[] frames = new Sprite.Frame[count];
			for (int i=frameIndexes.length-1; i>=0; --i){ // Remove old frames
				frames[i] = sprite.getFrame(frameIndexes[i]); 
				sprite.removeFrame(frameIndexes[i]);
			}
			int first = frameIndexes[0] + offset;
			if (first < 0){
				first = 0;
			}else if (first > sprite.getFrameCount()){
				first = sprite.getFrameCount();
			}
			for (int i=frameIndexes.length-1; i>=0; --i){ // Insert frames
				sprite.insertFrame(first, frames[i]);
			}
			return first;
		}
	}
	
	
	public void setAnchor(Sprite sprite, int[] frameIndexes, int type, int x, int y, boolean relative){
		if (relative){
			if (type == -1){
				for (int i=0; i<frameIndexes.length; ++i){
					Sprite.Frame frame = sprite.getFrame(frameIndexes[i]); 
					for (int TYPE : Sprite.DATA_TYPES){
						frame.setAnchor(TYPE, 
								x + frame.getAnchorX(TYPE), y + frame.getAnchorY(TYPE));
					}
				}
			}else{
				for (int i=0; i<frameIndexes.length; ++i){
					Sprite.Frame frame = sprite.getFrame(frameIndexes[i]); 
					frame.setAnchor(type, 
						x + frame.getAnchorX(type), y + frame.getAnchorY(type));
				}
			}
			
		}else if (type == -1){
			for (int i=0; i<frameIndexes.length; ++i){
				Sprite.Frame frame = sprite.getFrame(frameIndexes[i]);
				int x0 = frame.getAnchorX(Sprite.DATA_IMAGE);
				int y0 = frame.getAnchorY(Sprite.DATA_IMAGE);
				for (int TYPE : Sprite.DATA_TYPES){
					frame.setAnchor(TYPE, x + frame.getAnchorX(TYPE) - x0,
							y + frame.getAnchorY(TYPE) - y0);
				}
			}
		}else{
			for (int i=0; i<frameIndexes.length; ++i){
				Sprite.Frame frame = sprite.getFrame(frameIndexes[i]); 
				frame.setAnchor(type, x, y);
			}
		}
	}
	
	
	public void removeFrameData(Sprite sprite, int[] frameIndexes, int type){
		for (int index : frameIndexes){
			sprite.getFrame(index).remove(type);
		}
	}
	
	
	public void setPalette(Sprite sprite, int[] frameIndexes, int palette, boolean convert){
		if (sprite != null){
			if (convert){
				for (int index : frameIndexes){
					Sprite.Frame frame = sprite.getFrame(index);  
					if (frame != null){
						frame.changePixelsByPalette(Palette.getPalette(frame.getPalette()),
								Palette.getPalette(palette), false);
						frame.setPalette(palette);
					}
				}				
			}else{
				for (int index : frameIndexes){
					Sprite.Frame frame = sprite.getFrame(index);
					frame.setPalette(palette);
				}
			}
		}
	}
	
	public void setPlayerPalette(Sprite sprite, int version, boolean convert){
		if (sprite != null){
			if (convert){
				int playerId = mainFrame.getPreview().playerColorId;
				for (int i=0; i<sprite.getFrameCount(); ++i){
					Sprite.Frame frame = sprite.getFrame(i);
					Palette srcPal = Palette.getPlayerPalette(sprite.playerMode, playerId);
					Palette dstPal = Palette.getPlayerPalette(version, playerId);
					
					frame.changePixelsByPalette(srcPal, dstPal, true);
				}
			}
			sprite.playerMode = version;
		}
	}
	
}
