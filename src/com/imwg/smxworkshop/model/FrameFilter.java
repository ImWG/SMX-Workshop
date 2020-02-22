package com.imwg.smxworkshop.model;

import com.imwg.smxworkshop.sprite.Palette;
import com.imwg.smxworkshop.sprite.Sprite;

public class FrameFilter {
	
	private Sprite.Frame frame;
	
	public Sprite.Frame getFrame(){
		return this.frame;
	}
	
	public void setFrame(Sprite.Frame frame){
		this.frame = frame;
	}
	
	public void changeShadowToDithered(int mode, int power, int lowLimit, int highLimit){
		int levels = 1 << power;
		int limitRange = highLimit - lowLimit;
		int bind = Palette.palettes[frame.getPalette()].mapping(0x000000); // Find Darkest
		
		if ((mode & 2) != 0) { // Expand image layer
			int ml = 0, mr = 0, mu = 0, md = 0;
			ml = Math.max(frame.getAnchorX(Sprite.DATA_IMAGE), frame.getAnchorX(Sprite.DATA_SHADOW));
			mr = Math.max(frame.getWidth(Sprite.DATA_IMAGE) - frame.getAnchorX(Sprite.DATA_IMAGE),
					frame.getWidth(Sprite.DATA_SHADOW) - frame.getAnchorX(Sprite.DATA_SHADOW));
			mu = Math.max(frame.getAnchorY(Sprite.DATA_IMAGE), frame.getAnchorY(Sprite.DATA_SHADOW));
			md = Math.max(frame.getHeight(Sprite.DATA_IMAGE) - frame.getAnchorY(Sprite.DATA_IMAGE),
					frame.getHeight(Sprite.DATA_SHADOW) - frame.getAnchorY(Sprite.DATA_SHADOW));
			
			System.out.println(frame.getSprite());
			frame.expand(Sprite.DATA_IMAGE, ml, mu, mr, md);
			int dx = ml - frame.getAnchorX(Sprite.DATA_IMAGE);
			int dy = mu - frame.getAnchorY(Sprite.DATA_IMAGE);
			
			frame.setAnchor(Sprite.DATA_IMAGE, 
					frame.getAnchorX(Sprite.DATA_IMAGE) + dx,
					frame.getAnchorY(Sprite.DATA_IMAGE) + dy);
			
		}
		
		int x0 = frame.getAnchorX(Sprite.DATA_SHADOW);
		int y0 = frame.getAnchorY(Sprite.DATA_SHADOW);
		
		switch (mode){
		case 1: // Normal shadow
			for (int i=0; i<frame.getHeight(Sprite.DATA_SHADOW); ++i){
				for (int j=0; j<frame.getWidth(Sprite.DATA_SHADOW); ++j){
					int value = frame.getPixel(Sprite.DATA_SHADOW, j, i);
					if (value != Sprite.PIXEL_NULL){
						value = (value - lowLimit) * levels / limitRange;
						if (value >= ditherPattern(j-x0, i-y0, power))
							frame.setPixel(Sprite.DATA_SHADOW, j, i, 128);
						else
							frame.setPixel(Sprite.DATA_SHADOW, j, i, Sprite.PIXEL_NULL);
					}
				}
			} break;
			
		case 2: // Black shadow
			for (int i=0; i<frame.getHeight(Sprite.DATA_SHADOW); ++i){
				for (int j=0; j<frame.getWidth(Sprite.DATA_SHADOW); ++j){
					int value = frame.getPixel(Sprite.DATA_SHADOW, j, i);
					if (value != Sprite.PIXEL_NULL){
						value = (value - lowLimit) * levels / limitRange;
						if (value >= ditherPattern(j-x0, i-y0, power)){
							frame.setPixelRelative(Sprite.DATA_IMAGE, j-x0, i-y0, bind);
						}
					}
				}
			}
			frame.remove(Sprite.DATA_SHADOW);
			break;
			
		case 3: // Black & Normal shadow
			int levels1 = levels << 1;
			for (int i=0; i<frame.getHeight(Sprite.DATA_SHADOW); ++i){
				for (int j=0; j<frame.getWidth(Sprite.DATA_SHADOW); ++j){
					int value = frame.getPixel(Sprite.DATA_SHADOW, j, i);
					if (value != Sprite.PIXEL_NULL){
						value = (value - lowLimit) * levels1 / limitRange;
						if (value >= levels){
							if (value - levels >= ditherPattern(j-x0, i-y0, power)){
								frame.setPixel(Sprite.DATA_IMAGE, j, i, bind);
								frame.setPixel(Sprite.DATA_SHADOW, j, i, Sprite.PIXEL_NULL);
							}else{
								frame.setPixel(Sprite.DATA_SHADOW, j, i, 128);
							}
						}else if (value >= ditherPattern(j-x0, i-y0, power)){
							frame.setPixel(Sprite.DATA_SHADOW, j, i, 128);
						}else{
							frame.setPixel(Sprite.DATA_SHADOW, j, i, Sprite.PIXEL_NULL);
						}
					}
				}
			} break;
			
		}
	
	}
	
