package com.imwg.smxworkshop.sprite;


public class SMXSprite extends Sprite{
	
	static private final int PLAYER_COLOR_START = 16384;
	
	public SMXSprite(){
		super();
	}
	public SMXSprite(Sprite sprite){
		super(sprite);
	}
	
	@Override
	public int getVersion(){
		return Sprite.VERSION_SMX;
	}
	
	public class Frame extends Sprite.Frame{
		
		private short[] image;
		private byte[] shadow;
		private byte[] outline;
		private short[] smudge;
		private int[] props = new int[16]; // {Width, Height, AnchorX, AnchorY} x 3
		
		private Frame(){
			super();
		}
		
		private Frame(Sprite.Frame frame){
			this();
			cloneFrame(frame);
			setPalette(frame.getPalette());
		}
		
		@Override
		public int getVersion(){
			return Sprite.VERSION_SMX;
		}
		
		@Override
		public int getWidth(int type) {
			return props[type*4];
		}
		
		@Override
		public int getHeight(int type) {
			return props[type*4+1];
		}
		
		@Override
		public int getAnchorX(int type) {
			return props[type*4+2];
		}
		
		@Override
		public int getAnchorY(int type) {
			return props[type*4+3];
		}

		@Override
		public int getPixel(int type, int x, int y) {
			switch(type){
			case DATA_IMAGE:
				int value = image[props[0]*y+x];
				if (value < PLAYER_COLOR_START)
					return value;
				else
					return value - PLAYER_COLOR_START + Sprite.PIXEL_PLAYER_START;
			case DATA_SHADOW:
				byte value1 = shadow[props[4]*y+x]; 
				if (value1 == 0){
					return PIXEL_NULL;
				}else
					return value1 & 0xff;
			case DATA_OUTLINE:
				return (int)outline[props[8]*y+x];
			case DATA_SMUDGE:
				return smudge[props[12]*y+x];
			}
			return PIXEL_NULL;
		}

		@Override @Deprecated
		public void create(int type, int width, int height) {
			int size = width*height;
			switch(type){
			case DATA_IMAGE:
				image = new short[size];
				for (int i=0; i<size; ++i)
					image[i] = PIXEL_NULL;
				break; 
			case DATA_SHADOW:
				shadow = new byte[size];
				for (int i=0; i<size; ++i)
					shadow[i] = 0;
				break;  
			case DATA_OUTLINE:
				outline = new byte[size];
				for (int i=0; i<size; ++i)
					outline[i] = PIXEL_NULL;
				break; 
			case DATA_SMUDGE:
				smudge = new short[size];
				for (int i=0; i<size; ++i)
					smudge[i] = PIXEL_NULL;
				break; 
			}
			props[type*4] = width;
			props[type*4+1] = height;
		}
		
		@Override
		public void create(int width, int height) {
			int size = width*height;
			
			image = new short[size];
			for (int i=0; i<size; ++i)
				image[i] = PIXEL_NULL;
			
			shadow = new byte[size];
			for (int i=0; i<size; ++i)
				shadow[i] = 0;

			outline = new byte[size];
			for (int i=0; i<size; ++i)
				outline[i] = PIXEL_NULL;
			
			smudge = new short[size];
			for (int i=0; i<size; ++i)
				smudge[i] = PIXEL_NULL;
			
			for (int type : Sprite.DATA_TYPES){
				props[type*4] = width;
				props[type*4+1] = height;
				props[type*4+2] = 0;
				props[type*4+3] = 0;
			}
		}
		
