package com.imwg.smxworkshop.model;

import java.awt.Desktop;
import java.io.File;

import com.imwg.smxworkshop.sprite.Palette;
import com.imwg.smxworkshop.sprite.SMXSprite;
import com.imwg.smxworkshop.sprite.Sprite;
import com.imwg.smxworkshop.sprite.SpriteIO;
import com.imwg.smxworkshop.sprite.SpritePreview;
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
	
	public void openShellFile(String name){
	   if (Desktop.isDesktopSupported()) {
	    	try{
				Desktop desktop = Desktop.getDesktop();
				desktop.open(new File(name));
	    	}catch(Exception e){
	    		System.out.println(e.getMessage());
	    	}
	    }
	}

	
	public int saveSprite(Sprite sprite, File file, String format){
		String name = file.getName(); 
		if (format.equals("")){
			String[] ends = name.split("\\.");
			format = ends[ends.length-1].toUpperCase();
		}
		switch (format){
		case "SLP":
			SpriteIO.saveSLPSprite(sprite, file.getAbsolutePath());
			break;
		case "SMX":	
		default:
			SpriteIO.saveSMXSprite(sprite, file.getAbsolutePath());
		}
		return 1;
	}
	
	public void setComment(Sprite sprite, String comment){
		sprite.setMemo(comment);
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
			for (int TYPE : Sprite.getDataTypes())
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
						Palette.getPlayerPalette(sprite.getPlayerMode(), mainFrame.getPreview().playerColorId));
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
					for (int TYPE : Sprite.getDataTypes()){
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
				for (int TYPE : Sprite.getDataTypes()){
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
					Palette srcPal = Palette.getPlayerPalette(sprite.getPlayerMode(), playerId);
					Palette dstPal = Palette.getPlayerPalette(version, playerId);
					
					frame.changePixelsByPalette(srcPal, dstPal, true);
				}
			}
			sprite.setPlayerMode(version);
		}
	}
	
	// TODO
	public void adjustBrightness(Sprite sprite, int[] frameIndexes, double brightness, double contrast,
			int dstPal, int dstPlayerPal, int playerIndex){
		
		FrameFilter filter = new FrameFilter();
		for (int index : mainFrame.getSelectedFrames()){
			Sprite.Frame frame = sprite.getFrame(index);
			filter.setFrame(frame);
			if (dstPal >= 0){
				Palette srcPalette = Palette.getPalette(frame.getPalette());
				filter.adjustBrightness(
						brightness, contrast, srcPalette, srcPalette, false);
			}
			if (dstPlayerPal >= 0){
				Palette srcPalette = Palette.getPlayerPalette(frame.getPalette(), playerIndex);
				filter.adjustBrightness(
						brightness, contrast, srcPalette, srcPalette, true);
			}
		}
	}
	
	// TODO
	public void adjustHue(Sprite sprite, int[] frameIndexes, double hue, double saturation, double value,
			boolean tint, int dstPal, int dstPlayerPal, int playerIndex){
		
		FrameFilter filter = new FrameFilter();
		for (int index : mainFrame.getSelectedFrames()){
			Sprite.Frame frame = sprite.getFrame(index);
			filter.setFrame(frame);
			if (dstPal >= 0){
				Palette srcPalette = Palette.getPalette(frame.getPalette());
				filter.adjustHue(
						hue, saturation, value, tint, srcPalette, srcPalette, false);
			}
			if (dstPlayerPal >= 0){
				Palette srcPalette = Palette.getPlayerPalette(frame.getPalette(), playerIndex);
				filter.adjustHue(
						hue, saturation, value, tint, srcPalette, srcPalette, true);
			}
		}
	}
	
	/**
	 * Create mirrored angle frames. Original directions ranged from down to up, clockwise.
	 * @param sprite Sprite to change.
	 * @param angles Full angle count of sprite. Must be times of 4.
	 */
	public void completeMirrorAngles(Sprite sprite, int angles){
		if (angles >= 4 && angles % 4 == 0){
			int halfAngle = angles / 2 + 1;
			int perAngle = sprite.getFrameCount() / halfAngle;
			for (int j = halfAngle-2; j > 0; --j){
				int offset = j * perAngle;
				for (int i = 0; i < perAngle; ++i){
					Sprite.Frame frame = sprite.createFrame(sprite.getFrame(offset + i));
					frame.flip(Sprite.FLIP_HORIZONTAL);
					sprite.insertFrame(sprite.getFrameCount(), frame);
				}
			}
			adjustAngles(sprite, angles, angles / 4, 0, true, true);
		}
	}

	/**
	 * Remove mirrored angle frames. Directions start by right, clockwise.
	 * @param sprite Sprite to change.
	 * @param angles Full angle count of sprite. Must be times of 4.
	 */
	public void removeMirrorAngles(Sprite sprite, int angles) {
		if (angles >= 4 && angles % 4 == 0){
			int perAngle = sprite.getFrameCount() / angles;

			int count = (angles / 4 - 1) * perAngle; // Up to right 
			int offset = angles * perAngle;
			for (int i = 0; i < count; ++i){
				sprite.removeFrame(--offset);
			}
			
			count += perAngle; 
			for (int i = 0; i < count; ++i){ // Right to down
				sprite.removeFrame(0);
			}

		}
	}
	
	/**
	 * Reverse angle sequence of sprite. First direction is kept.
	 * @param sprite Sprite to change.
	 * @param angles Full angle count of sprite. Must be 3 at least.
	 */
	public void reverseAngles(Sprite sprite, int angles){
		if (angles > 2){
			int perAngle = sprite.getFrameCount() / angles;
			int limit = (angles + 1) / 2;
			for (int j = 1; j < limit; ++j){
				int offset1 = j * perAngle;
				int offset2 = (angles - j) * perAngle;
				for (int i = 0; i < perAngle; ++i){
					sprite.swapFrames(offset1 + i, offset2 + i);
				}
			}
		}
	}
	
	/**
	 * Adjust angles sequence, rotate from one angle started to another.
	 * @param sprite Sprite to adjust.
	 * @param angles Full angle count of sprite. Must be 3 at least.
	 * @param srcAngle Original start angle, in clockwise, 0 is right.
	 * @param dstAngle Adjusted start angle, in clockwise, 0 is right.
	 * @param srcClockwise Is original sequence clockwise.
	 * @param dstClockwise Is adjusted sequence clockwise.
	 */
	public void adjustAngles(Sprite sprite, int angles, int srcAngle, int dstAngle,
			boolean srcClockwise, boolean dstClockwise) {
		
		if (angles >= 4 && angles % 4 == 0){
			int perAngle = sprite.getFrameCount() / angles;
			
			// Change to clockwise
			if (!srcClockwise)
				reverseAngles(sprite, angles);

			// Shift
			int delta = (dstAngle - srcAngle + angles) % angles;
			int limit = delta * perAngle; 
			int[] indexes = new int[limit];
			for (int i = 0; i < limit; ++i){
				indexes[i] = i;
			}
			shiftFrames(sprite, indexes, (angles - delta) * perAngle);
			
			if (!dstClockwise)
				reverseAngles(sprite, angles);

		}
	}
	
	/**
	 * Interpolate each segments divided by total angles to certain length.
	 * Will be interpolated with nearest frame.
	 * @param sprite Sprite to adjust.
	 * @param angles Full angle count of sprite.
	 * @param toCount Target frame count per angle.
	 * @param loop Is frames per angle looped. If looped, each frames shares same
	 * stride in interpolation. Or the first and the last have only half of others. 
	 */
	public void interpolateAngles(Sprite sprite, int angles, 
			int toCount, boolean loop) {
		int fromCount = sprite.getFrameCount() / angles;
		int frameCount = angles * fromCount;
		if (fromCount > 0 && fromCount != toCount){
			int totalCount = toCount * angles;
			int[] indexes = new int[totalCount];
			double rate;
			
			if (loop){
				rate = (double) fromCount / toCount;
				for (int i = 0, index = 0; i < angles; ++i){
					double offset = i * fromCount;
					if (rate > 1)
						offset += rate / 2;
					for (int j = 0; j < toCount; ++j){
						// Avoid precision problems
						indexes[index++] = (int) Math.floor(Math.round(offset * 1024)) >> 10;
						offset += rate;
					}
				}
			}else{
				rate = (double) (fromCount - 1) / (toCount - 1);
				for (int i = 0, index = 0; i < angles; ++i){
					double offset = i * fromCount + .5;
					for (int j = 0; j < toCount; ++j){
						// Avoid precision problems
						indexes[index++] = (int) Math.round(Math.round(offset * 1024)) >> 10;
						offset += rate;
					}
				}
			}
			
			if (rate < 1){ // Increase
				int pointer = totalCount - 1;
				for (int i = frameCount - 1; i >= 0; --i){
					Sprite.Frame frame = sprite.getFrame(i);
					for (--pointer; pointer >= 0 && indexes[pointer] == i; --pointer){
						sprite.insertFrame(i, sprite.createFrame(frame));
					}
				}
			}else{ // Reduce
				int pointer = frameCount;
				for (int i = totalCount - 1; i >= 0; --i){
					for (--pointer; pointer >= 0 && indexes[i] < pointer; --pointer){
						sprite.removeFrame(pointer);
					}
				}
				if (indexes[0] > 0){
					for (pointer = indexes[0]; pointer > 0; --pointer){
						sprite.removeFrame(0);
					}
				}
			}
		}
	}

	public void trimAngleFrames(Sprite sprite, int angles, int startFrame,
			int frames, boolean removeSelected) {
		
		int framePerAngle = sprite.getFrameCount() / angles;
		for (int i = angles - 1; i >= 0; --i) {
			int offset = i * framePerAngle;
			if (removeSelected) {
				for (int j = startFrame + frames - 1; j >= startFrame; --j) {
					sprite.removeFrame(j + offset);
				}
			}else {
				for (int j = framePerAngle - 1; j >= 0; --j) {
					if (j == startFrame + frames - 1) {
						if ((j -= frames) < 0) {
							break;
						}
					}
					sprite.removeFrame(j + offset);
				}
			}
		}
		
	}
	

	public void shiftAngleFrames(Sprite sprite, int angles, int radialOffset,
			int normalOffset) {
		
		int framePerAngle = sprite.getFrameCount() / angles;
		for (int i = angles - 1; i >= 0; --i) {
			double angle = Math.PI * 2.0 * i / angles;
			double sa = Math.sin(angle), ca = Math.cos(angle);
			int dx = (int) Math.round(-ca * radialOffset + sa * normalOffset); 
			int dy = (int) Math.round((-sa * radialOffset - ca * normalOffset) * 0.5);
			int offset = i * framePerAngle;
			int[] indices = new int[framePerAngle];
			for (int j = 0; j < framePerAngle; ++j) {
				indices[j] = offset + j;
			}
			this.setAnchor(sprite, indices, -1, dx, dy, true);
		}
		
	}
	
}