	private int ditherPattern(int i, int j, int power){
		// Parameter levels is exponent of 2
		if (power >= 3)
			if (power % 2 == 1)
				return ditherPattern(i, j, power+1) >> 1;
			else
				return ditherPattern(i>>1, j>>1, 2) + (ditherPattern(i, j, power-2) << 2);
		else if (power == 1)
			return (i + j) & 0x1;
		else if (power == 2)
			if (j % 2 == 0)
				return (i & 0x1) * 3;
			else
				return 2 - (i & 0x1);
		else
			return 0;
	}
	
	/**
	 * Remove old outline data of the frame, then create new outline according to main image. 
	 * @param includeShadow If shadow is also being outlined.
	 * @param smart If only outermost is outlined.
	 */
	public void addOutline(boolean includeShadow, boolean smart){
		// Check if is bounded
		int width = frame.getWidth(Sprite.DATA_IMAGE);
		int height = frame.getHeight(Sprite.DATA_IMAGE);
		int ml = frame.getAnchorX(Sprite.DATA_IMAGE), mu = frame.getAnchorY(Sprite.DATA_IMAGE);
		int mr = width - ml, md = height - mu; 
		
		for (int i=0; i<width; ++i){
			if (frame.getPixel(Sprite.DATA_IMAGE, i, 0) != Sprite.PIXEL_NULL){
				++mu; break;
			}
		}
		for (int i=0; i<width; ++i){
			if (frame.getPixel(Sprite.DATA_IMAGE, i, height - 1) != Sprite.PIXEL_NULL){
				++md; break;
			}
		}
		for (int i=0; i<height; ++i){
			if (frame.getPixel(Sprite.DATA_IMAGE, 0, i) != Sprite.PIXEL_NULL){
				++ml; break;
			}
		}
		for (int i=0; i<height; ++i){
			if (frame.getPixel(Sprite.DATA_IMAGE, width - 1, i) != Sprite.PIXEL_NULL){
				++mr; break;
			}
		}
		
		if (includeShadow){
			int width1 = frame.getWidth(Sprite.DATA_SHADOW);
			int height1 = frame.getHeight(Sprite.DATA_SHADOW);
			int ml1 = frame.getAnchorX(Sprite.DATA_SHADOW), mu1 = frame.getAnchorY(Sprite.DATA_SHADOW);
			int mr1 = width1 - ml1, md1 = height1 - mu1; 
			
			for (int i=0; i<width1; ++i){
				if (frame.getPixel(Sprite.DATA_SHADOW, i, 0) != Sprite.PIXEL_NULL){
					++mu1; break;
				}
			}
			for (int i=0; i<width1; ++i){
				if (frame.getPixel(Sprite.DATA_SHADOW, i, height1 - 1) != Sprite.PIXEL_NULL){
					++md1; break;
				}
			}
			for (int i=0; i<height1; ++i){
				if (frame.getPixel(Sprite.DATA_SHADOW, 0, i) != Sprite.PIXEL_NULL){
					++ml1; break;
				}
			}
			for (int i=0; i<height1; ++i){
				if (frame.getPixel(Sprite.DATA_SHADOW, width1 - 1, i) != Sprite.PIXEL_NULL){
					++mr1; break;
				}
			}
			ml = Math.max(ml, ml1);
			mr = Math.max(mr, mr1);
			mu = Math.max(mu, mu1);
			md = Math.max(md, md1);
		}
		
		width = ml + mr; height = mu + md;
		byte[] flags = new byte[width * height];
		
		int dx = ml - frame.getAnchorX(Sprite.DATA_IMAGE);
		int dy = mu - frame.getAnchorY(Sprite.DATA_IMAGE);
		for (int y=0; y<frame.getHeight(Sprite.DATA_IMAGE); ++y){
			for (int x=0; x<frame.getWidth(Sprite.DATA_IMAGE); ++x){
				if (frame.getPixel(Sprite.DATA_IMAGE, x, y) != Sprite.PIXEL_NULL)
					flags[(y+dy)*width + (x+dx)] = 1;
			}
		}
		
		if (includeShadow){
			dx = ml - frame.getAnchorX(Sprite.DATA_SHADOW);
			dy = mu - frame.getAnchorY(Sprite.DATA_SHADOW);
			for (int y=0; y<frame.getHeight(Sprite.DATA_SHADOW); ++y){
				for (int x=0; x<frame.getWidth(Sprite.DATA_SHADOW); ++x){
					if (frame.getPixel(Sprite.DATA_SHADOW, x, y) != Sprite.PIXEL_NULL)
						flags[(y+dy)*width + (x+dx)] = 1;
				}
			}
		}
		
		if (smart){ // Find border
			for (int x=0; x<width; ++x){
				flags[x] = 2;
				flags[width * height - 1 - x] = 2;
			}
			
			boolean sets = true;
			while (sets){
				sets = false;
				for (int y=0; y<height; ++y){
					int yOffset = y * width;
					for (int x=0; x<width; ++x){
						if (flags[yOffset + x] == 0){
							boolean set = false;
							if (x > 0)
								if (flags[yOffset + x - 1] == 2)
									set = true;
							if (x < width - 1)
								if (flags[yOffset + x + 1] == 2)
									set = true;
							if (y > 0)
								if (flags[yOffset - width + x] == 2)
									set = true;
							if (y < width - 1)
								if (flags[yOffset + width + x] == 2)
									set = true;
							
							if (set) {
								flags[yOffset + x] = 2;
								sets = true;
							}
						}
					}
				}
			}
		}
		
		frame.remove(Sprite.DATA_OUTLINE);
		frame.expand(Sprite.DATA_OUTLINE, ml, mu, mr, md);
		for (int y=0; y<height; ++y){
			int yOffset = y * width;
			for (int x=0; x<width; ++x){
				if (flags[yOffset + x] == (smart ? 2 : 0)){
					boolean set = false;
					if (x > 0)
						if (flags[yOffset + x - 1] == 1)
							set = true;
					if (x < width - 1)
						if (flags[yOffset + x + 1] == 1)
							set = true;
					if (y > 0)
						if (flags[yOffset - width + x] == 1)
							set = true;
					if (y < height - 1)
						if (flags[yOffset + width + x] == 1)
							set = true;
					
					if (set)
						frame.setPixel(Sprite.DATA_OUTLINE, x, y, 1);
				}
			}
		}
		
	}

