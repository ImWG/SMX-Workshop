package com.imwg.smxworkshop.sprite;

public class SLPSprite extends Sprite {
	
	static private final int PLAYER_COLOR_START = 16384;
	static private final int PIXEL_SHADOW = -8;
	static private final int PIXEL_OUTLINE = -16;
	static private final int SHADOW_DEPTH = 128;
	
	// Custom memo is not Supported by Turtle Pack, this is the default one
	public final static String slpmemo = "ArtDesk 1.00 SLP Writer";
	
	public SLPSprite(){
		super();
	}
	public SLPSprite(Sprite sprite){
		super(sprite);
	}
	

	@Override
	public int getVersion(){
		return Sprite.VERSION_SLP;
	}
	
	public class Frame extends Sprite.Frame{
		
		private short[] image;
		private int[] props = new int[4]; // {Width, Height, AnchorX, AnchorY}
		
		public Frame(){
			super();
		}
		
		public Frame(Sprite.Frame frame){
			super();
			if (frame.getVersion() == Sprite.VERSION_SLP){
				SLPSprite.Frame frame1 = (SLPSprite.Frame) frame;
				props = frame1.props.clone();
				image = frame1.image.clone();
				
			}else{
				int width = frame.getWidth();
				int height = frame.getHeight();
				int ml = frame.getAnchorX();
				int mu = frame.getAnchorY();
				int dx, dy;
				
				this.create(width, height);
				this.setAnchor(DATA_IMAGE, ml, mu);
				dx = ml - frame.getAnchorX(DATA_OUTLINE);
				dy = mu - frame.getAnchorY(DATA_OUTLINE);
				for (int i=0; i<frame.getHeight(DATA_OUTLINE); ++i){
					for (int j=0; j<frame.getWidth(DATA_OUTLINE); ++j){
						int value = frame.getPixel(DATA_OUTLINE, j, i);
						if (value != PIXEL_NULL)
							this.setPixel(DATA_OUTLINE, j + dx, i + dy, frame.getPixel(DATA_OUTLINE, j, i));
					}
				}
				
				dx = ml - frame.getAnchorX(DATA_SHADOW);
				dy = mu - frame.getAnchorY(DATA_SHADOW);
				for (int i=0; i<frame.getHeight(DATA_SHADOW); ++i){
					for (int j=0; j<frame.getWidth(DATA_SHADOW); ++j){
						int value = frame.getPixel(DATA_SHADOW, j, i);
						if (value != PIXEL_NULL){
							this.setPixel(DATA_SHADOW, j + dx, i + dy, SHADOW_DEPTH);
						}
					}
				}
				
				dx = ml - frame.getAnchorX(DATA_IMAGE);
				dy = mu - frame.getAnchorY(DATA_IMAGE);
				for (int i=0; i<frame.getHeight(DATA_IMAGE); ++i){
					for (int j=0; j<frame.getWidth(DATA_IMAGE); ++j){
						int value = frame.getPixel(DATA_IMAGE, j, i);
						if (value != PIXEL_NULL){
							this.setPixel(DATA_IMAGE, j + dx, i + dy, value);
						}
					}
				}
			}
			setPalette(frame.getPalette());
		}
		
		@Override
		public int getVersion(){
			return Sprite.VERSION_SLP;
		}
		
		@Override
		public int getWidth(int type) {
			if (type == Sprite.DATA_SMUDGE)
				return 0;
			return props[0];
		}
		
		@Override
		public int getHeight(int type) {
			if (type == Sprite.DATA_SMUDGE)
				return 0;
			return props[1];
		}
		
		@Override
		public int getAnchorX(int type) {
			if (type == Sprite.DATA_SMUDGE)
				return 0;
			return props[2];
		}
		
		@Override
		public int getAnchorY(int type) {
			if (type == Sprite.DATA_SMUDGE)
				return 0;
			return props[3];
		}

		@Override
		public int getPixel(int type, int x, int y) {
			int value = image[props[0]*y+x]; 
			if (value == PIXEL_NULL)
				return PIXEL_NULL;
			
			switch(type){
			case DATA_IMAGE:
				if (value < 0)
					return PIXEL_NULL;
				else if (value < PLAYER_COLOR_START)
					return value;
				else
					return value - PLAYER_COLOR_START + Sprite.PIXEL_PLAYER_START;
			case DATA_SHADOW:
				if (value == PIXEL_SHADOW)
					return SHADOW_DEPTH;
				break;
			case DATA_OUTLINE:
				if (value == PIXEL_OUTLINE)
					return 0;
				break;
			}
			return PIXEL_NULL;
		}

