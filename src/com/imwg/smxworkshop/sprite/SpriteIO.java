package com.imwg.smxworkshop.sprite;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.imageio.ImageIO;

import com.imwg.smxworkshop.model.Configuration;
import com.imwg.smxworkshop.view.MainFrame;

final public class SpriteIO {
	
	public static final int IMAGE_MODE_AUTO = 0;
	public static final int IMAGE_MODE_BYALPHA = 1;
	public static final int IMAGE_MODE_SEPARATESHADOW = 2;
	public static final int IMAGE_MODE_SEPARATEPLAYER = 3;
	public static final int IMAGE_MODE_SEPARATEPLAYERMASK = 4;
	public static final int IMAGE_MODE_SHADOWONLY = 8;
	public static final int IMAGE_MODE_MASK = 0x0f;
	public static final int IMAGE_MODE_OUTLINE_MASK = 0x10;
	public static final int IMAGE_MODE_SMUDGE_MASK = 0x20;
	public static final int IMAGE_MODE_BACKGROUND_MASK = 0x100;
	
	public static final int ANCHOR_MODE_TIGHT = 0;
	public static final int ANCHOR_MODE_ALIGN = 1;
	public static final int ANCHOR_MODE_CENTER = 2;
	public static final int ANCHOR_MODE_MASK = 0x3;
	public static final int ANCHOR_MODE_CSV_MASK = 0x4;
	
	
	private SpriteIO(){
	}
	
