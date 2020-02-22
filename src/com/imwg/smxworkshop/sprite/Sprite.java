package com.imwg.smxworkshop.sprite;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

abstract public class Sprite implements Iterable<Sprite.Frame> {
	public static final int DATA_IMAGE = 0;
	public static final int DATA_SHADOW = 1;
	public static final int DATA_OUTLINE = 2;
	public static final int DATA_SMUDGE = 3;
	public static final int[] DATA_TYPES = new int[]{DATA_IMAGE, DATA_SHADOW, DATA_OUTLINE, DATA_SMUDGE};
	
	static public final int PIXEL_NULL = -1;
	static public final int PIXEL_PLAYER_START = 0x10000;
	
	static public final int VERSION_NONE = 0;
	static public final int VERSION_SLP = 1;
	static public final int VERSION_SMX = 2;

	static public final int PLAYER_PALETTE_NONE = -1;
	static public final int PLAYER_PALETTE_DE = 0;
	static public final int PLAYER_PALETTE_AOK = 1;
	static public final int PLAYER_PALETTE_AOE = 2;
	
	static public final int FLIP_HORIZONTAL = 1;
	static public final int FLIP_VERTICAL = 2;
	static public final int ROTATE_TRANSPOSE = 0;
	static public final int ROTATE_CLOCKWISE_90 = 1;
	static public final int ROTATE_180 = 2;
	static public final int ROTATE_COUNTER_CLOCKWISE_90 = 3;
	
	
	List<Frame> frames = new LinkedList<Frame>();
	public String memo;
	public int playerMode;
	
	abstract public int getVersion();
	
	public Sprite(){
		super();
	}
	public Sprite(Sprite sprite){
		super();
		this.memo = sprite.memo;
		this.playerMode = sprite.playerMode;
		for (int i=0; i<sprite.getFrameCount(); ++i){
			this.frames.add(this.createFrame(sprite.getFrame(i)));
		}
	}
	
	public Frame getFrame(int index){
		if (index >= 0 && index < frames.size())
			return frames.get(index);
		return null;
	}
	public void insertFrame(int index, Frame frame){
		if (frame.getSprite() == this)
			frames.add(index, frame);
		else
			frames.add(index, createFrame(frame));
	}
	public void setFrame(int index, Frame frame){
		if (frame.getSprite() == this)
			frames.set(index, frame);
		else
			frames.set(index, createFrame(frame));
	}
	public void removeFrame(int index){
		frames.remove(index);
	}
	public void swapFrames(int index1, int index2){
		Frame frame1 = frames.get(index1);
		frames.set(index1, frames.get(index2));
		frames.set(index2, frame1);
	}

	public int getFrameCount(){
		return frames.size();
	}
	
	public Iterator<Sprite.Frame> iterator(){
		return frames.iterator();
	}
	
	
	abstract public Frame createFrame();
	abstract public Frame createFrame(Sprite.Frame frame);
	
	abstract public class Frame{
		
		int palette;
		Sprite sprite;
		
		abstract public int getVersion();
		
		abstract public int getWidth(int type);
		abstract public int getHeight(int type);
		abstract public int getAnchorX(int type);
		abstract public int getAnchorY(int type);
		
		public int getWidth(){
			int width = getWidth(DATA_IMAGE) - getAnchorX(DATA_IMAGE);
			for (int TYPE : DATA_TYPES){
				width = Math.max(width, getWidth(TYPE) - getAnchorX(TYPE));
			}
			return width + getAnchorX();
		}
		public int getHeight(){
			int height = getHeight(DATA_IMAGE) - getAnchorY(DATA_IMAGE);
			for (int TYPE : DATA_TYPES){
				height = Math.max(height, getHeight(TYPE) - getAnchorY(TYPE));
			}
			return height + getAnchorY();
		}
		public int getAnchorX(){
			int x = getAnchorX(DATA_IMAGE);
			for (int TYPE : DATA_TYPES){
				x = Math.max(x, getAnchorX(TYPE));
			}
			return x;
		}
		public int getAnchorY(){
			int y = getAnchorY(DATA_IMAGE);
			for (int TYPE : DATA_TYPES){
				y = Math.max(y, getAnchorY(TYPE));
			}
			return y;
		}
		
		@Deprecated
		abstract public void create(int type, int width, int height);
		abstract public void create(int width, int height);
		/**
		 * Expands a layer to certain size. Size of new layer might be not changed.
		 * @param type Layer type.
		 * @param left Minimum size of left padding. 
		 * @param top Minimum size of top padding.
		 * @param right Minimum size of left padding.
		 * @param bottom Minimum size of left padding.
		 */
		abstract public void expand(int type, int left, int top, int right, int bottom);
		abstract public void remove(int type);
		abstract public void crop(int type);
		abstract public void setAnchor(int type, int x, int y);
		
		abstract public int getPixel(int type, int x, int y);
		abstract public void setPixel(int type, int x, int y, int value);
		
		abstract public void flip(int orientation);
		abstract public void rotate(int angle);
		abstract public void scale(double d, double d2);
		public void scale(double d){
			scale(d, d);
		}		

		protected void cloneFrame(Sprite.Frame frame){
			for (int type : DATA_TYPES){
				this.setAnchor(type, frame.getAnchorX(type), frame.getAnchorY(type));
				int width = frame.getWidth(type), height = frame.getHeight(type);
				int anchorx = frame.getAnchorX(type), anchory = frame.getAnchorY(type);
				this.expand(type, anchorx, anchory, width - anchorx, height - anchory);
				for (int i=0; i<frame.getHeight(type); ++i){
					for (int j=0; j<frame.getWidth(type); ++j){
						this.setPixel(type, j, i, frame.getPixel(type, j, i));
					}
				}
			}
			setPalette(frame.getPalette());
		}
		
		public int getPalette(){
			return palette;
		}
		public void setPalette(int palette){
			this.palette = palette;
		}
		
		public Sprite getSprite(){
			return this.sprite;
		}
		
		public int getPlayerColorMode(){
			return this.sprite.playerMode;
		}
		
		public void changePixelsByPalette(Palette srcPal, Palette dstPal, boolean player){
			int[] map = Palette.getMappingArray(srcPal, dstPal, false);
			
			for (int i=0; i<getHeight(DATA_IMAGE); ++i){
				for (int j=0; j<getWidth(DATA_IMAGE); ++j){
					int pixel = getPixel(DATA_IMAGE, j, i);
					if (pixel != PIXEL_NULL){
						if (!player){
							if (pixel < Sprite.PIXEL_PLAYER_START)
								setPixel(DATA_IMAGE, j, i, map[pixel]);
						}else {
							if (pixel >= Sprite.PIXEL_PLAYER_START)
								setPixel(DATA_IMAGE, j, i, map[pixel - Sprite.PIXEL_PLAYER_START] + Sprite.PIXEL_PLAYER_START);
						}
					}
				}
			}
		}
		
		public int getPixelRelative(int type, int x, int y){
			return getPixel(type, x + getAnchorX(type), y + getAnchorY(type));
		}
		public void setPixelRelative(int type, int x, int y, int value){
			setPixel(type, x + getAnchorX(type), y + getAnchorY(type), value);
		}
		
	}
	

}
