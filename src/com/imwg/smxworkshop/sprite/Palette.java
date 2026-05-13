package com.imwg.smxworkshop.sprite;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Palette {
		
	private int[] rgbs;
	private boolean cacheMode = false;
	private int[] cache;
	
	public static final int ORIGINAL_PALETTE_COUNT = 513;
	public static final Palette playerOriginalPalette = new Palette(new int[17]);
	// a color,depth->index map (17 x 16)
	private static final byte[] sldImportMap = new byte[] {
		15,15,15,15,30,30,30,30,29,29,28,28,14,14,14,14,
		31,31,31,30,30,30,30,30,29,29,28,28,27,13,13,13,
		31,31,31,30,30,30,30,29,29,29,28,28,27,11,11,11,
		47,47,46,46,46,30,30,29,29,28,28,27,26,26,9,9,
		47,47,46,46,46,45,45,44,44,43,42,27,26,25,7,7,
		47,47,46,46,46,45,45,44,44,43,42,41,40,23,22,5,
		63,62,62,61,61,60,59,58,43,43,42,40,39,38,20,3,
		63,62,62,61,61,60,59,58,58,56,55,54,53,52,35,1,
		63,62,62,61,61,60,59,58,57,57,55,54,53,82,97,0,
		63,62,62,61,75,74,74,73,72,71,70,69,99,98,98,97,
		79,78,77,76,76,75,74,73,87,86,101,100,100,99,98,97,
		79,78,77,77,76,90,89,89,103,102,102,101,100,99,99,98,
		79,94,93,92,91,106,105,104,104,103,102,101,101,100,99,99,
		95,94,93,108,107,106,106,105,104,103,103,102,101,101,100,99,
		95,110,109,108,107,107,106,105,105,104,103,103,102,101,100,99,
		111,110,109,109,108,107,107,106,105,105,104,103,102,101,101,100,
		127,126,125,124,123,122,121,120,119,118,117,116,115,114,113,112
	};
	
	private static Palette[] palettes;
	private static String[] paletteNames;
	private static Palette[] aoePlayerPalettes, aokPlayerPalettes;
	private static Map<String, int[]> mappings = new HashMap<String, int[]>();
	private static Palette[] customPalettes;
	private static String[] customPaletteNames;
	private static Palette trueColorPalette = new TrueColorPalette();
	private static Palette[] playerPaletteOriginalMap;
	private static short[] playerPaletteDEDepthMap;
	
	private static final int CUSTOM_PALETTE_LIMIT = 1024;
	private static final int PLAYER_PALETTE_START = 55;
	
	
	public Palette(String filename) throws IOException{
		String name1 = filename.toUpperCase();
		if (name1.endsWith(".ACT")){ // 3-byte binary
			FileInputStream is = new FileInputStream(filename);
			int size = is.available() / 3;
			this.rgbs = new int[size];
			byte[] bytes = new byte[3];
			for (int i=0; i<size; ++i){
				is.read(bytes);
				rgbs[i] = (bytes[0] & 0xff) << 16 | (bytes[1] & 0xff) << 8 | bytes[2] & 0xff | 0xff000000;
			}
			is.close();
			
		}else{ // Text
			loadFromFile(filename);
		}
	}
	
	public Palette(int[] rgbs){
		this.rgbs = rgbs.clone();
	}
	public Palette(int count){
		this.rgbs = new int[count];
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
		setColor(index, color(red, green, blue));
	}

	public int mapping(int red, int green, int blue, boolean rgbmode){
		if (cacheMode){
			int rgb = color(red, green, blue) & 0xffffff;
			if (cache[rgb] >= 0)
				return cache[rgb];
		}
		
		int len = rgbs.length;
		double distance = Double.POSITIVE_INFINITY;
		int mapto = -1;
		for (int j=0; j<len; ++j){
			int rgb = rgbs[j];
			if (rgb == 0) {
				continue;
			}
			double distance1 = distance(red, green, blue, rgb, rgbmode);
			if (distance1 < distance){
				if (distance <= 0)
					return j;
				
				mapto = j; distance = distance1;
			}
		}
		
		if (cacheMode){
			int rgb = color(red, green, blue) & 0xffffff;
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
	
	/**
	 * Save as a palette file.
	 * @param fileName Name of file to save.
	 * @param type File type.
	 * @return Succeed or not.
	 */
	public boolean saveAsFile(String fileName, String type){
		try {
			BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
			bw.write("JASC-PAL\r\n");
			bw.write("0100\r\n");
			bw.write(Integer.toString(this.rgbs.length) + "\r\n");
			for (int i = 0; i < this.rgbs.length; ++i){
				int color = this.rgbs[i];
				if (color == 0) {
					bw.write("-1 -1 -1\r\n");
				}else {
					bw.write(String.format("%d %d %d\r\n",
							(color >> 16) & 0xff, (color >> 8) & 0xff, color & 0xff));
				}
			}
			bw.flush();
			bw.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
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

	public static int mixColor(int color1, int color2, double factor) {
		int r1 = (color1 >> 16) & 0xff, g1 = (color1 >> 8) & 0xff, b1 = color1 & 0xff;
		int r2 = (color2 >> 16) & 0xff, g2 = (color2 >> 8) & 0xff, b2 = color2 & 0xff;
		int r = (int)((1 - factor) * r1 + factor * r2);
		int g = (int)((1 - factor) * g1 + factor * g2);
		int b = (int)((1 - factor) * b1 + factor * b2);
		return r << 16 | g << 8 | b | 0xff000000;
	}
	
	private static final int color(int red, int green, int blue){
		return Math.max(0, Math.min(255, red)) << 16 
				| Math.max(0, Math.min(255, green)) << 8 
				| Math.max(0, Math.min(255, blue)) | 0xff000000;
	}
	
	
	public static void loadPalettes(){
		BufferedReader br;
		try {
			// Load Original Palettes
			br = new BufferedReader(new FileReader("palettes/palettes.conf"));
			palettes = new Palette[ORIGINAL_PALETTE_COUNT];
			paletteNames = new String[ORIGINAL_PALETTE_COUNT];
			
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
			
			aokPlayerPalettes = new Palette[8];
			for (int i=0; i<8; ++i){
				aokPlayerPalettes[i] = new Palette(palettes[0], 16 + 16 * i, 8);
			}
			
			aoePlayerPalettes = new Palette[]{
				new Palette(palettes[256], 16, 10), // Blue
				new Palette(palettes[256], 32, 10), // Red
				new Palette(palettes[256], 96, 10), // Green
				new Palette(palettes[256], 48, 10), // Yellow
				new Palette(palettes[256], 80, 10), // Orange
				new Palette(palettes[256], 128, 10), // Cyan
				new Palette(palettes[256], 64, 10), // Purple(Brown)
				new Palette(palettes[256], 112, 10), // Gray
			};
			
			// Load Custom Palettes
			File customDirectory = new File("palettes/custom/");
			if (customDirectory.exists()){
				String [] list = customDirectory.list();
				final int count = Math.min(CUSTOM_PALETTE_LIMIT, list.length);
				customPalettes = new Palette[count];
				customPaletteNames = new String[count];
				for (int i=0; i<count; ++i){
					String fileName = "Custom/" + list[i];
					customPalettes[i] = new Palette("palettes/" + fileName);
					customPaletteNames[i] = fileName;
				}
			}else {
				customPalettes = new Palette[0];
			}
			
			// RGB555 Palette
			paletteNames[512] = "RGB 555";
			palettes[512] = new RGB555Palette();
			
			int[] brightnesses = new int[] {
				127,119,110,102, 93, 85, 76, 68, 59, 51, 42, 34, 25, 17,  8,  0,
				127,121,114,108,102, 96, 89, 83, 76, 70, 63, 57, 51, 45, 38, 32,
				127,123,119,115,110,106,102, 98, 93, 89, 85, 81, 76, 72, 68, 64,
				127,128,127,128,127,128,127,128,127,128,127,128,127,128,127,128,
				127,132,136,140,144,149,153,158,161,166,170,175,179,183,187,192,
				127,134,140,147,153,160,166,173,178,185,191,198,204,211,217,224,
				127,136,144,153,161,170,178,187,195,204,212,221,229,238,246,255
			};
			Palette grayscale = palettes[62];
			// DE Player Base Palette
			for (int i = 0; i < 16; ++i) {
				int k = i * 17;
				playerOriginalPalette.setColor(i, color(k, k, k));
			}
			playerOriginalPalette.setColor(16, grayscale.getColor(127));
			
			// DE Player Color Base Color Palette
			playerPaletteOriginalMap = new Palette[Sprite.NUM_OF_PLAYER_PALETTES];
			playerPaletteOriginalMap[Sprite.PLAYER_PALETTE_AOE] = aoePlayerPalettes[7];
			playerPaletteOriginalMap[Sprite.PLAYER_PALETTE_AOK] = aokPlayerPalettes[7];
			int[] dePlayerOriginals = new int[128];
			for (int i = 0; i < 112; ++i) {
				int v = brightnesses[i];
				dePlayerOriginals[i] = color(v, v, v);
			}
			for (int i = 112; i < 128; ++i) {
				dePlayerOriginals[i] = grayscale.getColor(127);
			}
			Palette dePlayerPaletteOriginalMap = new Palette(dePlayerOriginals);
			playerPaletteOriginalMap[Sprite.PLAYER_PALETTE_AOEDE] = dePlayerPaletteOriginalMap;
			playerPaletteOriginalMap[Sprite.PLAYER_PALETTE_DE] = dePlayerPaletteOriginalMap;
			
			playerPaletteDEDepthMap = new short[] {
				255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255, 255,   0,
				255, 252, 250, 247, 243, 239, 235, 229, 223, 214, 204, 190, 168, 139,  92,   0,
				255, 250, 245, 239, 233, 226, 217, 208, 198, 185, 169, 150, 127,  97,  56,   0,
				255, 245, 236, 226, 215, 203, 191, 176, 162, 145, 127, 107,  85,  59,  31,   0,
				255, 241, 228, 214, 200, 185, 169, 153, 137, 119, 101,  82,  63,  43,  22,   0,
				255, 239, 224, 208, 193, 176, 160, 143, 127, 110,  92,  74,  56,  38,  19,   0,
				255, 238, 221, 204, 187, 170, 153, 136, 119, 102,  85,  68,  51,  34,  17,   0,
				255, 238, 221, 204, 187, 170, 153, 136, 119, 102,  85,  68,  51,  34,  17,   0
			};
			// generatePlayerColorMap();
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static int getCustomPaletteCount(){
		return customPalettes.length;
	}
	
	public static int[] getMappingArray(Palette srcPal, Palette dstPal, boolean cached){
		String key = srcPal + ">" + dstPal;
		if (cached && mappings.containsKey(key)){
			return mappings.get(key);
			
		}else{
			int srcLen = srcPal.getColorCount();
			int[] map = new int[srcLen];

			for (int i=0; i<srcLen; ++i){ // Mapping to Nearest Color
				int rgb = srcPal.getColor(i);
				map[i] = dstPal.mapping(rgb);
			}
			if (cached)
				mappings.put(key, map);
			
			return map;
		}
	}
	public static int[] getMappingArray(Palette srcPal, Palette dstPal){
		return getMappingArray(srcPal, dstPal, false);
	}
	
	public static int getDEPlayerPaletteIndex(int color, int depth) {
		int colid = playerOriginalPalette.mapping(color);
		int depid = Math.round(depth / 17.0f);
		return Palette.sldImportMap[colid * 16 + depid];
	}
	
	public static int getPlayerPaletteOriginal(int playerPalette, int index) {
		return playerPaletteOriginalMap[playerPalette].getColor(index);
	}
	public static int getPlayerPaletteDepth(int playerPalette, int index) {
		if (playerPalette == Sprite.PLAYER_PALETTE_AOEDE || playerPalette == Sprite.PLAYER_PALETTE_DE) {
			return playerPaletteDEDepthMap[index];
		}
		return 255;
	}
	
	public static Palette getPalette(int index){
		if (index == -1)
			return trueColorPalette;
		else if (index >= 0)
			if (index < ORIGINAL_PALETTE_COUNT)
				return palettes[index];
			else if ((index -= ORIGINAL_PALETTE_COUNT) < customPalettes.length)
				return customPalettes[index];
		return null;
	}
	public static String getPaletteName(int index){
		if (index == -1)
			return "True Color Palette";
		else if (index >= 0)
			if (index < ORIGINAL_PALETTE_COUNT)
				return paletteNames[index];
			else if ((index -= ORIGINAL_PALETTE_COUNT) < customPalettes.length)
				return customPaletteNames[index];
		return null;
	}
	
	public static Palette getPlayerPalette(int version, int player){
		switch(version){
		default:
		case Sprite.PLAYER_PALETTE_DE:
			return palettes[Palette.PLAYER_PALETTE_START + player];
		case Sprite.PLAYER_PALETTE_AOE:
			return aoePlayerPalettes[player];
		case Sprite.PLAYER_PALETTE_AOK:
			return aokPlayerPalettes[player];
		case Sprite.PLAYER_PALETTE_AOEDE:
			return palettes[256 + Palette.PLAYER_PALETTE_START + player];
		}
	}
	
	public void loadFromFile(String fileName) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		br.readLine(); // Header
		br.readLine(); // Version
		
		int count = Integer.parseInt(br.readLine());
		
		int alpha = 0xff000000;
		boolean includeAlpha = false;
		String firstLine = br.readLine();
		//if (name1.endsWith(".PALX")){
		if (firstLine.startsWith("$ALPHA")){ // With one alpha value
			alpha = Integer.parseInt(firstLine.substring(7)) << 24;
			includeAlpha = true;
		}
		
		rgbs = new int[count];
		for (int i=0; i<count; ++i){
			String line = (i == 0 && !includeAlpha) ?
				firstLine : br.readLine();
				
			if (line == null)
				break;
			
			line.replaceFirst("#.+$", "");
			String[] figures = line.split(" ");
			if (figures.length >= 3){
				int rgb = Integer.parseInt(figures[0]);
				if (rgb < 0) {
					rgbs[i] = 0;
				} else {
					for (int j=1; j<3; ++j){
						rgb = rgb << 8 | Integer.parseInt(figures[j]);
					}
					rgbs[i] = rgb | alpha;
				}
			}
		}
		
		br.close();
	}
	
	@SuppressWarnings("unused")
	static private void generatePlayerColorMap() {
		// generating
		Palette pt = Palette.getPlayerPalette(Sprite.PLAYER_PALETTE_DE, 0);
//		palettes[Palette.PLAYER_PALETTE_START + 7] = palettes[0];
		
		// Brightness Map
		System.out.println("Brightness Map");
		for (int y = 0; y < 7; ++y) {
			for (int x = 0; x < 16; ++x) {
				int color = pt.getColor(y * 16 + x);
				// brightness
				int b = color & 0xff, g = (color >> 8) & 0xff, r = (color >> 16) & 0xff; 
				int br = (r + g + b * 2) / 4;
				System.out.printf("%3d,", br);
			}
			System.out.println();
		}
		
		// Depth Map
		System.out.println("Depth Map");
		for (int i = 0; i < 112; ++i) {
			int color = pt.getColor(i);
			int b = color & 0xff, g = (color >> 8) & 0xff, r = (color >> 16) & 0xff;
			double saturation = 1.0 - (double) Math.min(Math.min(r, g), b) / Math.max(Math.max(r, g), b);
			if (Double.isNaN(saturation)) {
				saturation = 0.0;
			}
			playerPaletteDEDepthMap[i] = (short) (saturation * 255);
		}
		for (int i = 0; i < 128; ++i) {
			System.out.printf("%3d,", playerPaletteDEDepthMap[i]);
			if (i % 16 == 15) {
				System.out.println();
			}
		}
		
		// Mapping Test
		System.out.println("Mapping Test");
		int[] s = new int[16*16];
		for (int i = s.length - 1; i >= 0; --i) {
			s[i] = -1;
		}
		int[][] mappings = new int[128][];
		for (int i = 0; i < 112; ++i) {
			int depth = Palette.getPlayerPaletteDepth(Sprite.PLAYER_PALETTE_DE, i);
			int original = Palette.getPlayerPaletteOriginal(Sprite.PLAYER_PALETTE_DE, i);
			int index = Palette.getDEPlayerPaletteIndex(original, depth);
			int b = original & 0xff, g = (original >> 8) & 0xff, r = (original >> 16) & 0xff;
			int x = (int) Math.round(depth / 17.0), y;
			if (r == g) {
				y = (int) Math.round(r / 17.0);
			}else {
				y = 16;
			}
			s[y * 16 + x] = i;

			int color = original;
			b = color & 0xff; g = (color >> 8) & 0xff; r = (color >> 16) & 0xff;
			mappings[i] = new int[]{r, g, b, depth};
			

			depth = Palette.getPlayerPaletteDepth(Sprite.PLAYER_PALETTE_DE, index);
			original = Palette.getPlayerPaletteOriginal(Sprite.PLAYER_PALETTE_DE, index);
			int index2 = Palette.getDEPlayerPaletteIndex(original, depth);
			if (index != index2) {
				System.out.println(i+","+index+","+index2);
			}
		}
		
		// Color - Depth Map
		System.out.println("Color - Depth Map");
		for (int y = 0; y < 16; ++y) {
			int v = y * 17; // brightness
			for (int x = 0; x < 16; ++x) {
				int depth = x * 17;
				int ind = 0;
				double d = Double.POSITIVE_INFINITY;
				for (int i = 0; i < 112; ++i) {
					int[] m = mappings[i];
					// No Depth weight
					double d1 = Math.pow(m[0] - v, 2) + Math.pow(m[1] - v, 2) + Math.pow(m[2] - v, 2) + 2 * Math.pow(m[3] - depth, 2);
					if (d1 < d) {
						d = d1;
						ind = i;
					}
				}
				s[x + y * 16] = ind;
			}
		}
		for (int y = 0; y < 16; ++y) {
			for (int x = 0; x < 16; ++x) {
				System.out.print(s[x + y * 16] + ",");
			}
			System.out.print("\n");
		}
		
	}
	
	
	static private class TrueColorPalette extends Palette{
		
		static private int[] cache = new int[0x1000000];
		static {
			for (int i = 0; i < 0x1000000; ++i)
				cache[i] = i;
		}
		
		public TrueColorPalette(){
		}
				
		@Override
		public int getColorCount(){
			return 0x1000000;
		}
		
		@Override
		public int getColor(int index){
			return index;
		}
		
		@Override
		public void setColor(int index, int rgb){
		}
		
		@Override
		public int mapping(int red, int green, int blue, boolean rgbmode){
			return red << 16 | green << 8 | blue;
		}
		
		@Override
		public void setCacheMode(boolean cached){
		}
	}
	

	static private class RGB555Palette extends Palette{
		
		static private int[] cache = new int[0x1000000];
		static {
			for (int i = 0; i < 0x1000000; ++i)
				cache[i] = -1;
		}
		
		public RGB555Palette(){
		}
				
		@Override
		public int getColorCount(){
			return 0x8000;
		}
		
		@Override
		public int getColor(int index){
			int red = ((index & 0x7c00) >> 7) * 0xff / 0xf8,
				green = ((index & 0x3e0) >> 2) * 0xff / 0xf8,
				blue = ((index & 0x1f) << 3) * 0xff / 0xf8;
			return red << 16 | green << 8 | blue | 0xff000000;
		}
		
		@Override
		public void setColor(int index, int rgb){
		}
		
		@Override
		public int mapping(int red, int green, int blue, boolean rgbmode){
			int red1 = red * 0xf8 / 0xff;
			int green1 = green * 0xf8 / 0xff;
			int blue1 = blue * 0xf8 / 0xff;
			int index = red1 << 16 | green1 << 8 | blue1;
			if (cache[index] < 0) {
				cache[index] = (red << 7) & 0x7c00 | (green << 2) & 0x3e0 | (blue >> 3) & 0x1f;
			}
			
			return cache[index];
		}
		
		@Override
		public void setCacheMode(boolean cached){
		}
	}
	
}