		@Override @Deprecated
		public void create(int type, int width, int height) {
			int size = width*height;
			image = new short[size];
			for (int i=0; i<size; ++i)
				image[i] = PIXEL_NULL;

			props[0] = width;
			props[1] = height;
			props[2] = 0;
			props[3] = 0;
		}
		
		@Override
		public void create(int width, int height) {
			create(Sprite.DATA_IMAGE, width, height);
		}
		
		@Override
		public void expand(int type, int left, int top, int right, int bottom) {
			// TODO Auto-generated method stub
			if (props[0] == 0){
				int width = left + right, height = top + bottom;
				int size = width * height;
				props[0] = width; props[1] = height;
				props[2] = left; props[3] = top;
				image = new short[size];
				for (int i=0; i<size; ++i)
					image[i] = PIXEL_NULL;
				
			}else{
				left = Math.max(left, props[2]);
				top = Math.max(top, props[3]);
				right = Math.max(props[0] - props[2], right);
				bottom = Math.max(props[1] - props[3], bottom);
				
				int width = left + right, height = top + bottom;
				int size = width * height;
				short[] image = new short[size];
				for (int i=0; i<size; ++i)
					image[i] = PIXEL_NULL;
				
				int width0 = props[0], height0 = props[1];
				for (int i=0; i<height0; ++i){
					int offset0 = i * width0, offset = (i+top-props[3]) * width;
					for (int j=0; j<width0; ++j)
						image[offset + j+left-props[2]] = this.image[offset0 + j];
				}
				props[0] = width; props[1] = height;
				props[2] = left; props[3] = top;
				this.image = image;
				
			}
		}
		
		@Override
		public void remove(int type) {
			switch(type){
			case DATA_IMAGE:
				for (int i=0; i<image.length; ++i)
					if (image[i] >= 0)
						image[i] = PIXEL_NULL;
				break; 
			case DATA_SHADOW:
				for (int i=0; i<image.length; ++i)
					if (image[i] == PIXEL_SHADOW)
						image[i] = PIXEL_NULL;
				break;  
			case DATA_OUTLINE:
				for (int i=0; i<image.length; ++i)
					if (image[i] == PIXEL_OUTLINE)
						image[i] = PIXEL_NULL;
				break; 
			}
		}
		
		@Override
		public void setAnchor(int type, int x, int y) {
			if (type == Sprite.DATA_IMAGE){
				props[2] = x;
				props[3] = y;
			}
		}
		
		@Override
		public void setPixel(int type, int x, int y, int value) {
			switch(type){
			case DATA_IMAGE:
				if (value < Sprite.PIXEL_PLAYER_START)
					image[props[0]*y+x] = (short) value;
				else
					image[props[0]*y+x] = (short) (value - Sprite.PIXEL_PLAYER_START + PLAYER_COLOR_START);
				break;
			case DATA_SHADOW:
				if (value > 0)
					image[props[0]*y+x] = PIXEL_SHADOW;
				else if (image[props[0]*y+x] == PIXEL_SHADOW)
					image[props[0]*y+x] = PIXEL_NULL;
				break;
			case DATA_OUTLINE:
				image[props[0]*y+x] = PIXEL_OUTLINE;
				break;
			}
		}

		@Override
		public void crop(int type) {
			int width = this.props[0];
			int height = this.props[1];
			int left = 0, top = -1, right = width - 1, bottom = height - 1;
			
			for (int y=0; y<height; ++y){
				boolean blank = true;
				for (int x=0; x<width; ++x){
					if (image[width*y+x] != Sprite.PIXEL_NULL){
						blank = false; break;
					}
				}
				if (!blank){
					top = y; break;
				}
			}
			
			if (top == -1){ // EMPTY
				left = this.getAnchorX(type);
				top = this.getAnchorY(type);
				right = left - 1;
				bottom = top - 1;
				
			}else{
				for (int y=height-1; y>=top; --y){
					boolean blank = true;
					for (int x=0; x<width; ++x){
						if (image[width*y+x] != Sprite.PIXEL_NULL){
							blank = false; break;
						}
					}
					if (!blank){
						bottom = y; break;
					}
				}
				for (int x=0; x<width; ++x){
					boolean blank = true;
					for (int y=top; y<=bottom; ++y){
						if (image[width*y+x] != Sprite.PIXEL_NULL){
							blank = false; break;
						}
					}
					if (!blank){
						left = x; break;
					}
				}
				for (int x=width-1; x>=left; --x){
					boolean blank = true;
					for (int y=top; y<=bottom; ++y){
						if (image[width*y+x] != Sprite.PIXEL_NULL){
							blank = false; break;
						}
					}
					if (!blank){
						right = x; break;
					}
				}
			}	
			
			int width1 = right - left + 1;
			int height1 = bottom - top + 1;
			
			if (width1 == width && height1 == height) // No change
				return;
			
			short[] image = new short[width1 * height1];
			for (int y=0; y<height1; ++y){
				for (int x=0; x<width1; ++x){
					image[x + y*width1] = this.image[x+left + (y+top)*width];
				}
			}
			this.image = image;
			this.props[0] = width1;
			this.props[1] = height1;
			this.props[2] -= left;
			this.props[3] -= top;
		}