		@Override
		public void expand(int type, int left, int top, int right, int bottom) {
			if (props[type*4] == 0){
				int width = left + right, height = top + bottom;
				int size = width*height;
				switch(type){
				case DATA_IMAGE:
					image = new short[size];
					for (int i=0; i<size; ++i)
						image[i] = PIXEL_NULL;
					break; 
				case DATA_SHADOW:
					shadow = new byte[size];
					for (int i=0; i<size; ++i)
						shadow[i] = 0;
					break;  
				case DATA_OUTLINE:
					outline = new byte[size];
					for (int i=0; i<size; ++i)
						outline[i] = PIXEL_NULL;
					break; 
				case DATA_SMUDGE:
					smudge = new short[size];
					for (int i=0; i<size; ++i)
						smudge[i] = PIXEL_NULL;
					break; 
				}
				props[type*4] = width;
				props[type*4+1] = height;
				props[type*4+2] = left;
				props[type*4+3] = top;
				
			}else{
				left = Math.max(left, props[type*4+2]);
				top = Math.max(top, props[type*4+3]);
				right = Math.max(props[type*4] - props[type*4+2], right);
				bottom = Math.max(props[type*4+1] - props[type*4+3], bottom);
				
				int width = left + right, height = top + bottom;
				int size = width * height;
				if (type == DATA_IMAGE){
					short[] image = new short[size];
					for (int i=0; i<size; ++i)
						image[i] = PIXEL_NULL;
					int width0 = props[0], height0 = props[1];
					for (int i=0; i<height0; ++i){
						int offset0 = i * width0, offset = (i+top-props[3]) * width;
						for (int j=0; j<width0; ++j)
							image[offset + j+left-props[2]] = this.image[offset0 + j];
					}
					this.image = image;

				}else if (type == DATA_SHADOW){
					byte[] shadow = new byte[size];
					for (int i=0; i<size; ++i)
						shadow[i] = 0;
					int width0 = props[4], height0 = props[5];
					for (int i=0; i<height0; ++i){
						int offset0 = i * width0, offset = (i+top-props[7]) * width;
						for (int j=0; j<width0; ++j)
							shadow[offset + j+left-props[6]] = this.shadow[offset0 + j];
					}
					this.shadow = shadow;

				}else if (type == DATA_OUTLINE){
					byte[] outline = new byte[size];
					for (int i=0; i<size; ++i)
						outline[i] = PIXEL_NULL;
					int width0 = props[8], height0 = props[9];
					for (int i=0; i<height0; ++i){
						int offset0 = i * width0, offset = (i+top-props[11]) * width;
						for (int j=0; j<width0; ++j)
							outline[offset + j+left-props[10]] = this.outline[offset0 + j];
					}
					this.outline = outline;

				}else if (type == DATA_SMUDGE){
					short[] smudge = new short[size];
					for (int i=0; i<size; ++i)
						smudge[i] = PIXEL_NULL;
					int width0 = props[12], height0 = props[13];
					for (int i=0; i<height0; ++i){
						int offset0 = i * width0, offset = (i+top-props[15]) * width;
						for (int j=0; j<width0; ++j)
							smudge[offset + j+left-props[14]] = this.smudge[offset0 + j];
					}
					this.smudge = smudge;

				}
				
				props[type*4] = width; props[type*4+1] = height;
				props[type*4+2] = left; props[type*4+3] = top;
				
			}
		}
		
		@Override
		public void remove(int type) {
			switch(type){
			case DATA_IMAGE:
				image = new short[0];
				break; 
			case DATA_SHADOW:
				shadow = new byte[0];
				break;  
			case DATA_OUTLINE:
				outline = new byte[0];
				break; 
			case DATA_SMUDGE:
				smudge = new short[0];
				break; 
			}
			props[type*4] = 0;
			props[type*4+1] = 0;
			props[type*4+2] = 0;
			props[type*4+3] = 0;
		}
		