	/**
	 * Scale frame to certain size. Different from scale in Sprite.Frame class,
	 * interpolation is used there, so this requires arguments for normal and player color palette.
	 * @param factorX Horizontal scaling rate.
	 * @param factorY Vertical scaling rate.
	 * @param palette Determines what palette is used in interpolation. 
	 * @param playerPalette Determines what player palette is used in interpolation.
	 */
	public void scale(double factorX, double factorY, Palette palette, Palette playerPalette){
		Sprite.Frame frame0 = frame.getSprite().createFrame(frame);
		frame.scale(factorX, factorY);
		
		// Interpolate normal image
		if (frame.getWidth(Sprite.DATA_IMAGE) > 0
				&& frame.getHeight(Sprite.DATA_IMAGE) > 0){
			int anchorX0 = frame0.getAnchorX(Sprite.DATA_IMAGE);
			int anchorY0 = frame0.getAnchorY(Sprite.DATA_IMAGE);
			int anchorX = frame.getAnchorX(Sprite.DATA_IMAGE);
			int anchorY = frame.getAnchorY(Sprite.DATA_IMAGE);
			
			int width0 = frame0.getWidth(Sprite.DATA_IMAGE);
			int height0 = frame0.getHeight(Sprite.DATA_IMAGE);
			int width = frame.getWidth(Sprite.DATA_IMAGE);
			int height = frame.getHeight(Sprite.DATA_IMAGE);
			for (int y=0; y<height; ++y){
				double y0 = Math.max(0, Math.min(height0-1, (y - anchorY) / factorY + anchorY0));
				int y1 = (int) Math.floor(y0), y2 = (int) Math.ceil(y0);
				double dy1, dy2;
				if (y1 == y2){
					dy1 = .5; dy2 = .5;
				} else{
					dy1 = y0 - y1; dy2 = y2 - y0;
				}
				
				for (int x=0; x<width; ++x){
					double x0 = Math.max(0, Math.min(width0-1, (x - anchorX) / factorX + anchorX0));
					int x1 = (int) Math.floor(x0), x2 = (int) Math.ceil(x0);
					double dx1, dx2;
					if (x1 == x2){
						dx1 = .5; dx2 = .5;
					} else{
						dx1 = x0 - x1; dx2 = x2 - x0;
					}
					
					// Pixels
					final int[] ps = {
							frame0.getPixel(Sprite.DATA_IMAGE, x1, y1),
							frame0.getPixel(Sprite.DATA_IMAGE, x2, y1),
							frame0.getPixel(Sprite.DATA_IMAGE, x1, y2),
							frame0.getPixel(Sprite.DATA_IMAGE, x2, y2)
							};
					
					// Weights
					final double[] ws = {dy2 * dx2, dy2 * dx1, dy1 * dx2, dy1 * dx1}; 

					double normalWeight = 0, playerWeight = 0;
					for (int i=0; i<4; ++i){
						if (ps[i] >= Sprite.PIXEL_PLAYER_START)
							playerWeight += ws[i];
						else if (ps[i] != Sprite.PIXEL_NULL)
							normalWeight += ws[i];
					}

					// Priority: player color, normal color, null
					if (playerWeight < .5 && normalWeight < .5){ // Null
						frame.setPixel(Sprite.DATA_IMAGE, x, y, Sprite.PIXEL_NULL);
						continue;
					}
					
					// Total weight
					double wt = 0;
					boolean player = playerWeight >= normalWeight;
					if (player){
						double red = 0, green = 0, blue = 0;
						for (int i=0; i<4; ++i){
							if (ps[i] >= Sprite.PIXEL_PLAYER_START){
								int rgb = playerPalette.getColor(ps[i] - Sprite.PIXEL_PLAYER_START);
								red += (rgb >> 16 & 0xff) * ws[i];
								green += (rgb >> 8 & 0xff) * ws[i];
								blue += (rgb & 0xff) * ws[i];
								wt += ws[i];
							}
						}
						frame.setPixel(Sprite.DATA_IMAGE, x, y, 
								playerPalette.mapping(red / wt, green / wt, blue / wt)
								+ Sprite.PIXEL_PLAYER_START);
						
					} else {
						int red = 0, green = 0, blue = 0;
						for (int i=0; i<4; ++i){
							if (ps[i] < Sprite.PIXEL_PLAYER_START && ps[i] != Sprite.PIXEL_NULL){
								int rgb = palette.getColor(ps[i]);
								red += (rgb >> 16 & 0xff) * ws[i];
								green += (rgb >> 8 & 0xff) * ws[i];
								blue += (rgb & 0xff) * ws[i];
								wt += ws[i];
							}
						}
						frame.setPixel(Sprite.DATA_IMAGE, x, y, 
								palette.mapping(red / wt, green / wt, blue / wt));
					}
 
				}
			}
		}
		
		// Interpolate shadow image
		if (frame.getWidth(Sprite.DATA_SHADOW) > 0
				&& frame.getHeight(Sprite.DATA_SHADOW) > 0){
			int anchorX0 = frame0.getAnchorX(Sprite.DATA_SHADOW);
			int anchorY0 = frame0.getAnchorY(Sprite.DATA_SHADOW);
			int anchorX = frame.getAnchorX(Sprite.DATA_SHADOW);
			int anchorY = frame.getAnchorY(Sprite.DATA_SHADOW);
			
			int width0 = frame0.getWidth(Sprite.DATA_SHADOW);
			int height0 = frame0.getHeight(Sprite.DATA_SHADOW);
			int width = frame.getWidth(Sprite.DATA_SHADOW);
			int height = frame.getHeight(Sprite.DATA_SHADOW);
			for (int y=0; y<height; ++y){
				double y0 = Math.max(0, Math.min(height0-1, (y - anchorY) / factorY + anchorY0));
				int y1 = (int) Math.floor(y0), y2 = (int) Math.ceil(y0);
				double dy1, dy2;
				if (y1 == y2){
					dy1 = .5; dy2 = .5;
				} else{
					dy1 = y0 - y1; dy2 = y2 - y0;
				}
				
				for (int x=0; x<width; ++x){
					double x0 = Math.max(0, Math.min(width0-1, (x - anchorX) / factorX + anchorX0));
					int x1 = (int) Math.floor(x0), x2 = (int) Math.ceil(x0);
					double dx1, dx2;
					if (x1 == x2){
						dx1 = .5; dx2 = .5;
					} else{
						dx1 = x0 - x1; dx2 = x2 - x0;
					}
					
					// Pixels
					final int[] ps = {
							frame0.getPixel(Sprite.DATA_SHADOW, x1, y1),
							frame0.getPixel(Sprite.DATA_SHADOW, x2, y1),
							frame0.getPixel(Sprite.DATA_SHADOW, x1, y2),
							frame0.getPixel(Sprite.DATA_SHADOW, x2, y2)
							};
					
					// Weights
					final double[] ws = {dy2 * dx2, dy2 * dx1, dy1 * dx2, dy1 * dx1}; 

					double weight = 0;
					for (int i=0; i<4; ++i){
						if (ps[i] != Sprite.PIXEL_NULL)
							weight += ws[i];
					}

					// Priority: player color, normal color, null
					if (weight < .5){ // Null
						frame.setPixel(Sprite.DATA_SHADOW, x, y, Sprite.PIXEL_NULL);
						continue;
					}
					
					// Interpolation
					double value = 0, wt = 0;
					for (int i=0; i<4; ++i){
						if (ps[i] != Sprite.PIXEL_NULL){
							value += ps[i] * ws[i];
							wt += ws[i];
						}
					}					
					frame.setPixel(Sprite.DATA_SHADOW, x, y, (int) (value / wt));
 
				}
			}
		}
		
		// Interpolate smudge image
		if (frame.getWidth(Sprite.DATA_SMUDGE) > 0
				&& frame.getHeight(Sprite.DATA_SMUDGE) > 0){
			int anchorX0 = frame0.getAnchorX(Sprite.DATA_SMUDGE);
			int anchorY0 = frame0.getAnchorY(Sprite.DATA_SMUDGE);
			int anchorX = frame.getAnchorX(Sprite.DATA_SMUDGE);
			int anchorY = frame.getAnchorY(Sprite.DATA_SMUDGE);
			
			int width0 = frame0.getWidth(Sprite.DATA_SMUDGE);
			int height0 = frame0.getHeight(Sprite.DATA_SMUDGE);
			int width = frame.getWidth(Sprite.DATA_SMUDGE);
			int height = frame.getHeight(Sprite.DATA_SMUDGE);
			for (int y=0; y<height; ++y){
				double y0 = Math.max(0, Math.min(height0-1, (y - anchorY) / factorY + anchorY0));
				int y1 = (int) Math.floor(y0), y2 = (int) Math.ceil(y0);
				double dy1, dy2;
				if (y1 == y2){
					dy1 = .5; dy2 = .5;
				} else{
					dy1 = y0 - y1; dy2 = y2 - y0;
				}
				
				for (int x=0; x<width; ++x){
					double x0 = Math.max(0, Math.min(width0-1, (x - anchorX) / factorX + anchorX0));
					int x1 = (int) Math.floor(x0), x2 = (int) Math.ceil(x0);
					double dx1, dx2;
					if (x1 == x2){
						dx1 = .5; dx2 = .5;
					} else{
						dx1 = x0 - x1; dx2 = x2 - x0;
					}
					
					// Pixels
					final int[] ps = {
							frame0.getPixel(Sprite.DATA_SMUDGE, x1, y1),
							frame0.getPixel(Sprite.DATA_SMUDGE, x2, y1),
							frame0.getPixel(Sprite.DATA_SMUDGE, x1, y2),
							frame0.getPixel(Sprite.DATA_SMUDGE, x2, y2)
							};
					
					// Weights
					final double[] ws = {dy2 * dx2, dy2 * dx1, dy1 * dx2, dy1 * dx1}; 

					double weight = 0;
					for (int i=0; i<4; ++i){
						if (ps[i] != Sprite.PIXEL_NULL)
							weight += ws[i];
					}

					// Priority: player color, normal color, null
					if (weight < .5){ // Null
						frame.setPixel(Sprite.DATA_SMUDGE, x, y, Sprite.PIXEL_NULL);
						continue;
					}
					
					// Interpolation
					double value = 0, wt = 0;
					for (int i=0; i<4; ++i){
						if (ps[i] != Sprite.PIXEL_NULL){
							value += ps[i] * ws[i];
							wt += ws[i];
						}
					}					
					frame.setPixel(Sprite.DATA_SMUDGE, x, y, (int) (value / wt));
 
				}
			}
		}
		
	}

}
