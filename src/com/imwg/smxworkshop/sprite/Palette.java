package com.imwg.smxworkshop.sprite;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Palette {
	
	int[] rgbs;
	private boolean cacheMode = false;
	private int[] cache;
	
	public static Palette[] palettes;
	public static String[] paletteNames;
	private static Palette[] oldPlayerPalettes1, oldPlayerPalettes2;
	private static Map<String, int[]> mappings = new HashMap<String, int[]>();
	private static int PLAYER_PALETTE_START = 55;
	
	public Palette(String filename) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(filename));
		br.readLine(); // Header
		br.readLine(); // Version
		
		int count = Integer.parseInt(br.readLine());
		
		int alpha = 0xff000000;
		if (filename.endsWith(".palx")){ // With one alpha value
			alpha = Integer.parseInt(br.readLine().substring(7)) << 24;
		}
		
		rgbs = new int[count];
		for (int i=0; i<count; ++i){
			String[] figures = br.readLine().split(" ");
			int rgb = Integer.parseInt(figures[0]);
			for (int j=1; j<3; ++j){
				rgb = rgb << 8 | Integer.parseInt(figures[j]);
			}
			rgbs[i] = rgb | alpha;
		}
		
		br.close();
	}
	
	public Palette(int[] rgbs){
		this.rgbs = rgbs;
	}
	public Palette(Palette palette, int start, int length){
		this.rgbs = new int[length];
		for (int i=0; i<length; ++i){
			this.rgbs[i] = palette.rgbs[start + i];
		}
	}
	public Palette(Palette palette){
		this(palette, 0, palette.getColorCount());
	}
	public Palette() {
		this.rgbs = new int[0];
	}
	
	public int getColorCount(){
		return rgbs.length;
	}
	
	public int getColor(int index){
		return rgbs[index];
	}
	public void setColor(int index, int rgb){
		rgbs[index] = rgb;
	}
	public void setColor(int index, int red, int green, int blue){
		rgbs[index] = color(red, green, blue);
	}
	

	public int mapping(int red, int green, int blue, boolean rgbmode){
		if (cacheMode){
			int rgb = color(red, green, blue);
			if (cache[rgb] >= 0)
				return cache[rgb];
		}
		
		int len = rgbs.length;
		double distance = Double.POSITIVE_INFINITY;
		int mapto = -1;
		for (int j=0; j<len; ++j){
			double distance1 = distance(red, green, blue, rgbs[j], rgbmode);
			if (distance1 < distance){
				if (distance <= 0)
					return j;
				
				mapto = j; distance = distance1;
			}
		}
		
		if (cacheMode){
			int rgb = color(red, green, blue);
			cache[rgb] = mapto;
		}
		return mapto;
	}
	public int mapping(double red, double green, double blue, boolean rgbmode){
		return mapping((int)Math.round(red), (int)Math.round(green), (int)Math.round(blue), true);
	}
	public int mapping(int argb, boolean rgbmode){
		return mapping((argb >> 16) & 0xff, (argb >> 8) & 0xff, argb & 0xff, rgbmode);
	}
	public int mapping(int red, int green, int blue){
		return mapping(red, green, blue, true);
	}
	public int mapping(int argb){
		return mapping((argb >> 16) & 0xff, (argb >> 8) & 0xff, argb & 0xff, true);
	}
	public int mapping(double red, double green, double blue){
		return mapping(red, green, blue, true);
	}
	
	/**
	 * Enable or disable cache mode. If enabled, it will allocate 16MB memory for the palette mapping.
	 * Disable will free that.
	 * @param cached Enabled or disabled.
	 */
	public void setCacheMode(boolean cached){
		if (cacheMode != cached){
			if (cacheMode){
				cache = null;
			}else{
				cache = new int[0x1000000];
				for (int i=0; i<0x1000000; ++i)
					cache[i] = -1;
			}
			cacheMode = cached;
		}
	}
	
	
	public static Palette merge(Palette... palettes){
		int count = palettes.length;
		int length = 0;
		for (int i=0; i<count; ++i){
			length += palettes[i].rgbs.length;
		}
		int[] rgbs = new int[length];
		int index = 0;
		for (int i=0; i<count; ++i){
			Palette palette = palettes[i];
			int length1 = palette.rgbs.length;
			for (int j=0; j<length1; ++j){
				rgbs[index++] = palette.rgbs[j];
			}
		}		
		return new Palette(rgbs);
	}	
	
	public static double distance(int red1, int green1, int blue1, 
			int red2, int green2, int blue2, boolean rgbmode){
		if (rgbmode){
			return Math.sqrt(Math.pow(red1 - red2, 2) + Math.pow(green1 - green2, 2)
					+ Math.pow(blue1 - blue2, 2));
		}else{
			float[] hsv1 = new float[3], hsv2 = new float[3];
			Color.RGBtoHSB(red1, green1, blue1, hsv1);
			Color.RGBtoHSB(red2, green2, blue2, hsv2);
			float dHue = Math.abs(hsv1[0] - hsv2[0]);
			return Math.sqrt(Math.pow(Math.min(dHue, 1 - dHue), 2)
					+ Math.pow(hsv1[1] - hsv2[1], 2) + Math.pow(hsv1[2] - hsv2[2], 2)) * 255;
		}
	}
	public static double distance(int color1, int color2, boolean rgbmode){
		return distance(color1 >> 16 & 0xff, color1 >> 8 & 0xff, color1 & 0xff,
				color2 >> 16 & 0xff, color2 >> 8 & 0xff, color2 & 0xff, rgbmode);
	}
	public static double distance(int red1, int green1, int blue1, 
			int red2, int green2, int blue2){
		return distance(red1, green1, blue1, red2, green2, blue2, true);
	}
	public static double distance(int color1, int color2){
		return distance(color1, color2, true);
	}
	private static double distance(int red1, int green1, int blue1, 
			int color2, boolean rgbmode){
		return distance(red1, green1, blue1, 
				color2 >> 16 & 0xff, color2 >> 8 & 0xff, color2 & 0xff, rgbmode);
	}
	
	private static final int color(int red, int green, int blue){
		return Math.max(0, Math.min(255, red)) << 16 
				| Math.max(0, Math.min(255, green)) << 8 
				| Math.max(0, Math.min(255, blue));
	}
	
	
	public static void loadPalettes(){
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader("palettes/palettes.conf"));

			palettes = new Palette[288];
			paletteNames = new String[288];
			
			String line;
			while ((line = br.readLine()) != null){
				String[] keyValue = line.replaceFirst("\\/\\/.+", "").split(",");
				if (keyValue.length == 2){
					try{
						int index = Integer.parseInt(keyValue[0].trim());
						if (index >= 0){
							palettes[index] = new Palette("palettes/"+keyValue[1]);
							paletteNames[index] = keyValue[1];
						}
					}catch (NumberFormatException e){}
				}
			}
			
			oldPlayerPalettes2 = new Palette[8];
			for (int i=0; i<8; ++i){
				oldPlayerPalettes2[i] = new Palette(palettes[0], 16 + 16 * i, 8);
			}
			
			oldPlayerPalettes1 = new Palette[]{
				new Palette(palettes[256], 16, 10), // Blue
				new Palette(palettes[256], 32, 10), // Red
				new Palette(palettes[256], 96, 10), // Green
				new Palette(palettes[256], 48, 10), // Yellow
				new Palette(palettes[256], 80, 10), // Orange
				new Palette(palettes[256], 128, 10), // Cyan
				new Palette(palettes[256], 64, 10), // Purple(Brown)
				new Palette(palettes[256], 112, 10), // Gray
			};
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static int[] getMappingArray(Palette srcPal, Palette dstPal, boolean cached){
		String key = srcPal + ">" + dstPal;
		if (cached && mappings.containsKey(key)){
			return mappings.get(key);
			
		}else{
			int srcLen = srcPal.rgbs.length;
			int[] map = new int[srcLen];

			for (int i=0; i<srcLen; ++i){ // Mapping to Nearest Color
				int rgb = srcPal.rgbs[i];
				map[i] = dstPal.mapping(rgb);
			}
			if (cached)
				mappings.put(key, map);
			
			return map;
		}
	}
	
	public static Palette getPlayerPalette(int version, int player){
		switch(version){
		default:
		case Sprite.PLAYER_PALETTE_DE:
			return palettes[Palette.PLAYER_PALETTE_START + player];
		case Sprite.PLAYER_PALETTE_AOE:
			return oldPlayerPalettes1[player];
		case Sprite.PLAYER_PALETTE_AOK:
			return oldPlayerPalettes2[player];
		}
	}
	
	public static Palette trueColorPalette(){
		return new Palette(){
			@Override
			public int getColorCount(){
				return 0x1000000;
			}
			@Override
			public int getColor(int index){
				return index;
			}
			@Override
			public int mapping(int red, int green, int blue, boolean rgbmode){
				return red << 16 | green << 8 | blue;
			}
			@Override
			public void setCacheMode(boolean cached){
				
			}
		};
	}
	
}
