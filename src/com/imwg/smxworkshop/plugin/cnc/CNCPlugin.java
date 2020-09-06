package com.imwg.smxworkshop.plugin.cnc;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileView;

import com.imwg.smxworkshop.plugin.Plugin;
import com.imwg.smxworkshop.sprite.Palette;
import com.imwg.smxworkshop.sprite.SMXSprite;
import com.imwg.smxworkshop.sprite.Sprite;
import com.imwg.smxworkshop.sprite.SpriteIO;
import com.imwg.smxworkshop.view.MainFrame;

public class CNCPlugin extends Plugin{
	
	private static final int PLAYER_COLOR_INDEX = 1; // Red
	private static String currentSpritePath = ".";
	
	static private final Palette cncPlayerPalette = 
			new Palette(new int[]{
					0xffff0000, 0xffee0000, 0xffde0000, 0xffd20000,
					0xffc20000, 0xffb20000, 0xffa50000, 0xff950000,
					0xff850000, 0xff790000, 0xff690000, 0xff590000,
					0xff4c0000, 0xff3c0000, 0xff2c0000, 0xff200000});
	
	@Override
	public String onGetName(){
		return "Command & Conqueror";
	}
	
	@Override
	public String[] onGetMenuItems() {
		return new String[]{"Load", "Load SHP File...", "Save", "Save as SHP File...",
				"-", "-", "Shadow","Combine Shadow", "PlayerColor", "Convert Selected Player Color"};
	}