	static public Sprite loadFromFile(File file){
		Sprite sprite = null;
		try {
			FileInputStream fis = new FileInputStream(file);
			byte[] headerBytes = new byte[4];
			fis.read(headerBytes, 0, 4);
			String header = new String(headerBytes);
			
			try{
				if (header.equals("SMPX")){
					sprite = loadFromSMX(fis);
				}else if (header.equals("2.0N")){
					sprite = loadFromSLP(fis);
				}else if (header.equals("SMP$")){
					sprite = loadFromSMP(fis);
				}else if (header.equals("4.0X")){
					sprite = loadFromDESLP(fis);
				}
			}catch(NullPointerException | ArrayIndexOutOfBoundsException | NegativeArraySizeException e){
				e.printStackTrace();
				sprite = new SMXSprite();
			}finally{
				fis.close();
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();	
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return sprite;
	}
	
	static private SMXSprite loadFromSMX(InputStream fis)
			throws IOException, NullPointerException, ArrayIndexOutOfBoundsException, NegativeArraySizeException{
		SMXSprite sprite = new SMXSprite();
		
		// Header
		readInteger(fis, 2); // Unknown Prop
		final int frameCount = readInteger(fis, 2);
		@SuppressWarnings("unused")
		int size = readInteger(fis, 4), unknown = readInteger(fis, 4);
		byte[] memoBytes = new byte[16];
		fis.read(memoBytes);
		sprite.memo = new String(memoBytes);
		sprite.playerMode = Sprite.PLAYER_PALETTE_DE;
		
		for (int index=0; index<frameCount; ++index){
			byte flags = (byte)readInteger(fis, 1, false);

			int palette = readInteger(fis, 1);
			readInteger(fis, 4); // SMP size
			
			SMXSprite.Frame frame = sprite.createFrame();
			sprite.frames.add(index, frame);
			frame.palette = palette;
			
			for (int type=0; type<3; ++type){
				if ((flags & 1 << type) == 0){
					continue;
				}
				
				int width, height, anchorx, anchory, size1;
				width = readInteger(fis, 2);
				height = readInteger(fis, 2);
				anchorx = readInteger(fis, 2);
				anchory = readInteger(fis, 2);
				size1 = readInteger(fis, 4);
				fis.skip(4); // Unknown Prop
				
				int[] borders = new int[height * 2 + 2]; // For out of bounds protection
				for (int i=0; i<height; ++i){
					borders[i*2] = readInteger(fis, 2);
					borders[i*2+1] = readInteger(fis, 2);
				}
				
				frame.expand(type, 0, 0, width, height);
				frame.setAnchor(type, anchorx, anchory);
						
				if (type == Sprite.DATA_IMAGE){
					int size2;
					size1 = readInteger(fis, 4);
					size2 = readInteger(fis, 4);
					byte[] map = new byte[size1], data = new byte[size2];
					fis.read(map); fis.read(data);
					
					if (size1 > 0){
						if ((flags & 0x8) == 0){
							int x, y = 0;
							int start, offset = 0, offset1 = 0, offset2 = 4;
							x = borders[y*2];
							do{
								if (x < 0){
									++y; x = borders[y*2];
									continue;
								}
								
								start = map[offset++] & 0xff;
								switch(start & 0x3){
									case 0:
										x += (start >> 2) + 1; break;
									case 1:
										start = (start >> 2) + 1;
										for (; start > 0; --start){
											int value = (data[offset1] & 0xff) 
													| (data[offset2] << 2*(offset2 - offset1) & 0x300);
											
											if (++offset1 == offset2){
												++offset1; offset2 += 5; 
											}
											frame.setPixel(Sprite.DATA_IMAGE, x++, y, value);
										}
										break;
									case 2:
										start = (start >> 2) + 1;
										for (; start > 0; --start){
											int value = (data[offset1] & 0xff) 
													| (data[offset2] << 2*(offset2 - offset1) & 0x300);
											
											if (++offset1 == offset2){
												++offset1; offset2 += 5; 
											}
											frame.setPixel(Sprite.DATA_IMAGE, x++, y, value + Sprite.PIXEL_PLAYER_START);
										}
										break;
									case 3:
										++y; x = borders[y*2];
										break; 
								}
								
							}while(offset < size1 && y < height);
							
						}else{
							frame.expand(Sprite.DATA_SMUDGE, 0, 0, width, height);
							frame.setAnchor(Sprite.DATA_SMUDGE, anchorx, anchory);
							int x, y = 0;
							int start, offset = 0, offset1 = 0, offset2 = 2;
							x = borders[y*2];
							do{
								if (x < 0){
									++y; x = borders[y*2];
									continue;
								}
								
								start = map[offset++] & 0xff;
								switch(start & 0x3){
									case 0:
										x += (start >> 2) + 1; break;
									case 1:
										start = (start >> 2) + 1;
										for (; start > 0; --start){
											int pvalue, svalue;
											if (offset2 - offset1 == 2){
												pvalue = (data[offset1] & 0xff) | (data[offset1+1] << 8 & 0x300);
												svalue = (data[offset1+2] >> 4 & 0x0f) | (data[offset1+3] << 4 & 0x3f0);
											}else{
												pvalue = (data[offset1] >> 2 & 0x3f) | (data[offset1+1] << 6 & 0x3c0);
												svalue = (data[offset1+2] & 0x03) | (data[offset1+3] << 2 & 0x3fc);
											}
											if (++offset1 == offset2){
												offset1 += 3; offset2 += 5; 
											}
											frame.setPixel(Sprite.DATA_IMAGE, x, y, pvalue);
											frame.setPixel(Sprite.DATA_SMUDGE, x++, y, svalue);
										}
										break;
									case 2:
										start = (start >> 2) + 1;
										for (; start > 0; --start){
											int pvalue, svalue;
											if (offset2 - offset1 == 2){
												pvalue = (data[offset1] & 0xff) | (data[offset1+1] << 8 & 0x300);
												svalue = (data[offset1+2] >> 4 & 0x0f) | (data[offset1+3] << 4 & 0x3f0);
											}else{
												pvalue = (data[offset1] >> 2 & 0x3f) | (data[offset1+1] << 6 & 0x3c0);
												svalue = (data[offset1+2] & 0x03) | (data[offset1+3] << 2 & 0x3fc);
											}
											if (++offset1 == offset2){
												offset1 += 3; offset2 += 5; 
											}
											frame.setPixel(Sprite.DATA_IMAGE, x, y, pvalue + Sprite.PIXEL_PLAYER_START);
											frame.setPixel(Sprite.DATA_SMUDGE, x++, y, svalue);
										}
										break;
									case 3:
										++y; x = borders[y*2];
										break; 
								}
								
							}while(offset < size1 && y < height);
							
						}
						
					}
					
				}else{
					if (type == Sprite.DATA_SHADOW){
						size1 = readInteger(fis, 4);
						byte[] data = new byte[size1];
						fis.read(data);
						
						if (size1 > 0){
							int x, y = 0;
							int start, offset = 0;
							x = borders[y*2];
							do{
								if (x < 0){
									++y; x = borders[y*2];
									continue;
								}
								start = data[offset++] & 0xff;
								switch(start & 0x3){
									case 0:
										x += (start >> 2) + 1; break;
									case 1: case 2:
										start = (start >> 2) + 1;
										for (; start > 0; --start){
											int value = (data[offset++] & 0xff);
											frame.setPixel(Sprite.DATA_SHADOW, x++, y, value);
										}
										break;
									case 3:
										++y; x = borders[y*2];
										break; 
								}
								
							}while(offset < size1 && y < height);
						}
						
					}else{
						size1 = readInteger(fis, 4);
						byte[] data = new byte[size1];
						fis.read(data);
						
						if (size1 > 0){
							int x, y = 0;
							int start, offset = 0;
							x = borders[y*2];
							do{
								if (x < 0){
									++y; x = borders[y*2];
									continue;
								}
								start = data[offset++] & 0xff;
								switch(start & 0x3){
									case 0:
										x += (start >> 2) + 1; break;
									case 1: case 2:
										start = (start >> 2) + 1;
										for (; start > 0; --start){
											frame.setPixel(Sprite.DATA_OUTLINE, x++, y, 0);
										}
										break;
									case 3:
										++y; x = borders[y*2];
										break; 
								}
								
							}while(offset < size1 && y < height);
						}
						
					}
					
				}
				
			}
			
			MainFrame.setProcessString(String.format("Loading %d/%d ...", index, frameCount));
			
		}
		
		return sprite;
	}
	
	static private SLPSprite loadFromSLP(InputStream fis) throws IOException, NullPointerException{
		SLPSprite sprite = new SLPSprite();
		
		final int frameCount = readInteger(fis, 4);
		byte[] memoBytes = new byte[24];
		fis.read(memoBytes);
		sprite.memo = new String(memoBytes);
		sprite.playerMode = Sprite.PLAYER_PALETTE_AOK;
		
		for (int index=0; index<frameCount; ++index){
			
			SLPSprite.Frame frame = sprite.createFrame();
			
			fis.skip(8);
			frame.palette = readInteger(fis, 4);
			fis.skip(4);
			
			frame.create(readInteger(fis, 4), readInteger(fis, 4));
			frame.setAnchor(Sprite.DATA_IMAGE, readInteger(fis, 4), readInteger(fis, 4));
			sprite.frames.add(index, frame);
		}
		
		// Contents of Each Frame
		for (int index=0; index<frameCount; ++index){
			
			Sprite.Frame frame = sprite.getFrame(index);

			int h = frame.getHeight(Sprite.DATA_IMAGE);
			int[] borders = new int[h*2];
			
			//fis.skip(frame.addrImage);
			
			//Left and right padding
			for (int j=0; j<h*2; ++j){
				borders[j] = readInteger(fis, 2);
			}
			fis.skip(h*4); //Addresses of lines
			
			for (int y=0; y<h; ++y){
				int x = borders[y*2];
				//if (x <= 0)	continue;
				
				int start = readInteger(fis, 1, false);
				while(true){
					
					byte byt = (byte) start; 
					
					int bytl = byt & 0xF;
					int byth = byt & 0xF0;
					
					int len = 1;
					if (bytl == 0xF){
						//End of row (0000 1111)
						break;
					}else if ((bytl & 0x3) == 0x0){
						//Small amount of points (**** **00)
						//amount = Byte >> 2
						len = (start & 0xff) >> 2;
						byte[] bytet = new byte[len];
						fis.read(bytet);
						for (int j=0; j<len; ++j){
							frame.setPixel(Sprite.DATA_IMAGE, x++, y, bytet[j] & 0xff);
						}
						
					}else if ((bytl & 0x3) == 0x1){
						//Small amount of blanks (**** **01)
						x += (start & 0xff) >> 2;
						
					}else if (bytl == 2){
						//Large amount of points (**** 0010)
						//amount = H << 4 + Byte2
						len = (byth << 4) + readInteger(fis, 1, false);
						byte[] bytet = new byte[len];
						fis.read(bytet);
						for (int j=0; j<len; ++j){
							frame.setPixel(Sprite.DATA_IMAGE, x++, y, bytet[j] & 0xff);
						}

					}else if (bytl == 3){
						//Large amount of blanks (**** 0011)
						len = (byth << 4) + readInteger(fis, 1, false);
						x += len;

					}else if (bytl == 6){
						//Amount of player color points (**** 0110)
						//amount = H >> 4 (if H = 0x0, amount = Byte2)
						len = byth >> 4;
						if (len == 0){
							len = readInteger(fis, 1, false);
						}
						for (int j=0; j<len; ++j){
							frame.setPixel(Sprite.DATA_IMAGE, x++, y, readInteger(fis, 1, false) + Sprite.PIXEL_PLAYER_START);
						}

					}else if (bytl == 7){
						//Repeat of next point (**** 0111)
						//amount = H >> 4 (if H = 0x0, amount = Byte2)
						len = byth >> 4;
						if (len == 0){
							len = readInteger(fis, 1, false);
						}
						int pixel = readInteger(fis, 1, false);
						for (int j=0; j<len; ++j){
							frame.setPixel(Sprite.DATA_IMAGE, x++, y, pixel);
						}

					}else if (bytl == 0xA){
						//Repeat of next player color point (**** 1010)
						//amount = H >> 4 (if H = 0x0, amount = Byte2)
						len = byth >> 4;
						if (len == 0){
							len = readInteger(fis, 1, false);
						}
						int pixel = readInteger(fis, 1, false) + Sprite.PIXEL_PLAYER_START;
						for (int j=0; j<len; ++j){
							frame.setPixel(Sprite.DATA_IMAGE, x++, y, pixel);
						}

					}else if (bytl == 0xB){
						//Repeat of shadow points (**** 1011)
						//amount = H >> 4 (if H = 0x0, amount = Byte2)
						len = byth >> 4;
						if (len == 0){
							len = readInteger(fis, 1, false);
						}
						for (int j=0; j<len; ++j){
							frame.setPixel(Sprite.DATA_SHADOW, x++, y, 0);
						}
						
					}else if (bytl == 0xE){
						//Outlines (**** 1110)
						//if H = 0x4, amount = 1; if H = 0x5, amount = Byte2
						if (start == 0x4e){
							frame.setPixel(Sprite.DATA_OUTLINE, x++, y, 0);
						}else if (start == 0x5E){
							len = readInteger(fis, 1, false);
							for (int j=0; j<len; ++j){
								frame.setPixel(Sprite.DATA_OUTLINE, x++, y, 0);
							}
						}
					}
					start = readInteger(fis, 1, false);
					
				}

			}
			
			MainFrame.setProcessString(String.format("Loading %d/%d ...", index, frameCount));

		}
		
		return sprite;
	}

	static private SLPSprite loadFromSMP(InputStream fis) throws IOException, NullPointerException{
		SLPSprite sprite = new SLPSprite();
		
		// Header
		readInteger(fis, 4); // Unknown Prop
		final int frameCount = readInteger(fis, 4);
		fis.skip(20);
		byte[] memoBytes = new byte[32];
		fis.read(memoBytes);
		sprite.memo = new String(memoBytes);
		sprite.playerMode = Sprite.PLAYER_PALETTE_DE;
		
		// Because chunks are aligned by 64 bytes, which made by offical DEAssetTool?
		int[] addresses = new int[frameCount];
		for (int i=0; i<frameCount; ++i){
			addresses[i] = readInteger(fis, 4);
		}
		
		fis.skip(addresses[0] - frameCount * 4 - 64);

		for (int index=0; index<frameCount; ++index){
			SLPSprite.Frame frame = sprite.createFrame();
			sprite.frames.add(index, frame);
			frame.palette = 0; // To be found
			
			int width, height, anchorx, anchory;
			width = readInteger(fis, 4);
			height = readInteger(fis, 4);
			anchorx = readInteger(fis, 4);
			anchory = readInteger(fis, 4);
			fis.skip(48); // Unknown Prop
			
			int[] borders = new int[height * 2 + 2]; // For out of bounds protection
			for (int i=0; i<height; ++i){
				borders[i*2] = readInteger(fis, 2);
				borders[i*2+1] = readInteger(fis, 2);
			}
			
			frame.create(width, height);
			frame.setAnchor(Sprite.DATA_IMAGE, anchorx, anchory);
					
			fis.skip(height * 4); // Line addresses
			
			int chunkSize = 64 + height * 8; 
			
			int x, y = 0;
			int start;
			x = borders[y*2];
			do{	
				if (x < 0){
					++y; x = borders[y*2];
					continue;
				}
				start = readInteger(fis, 1, false);
				++chunkSize;
				switch(start & 0x3){
					case 0:
						x += (start >> 2) + 1; break;
					case 1:
						start = (start >> 2) + 1;
						chunkSize += start << 2;
						for (; start > 0; --start){
							int value = readInteger(fis, 4, false);
							frame.setPixel(Sprite.DATA_IMAGE, x++, y, value);
						}
						break;
					case 2:
						start = (start >> 2) + 1;
						chunkSize += start << 2;
						for (; start > 0; --start){
							int value = readInteger(fis, 4, false);
							frame.setPixel(Sprite.DATA_IMAGE, x++, y, value + Sprite.PIXEL_PLAYER_START);
						}
						break;
					case 3:
						++y; x = borders[y*2];
						break; 
				}
				
			}while(y < height);
			
			if (index < frameCount - 1){
				fis.skip(addresses[index + 1] - addresses[index] - chunkSize);
			}
			
			MainFrame.setProcessString(String.format("Loading %d/%d ...", index, frameCount));
			
		}
		
		return sprite;
	}
	
	static private SMXSprite loadFromDESLP(InputStream fis) throws IOException, NullPointerException{
		SMXSprite sprite = new SMXSprite();
		
		final int frameCount = readInteger(fis, 4);
		fis.skip(2);
		final int shadowCount = readInteger(fis, 2);
		fis.skip(4);
		final int frameDataOffset = readInteger(fis, 4);
		final int shadowDataOffset = readInteger(fis, 4);
		
		byte[] memoBytes = new byte[8]; // Doubt
		fis.read(memoBytes);
		sprite.memo = new String(memoBytes);
		sprite.playerMode = Sprite.PLAYER_PALETTE_AOEDE;
		
		int[] offsets = new int[frameCount * 2];
		fis.skip(frameDataOffset - 0x20);
		for (int index=0; index<frameCount; ++index){
			Sprite.Frame frame = sprite.createFrame();
			offsets[index*2+1] = readInteger(fis, 4);
			offsets[index*2] = readInteger(fis, 4);
			fis.skip(6);
			frame.palette = readInteger(fis, 2) + 256;
			
			int width = readInteger(fis, 4);
			int height = readInteger(fis, 4);
			int anchorX = readInteger(fis, 4);
			int anchorY = readInteger(fis, 4);
			frame.setAnchor(Sprite.DATA_IMAGE, anchorX, anchorY);
			frame.expand(Sprite.DATA_IMAGE, anchorX, anchorY, width-anchorX, height-anchorY);
			sprite.frames.add(index, frame);
			
		}
		fis.skip(offsets[0] - 32 - (frameCount << 5));

		// Contents of Each Frame
		for (int index=0; index<frameCount; ++index){
			
			Sprite.Frame frame = sprite.getFrame(index);

			InputStream tis;
			if (index == frameCount - 1 && shadowDataOffset <= frameDataOffset){ // No shadow
				tis = fis;
			}else{
				int length;
				if (index == frameCount - 1)
					length = shadowDataOffset - offsets[index*2];
				else
					length = offsets[index*2+2] - offsets[index*2];

				byte[] segment = new byte[length];
				fis.read(segment);
				tis = new ByteArrayInputStream(segment);
			}
			
			int h = frame.getHeight(Sprite.DATA_IMAGE);

			int[] borders = new int[h*2]; //Left and right padding
			for (int j=0; j<h*2; ++j){
				borders[j] = readInteger(tis, 2);
			}
			tis.skip(offsets[index*2+1] - offsets[index*2] - h*4);
			tis.skip(h*4); //Addresses of lines
			
			for (int y=0; y<h; ++y){
				int x = borders[y*2];
				
				int start = readInteger(tis, 1, false);
				while(true){
					
					byte byt = (byte) start; 
					
					int bytl = byt & 0xF;
					int byth = byt & 0xF0;
					
					int len = 1;
					if (bytl == 0xF){ //End of line
						break;
					}else if ((bytl & 0x3) == 0x0){ // Short normal pixels
						len = (start & 0xff) >> 2;
						byte[] bytet = new byte[len];
						tis.read(bytet);
						for (int j=0; j<len; ++j){
							frame.setPixel(Sprite.DATA_IMAGE, x++, y, bytet[j] & 0xff);
						}
						
					}else if ((bytl & 0x3) == 0x1){ // Short blank
						x += (start & 0xff) >> 2;
						
					}else if (bytl == 2){ // Long normal pixels
						len = (byth << 4) + readInteger(tis, 1, false);
						byte[] bytet = new byte[len];
						tis.read(bytet);
						for (int j=0; j<len; ++j){
							frame.setPixel(Sprite.DATA_IMAGE, x++, y, bytet[j] & 0xff);
						}

					}else if (bytl == 3){ // Long blank
						len = (byth << 4) + readInteger(tis, 1, false);
						x += len;

					}else if (bytl == 6){ // Player color
						len = byth >> 4;
						if (len == 0){
							len = readInteger(tis, 1, false);
						}
						for (int j=0; j<len; ++j){
							frame.setPixel(Sprite.DATA_IMAGE, x++, y, readInteger(tis, 1, false) + Sprite.PIXEL_PLAYER_START);
						}

					}else if (bytl == 7){ // Repeated pixel
						len = byth >> 4;
						if (len == 0){
							len = readInteger(tis, 1, false);
						}
						int pixel = readInteger(tis, 1, false);
						for (int j=0; j<len; ++j){
							frame.setPixel(Sprite.DATA_IMAGE, x++, y, pixel);
						}

					}else if (bytl == 0xA){ // Repeated player color
						len = byth >> 4;
						if (len == 0){
							len = readInteger(tis, 1, false);
						}
						int pixel = readInteger(tis, 1, false) + Sprite.PIXEL_PLAYER_START;
						for (int j=0; j<len; ++j){
							frame.setPixel(Sprite.DATA_IMAGE, x++, y, pixel);
						}

					}
					start = readInteger(tis, 1, false);
				}

			}
			
			if (tis != fis)
				tis.close();
			
			MainFrame.setProcessString(String.format("Loading %d/%d ...", index, frameCount));
			
		}
		
		// Load Shadows 
		if (shadowDataOffset > frameDataOffset){
			
			for (int index=0; index<shadowCount; ++index){
				offsets[index*2+1] = readInteger(fis, 4);
				offsets[index*2] = readInteger(fis, 4);
				fis.skip(8);
	
				int width = readInteger(fis, 4);
				int height = readInteger(fis, 4);
				int anchorX = readInteger(fis, 4);
				int anchorY = readInteger(fis, 4);
				Sprite.Frame frame = sprite.getFrame(index);
				frame.setAnchor(Sprite.DATA_SHADOW, anchorX, anchorY);
				frame.expand(Sprite.DATA_SHADOW, anchorX, anchorY, width-anchorX, height-anchorY);
				
			}
			fis.skip(offsets[0] - (shadowCount << 5) - shadowDataOffset);
	
			// Contents of Each Frame
			for (int index=0; index<shadowCount; ++index){
				
				Sprite.Frame frame = sprite.getFrame(index);
	
				InputStream tis;
				if (index == shadowCount - 1){
					tis = fis;
				}else{
					byte[] segment = new byte[offsets[index*2+2] - offsets[index*2]];
					fis.read(segment);
					tis = new ByteArrayInputStream(segment);
				}
				
				int h = frame.getHeight(Sprite.DATA_SHADOW);
	
				int[] borders = new int[h*2]; //Left and right padding
				for (int j=0; j<h*2; ++j){
					borders[j] = readInteger(tis, 2);
				}
				tis.skip(offsets[index*2+1] - offsets[index*2] - h*4);
				tis.skip(h*4); //Addresses of lines
				
				for (int y=0; y<h; ++y){
					int x = borders[y*2];
					
					int start = readInteger(tis, 1, false);
					while(true){
						
						byte byt = (byte) start; 
						
						int bytl = byt & 0xF;
						int byth = byt & 0xF0;
						
						int len = 1;
						if (bytl == 0xF){ // End of line
							break;
						}else if ((bytl & 0x3) == 0x0){ // Short pixels
							len = (start & 0xff) >> 2;
							byte[] bytet = new byte[len];
							tis.read(bytet);
							for (int j=0; j<len; ++j){
								frame.setPixel(Sprite.DATA_SHADOW, x++, y, 
										getAOEDEShadowDepth(bytet[j] & 0xff));
							}
							
						}else if ((bytl & 0x3) == 0x1){ // Short blank
							x += (start & 0xff) >> 2;
							
						}else if (bytl == 2){ // Large pixels
							len = (byth << 4) + readInteger(tis, 1, false);
							byte[] bytet = new byte[len];
							tis.read(bytet);
							for (int j=0; j<len; ++j){
								frame.setPixel(Sprite.DATA_SHADOW, x++, y,
										getAOEDEShadowDepth(bytet[j] & 0xff));
							}
	
						}else if (bytl == 3){ // Large blank
							len = (byth << 4) + readInteger(tis, 1, false);
							x += len;
	
						}else if (bytl == 7){ // Repeated pixel
							len = byth >> 4;
							if (len == 0){
								len = readInteger(tis, 1, false);
							}
							int pixel = readInteger(tis, 1, false);
							for (int j=0; j<len; ++j){
								frame.setPixel(Sprite.DATA_SHADOW, x++, y, 
										getAOEDEShadowDepth(pixel));
							}
						}
						start = readInteger(tis, 1, false);
					}
	
				}
				
				if (tis != fis)
					tis.close();
				
				MainFrame.setProcessString(String.format("Loading %d/%d ...", index, frameCount));
				
			}
		
		}
		
		return sprite;
	}

	
	/**
	 * Convert shadow depth version from AOE:DE to common.
	 * Maximum of source is 63, while minimum is 32, mapping to 2 to 126. 
	 * @param depth Source depth.
	 * @return Mapped depth.
	 */
	static private final int getAOEDEShadowDepth(int depth){
		return Math.max(0, Math.min(63, 63 - depth)) * 4 + 2;
	}

	
	
	static private class CompiledSLPFrame{
		int[] paddings;
		int[] lineOffsets;
		byte[] data;
		Sprite.Frame frame;
		public CompiledSLPFrame(Sprite.Frame frame){
			SLPSprite sprite = new SLPSprite(); 
			if (!(frame instanceof SLPSprite.Frame)){
				frame = sprite.createFrame(frame);
			}
			this.frame = frame;
			int width = frame.getWidth(Sprite.DATA_IMAGE);
			int height = frame.getHeight(Sprite.DATA_IMAGE);
			byte[] data = new byte[(width + 1) * height * 2];
			paddings = new int[height * 2];
			lineOffsets = new int[height];
			
			int offset = 0;
			for (int y=0; y<height; ++y){
				lineOffsets[y] = offset;
				
				int left = 0, right = width - 1;
				for (; left<width; ++left){
					if (frame.getPixel(Sprite.DATA_IMAGE, left, y) != Sprite.PIXEL_NULL)
						break;
					else if (frame.getPixel(Sprite.DATA_SHADOW, left, y) != Sprite.PIXEL_NULL)
						break;
					else if (frame.getPixel(Sprite.DATA_OUTLINE, left, y) != Sprite.PIXEL_NULL)
						break;
				}
				
				if (left >= width){
					paddings[y*2] = -1;
					paddings[y*2+1] = -1;
					
				}else{
					paddings[y*2] = left;
					paddings[y*2+1] = width - right - 1;
					for (; right>0; --right){
						if (frame.getPixel(Sprite.DATA_IMAGE, right, y) != Sprite.PIXEL_NULL)
							break;
						else if (frame.getPixel(Sprite.DATA_SHADOW, right, y) != Sprite.PIXEL_NULL)
							break;
						else if (frame.getPixel(Sprite.DATA_OUTLINE, right, y) != Sprite.PIXEL_NULL)
							break;
					}
					
					for (int x=left; x<=right;){
						int pixel;
						if ((pixel = frame.getPixel(Sprite.DATA_IMAGE, x, y)) != Sprite.PIXEL_NULL){
							if (pixel >= Sprite.PIXEL_PLAYER_START) { // Player Color
								int limit = Math.min(right + 1, x + 0xff); // Check if Repeat
								int x1 = x + 1;
								for (; x1 < limit && frame.getPixel(Sprite.DATA_IMAGE, x1, y) == pixel; ++x1);
								int length = x1 - x;
								
								if (length >= 0x10){ // Large Repeat
									data[offset++] = 0x0A;
									data[offset++] = (byte) length;
									data[offset++] = (byte) (pixel - Sprite.PIXEL_PLAYER_START);
									x += length;
									
								}else if (length >= 0x4){ // Small Repeat
									data[offset++] = (byte) (length << 4 | 0x0A);
									data[offset++] = (byte) (pixel - Sprite.PIXEL_PLAYER_START);
									x += length;
									
								}else{ // General
									for (int last=Sprite.PIXEL_NULL, times=0; x1 < limit; ++x1){
										pixel = frame.getPixel(Sprite.DATA_IMAGE, x1, y);
										if (pixel < Sprite.PIXEL_PLAYER_START){
											break;
										}else if (pixel == last){ // Repeated trailing pixel
											if (++times >= 4){
												x1 -= times - 1; break;
											}
										}else{
											last = pixel; times = 0;
										}
									}
									length = x1 - x;
									
									if (length >= 0x10){ // Large
										data[offset++] = 0x06;
										data[offset++] = (byte) length;
									}else{ // Small
										data[offset++] = (byte) (length << 4 | 0x06);
									}
									for (; x<x1; ++x){
										pixel = frame.getPixel(Sprite.DATA_IMAGE, x, y);
										data[offset++] = (byte) (pixel - Sprite.PIXEL_PLAYER_START);
									}
								}
								
							}else{ // Normal Color
								int limit = Math.min(right + 1, x + 0xff); // Check if Repeat
								int x1 = x + 1;
								for (; x1 < limit && frame.getPixel(Sprite.DATA_IMAGE, x1, y) == pixel; ++x1);
								int length = x1 - x;
								
								if (length >= 0x10){ // Large Repeat
									data[offset++] = 0x07;
									data[offset++] = (byte) length;
									data[offset++] = (byte) pixel;
									x += length;
									
								}else if (length >= 0x4){ // Small Repeat
									data[offset++] = (byte) (length << 4 | 0x07);
									data[offset++] = (byte) pixel;
									x += length;
									
								}else{ // General
									limit = Math.min(right + 1, x + 0xfff);
									for (int last=Sprite.PIXEL_NULL, times=0; x1 < limit; ++x1){
										pixel = frame.getPixel(Sprite.DATA_IMAGE, x1, y);
										if (pixel == Sprite.PIXEL_NULL){
											break;
										}else if (pixel >= Sprite.PIXEL_PLAYER_START){
											break;
										}else if (pixel == last){ // Repeated trailing pixel
											if (++times >= 4){
												x1 -= times - 1; break;
											}
										}else{
											last = pixel; times = 0;
										}
									}
									length = x1 - x;
									
									if (length >= 0x40){ // Large
										data[offset++] = (byte) (length >> 4 & 0xf0 | 0x02);
										data[offset++] = (byte) length;
									}else{ // Small
										data[offset++] = (byte) (length << 2);
									}
									for (; x<x1; ++x){
										data[offset++] = (byte) frame.getPixel(Sprite.DATA_IMAGE, x, y);
									}
								}
							}
							
						}else if ((pixel = frame.getPixel(Sprite.DATA_SHADOW, x, y)) != Sprite.PIXEL_NULL){ // Shadow
							int limit = Math.min(right + 1, x + 0xff); // Check if Repeat
							int x1 = x + 1;
							for (; x1 < limit && frame.getPixel(Sprite.DATA_SHADOW, x1, y) != Sprite.PIXEL_NULL; ++x1);
							int length = x1 - x;
							
							if (length >= 0x10){ // Large
								data[offset++] = 0x0b;
								data[offset++] = (byte) length;
							}else{ // Small
								data[offset++] = (byte) (length << 4 | 0x0b);
							}
							x += length;
							
						}else if ((pixel = frame.getPixel(Sprite.DATA_OUTLINE, x, y)) != Sprite.PIXEL_NULL){ // Outline
							int limit = Math.min(right + 1, x + 0xff); // Check if Repeat
							int x1 = x + 1;
							for (; x1 < limit && frame.getPixel(Sprite.DATA_OUTLINE, x1, y) != Sprite.PIXEL_NULL; ++x1);
							int length = x1 - x;
							
							if (length > 1){ // Large
								data[offset++] = 0x5e;
								data[offset++] = (byte) length;
							}else{ // Small
								data[offset++] = 0x4e;
							}
							x += length;
							
						}else{ // Blank
							int limit = Math.min(right, x + 0xff);
							int x1 = x + 1;
							for (; x1 < limit; ++x1){
								if (frame.getPixel(Sprite.DATA_IMAGE, x1, y) != Sprite.PIXEL_NULL)
									break;
								else if (frame.getPixel(Sprite.DATA_SHADOW, x1, y) != Sprite.PIXEL_NULL)
									break;
								else if (frame.getPixel(Sprite.DATA_OUTLINE, x1, y) != Sprite.PIXEL_NULL)
									break;
							}
							int length = x1 - x;
							
							if (length >= 0x40){ // Large
								data[offset++] = (byte) (length >> 4 & 0xf0 | 0x03);
								data[offset++] = (byte) length;
							}else{ // Small
								data[offset++] = (byte) (length << 2 | 0x01);
							}
							x += length;
							
						}
					}
				}
				data[offset++] = 0x0f;
				
			}
			
			this.data = new byte[offset];
			for (int i=0; i<offset; ++i){
				this.data[i] = data[i];
			}
		}
	}

	static private class CompiledSMXFrame{ // Only normal format, not enhanced at present
		int smpSize;
		int[] paddingsImage;
		int[] paddingsOutline;
		int[] paddingsShadow;
		byte[] mapImage;
		byte[] dataImage;
		byte[] dataOutline;
		byte[] dataShadow;
		Sprite.Frame frame;
		
		public CompiledSMXFrame(Sprite.Frame frame){
			this.frame = frame;
			smpSize = 0;
			int width, height;
			
			// Normal Image
			width = frame.getWidth(Sprite.DATA_IMAGE);
			height = frame.getHeight(Sprite.DATA_IMAGE);
			byte[] mapImage = new byte[width * height];
			if (height > 0){
				smpSize += 32 + height * 8;
			
				if (frame.getHeight(Sprite.DATA_SMUDGE) > 0){				
					byte[] dataImage = new byte[width * height * 5 / 2 + 5];
					paddingsImage = new int[height * 2];
	
					int offset = 0, offset1 = 0, counter = 0;
					for (int y=0; y<height; ++y){
						int left = 0, right = width-1;
						while(left < width && frame.getPixel(Sprite.DATA_IMAGE, left, y) == Sprite.PIXEL_NULL)
							++left;
						if (left >= width){ // Blank Line
							paddingsImage[y*2] = -1;
							paddingsImage[y*2+1] = -1;
							continue;
						}
						
						while(frame.getPixel(Sprite.DATA_IMAGE, right, y) == Sprite.PIXEL_NULL)
							--right;
						
						paddingsImage[y*2] = left;
						paddingsImage[y*2+1] = width - right - 1;
						
						for (int x=left; x<=right;){
							int pixel = frame.getPixel(Sprite.DATA_IMAGE, x, y);
							int smudge;
							int limit = Math.min(right, x + 0x3f);
							if (pixel == Sprite.PIXEL_NULL){ // Blank
								int x1 = x + 1;
								for (; x1<=limit; ++x1){
									if (frame.getPixel(Sprite.DATA_IMAGE, x1, y) != Sprite.PIXEL_NULL)
										break;
								}
								mapImage[offset++] = (byte)(x1 - x - 1 << 2);
								x = x1;
								
							}else if (pixel >= Sprite.PIXEL_PLAYER_START){ // Player Color
								int x1 = x + 1;
								for (; x1<=limit; ++x1){
									pixel = frame.getPixel(Sprite.DATA_IMAGE, x1, y); 
									if (pixel == Sprite.PIXEL_NULL || pixel < Sprite.PIXEL_PLAYER_START)
										break;
								}
								int length = x1 - x;
								mapImage[offset++] = (byte)(length - 1 << 2 | 0x2);
								smpSize += length << 2;
								
								for (; length>0; ++x, --length, ++counter){
									pixel = frame.getPixel(Sprite.DATA_IMAGE, x, y) - Sprite.PIXEL_PLAYER_START;
									smudge = frame.getPixel(Sprite.DATA_SMUDGE, x, y);
									if ((counter & 1) == 0){
										dataImage[offset1] = (byte) (pixel & 0xff);
										dataImage[offset1+1] = (byte) (pixel >> 8 & 0x3);
										dataImage[offset1+2] = (byte) (smudge << 4 & 0xf0);
										dataImage[offset1+3] = (byte) (smudge >> 4 & 0x3f);
										++offset1;
									}else{
										dataImage[offset1] |= (pixel << 2 & 0xfc);
										dataImage[offset1+1] |= (pixel >> 6 & 0xf);
										dataImage[offset1+2] |= (smudge << 6 & 0xc0);
										dataImage[offset1+3] = (byte) (smudge >> 2 & 0xff);
										offset1 += 4;
									}
								}
								
							}else{ // Normal Color
								int x1 = x + 1;
								for (; x1<=limit; ++x1){
									pixel = frame.getPixel(Sprite.DATA_IMAGE, x1, y); 
									if (pixel == Sprite.PIXEL_NULL || pixel >= Sprite.PIXEL_PLAYER_START)
										break;
								}
								int length = x1 - x;
								mapImage[offset++] = (byte)(length - 1 << 2 | 0x1);
								smpSize += length << 2;
								
								for (; length>0; ++x, --length, ++counter){
									pixel = frame.getPixel(Sprite.DATA_IMAGE, x, y);
									smudge = frame.getPixel(Sprite.DATA_SMUDGE, x, y);
									if ((counter & 1) == 0){
										dataImage[offset1] = (byte) (pixel & 0xff);
										dataImage[offset1+1] = (byte) (pixel >> 8 & 0x3);
										dataImage[offset1+2] = (byte) (smudge << 4 & 0xf0);
										dataImage[offset1+3] = (byte) (smudge >> 4 & 0x3f);
										++offset1;
									}else{
										dataImage[offset1] |= (byte) (pixel << 2 & 0xfc);
										dataImage[offset1+1] |= (byte) (pixel >> 6 & 0xf);
										dataImage[offset1+2] |= (byte) (smudge << 6 & 0xc0);
										dataImage[offset1+3] = (byte) (smudge >> 2 & 0xff);
										offset1 += 4;
									}
								}
								
							}
							++smpSize;
						}
						mapImage[offset++] = 0x3;
						++smpSize;
						
					}
					this.mapImage = new byte[offset];
					for (int i=0; i<offset; ++i){
						this.mapImage[i] = mapImage[i];
					}
					offset1 = (counter + 1) / 2 * 5;
					this.dataImage = new byte[offset1];
					for (int i=0; i<offset1; ++i){
						this.dataImage[i] = dataImage[i];
					}
					
				}else{
					byte[] dataImage = new byte[width * height * 5 / 4 + 5];
					paddingsImage = new int[height * 2];
					
					int offset = 0, offset1 = 0, offset2 = 4;
					for (int y=0; y<height; ++y){
						int left = 0, right = width-1;
						while(left < width && frame.getPixel(Sprite.DATA_IMAGE, left, y) == Sprite.PIXEL_NULL)
							++left;
						if (left >= width){ // Blank Line
							paddingsImage[y*2] = -1;
							paddingsImage[y*2+1] = -1;
							continue;
						}
						
						while(frame.getPixel(Sprite.DATA_IMAGE, right, y) == Sprite.PIXEL_NULL)
							--right;
						
						paddingsImage[y*2] = left;
						paddingsImage[y*2+1] = width - right - 1;
						
						for (int x=left; x<=right;){
							int pixel = frame.getPixel(Sprite.DATA_IMAGE, x, y);
							int limit = Math.min(right, x + 0x3f);
							if (pixel == Sprite.PIXEL_NULL){ // Blank
								int x1 = x + 1;
								for (; x1<=limit; ++x1){
									if (frame.getPixel(Sprite.DATA_IMAGE, x1, y) != Sprite.PIXEL_NULL)
										break;
								}
								mapImage[offset++] = (byte)(x1 - x - 1 << 2);
								x = x1;
								
							}else if (pixel >= Sprite.PIXEL_PLAYER_START){ // Player Color
								int x1 = x + 1;
								for (; x1<=limit; ++x1){
									pixel = frame.getPixel(Sprite.DATA_IMAGE, x1, y); 
									if (pixel == Sprite.PIXEL_NULL || pixel < Sprite.PIXEL_PLAYER_START)
										break;
								}
								int length = x1 - x;
								mapImage[offset++] = (byte)(length - 1 << 2 | 0x2);
								smpSize += length << 2;
								
								for (; length>0; ++x, --length){
									pixel = frame.getPixel(Sprite.DATA_IMAGE, x, y) - Sprite.PIXEL_PLAYER_START;
									dataImage[offset2] |= (byte) ((pixel & 0x300) >> 2 * (offset2 - offset1));
									dataImage[offset1++] = (byte) pixel;
									if (offset1 == offset2){
										++offset1; offset2 += 5;
									}
								}
								
							}else{ // Normal Color
								int x1 = x + 1;
								for (; x1<=limit; ++x1){
									pixel = frame.getPixel(Sprite.DATA_IMAGE, x1, y); 
									if (pixel == Sprite.PIXEL_NULL || pixel >= Sprite.PIXEL_PLAYER_START)
										break;
								}
								int length = x1 - x;
								mapImage[offset++] = (byte)(length - 1 << 2 | 0x1);
								smpSize += length << 2;
								
								for (; length>0; ++x, --length){
									pixel = frame.getPixel(Sprite.DATA_IMAGE, x, y);
									dataImage[offset2] |= (byte) ((pixel & 0x300) >> 2 * (offset2 - offset1));
									dataImage[offset1++] = (byte) pixel;
									if (offset1 == offset2){
										++offset1; offset2 += 5;
									}
								}
								
							}
							++smpSize;
						}
						mapImage[offset++] = 0x3;
						++smpSize;
						
					}
					this.mapImage = new byte[offset];
					for (int i=0; i<offset; ++i){
						this.mapImage[i] = mapImage[i];
					}
					this.dataImage = new byte[offset2+1];
					for (int i=0; i<=offset2; ++i){
						this.dataImage[i] = dataImage[i];
					}
					
				}
				
			}
			
			
			// Shadow
			width = frame.getWidth(Sprite.DATA_SHADOW);
			height = frame.getHeight(Sprite.DATA_SHADOW);
			byte[] dataShadow = new byte[width * height * 2];
			paddingsShadow = new int[height * 2];
			if (height > 0){
				smpSize += 32 + height * 8;
			
				int offset = 0;
				for (int y=0; y<height; ++y){
					int left = 0, right = width-1;
					while(left < width && frame.getPixel(Sprite.DATA_SHADOW, left, y) == Sprite.PIXEL_NULL)
						++left;
					if (left >= width){ // Blank Line
						paddingsShadow[y*2] = -1;
						paddingsShadow[y*2+1] = -1;
						continue;
					}
					
					while(frame.getPixel(Sprite.DATA_SHADOW, right, y) == Sprite.PIXEL_NULL)
						--right;
					
					paddingsShadow[y*2] = left;
					paddingsShadow[y*2+1] = width - right - 1;
					
					for (int x=left; x<=right;){
						int pixel = frame.getPixel(Sprite.DATA_SHADOW, x, y);
						int limit = Math.min(right, x + 0x3f);
						if (pixel == Sprite.PIXEL_NULL){ // Blank
							int x1 = x + 1;
							for (; x1<=limit; ++x1){
								if (frame.getPixel(Sprite.DATA_SHADOW, x1, y) != Sprite.PIXEL_NULL)
									break;
							}
							dataShadow[offset++] = (byte)(x1 - x - 1 << 2);
							x = x1;
							
						}else{ // Shadow
							int x1 = x + 1;
							for (; x1<=limit; ++x1){
								if (frame.getPixel(Sprite.DATA_SHADOW, x1, y) == Sprite.PIXEL_NULL)
									break;
							}
							int length = x1 - x;
							dataShadow[offset++] = (byte)(length - 1 << 2 | 0x1);
							smpSize += length;
							
							for (; length>0; ++x, --length){
								pixel = frame.getPixel(Sprite.DATA_SHADOW, x, y); 
								dataShadow[offset++] = (byte) pixel;
							}
							
						}
						++smpSize;
					}
					dataShadow[offset++] = 0x3;
					++smpSize;
					
				}
				this.dataShadow = new byte[offset];
				for (int i=0; i<offset; ++i){
					this.dataShadow[i] = dataShadow[i];
				}
				
			}
			
			
			// Outline
			width = frame.getWidth(Sprite.DATA_OUTLINE);
			height = frame.getHeight(Sprite.DATA_OUTLINE);
			byte[] dataOutline = new byte[width * height * 2];
			paddingsOutline = new int[height * 2];
			if (height > 0){
				smpSize += 32 + height * 8;
				
				int offset = 0;
				for (int y=0; y<height; ++y){
					int left = 0, right = width-1;
					while(left < width && frame.getPixel(Sprite.DATA_OUTLINE, left, y) == Sprite.PIXEL_NULL)
						++left;
					if (left >= width){ // Blank Line
						paddingsOutline[y*2] = -1;
						paddingsOutline[y*2+1] = -1;
						continue;
					}
					
					while(frame.getPixel(Sprite.DATA_OUTLINE, right, y) == Sprite.PIXEL_NULL)
						--right;
					
					paddingsOutline[y*2] = left;
					paddingsOutline[y*2+1] = width - right - 1;
					
					for (int x=left; x<=right;){
						int pixel = frame.getPixel(Sprite.DATA_OUTLINE, x, y);
						int limit = Math.min(right, x + 0x3f);
						if (pixel == Sprite.PIXEL_NULL){ // Blank
							int x1 = x + 1;
							for (; x1<=limit; ++x1){
								if (frame.getPixel(Sprite.DATA_OUTLINE, x1, y) != Sprite.PIXEL_NULL)
									break;
							}
							dataOutline[offset++] = (byte)(x1 - x - 1 << 2);
							x = x1;
							
						}else{ // Outline
							int x1 = x + 1;
							for (; x1<=limit; ++x1){
								if (frame.getPixel(Sprite.DATA_OUTLINE, x1, y) == Sprite.PIXEL_NULL)
									break;
							}
							dataOutline[offset++] = (byte)(x1 - x - 1 << 2 | 0x1);
							x = x1;
							
						}
						++smpSize;
					}
					dataOutline[offset++] = 0x3;
					++smpSize;
					
				}
				this.dataOutline = new byte[offset];
				for (int i=0; i<offset; ++i){
					this.dataOutline[i] = dataOutline[i];
				}
				
			}
			
			smpSize = smpSize + 63 & 0xffffffc0; // Ceil by 64
		}
	}
	
	
	static public boolean saveSLPSprite(Sprite sprite, String fname){
		int count = sprite.getFrameCount();
		CompiledSLPFrame[] compiledFrames = new CompiledSLPFrame[count];
		int[] frameOffsets = new int[count];
		int offset = 0x20 + 0x20 * count;
		for (int i=0; i<count; ++i){
			frameOffsets[i] = offset;
			compiledFrames[i] = new CompiledSLPFrame(sprite.getFrame(i));
			offset += compiledFrames[i].data.length
					+ compiledFrames[i].frame.getHeight(Sprite.DATA_IMAGE)*8;
		}
		
		try {
			File f = new File(fname);
			FileOutputStream fos = new FileOutputStream(f);
			fos.write("2.0N".getBytes()); // Header
			writeInteger(fos, count, 4); // Count
			
			byte[] memoBytes = Configuration.isDefaultMemo() ?
					SLPSprite.slpmemo.getBytes() : sprite.memo.getBytes();
			if (memoBytes.length >= 24){
				fos.write(memoBytes, 0, 24); // Memo
			}else{
				fos.write(memoBytes);
				fos.write(new byte[24 - memoBytes.length]);
			}
			
			for (int i=0; i<count; ++i){
				Sprite.Frame frame = compiledFrames[i].frame;
				writeInteger(fos, frameOffsets[i] + frame.getHeight(Sprite.DATA_IMAGE) * 4, 4); // Address
				writeInteger(fos, frameOffsets[i], 4); // Offset
				writeInteger(fos, frame.palette, 4); // Palette
				writeInteger(fos, 0x10, 4); // Cardinal
				writeInteger(fos, frame.getWidth(Sprite.DATA_IMAGE), 4);
				writeInteger(fos, frame.getHeight(Sprite.DATA_IMAGE), 4);
				writeInteger(fos, frame.getAnchorX(Sprite.DATA_IMAGE), 4);
				writeInteger(fos, frame.getAnchorY(Sprite.DATA_IMAGE), 4);
			}
			
			offset = 0x20 + 0x20 * count;
			for (int i=0; i<count; ++i){
				Sprite.Frame frame = compiledFrames[i].frame;
				CompiledSLPFrame compiledFrame = compiledFrames[i]; 
				int height = frame.getHeight(Sprite.DATA_IMAGE);
				offset += height * 8;
				for (int y=0; y<height; ++y){
					writeInteger(fos, compiledFrame.paddings[y*2], 2); // Left
					writeInteger(fos, compiledFrame.paddings[y*2+1], 2); // Right
				}
				for (int y=0; y<height; ++y){
					writeInteger(fos, compiledFrame.lineOffsets[y] + offset, 4); // Address
				}
				fos.write(compiledFrame.data);
				offset += compiledFrame.data.length;
			}
			
			fos.close();
			return true;
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();	
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	static public boolean saveSMXSprite(Sprite sprite, String fname){
		int count = sprite.getFrameCount();
		int[] sizes = new int[count * 3];
		byte[] flags = new byte[count];
		CompiledSMXFrame[] compiledFrames = new CompiledSMXFrame[count];
		int size = 0, smpSize = count * 4;
		for (int i=0; i<count; ++i){
			CompiledSMXFrame compiledFrame = new CompiledSMXFrame(sprite.getFrame(i));
			flags[i] = 0;
			// Map size + data size + both sizes size + borders size
			if (compiledFrame.frame.getHeight(Sprite.DATA_IMAGE) > 0){
				sizes[i*3] = compiledFrame.mapImage.length + compiledFrame.dataImage.length
						+ compiledFrame.frame.getHeight(Sprite.DATA_IMAGE) * 4 + 8;
				size += sizes[i*3] + 16;
				flags[i] |= 1;
				if (compiledFrame.frame.getHeight(Sprite.DATA_SMUDGE) > 0){
					flags[i] |= 8;
				}
			}
			if (compiledFrame.frame.getHeight(Sprite.DATA_SHADOW) > 0){
				sizes[i*3+1] = compiledFrame.dataShadow.length
						+ compiledFrame.frame.getHeight(Sprite.DATA_SHADOW) * 4 + 4;
				size += sizes[i*3+1] + 16;
				flags[i] |= 2;
			}
			if (compiledFrame.frame.getHeight(Sprite.DATA_OUTLINE) > 0){
				sizes[i*3+2] = compiledFrame.dataOutline.length
						+ compiledFrame.frame.getHeight(Sprite.DATA_OUTLINE) * 4 + 4;
				size += sizes[i*3+2] + 16;
				flags[i] |= 4;
			}
			size += 6;
			
			smpSize += compiledFrame.smpSize;
			compiledFrames[i] = compiledFrame;
		}
		
		try {
			File f = new File(fname);
			FileOutputStream fos = new FileOutputStream(f);
			fos.write("SMPX".getBytes()); // Header
			writeInteger(fos, 2, 2); // Version?
			writeInteger(fos, count, 2); // Count
			writeInteger(fos, size, 4); // Size
			writeInteger(fos, smpSize, 4); // Unknown, allocation memory size?
			
			byte[] memoBytes = Configuration.isDefaultMemo() ?
					SLPSprite.slpmemo.getBytes() : sprite.memo.getBytes();
			if (memoBytes.length >= 16){
				fos.write(memoBytes, 0, 16); // Memo
			}else{
				fos.write(memoBytes);
				fos.write(new byte[16 - memoBytes.length]);
			}
			
			for (int i=0; i<count; ++i){
				CompiledSMXFrame compiledFrame = compiledFrames[i];
				byte flag = flags[i];
				Sprite.Frame frame = compiledFrame.frame;
				
				writeInteger(fos, flag, 1); // Flags
				writeInteger(fos, frame.palette, 1); // Palette
				writeInteger(fos, compiledFrame.smpSize, 4); // Unknown
				
				if ((flag & 1) != 0){ // Normal Image
					int height = frame.getHeight(Sprite.DATA_IMAGE);
					writeInteger(fos, frame.getWidth(Sprite.DATA_IMAGE), 2);
					writeInteger(fos, height, 2);
					writeInteger(fos, frame.getAnchorX(Sprite.DATA_IMAGE), 2);
					writeInteger(fos, frame.getAnchorY(Sprite.DATA_IMAGE), 2);
					writeInteger(fos, sizes[i*3], 4);
					writeInteger(fos, 0, 4);
					
					for (int y=0; y<height; ++y){
						writeInteger(fos, compiledFrame.paddingsImage[y*2], 2); // Left
						writeInteger(fos, compiledFrame.paddingsImage[y*2+1], 2); // Right
					}
					writeInteger(fos, compiledFrame.mapImage.length, 4);
					writeInteger(fos, compiledFrame.dataImage.length, 4);
					fos.write(compiledFrame.mapImage);
					fos.write(compiledFrame.dataImage);
				}
				
				if ((flag & 2) != 0){ // Shadow
					int height = frame.getHeight(Sprite.DATA_SHADOW);
					writeInteger(fos, frame.getWidth(Sprite.DATA_SHADOW), 2);
					writeInteger(fos, height, 2);
					writeInteger(fos, frame.getAnchorX(Sprite.DATA_SHADOW), 2);
					writeInteger(fos, frame.getAnchorY(Sprite.DATA_SHADOW), 2);
					writeInteger(fos, sizes[i*3+1], 4);
					writeInteger(fos, 0, 4);
					
					for (int y=0; y<height; ++y){
						writeInteger(fos, compiledFrame.paddingsShadow[y*2], 2); // Left
						writeInteger(fos, compiledFrame.paddingsShadow[y*2+1], 2); // Right
					}
					writeInteger(fos, compiledFrame.dataShadow.length, 4);
					fos.write(compiledFrame.dataShadow);
				}
				
				if ((flag & 4) != 0){ // Outline
					int height = frame.getHeight(Sprite.DATA_OUTLINE);
					writeInteger(fos, frame.getWidth(Sprite.DATA_OUTLINE), 2);
					writeInteger(fos, height, 2);
					writeInteger(fos, frame.getAnchorX(Sprite.DATA_OUTLINE), 2);
					writeInteger(fos, frame.getAnchorY(Sprite.DATA_OUTLINE), 2);
					writeInteger(fos, sizes[i*3+2], 4);
					writeInteger(fos, 0, 4);
					
					for (int y=0; y<height; ++y){
						writeInteger(fos, compiledFrame.paddingsOutline[y*2], 2); // Left
						writeInteger(fos, compiledFrame.paddingsOutline[y*2+1], 2); // Right
					}
					writeInteger(fos, compiledFrame.dataOutline.length, 4);
					fos.write(compiledFrame.dataOutline);
				}
				
			}
			
			fos.close();
			return true;
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();	
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	
	static private int getBrightness(int pixel){
		int red = pixel >> 16 & 0xff;
		int green = pixel >> 8 & 0xff;
		int blue = pixel & 0xff;
		int max = Math.max(red, Math.max(green, blue));
		return (max + red + green + blue) >> 2;
	}
	
	static public Sprite importFromImages(Sprite sprite, File[] files,
			Map<String, Integer> settings) throws IOException{
		if (sprite == null){
			sprite = new SMXSprite();
		}
		
		int palette = settings.get("palette");
		int playerMode = settings.get("playerMode");
		int playerPalette = settings.get("playerPalette");
		int playerPaletteTolerance = settings.get("playerPaletteTolerance");
		int cutOpaqueTolerance = settings.get("cutOpaqueTolerance");
		int frameRows = settings.get("rows");
		int frameColumns = settings.get("columns");
		
		int imageMode = settings.get("imageMode");
		boolean outline = (imageMode & IMAGE_MODE_OUTLINE_MASK) != 0;
		boolean smudge = (imageMode & IMAGE_MODE_SMUDGE_MASK) != 0;
		boolean cutOpaque = (imageMode & IMAGE_MODE_BACKGROUND_MASK) != 0;
		boolean csv = settings.get("csv") > 0;
		
		int stride = 1;
		imageMode &= IMAGE_MODE_MASK;
		if (imageMode == IMAGE_MODE_SEPARATESHADOW){
			stride = 2;
		}
		
		if (outline)
			++stride;
		if (smudge)
			++stride;
		
		//System.out.println(stride);
		
		boolean rgbMode = !(settings.get("hsvMode") > 0); 
		boolean autoCrop = settings.get("autoCrop") > 0;
		
		if (playerPalette < 0)
			sprite.playerMode = Sprite.PLAYER_PALETTE_DE;
		else
			sprite.playerMode = playerMode;
		
		Palette pal = Palette.getPalette(palette);
		Palette ppal = null;
		if (playerPalette >= 0 && playerPalette < 8){
			ppal = Palette.getPlayerPalette(playerMode, playerPalette);
		}
		
		final int framesPerImage = frameRows * frameColumns;

		for (int imageIndex=0; imageIndex<files.length; ++imageIndex){
			//System.out.println(files[index].getAbsolutePath());
			BufferedImage image = ImageIO.read(files[imageIndex]);

			int imageWidth = image.getWidth(null);
			int imageHeight = image.getHeight(null);
			int opaqueColor = image.getRGB(0, 0);
			
			int width = imageWidth / (frameColumns * stride);
			int height = imageHeight / frameRows;
			
			int[] anchors = new int[framesPerImage * 2];
			if (csv){ // Load anchors from csv
				String csvFile = files[imageIndex].getAbsolutePath() + ".CSV";
				BufferedReader fr = new BufferedReader(new FileReader(csvFile));
				for (int i=0; i<framesPerImage; ++i){
					String line = fr.readLine();
					if (line == null)
						break;
					
					String[] numbers = line.split("\\s*,\\s*");
					try{
						if (numbers.length > 0){
							anchors[i*2] = Integer.parseInt(numbers[0]);
							if (numbers.length > 1){
								anchors[i*2 + 1] = Integer.parseInt(numbers[1]);
							}
						}
					} catch (NumberFormatException e){
					}
				}
			}
			
			
			for (int index=0; index<framesPerImage; ++index){
				
				int x0 = (index % frameColumns) * width * stride;
				int y0 = (index / frameColumns) * height;
				
				Sprite.Frame frame = sprite.createFrame();
				frame.palette = palette;
				frame.create(width, height);
				if (csv){
					for (int TYPE : Sprite.DATA_TYPES)
						frame.setAnchor(TYPE, anchors[index*2], anchors[index*2 + 1]);
				} else {
					for (int TYPE : Sprite.DATA_TYPES)
						frame.setAnchor(TYPE, width/2, height/2);
				}
				
				if (imageMode == IMAGE_MODE_SHADOWONLY){ // All as Shadow
					for (int y=0; y<height; ++y){
						for (int x=0; x<width; ++x){
							int pixel = image.getRGB(x+x0, y+y0);
							if (cutOpaque) { // By Brightness, brighter is thicker
								pixel = Math.max(pixel >> 16 & 0xff,
										Math.max(pixel & 0xff, pixel >> 8 & 0xff));
							} else {
								pixel = pixel >> 24 & 0xff;
							}
							frame.setPixel(Sprite.DATA_SHADOW, x, y, pixel);
						}
					}				
					
				} else {
					// By Alpha
					if (imageMode == IMAGE_MODE_BYALPHA && ppal != null){
						for (int y=0; y<height; ++y){
							for (int x=0; x<width; ++x){
								int pixel = image.getRGB(x+x0, y+y0);
								if (cutOpaque)
									if (Palette.distance(pixel, opaqueColor) <= cutOpaqueTolerance)
										continue;
								
								int alpha = pixel >> 24 & 0xff;
								if (alpha == 255){ // As Player Color
									frame.setPixel(Sprite.DATA_IMAGE, x, y, 
											ppal.mapping(pixel, rgbMode) + Sprite.PIXEL_PLAYER_START);
								}else if (alpha >= 128){ // As Normal
									frame.setPixel(Sprite.DATA_IMAGE, x, y, pal.mapping(pixel, rgbMode));
								}else{ // As shadow
									int brightness = getBrightness(pixel);
									if (alpha < 128 && alpha * brightness < 32258){
										frame.setPixel(Sprite.DATA_SHADOW, x, y, alpha);
									}
								}
							}
						}
						
					}else{ // Normal
						for (int y=0; y<height; ++y){
							for (int x=0; x<width; ++x){
								int pixel = image.getRGB(x+x0, y+y0);
								if (cutOpaque)
									if (Palette.distance(pixel, opaqueColor) <= cutOpaqueTolerance)
										continue;
								
								int alpha = pixel >> 24 & 0xff;
								int brightness = getBrightness(pixel);
								if (alpha < 128 && alpha * brightness < 32258){
									frame.setPixel(Sprite.DATA_SHADOW, x, y, alpha);
								}else{
									if (ppal != null){
										int pixel1 = pal.mapping(pixel, rgbMode), pixel2 = ppal.mapping(pixel, rgbMode);
										if (Palette.distance(pixel, ppal.rgbs[pixel2], rgbMode) <=
												Palette.distance(pixel, pal.rgbs[pixel1], rgbMode) + playerPaletteTolerance)
											frame.setPixel(Sprite.DATA_IMAGE, x, y, pixel2 + Sprite.PIXEL_PLAYER_START);
										else
											frame.setPixel(Sprite.DATA_IMAGE, x, y, pixel1);
									}else{
										frame.setPixel(Sprite.DATA_IMAGE, x, y, pal.mapping(pixel, rgbMode));
									}
								}
							}
						}
						
					}
					
				}
				
				if (imageMode == IMAGE_MODE_SEPARATESHADOW){ // Separated Shadow
					x0 += width;
					for (int y=0; y<height; ++y){
						for (int x=0; x<width; ++x){
							int pixel = image.getRGB(x+x0, y+y0);
							if (cutOpaque) { // By Brightness, brighter is thicker
								pixel = Math.max(pixel >> 16 & 0xff,
										Math.max(pixel & 0xff, pixel >> 8 & 0xff));
							} else {
								pixel = pixel >> 24 & 0xff;
							}
							frame.setPixel(Sprite.DATA_SHADOW, x, y, pixel);
						}
					}
				}
				
				if (!outline){
					frame.remove(Sprite.DATA_OUTLINE);
				}else{
					x0 += width;
					for (int y=0; y<height; ++y){
						for (int x=0; x<width; ++x){
							int pixel = image.getRGB(x+x0, y+y0);
							
							if (cutOpaque)
								pixel &= 0xff;
							else
								pixel = pixel >> 24 & 0xff;
										
							if (pixel >= 128){
								frame.setPixel(Sprite.DATA_OUTLINE, x, y, 0);
							}
						}
					}
				}
				
				if (!smudge){
					frame.remove(Sprite.DATA_SMUDGE);
				}else{
					x0 += width;
					for (int y=0; y<height; ++y){
						for (int x=0; x<width; ++x){
							int pixel = image.getRGB(x+x0, y+y0);
							if (cutOpaque)
								pixel &= 0xff;
							else
								pixel = pixel >> 24 & 0xff;
						
							frame.setPixel(Sprite.DATA_SMUDGE, x, y, pixel << 2);
						}
					}
				}
				
				if (autoCrop) {
					for (int TYPE : Sprite.DATA_TYPES) 
						frame.crop(TYPE);
				}
				
				sprite.frames.add(frame);
				
				MainFrame.setProcessString(String.format("Loading %d/%d ...", 
						framesPerImage * imageIndex + index, files.length * framesPerImage));
				
			}
			
		}
		
		return sprite;
	}
	
	static public int getImageModeStride(int imageMode){
		int stride = 1;
		
		if ((imageMode & IMAGE_MODE_MASK) == IMAGE_MODE_SEPARATESHADOW)
			++stride;
		if ((imageMode & IMAGE_MODE_OUTLINE_MASK) != 0)
			++stride;
		if ((imageMode & IMAGE_MODE_SMUDGE_MASK) != 0)
			++stride;
		
		return stride;
	}
	
	static public int exportToImages(Sprite sprite, File file, Map<String, Integer> settings,
			int[] frameIds) throws IOException{
		
		if (sprite.getFrameCount() <= 0)
			return 0;
		
		String fileName = file.getName();
		String[] fileSegments = fileName.split("\\.");
		String fileFormat;
		String filePath = file.getAbsolutePath();
		if (fileSegments.length <= 1){ // No postfix
			fileFormat = "PNG";
		}else{
			fileFormat = fileSegments[fileSegments.length - 1].toUpperCase();
			if (fileFormat.equals("BMP") || fileFormat.equals("JPG")
					|| fileFormat.equals("PNG") || fileFormat.equals("GIF")){
				filePath = filePath.substring(0, filePath.length() - fileFormat.length() - 1);
			}else{
				fileFormat = "PNG";
			}
		}
		
		int imageMode = settings.get("imageMode");
		int anchorMode = settings.get("anchorMode");
		int rows = settings.get("rows");
		int columns = settings.get("columns");
		int padding = settings.get("padding");
		Color backgroundColor = new Color(settings.get("backgroundColor"));
		
		boolean outline = (imageMode & IMAGE_MODE_OUTLINE_MASK) != 0;
		boolean smudge = (imageMode & IMAGE_MODE_SMUDGE_MASK) != 0;
		boolean background = (imageMode & IMAGE_MODE_BACKGROUND_MASK) != 0;
		boolean csv = (anchorMode & ANCHOR_MODE_CSV_MASK) != 0;
		int stride = getImageModeStride(imageMode);
		imageMode &= IMAGE_MODE_MASK;
		anchorMode &= ANCHOR_MODE_MASK;

		SpritePreview preview = new SpritePreview();
		preview.setSprite(sprite);
		preview.playerColorId = settings.get("playerColor");
		preview.refresh();
		
		if (settings.get("selectedOnly") == 0){
			frameIds = new int[sprite.getFrameCount()];
			for (int i = 0; i < sprite.getFrameCount(); ++i){
				frameIds[i] = i;
			}
		}
		
		int perImage = rows * columns;
		int frameCount = frameIds.length;
		int imageCount = (int) Math.ceil(((double)frameCount) / perImage);
		
		int frameWidth = 0, frameHeight = 0;
		int frameX = 0, frameY = 0;
		BufferedImage im = null;
		Graphics gr = null;
		StringBuilder anchorList = csv ? new StringBuilder() : null;
		
		int offset = -perImage;
		int imageIndex = 0;
		for (int index = 0; index < frameCount; ++index){
			// Create a new image
			if (index >= offset + perImage){
				// Finish an image
				if (im != null){
					ImageIO.write((RenderedImage) im, fileFormat,
							new File(filePath + imageIndex + "." + fileFormat));
					if (csv){
						FileOutputStream fos = 
								new FileOutputStream(filePath + imageIndex + "." + fileFormat + ".CSV");
						fos.write(anchorList.toString().getBytes());
						fos.close();
					}
					++imageIndex;
				}
				
				offset += perImage;
				int limit = Math.min(perImage, frameCount - offset);
				
				// Calculate size of strip frames
				switch (anchorMode){
				case ANCHOR_MODE_TIGHT: // None
					frameWidth = 0;
					frameHeight = 0;
					for (int i = 0; i < limit; ++i){
						Sprite.Frame frame = sprite.getFrame(frameIds[i + offset]);
						frameWidth = Math.max(frameWidth, frame.getWidth()); 
						frameHeight = Math.max(frameHeight, frame.getHeight());
					}
					break;
				case ANCHOR_MODE_ALIGN: // Aligned
					int ml = 0, mr = 0, mu = 0, md = 0;
					for (int i = 0; i < limit; ++i){
						Sprite.Frame frame = sprite.getFrame(frameIds[i + offset]);
						int anchorX = frame.getAnchorX(), anchorY = frame.getAnchorY();
						ml = Math.max(ml, anchorX);
						mu = Math.max(mu, anchorY);
						mr = Math.max(mr, frame.getWidth() - anchorX);
						md = Math.max(md, frame.getHeight() - anchorY);
					}
					frameX = ml;
					frameY = mu;
					frameWidth = ml + mr; 
					frameHeight = mu + md;
					break;
				case ANCHOR_MODE_CENTER: // Aligned, center
					for (int i = 0; i < limit; ++i){
						Sprite.Frame frame = sprite.getFrame(frameIds[i + offset]);
						int anchorX = frame.getAnchorX(), anchorY = frame.getAnchorY();
						frameWidth = Math.max(frameWidth, Math.max(anchorX, frame.getWidth() - anchorX));
						frameHeight = Math.max(frameHeight, Math.max(anchorY, frame.getHeight() - anchorY));
					}
					frameX = frameWidth;
					frameY = frameHeight;
					frameWidth <<= 1; 
					frameHeight <<= 1;
					break;
				}
				
				frameWidth += padding * 2;
				frameHeight += padding * 2;
				
				im = new BufferedImage(frameWidth * columns * stride,
						frameHeight * rows, BufferedImage.TYPE_INT_ARGB);
				gr = im.getGraphics();
				gr.setColor(Color.WHITE);
				gr.setPaintMode();
				
				if (csv)
					anchorList.delete(0, anchorList.length());
				
			}
			
			int fx0 = frameWidth * ((index - offset) % columns) * stride;
			int fy0 = frameHeight * ((index - offset) / columns);
			int x0 = fx0 + padding, y0 = fy0 + padding;
			
			Sprite.Frame frame = sprite.getFrame(frameIds[index]);
			
			if (anchorMode == ANCHOR_MODE_TIGHT){
				x0 += frame.getAnchorX(); y0 += frame.getAnchorY();
			} else {
				x0 += frameX; y0 += frameY;
			}
			if (csv){
				anchorList.append(x0 - fx0)
						.append(", ").append(y0 - fy0).append("\r\n");
			}
			
			// Wait for Notification
			while (!preview.getFrameStatus(frameIds[index]));
			
			// Draw background color
			if (background){
				gr.setColor(backgroundColor);
				gr.fillRect(fx0, fy0, frameWidth, frameHeight);
				gr.setColor(Color.WHITE);
			}
			
			// Draw lower Shadow
			if (imageMode != IMAGE_MODE_SEPARATESHADOW){
				gr.drawImage(preview.getFrameImage(index, Sprite.DATA_SHADOW), 
						x0 - frame.getAnchorX(Sprite.DATA_SHADOW),
						y0 - frame.getAnchorY(Sprite.DATA_SHADOW),
						null);
			}
			
			// Adjust Alpha and draw main image
			if (imageMode == IMAGE_MODE_BYALPHA){
				int x1 = x0 - frame.getAnchorX(Sprite.DATA_SHADOW);
				int y1 = y0 - frame.getAnchorY(Sprite.DATA_SHADOW);
				for (int y = 0; y < frame.getHeight(Sprite.DATA_SHADOW); ++y){
					for (int x = 0; x < frame.getWidth(Sprite.DATA_SHADOW); ++x){
						int pixel = frame.getPixel(Sprite.DATA_SHADOW, x, y); 
						if (pixel != Sprite.PIXEL_NULL)
							if (pixel >= 254)
								im.setRGB(x + x1, y + y1, 0xfd000000);
					}
				}
			
				gr.drawImage(preview.getFrameImage(index, Sprite.DATA_IMAGE), 
						x0 - frame.getAnchorX(Sprite.DATA_IMAGE),
						y0 - frame.getAnchorY(Sprite.DATA_IMAGE),
						null);
			
				x1 = x0 - frame.getAnchorX(Sprite.DATA_IMAGE);
				y1 = y0 - frame.getAnchorY(Sprite.DATA_IMAGE);
				BufferedImage frameImage = preview.getFrameImage(index, Sprite.DATA_IMAGE); 
				for (int y = 0; y < frame.getHeight(Sprite.DATA_IMAGE); ++y){
					for (int x = 0; x < frame.getWidth(Sprite.DATA_IMAGE); ++x){
						int pixel = frame.getPixel(Sprite.DATA_IMAGE, x, y); 
						if (pixel != Sprite.PIXEL_NULL){
							if (pixel < Sprite.PIXEL_PLAYER_START) // Normal Pixel
								im.setRGB(x + x1, y + y1, frameImage.getRGB(x, y) & 0xffffff | 0xfe000000);
						}
					}
				}
				
			}else if (imageMode != IMAGE_MODE_SHADOWONLY){
				
				gr.drawImage(preview.getFrameImage(index, Sprite.DATA_IMAGE), 
						x0 - frame.getAnchorX(Sprite.DATA_IMAGE),
						y0 - frame.getAnchorY(Sprite.DATA_IMAGE),
						null);
				
			}
			
			// Draw separated shadow
			if (imageMode == IMAGE_MODE_SEPARATESHADOW){
				fx0 += frameWidth; x0 += frameWidth;
				if (background){
					gr.fillRect(fx0, fy0, frameWidth, frameHeight);
					gr.drawImage(preview.getFrameImage(index, Sprite.DATA_SHADOW), 
							x0 - frame.getAnchorX(Sprite.DATA_SHADOW),
							y0 - frame.getAnchorY(Sprite.DATA_SHADOW),
							null);
					gr.setXORMode(Color.BLACK);
					gr.fillRect(fx0, fy0, frameWidth, frameHeight);
					gr.setPaintMode();
					gr.setColor(Color.WHITE);
				}else{
					gr.drawImage(preview.getFrameImage(index, Sprite.DATA_SHADOW), 
							x0 - frame.getAnchorX(Sprite.DATA_SHADOW),
							y0 - frame.getAnchorY(Sprite.DATA_SHADOW),
							null);
				}
			}
			
			if (outline){
				fx0 += frameWidth; x0 += frameWidth;
				if (background){
					gr.setColor(Color.BLACK);
					gr.fillRect(fx0, fy0, frameWidth, frameHeight);
					gr.setPaintMode();
					gr.setColor(Color.WHITE);
				}
				gr.drawImage(preview.getFrameImage(index, Sprite.DATA_OUTLINE), 
						x0 - frame.getAnchorX(Sprite.DATA_OUTLINE),
						y0 - frame.getAnchorY(Sprite.DATA_OUTLINE),
						null);
			}
			if (smudge){
				fx0 += frameWidth; x0 += frameWidth;
				if (background){
					gr.fillRect(fx0, fy0, frameWidth, frameHeight);
					gr.drawImage(preview.getFrameImage(index, Sprite.DATA_SMUDGE), 
							x0 - frame.getAnchorX(Sprite.DATA_SMUDGE),
							y0 - frame.getAnchorY(Sprite.DATA_SMUDGE),
							null);
					gr.setXORMode(Color.BLACK);
					gr.fillRect(fx0, fy0, frameWidth, frameHeight);
					gr.setPaintMode();
					gr.setColor(Color.WHITE);
				}else{
					gr.drawImage(preview.getFrameImage(index, Sprite.DATA_SMUDGE), 
							x0 - frame.getAnchorX(Sprite.DATA_SMUDGE),
							y0 - frame.getAnchorY(Sprite.DATA_SMUDGE),
							null);
				}
			}
			
		}
		String finalfileName;
		if (imageCount == 1){
			finalfileName = filePath + "." + fileFormat; 
		}else{
			finalfileName = filePath + imageIndex + "." + fileFormat;
		}
		ImageIO.write((RenderedImage) im, fileFormat, new File(finalfileName));
		if (csv){
			FileOutputStream fos = new FileOutputStream(finalfileName + ".CSV");
			fos.write(anchorList.toString().getBytes());
			fos.close();
		}
		
		return imageCount;
	}
	
	
	static public int readInteger(InputStream is, int length, boolean signed) throws IOException {
		byte[] bytes = new byte[length];
		is.read(bytes, 0, length);
		if (length == 1){
			if (signed)
				return (int)bytes[0];
			else
				return bytes[0] & 0xff;
			
		}else if (length == 2){
			if (signed)
				return (((bytes[0] & 0xff) | (bytes[1] & 0xff) << 8) + 0x8000 & 0xffff) - 0x8000;
			else
				return ((bytes[0] & 0xff) | (bytes[1] & 0xff) << 8);
			
		}else if (length == 4){
			if (signed)
				return (((bytes[0] & 0xff) | (bytes[1] & 0xff) << 8 |
						(bytes[2] & 0xff) << 16 | (bytes[3] & 0xff) << 24)
						+ 0x80000000) & 0xffffffff - 0x80000000;
			else
				return (bytes[0] & 0xff) | (bytes[1] & 0xff) << 8 |
						(bytes[2] & 0xff) << 16 | (bytes[3] & 0xff) << 24;
		}
		return 0;
		
	}
	static public int readInteger(InputStream is, int length) throws IOException {
		return readInteger(is, length, true);
	}
	
	static public int writeInteger(OutputStream os, int number, int length){
		byte[] bytes = new byte[length];
		if (length == 1){
			bytes[0] = (byte) number;
			
		}else if (length == 2){
			bytes[0] = (byte) number;
			bytes[1] = (byte) (number >> 8);
			
		}else if (length == 4){
			bytes[0] = (byte) number;
			bytes[1] = (byte) (number >> 8);
			bytes[2] = (byte) (number >> 16);
			bytes[3] = (byte) (number >> 24);
		}else{
			return 0;
		}
		try {
			os.write(bytes);
		} catch (IOException e) {
			e.printStackTrace();
			return 0;
		}
		return length;
	}

}