		@Override
		public void flip(int orientation) {
			if (orientation == Sprite.FLIP_HORIZONTAL){
				int width = props[0], limit = width / 2;
				for (int y=0; y<props[1]; ++y) {
					int offset = width * y;
					for (int x=0; x<limit; ++x) {
						short t = image[offset+width-x-1];
						image[offset+width-x-1] = image[offset+x];
						image[offset+x] = t;
					}
				}
				props[2] = width - props[2];
			}else if (orientation == Sprite.FLIP_VERTICAL){
				int width = props[0], limit = props[1] / 2;
				for (int y=0; y<limit; ++y) {
					int offset1 = width * y, offset2 = (props[1] - y - 1) * width;
					for (int x=0; x<width; ++x) {
						short t = image[offset1+x];
						image[offset1+x] = image[offset2+x];
						image[offset2+x] = t;
					}
				}
				props[3] = props[1] - props[3];
			}
			
		}

		@Override
		public void rotate(int angle) {
			if (angle == Sprite.ROTATE_180){
				int limit = image.length / 2;
				for (int i=0; i<limit; ++i) {
					short t = image[image.length-i-1];
					image[image.length-i-1] = image[i];
					image[i] = t;
				}
				props[2] = props[0] - props[2];
				props[3] = props[1] - props[3];
			}else if (angle == Sprite.ROTATE_TRANSPOSE){
				int width = props[0], height = props[1];
				short[] image1 = new short[image.length];
				for (int y=0; y<height; ++y) {
					int offset = width*y;
					for (int x=0; x<width; ++x) {
						image1[y + height*x] = image[x + offset];
					}
				}
				image = image1;
				props[0] = height;
				props[1] = width;
				width = props[2];
				props[2] = props[3];
				props[3] = width;
			}else if (angle == Sprite.ROTATE_CLOCKWISE_90){
				rotate(Sprite.ROTATE_TRANSPOSE);
				flip(Sprite.FLIP_HORIZONTAL);
			}else if (angle == Sprite.ROTATE_COUNTER_CLOCKWISE_90){
				rotate(Sprite.ROTATE_TRANSPOSE);
				flip(Sprite.FLIP_VERTICAL);
			}
			
		}

		@Override
		public void scale(double factorX, double factorY) {
			int ml, mr, mu, md, x0, y0, width0, height0;
			x0 = props[2]; y0 = props[3];
			width0 = props[0]; height0 = props[1];
			ml = (int)(x0 * factorX);
			mr = (int)((width0 - x0) * factorX);
			mu = (int)(y0 * factorY);
			md = (int)((height0 - y0) * factorY);
			int width1 = ml + mr, height1 = mu + md;
			
			short[] image1 = new short[width1 * height1];
			for (int y=0; y<height1; ++y){
				int offset = y * width1;
				int offset0 = (int)((y-mu) / factorY + y0) * width0;
				for (int x=0; x<width1; ++x){
					image1[offset + x] = image[offset0 + (int)((x-ml) / factorX + x0)]; 
				}
			}
			this.image = image1;
			this.props[0] = width1;
			this.props[1] = height1;
			this.props[2] = ml;
			this.props[3] = mu;
			
		}

	}

	@Override
	public Frame createFrame() {
		Frame frame = new Frame();
		frame.sprite = this;
		return frame;
	}

	@Override
	public Frame createFrame(Sprite.Frame frame) {
		Frame frame1 = new Frame(frame);
		frame1.sprite = this;
		return frame1;
	}
	
	
}