	@Override
	public void onSelectMenu(final MainFrame mainFrame, String name) {
		if (name.equals("Load")){
			final File file = popupChooseSpriteFile(mainFrame, JFileChooser.OPEN_DIALOG);
			if (file != null){
				LoadSaveDialog dialog = new LoadSaveDialog(mainFrame, true);
				dialog.setVisible(true);
				dialog.setConfirmedListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent event) {
						try { 
							mainFrame.loadSprite(loadSHP(file, 
									LoadSaveDialog.combineShadow, LoadSaveDialog.playerColor));
						} catch (NullPointerException e) {
						} catch (IOException e) {
						}
					}
				});
			}
			
		}else if (name.equals("Save")){
			int[] palettes = mainFrame.getSprite().getUsedPalettes();
			boolean tooLargePalette = false;
			for (int palette : palettes){
				if (Palette.getPalette(palette).getColorCount() > 256){
					tooLargePalette = true; break;
				}
			}
			if (tooLargePalette)
				if (JOptionPane.showConfirmDialog(mainFrame, 
						"Warning: some frames are of palettes more than 256 colors.\r\nContinue?",
						"Save", JOptionPane.WARNING_MESSAGE | JOptionPane.YES_NO_OPTION)
						== JOptionPane.NO_OPTION){
					return;
				}
			
			final File file = popupChooseSpriteFile(mainFrame, JFileChooser.SAVE_DIALOG);
			if (file != null){
				LoadSaveDialog dialog = new LoadSaveDialog(mainFrame, false);
				dialog.setVisible(true);
				dialog.setConfirmedListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent event) {
						saveSHP(mainFrame.getSprite(), file, 
								LoadSaveDialog.saveShadow, LoadSaveDialog.savePlayerColor);
					}
				});
			}
			
		}else if (name.equals("Shadow")){
			combineShadow(mainFrame.getSprite());
			mainFrame.reload();
			
		}else if (name.equals("PlayerColor")){
			Sprite sprite = mainFrame.getSprite();			
			Palette playerPalette = Palette.getPlayerPalette(sprite.getPlayerMode(), PLAYER_COLOR_INDEX);
			int[] mapping = Palette.getMappingArray(cncPlayerPalette, playerPalette);
			
			for (int frameId : mainFrame.getSelectedFrames()){
				Sprite.Frame frame = sprite.getFrame(frameId);
				int width = frame.getWidth(Sprite.DATA_IMAGE);
				int height = frame.getHeight(Sprite.DATA_IMAGE);
				for (int y = 0; y < height; ++y){
					for (int x = 0; x < width; ++x){
						int pixel = frame.getPixel(Sprite.DATA_IMAGE, x, y); 
						if (pixel != Sprite.PIXEL_NULL){
							if (pixel >= 16 && pixel < 32){
								System.out.println(pixel);
								frame.setPixel(Sprite.DATA_IMAGE, x, y, 
										Sprite.PIXEL_PLAYER_START + mapping[pixel - 16]);
							}
						}
					}
				}
			}
			mainFrame.refreshAll();
		}
	}
	
	public File popupChooseSpriteFile(MainFrame mainFrame, int type){
		final JFileChooser fd = new JFileChooser();
		fd.setDialogType(type);
		fd.setFileView(new FileView() {
		    public Icon getIcon(File f) {
		    	return fd.getFileSystemView().getSystemIcon(f);
		    }
		});
		fd.setFileFilter(new FileNameExtensionFilter("SHP File", "SHP"));
		fd.setCurrentDirectory(new File(currentSpritePath));
		fd.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fd.setMultiSelectionEnabled(false);
		
		if (fd.showDialog(mainFrame, null) == JFileChooser.APPROVE_OPTION){
			if (fd.getSelectedFile() != null) {
				File file = fd.getSelectedFile();
				currentSpritePath = file.getAbsolutePath();
				if (type == JFileChooser.SAVE_DIALOG){
					String filePath = file.getAbsolutePath();
					if (!file.getName().contains(".")){ // Auto complete
						filePath += ".SHP";
						file = new File(filePath);
					}
				}
				return file;
			}
		}
		return null;
	}
	
	static private SMXSprite loadSHP(File file, boolean shadow, int playerMode) throws IOException, NullPointerException{
		FileInputStream fis = new FileInputStream(file);
		fis.skip(2);
		final int fullWidth = SpriteIO.readInteger(fis, 2);
		final int fullHeight = SpriteIO.readInteger(fis, 2);
		final int frameCount = SpriteIO.readInteger(fis, 2);
		
		int[] playerMap = null;
		if (playerMode != Sprite.PLAYER_PALETTE_NONE)
			playerMap = Palette.getMappingArray(cncPlayerPalette,
					Palette.getPlayerPalette(playerMode, PLAYER_COLOR_INDEX));

		SMXSprite sprite = new SMXSprite();
		sprite.setPlayerMode(playerMode);
		long[] offsets = new long[frameCount];
		for (int index=0; index<frameCount; ++index){
			int left = SpriteIO.readInteger(fis, 2);
			int top = SpriteIO.readInteger(fis, 2);
			int width = SpriteIO.readInteger(fis, 2);
			int height = SpriteIO.readInteger(fis, 2);
			int anchorX = fullWidth / 2 - left;
			int anchorY = fullHeight / 2 - top;
			fis.skip(12);
			
			offsets[index] = SpriteIO.readInteger(fis, 4);
			Sprite.Frame frame = sprite.createFrame();
			frame.setAnchor(Sprite.DATA_IMAGE, anchorX, anchorY);
			frame.expand(Sprite.DATA_IMAGE, anchorX, anchorY, width - anchorX, height - anchorY);
			sprite.insertFrame(index, frame);
			
		}
		long current = frameCount * 24 + 8;

		for (int index=0; index<frameCount; ++index){
			fis.skip(offsets[index] - current);
			current = offsets[index];
			
			Sprite.Frame frame = sprite.getFrame(index);
			int height = frame.getHeight(Sprite.DATA_IMAGE);
			for (int y=0; y<height; ++y){
				int len = SpriteIO.readInteger(fis, 2) - 2;
				int x = 0;
				for (int i=0; i<len; ++i){
					int c = fis.read(); 
					if (c == 0){
						x += fis.read(); ++i;
					}else{
						if (playerMode != Sprite.PLAYER_PALETTE_NONE){
							if (c >= 16 && c < 32){
								frame.setPixel(Sprite.DATA_IMAGE, x++, y, 
										playerMap[c - 16] + Sprite.PIXEL_PLAYER_START);
								continue;
							}
						}
						frame.setPixel(Sprite.DATA_IMAGE, x++, y, c);
					}
				}
				current += len + 2;
			}
		}
		
		fis.close();
		
		if (shadow){
			combineShadow(sprite);
		}
		
		return sprite;
	}
	
	static private boolean saveSHP(Sprite sprite, File file, boolean shadow, boolean playerMode){ // TODO
		
		if (shadow)
			separateShadow(sprite);
		
		// Mapping from original player colors to CNC's
		int[] playerMap = Palette.getMappingArray(
				Palette.getPlayerPalette(sprite.getPlayerMode(), PLAYER_COLOR_INDEX),
				cncPlayerPalette);
		
		// Mapping for color #0 and color #16~31
		int[][] mappings = new int[Palette.ORIGINAL_PALETTE_COUNT + Palette.getCustomPaletteCount()][];
		for (int paletteId : sprite.getUsedPalettes()){
			// Mapping color #0 to other nearest color, prevent from crash
			Palette originalPalette = Palette.getPalette(paletteId);
			int originalPaletteSize = originalPalette.getColorCount();
			int targetPaletteSize = Math.min(originalPaletteSize, 256);
			int[] mapping = new int[originalPaletteSize];
			if (playerMode){
				Palette palette = Palette.merge(
						new Palette(originalPalette, 1, 15),
						new Palette(originalPalette, 32, targetPaletteSize - 32));
				for (int i = 0; i < targetPaletteSize; ++i){
					if (i == 0 || i >= 16 && i < 32){
						int mapid = palette.mapping(originalPalette.getColor(i)) + 1;
						if (mapid >= 16)
							mapid += 16;
						mapping[i] = mapid;
					}else{
						mapping[i] = i;
					}
				}
			}else{
				Palette palette = new Palette(originalPalette, 1, targetPaletteSize - 1);
				mapping[0] = palette.mapping(originalPalette.getColor(0)) + 1;
				for (int i = 1; i < targetPaletteSize; ++i){
					mapping[i] = i;
				}
			}
			mappings[paletteId] = mapping; 
		}
		
		// Convert frame content
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(file);
			fos.write(new byte[]{0, 0});
			
			final int frameCount = sprite.getFrameCount();
			int halfWidth = 0;
			int halfHeight = 0;
			for (int index = 0; index < frameCount; ++index){
				Sprite.Frame frame = sprite.getFrame(index);
				int left = frame.getAnchorX();
				int top = frame.getAnchorY();
				int width = frame.getWidth();
				int height = frame.getHeight();
				halfWidth = Math.max(halfWidth, Math.max(left, width - left));
				halfHeight = Math.max(halfHeight, Math.max(top, height - top));
			}
			final int fullWidth = halfWidth * 2;
			final int fullHeight = halfHeight * 2;
			
			SpriteIO.writeInteger(fos, fullWidth, 2);
			SpriteIO.writeInteger(fos, fullHeight, 2);
			SpriteIO.writeInteger(fos, frameCount, 2);
			
			if (frameCount > 0){
				int[] offsets = new int[frameCount];
				int[] lengths = new int[frameCount];
				byte[][] segments = new byte[frameCount][];
				offsets[0] = 8 + frameCount * 24;
				for (int index = 0; index < frameCount; ++index){
					
					Sprite.Frame frame = sprite.getFrame(index);
					int[] mapping = mappings[frame.getPalette()];
					
					int width = frame.getWidth(Sprite.DATA_IMAGE);
					int height = frame.getHeight(Sprite.DATA_IMAGE);
					if (width <= 0){ // Empty
						segments[index] = new byte[0];
						lengths[index] = 0;
						
					}else{
						byte[] segment = new byte[(width + 2) * height * 3 / 2 + 7];
						int len = 0;
						int segmentOffset = 0;
						for (int y = 0; y < height; ++y){
							int lineLen = 2;
							for (int x = 0; x < width; ++x){
								int pixel = frame.getPixel(Sprite.DATA_IMAGE, x, y);
								if (pixel == Sprite.PIXEL_NULL){
									segment[segmentOffset + lineLen] = 0;
									int i = 0;
									for (; i < 255 && x < width; ++i, ++x){
										if (frame.getPixel(Sprite.DATA_IMAGE, x, y) != Sprite.PIXEL_NULL)
											break;
									}
									--x;
									if (x == width)
										i += 2;
									segment[segmentOffset + lineLen + 1] = (byte) i;
									lineLen += 2;
								}else{
									if (pixel >= Sprite.PIXEL_PLAYER_START)
										segment[segmentOffset + lineLen++] = 
											(byte) (playerMap[pixel - Sprite.PIXEL_PLAYER_START] + 16);
									else
										segment[segmentOffset + lineLen++] = (byte) mapping[pixel & 0xff];
								}
							}
							segment[segmentOffset] = (byte) (lineLen & 0xff);
							segment[segmentOffset + 1] = (byte) (lineLen >> 8);
							len += lineLen;
							segmentOffset += lineLen;
						}
						segments[index] = segment;
						len = (len + 7) >> 3 << 3; // Ceil by 8
						lengths[index] = len;
					}
					if (index > 0)
						offsets[index] = offsets[index - 1] + lengths[index - 1];
				}
				
				for (int index = 0; index < frameCount; ++index){
					Sprite.Frame frame = sprite.getFrame(index);
					int left = frame.getAnchorX(Sprite.DATA_IMAGE);
					int top = frame.getAnchorY(Sprite.DATA_IMAGE);
					int width = frame.getWidth(Sprite.DATA_IMAGE);
					int height = frame.getHeight(Sprite.DATA_IMAGE);
					SpriteIO.writeInteger(fos, halfWidth - left, 2);
					SpriteIO.writeInteger(fos, halfHeight - top, 2);
					SpriteIO.writeInteger(fos, width, 2);
					SpriteIO.writeInteger(fos, height, 2);
					SpriteIO.writeInteger(fos, 3, 4);
					SpriteIO.writeInteger(fos, 0, 4);
					SpriteIO.writeInteger(fos, 0, 4);
					if (lengths[index] == 0)
						SpriteIO.writeInteger(fos, 0, 4);
					else
						SpriteIO.writeInteger(fos, offsets[index], 4);
				}
				for (int index = 0; index < frameCount; ++index){
					fos.write(segments[index], 0, lengths[index]);
				}
			}
			fos.close();
			return true;
		
		} catch (FileNotFoundException e) {
			
		} catch (IOException e) {
			
		}
		return false;
		
	}
	
	static private void combineShadow(Sprite sprite){
		final int frameCount = sprite.getFrameCount();
		final int clipCount = (frameCount + 1) / 2;
		final int clipOffset = frameCount / 2;
		
		for (int index=0; index<clipCount; ++index){
			Sprite.Frame frame = sprite.getFrame(index);
			Sprite.Frame shadowFrame = sprite.getFrame(index + clipOffset);
			int width = shadowFrame.getWidth(Sprite.DATA_IMAGE);
			int height = shadowFrame.getHeight(Sprite.DATA_IMAGE);
			int anchorX = shadowFrame.getAnchorX(Sprite.DATA_IMAGE);
			int anchorY = shadowFrame.getAnchorY(Sprite.DATA_IMAGE);
			
			frame.expand(Sprite.DATA_SHADOW, anchorX, anchorY, width - anchorX, height - anchorY);
			for (int y = 0; y < height; ++y){
				for (int x = 0; x < width; ++x){
					if (shadowFrame.getPixel(Sprite.DATA_IMAGE, x, y) != Sprite.PIXEL_NULL)
						frame.setPixelRelative(Sprite.DATA_SHADOW, x - anchorX, y - anchorY, 128);
				}
			}
		}
		for (int index=0; index<clipOffset; ++index){
			sprite.removeFrame(clipCount);
		}
	}
	
	static private void separateShadow(Sprite sprite){
		final int frameCount = sprite.getFrameCount();
		for (int index=0; index<frameCount; ++index){
			Sprite.Frame frame = sprite.getFrame(index);
			int width = frame.getWidth(Sprite.DATA_SHADOW);
			int height = frame.getHeight(Sprite.DATA_SHADOW);
			int anchorX = frame.getAnchorX(Sprite.DATA_SHADOW);
			int anchorY = frame.getAnchorY(Sprite.DATA_SHADOW);
			
			Sprite.Frame shadowFrame = sprite.createFrame(frame);
			shadowFrame.remove(Sprite.DATA_IMAGE);
			shadowFrame.expand(Sprite.DATA_IMAGE, anchorX, anchorY, width - anchorX, height - anchorY);
			for (int y = 0; y < height; ++y){
				for (int x = 0; x < width; ++x){
					if (frame.getPixel(Sprite.DATA_SHADOW, x, y) != Sprite.PIXEL_NULL)
						shadowFrame.setPixelRelative(Sprite.DATA_IMAGE, x - anchorX, y - anchorY, 1);
				}
			}
			sprite.insertFrame(frameCount + index, shadowFrame);
		}
	}
}