		@Override
		public void setAnchor(int type, int x, int y) {
			props[type*4+2] = x;
			props[type*4+3] = y;
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
				if (value == Sprite.PIXEL_NULL)
					shadow[props[4]*y+x] = 0;
				else
					shadow[props[4]*y+x] = (byte) value;
				break;
			case DATA_OUTLINE:
				outline[props[8]*y+x] = (byte) value; break;
			case DATA_SMUDGE:
				smudge[props[12]*y+x] = (short) value; break;
			}
		}
		
		
		public void crop(int type){
			int width = this.getWidth(type);
			int height = this.getHeight(type);
			int left = 0, top = -1, right = width - 1, bottom = height - 1;
			for (int y=0; y<height; ++y){
				boolean blank = true;
				for (int x=0; x<width; ++x){
					if (this.getPixel(type, x, y) != Sprite.PIXEL_NULL){
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
				width = 0;
				height = 0;
				
			}else{
				for (int y=height-1; y>=top; --y){
					boolean blank = true;
					for (int x=0; x<width; ++x){
						if (this.getPixel(type, x, y) != Sprite.PIXEL_NULL){
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
						if (this.getPixel(type, x, y) != Sprite.PIXEL_NULL){
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
						if (this.getPixel(type, x, y) != Sprite.PIXEL_NULL){
							blank = false; break;
						}
					}
					if (!blank){
						right = x; break;
					}
				}
				width = right - left + 1;
				height = bottom - top + 1;
			}
			
			int[] pixels = new int[width * height];
			for (int y=0; y<height; ++y){
				for (int x=0; x<width; ++x){
					pixels[x + y*width] = this.getPixel(type, x+left, y+top);
				}
			}
			this.create(type, width, height);
			for (int y=0; y<height; ++y){
				for (int x=0; x<width; ++x){
					this.setPixel(type, x, y, pixels[x + y*width]);
				}
			}
			this.setAnchor(type, this.getAnchorX(type) - left, this.getAnchorY(type) - top);
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
				
				width = props[4]; limit = width / 2;
				for (int y=0; y<props[5]; ++y) {
					int offset = width * y;
					for (int x=0; x<limit; ++x) {
						byte t = shadow[offset+width-x-1];
						shadow[offset+width-x-1] = shadow[offset+x];
						shadow[offset+x] = t;
					}
				}
				props[6] = width - props[6];
				
				width = props[8]; limit = width / 2;
				for (int y=0; y<props[9]; ++y) {
					int offset = width * y;
					for (int x=0; x<limit; ++x) {
						byte t = outline[offset+width-x-1];
						outline[offset+width-x-1] = outline[offset+x];
						outline[offset+x] = t;
					}
				}
				props[10] = width - props[10];
				
				width = props[12]; limit = width / 2;
				for (int y=0; y<props[13]; ++y) {
					int offset = width * y;
					for (int x=0; x<limit; ++x) {
						short t = smudge[offset+width-x-1];
						smudge[offset+width-x-1] = smudge[offset+x];
						smudge[offset+x] = t;
					}
				}
				props[14] = width - props[14];
				
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
				
				width = props[4]; limit = props[5] / 2;
				for (int y=0; y<limit; ++y) {
					int offset1 = width * y, offset2 = (props[5] - y - 1) * width;
					for (int x=0; x<width; ++x) {
						byte t = shadow[offset1+x];
						shadow[offset1+x] = shadow[offset2+x];
						shadow[offset2+x] = t;
					}
				}
				props[7] = props[5] - props[7];
				
				width = props[8]; limit = props[9] / 2;
				for (int y=0; y<limit; ++y) {
					int offset1 = width * y, offset2 = (props[9] - y - 1) * width;
					for (int x=0; x<width; ++x) {
						byte t = outline[offset1+x];
						outline[offset1+x] = outline[offset2+x];
						outline[offset2+x] = t;
					}
				}
				props[11] = props[9] - props[11];
				
				width = props[12]; limit = props[13] / 2;
				for (int y=0; y<limit; ++y) {
					int offset1 = width * y, offset2 = (props[13] - y - 1) * width;
					for (int x=0; x<width; ++x) {
						short t = smudge[offset1+x];
						smudge[offset1+x] = smudge[offset2+x];
						smudge[offset2+x] = t;
					}
				}
				props[15] = props[13] - props[15];
			}
			
		}

		@Override
		public void rotate(int angle) {
			if (angle == Sprite.ROTATE_180){
				if (image != null){ // image.length is Required
					int limit = image.length / 2;
					for (int i=0; i<limit; ++i) {
						short t = image[image.length-i-1];
						image[image.length-i-1] = image[i];
						image[i] = t;
					}
					props[2] = props[0] - props[2];
					props[3] = props[1] - props[3];
				}
				
				if (shadow != null){
					int limit = shadow.length / 2;
					for (int i=0; i<limit; ++i) {
						byte t = shadow[shadow.length-i-1];
						shadow[shadow.length-i-1] = shadow[i];
						shadow[i] = t;
					}
					props[6] = props[4] - props[6];
					props[7] = props[5] - props[7];
				}
				
				if (outline != null){
					int limit = outline.length / 2;
					for (int i=0; i<limit; ++i) {
						byte t = outline[outline.length-i-1];
						outline[outline.length-i-1] = outline[i];
						outline[i] = t;
					}
					props[10] = props[8] - props[10];
					props[11] = props[9] - props[11];
				}
				
				if (smudge != null){
					int limit = smudge.length / 2;
					for (int i=0; i<limit; ++i) {
						short t = smudge[smudge.length-i-1];
						smudge[smudge.length-i-1] = smudge[i];
						smudge[i] = t;
					}
					props[14] = props[12] - props[14];
					props[15] = props[13] - props[15];
				}
				
			}else if (angle == Sprite.ROTATE_TRANSPOSE){
				if (image != null){
					int width = props[0], height = props[1];
					short[] image1 = new short[image.length];
					for (int y=0; y<height; ++y) {
						int offset = width*y;
						for (int x=0; x<width; ++x) {
							image1[y + height*x] = image[x + offset];
						}
					}
					image = image1;
					props[0] = height; props[1] = width;
					width = props[2]; props[2] = props[3]; props[3] = width;
				}
				
				if (shadow != null){
					int width = props[4], height = props[5];
					byte[] shadow1 = new byte[shadow.length];
					for (int y=0; y<height; ++y) {
						int offset = width*y;
						for (int x=0; x<width; ++x) {
							shadow1[y + height*x] = shadow[x + offset];
						}
					}
					shadow = shadow1;
					props[4] = height; props[5] = width;
					width = props[6]; props[6] = props[7]; props[7] = width;
				}
				
				if (outline != null){
					int width = props[8], height = props[9];
					byte[] outline1 = new byte[outline.length];
					for (int y=0; y<height; ++y) {
						int offset = width*y;
						for (int x=0; x<width; ++x) {
							outline1[y + height*x] = outline[x + offset];
						}
					}
					outline = outline1;
					props[8] = height; props[9] = width;
					width = props[10]; props[10] = props[11]; props[11] = width;
				}
				
				if (smudge != null){
					int width = props[12], height = props[13];
					short[] smudge1 = new short[smudge.length];
					for (int y=0; y<height; ++y) {
						int offset = width*y;
						for (int x=0; x<width; ++x) {
							smudge1[y + height*x] = smudge[x + offset];
						}
					}
					smudge = smudge1;
					props[12] = height; props[13] = width;
					width = props[14]; props[14] = props[15]; props[15] = width;
				}
				
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
			
			for (int type : Sprite.DATA_TYPES){
				x0 = props[type*4+2]; y0 = props[type*4+3]; // Image
				width0 = props[type*4]; height0 = props[type*4+1];
				ml = (int)(x0 * factorX);
				mr = (int)((width0 - x0) * factorX);
				mu = (int)(y0 * factorY);
				md = (int)((height0 - y0) * factorY);
				int width1 = ml + mr, height1 = mu + md;
				
				switch(type){
				case DATA_IMAGE:
					short[] image1 = new short[width1 * height1];
					for (int y=0; y<height1; ++y){
						int offset = y * width1;
						int offset0 = (int)((y-mu) / factorY + y0) * width0;
						for (int x=0; x<width1; ++x){
							image1[offset + x] = image[offset0 + (int)((x-ml) / factorX + x0)]; 
						}
					}
					this.image = image1;
					break;
				case DATA_SHADOW:
					byte[] shadow1 = new byte[width1 * height1];
					for (int y=0; y<height1; ++y){
						int offset = y * width1;
						int offset0 = (int)((y-mu) / factorY + y0) * width0;
						for (int x=0; x<width1; ++x){
							shadow1[offset + x] = shadow[offset0 + (int)((x-ml) / factorX + x0)]; 
						}
					}
					this.shadow = shadow1;
					break;
				case DATA_OUTLINE:
					byte[] outline1 = new byte[width1 * height1];
					for (int y=0; y<height1; ++y){
						int offset = y * width1;
						int offset0 = (int)((y-mu) / factorY + y0) * width0;
						for (int x=0; x<width1; ++x){
							outline1[offset + x] = outline[offset0 + (int)((x-ml) / factorX + x0)]; 
						}
					}
					this.outline = outline1;
					break;
				case DATA_SMUDGE:
					short[] smudge1 = new short[width1 * height1];
					for (int y=0; y<height1; ++y){
						int offset = y * width1;
						int offset0 = (int)((y-mu) / factorY + y0) * width0;
						for (int x=0; x<width1; ++x){
							smudge1[offset + x] = smudge[offset0 + (int)((x-ml) / factorX + x0)]; 
						}
					}
					this.smudge = smudge1;
					break;
				}
				this.props[type*4] = width1;
				this.props[type*4+1] = height1;
				this.props[type*4+2] = ml;
				this.props[type*4+3] = mu;
			}
			
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
