package com.imwg.smxworkshop.view;

import java.awt.CheckboxMenuItem;
import java.awt.Cursor;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.MenuShortcut;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.imwg.smxworkshop.model.Configuration;
import com.imwg.smxworkshop.model.FrameFilter;
import com.imwg.smxworkshop.model.MainModel;
import com.imwg.smxworkshop.plugin.Plugin;
import com.imwg.smxworkshop.sprite.Palette;
import com.imwg.smxworkshop.sprite.SLPSprite;
import com.imwg.smxworkshop.sprite.SMXSprite;
import com.imwg.smxworkshop.sprite.Sprite;
import com.imwg.smxworkshop.sprite.SpriteIO;


public class MainMenu extends MenuBar{
	
	private static final long serialVersionUID = 6448205397024840934L;
	static Properties properties;
	
	private MenuListener fileListener;
	private MenuListener editListener;
	private MenuListener viewListener, helpListener;
	private MenuListener setPaletteListener;
	private MenuListener changePaletteListener;
	private List<CheckMenuItem> checkMenuItems = new ArrayList<CheckMenuItem>();
	
	private Menu setPaletteMenu, changePaletteMenu, languageMenu, rencentFilesMenu;
	
	public void setPaletteMenu(){
		setPaletteMenu.removeAll();
		changePaletteMenu.removeAll();
		
		for (int i=0; i<Palette.ORIGINAL_PALETTE_COUNT + Palette.getCustomPaletteCount(); ++i){
			if (Palette.getPalette(i) != null){
				String name;
				if (i >= Palette.ORIGINAL_PALETTE_COUNT)
					name = Palette.getPaletteName(i) +" ("+Palette.getPalette(i).getColorCount()+")";
				else
					name = "#"+i+" - "+ Palette.getPaletteName(i) +" ("+Palette.getPalette(i).getColorCount()+")";
				
				final int index = i;
				CheckMenuItem item = new CheckMenuItem(
						name, setPaletteListener, Integer.toString(i), -1){
					
					private static final long serialVersionUID = -8262755890897558852L;

					@Override
					public boolean getReferenceState(){ // Reference of the status
						MainFrame mainFrame = getMainFrame();
						if (mainFrame.sprite != null){
							Sprite.Frame frame = mainFrame.sprite.getFrame(mainFrame.current);
							if (frame == null)
								return false;
							else
								return frame.getPalette() == index;
						}
						return false;
					}
				};
				setPaletteMenu.add(item);

				MenuItem item2 = createMenuItem(
						name, changePaletteListener, Integer.toString(i), -1);
				changePaletteMenu.add(item2);
			}
		}
	}
	
	public void setLanguageMenu(){
		languageMenu.removeAll();
		for (Entry<Integer, String> key : ViewConfig.languages.entrySet()){
			MenuItem menuItem = new MenuItem(key.getValue());
			menuItem.setActionCommand(key.getKey().toString());
			menuItem.addActionListener(helpListener);
			languageMenu.add(menuItem);
		}
	}
	
	public void setRencentFilesMenu(){
		rencentFilesMenu.removeAll();
		for (final String fileName : Configuration.getRecentFiles()){
			MenuItem menu = new MenuItem(fileName);
			rencentFilesMenu.add(menu);
			menu.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					MainFrame mainFrame = getMainFrame();
					File file = new File(fileName);
					if (file != null){
						String fname = file.getAbsolutePath();
						mainFrame.popupProcessDialog();
						mainFrame.loadSprite(file);
						mainFrame.closeProcessDialog();
						MainFrame.currentSpritePath = fname;
						mainFrame.addRecentFile(fname);
					}
				}
			});
		}
	}
	
	
	public MainMenu() {
		super();
		
		fileListener = new MenuListener(){
			@Override
			public void menuClicked(String action) {
				final MainFrame mainFrame = getMainFrame();
				switch(action){
				case "root.File.New":
					mainFrame.loadSprite(new SMXSprite());
					break;
				case "root.File.Open":
					File file = mainFrame.popupChooseSpriteFile(JFileChooser.OPEN_DIALOG);
					if (file != null){
						String fname = file.getAbsolutePath();
						mainFrame.popupProcessDialog();
						mainFrame.loadSprite(file);
						mainFrame.closeProcessDialog();
						MainFrame.currentSpritePath = fname;
						mainFrame.addRecentFile(fname);
					}
					break;
					
				case "root.File.Save":
					if (mainFrame.currentFile != null){
						mainFrame.getModel().saveSprite(mainFrame.getSprite(), 
								new File(mainFrame.currentFile), "");
						mainFrame.addRecentFile(mainFrame.currentFile);
						return;
					}
					
				case "root.File.SaveAs":
					file = mainFrame.popupChooseSpriteFile(JFileChooser.SAVE_DIALOG);
					if (file != null){
						mainFrame.getModel().saveSprite(mainFrame.getSprite(), file, "");
						MainFrame.currentSpritePath = file.getAbsolutePath();
						mainFrame.addRecentFile(file.getAbsolutePath());
					}
					break;
					
				case "root.File.Import":
					final File[] files = mainFrame.popupChooseImagesFile(JFileChooser.OPEN_DIALOG);
					if (files != null){
						ImportImagesDialog dialog = new ImportImagesDialog(mainFrame);
						dialog.setConfirmedListener(new ActionListener(){
							@Override
							public void actionPerformed(ActionEvent e) {
								Sprite sprite = null;
								mainFrame.popupProcessDialog();
								try {
									sprite = SpriteIO.importFromImages(
											mainFrame.getSprite(), files, ImportImagesDialog.settings);
								} catch (IOException e1) {
									e1.printStackTrace();
								}
								mainFrame.loadSprite(sprite);
								mainFrame.closeProcessDialog();
								MainFrame.currentImagePath = files[0].getAbsolutePath();
							}

						});
						dialog.setVisible(true);
						
					}
					break;
					
				case "root.File.Export":
					final File[] files2 = mainFrame.popupChooseImagesFile(JFileChooser.SAVE_DIALOG);
					if (files2 != null){
						ExportImagesDialog dialog = new ExportImagesDialog(mainFrame);
						dialog.setConfirmedListener(new ActionListener(){
							@Override
							public void actionPerformed(ActionEvent e) {
								mainFrame.popupProcessDialog();
								try {
									SpriteIO.exportToImages(mainFrame.getSprite(),
											files2[0], ExportImagesDialog.settings,
											mainFrame.getSelectedFrames());
								} catch (IOException e1) {
									e1.printStackTrace();
								}
								mainFrame.closeProcessDialog();
								MainFrame.currentImagePath = files2[0].getAbsolutePath();
							}
						});
						dialog.setVisible(true);
					}
					break;
				
				case "root.File.ExportGif":
					final File[] files3 = mainFrame.popupChooseImagesFile(JFileChooser.SAVE_DIALOG);
					if (files3 != null){
						int[] selected = mainFrame.getSelectedFrames();
						int selectedOnly = 0;
						if (selected.length < mainFrame.getSprite().getFrameCount()) {
							selectedOnly = JOptionPane.showConfirmDialog(mainFrame,
									ViewConfig.getString("ExportImagesDialog.Checkbox.selectedOnly"),
									ViewConfig.getString("MainMenu.File.ExportGif"),
									JOptionPane.YES_NO_OPTION,
									JOptionPane.INFORMATION_MESSAGE) == JOptionPane.YES_OPTION
									? 1 : 0;	
						}
						
						int imageMode = (mainFrame.displayFlags & 1) != 0 ?
								SpriteIO.GIF_MODE_NORMAL : SpriteIO.GIF_MODE_NEITHER;
						if ((mainFrame.displayFlags & 2) != 0)
							imageMode |= SpriteIO.GIF_MODE_SHADOW_MASK;
						if ((mainFrame.displayFlags & 4) != 0)
							imageMode |= SpriteIO.IMAGE_MODE_OUTLINE_MASK;
						if ((mainFrame.displayFlags & 8) != 0)
							imageMode |= SpriteIO.IMAGE_MODE_SMUDGE_MASK;
						imageMode |= SpriteIO.IMAGE_MODE_BACKGROUND_MASK;
					
						Map<String, Integer> settings = new Hashtable<String, Integer>();
						settings.put("imageMode", imageMode);
						settings.put("anchorMode", SpriteIO.ANCHOR_MODE_ALIGN);
						settings.put("padding", 0);
						settings.put("backgroundColor", 0xffffff);
						settings.put("playerColor", mainFrame.preview.playerColorId);
						settings.put("selectedOnly", selectedOnly);
						settings.put("frameRate", Configuration.getAnimationSpeed());
						try {
							mainFrame.popupProcessDialog();
							SpriteIO.exportToGif(mainFrame.getSprite(), files3[0], settings, selected);
						} catch (IOException e) {
							e.printStackTrace();
						}finally{
							mainFrame.closeProcessDialog();
							MainFrame.currentImagePath = files3[0].getAbsolutePath();
						}
					}
					break;

				case "root.File.Comment":
					final String comment = (String) JOptionPane.showInputDialog(
							mainFrame, null,
							ViewConfig.getString("MainMenu.File.Comment"),
							JOptionPane.QUESTION_MESSAGE, null, null,
							mainFrame.getSprite().getMemo());
					if (comment != null)
						mainFrame.getSprite().setMemo(comment);
					break;
					
				case "root.File.DefaultComment":
					Configuration.setDefaultMemo(!Configuration.isDefaultMemo());
					break;
					
				case "root.File.Exit":
					mainFrame.exit();
					break;
				}
			}
		};
		viewListener = new MenuListener(){
			@Override
			public void menuClicked(String action) {
				MainFrame mainFrame = (MainFrame) getParent();
				switch(action){
				case "root.View.Main":
					mainFrame.displayFlags ^= 1; mainFrame.getCanvas().repaint();
					break;
				case "root.View.Shadow":
					mainFrame.displayFlags ^= 2; mainFrame.getCanvas().repaint();
					break;
				case "root.View.Outline":
					mainFrame.displayFlags ^= 4; mainFrame.getCanvas().repaint();
					break;
				case "root.View.Smudge":
					mainFrame.displayFlags ^= 8; mainFrame.getCanvas().repaint();
					break;
				case "root.View.Border":
					mainFrame.displayFlags ^= 0x10; mainFrame.getCanvas().repaint();
					break;
				case "root.View.Sc05":
					mainFrame.scaleRate = 50; mainFrame.getCanvas().repaint();
					break;
				case "root.View.Sc1":
					mainFrame.scaleRate = 100; mainFrame.getCanvas().repaint();
					break;
				case "root.View.Sc2":
					mainFrame.scaleRate = 200; mainFrame.getCanvas().repaint();
					break;
				case "root.View.Sc4":
					mainFrame.scaleRate = 400; mainFrame.getCanvas().repaint();
					break;
				case "root.View.PlrColor.P1":
				case "root.View.PlrColor.P2":
				case "root.View.PlrColor.P3":
				case "root.View.PlrColor.P4":
				case "root.View.PlrColor.P5":
				case "root.View.PlrColor.P6":
				case "root.View.PlrColor.P7":
				case "root.View.PlrColor.P8":
					mainFrame.getPreview().playerColorId = (int)(action.charAt(action.length() - 1) - '1');
					mainFrame.refreshAll();
					break;
				case "root.View.Animated":
					mainFrame.toggleAnimationMode();
					break;
				case "root.View.Loop":
					mainFrame.animateLoop = !mainFrame.animateLoop;
					break;
				case "root.View.Restore":
					mainFrame.canvasCenterX = .5;
					mainFrame.canvasCenterY = .5;
					mainFrame.getCanvas().repaint();
					break;
				case "root.View.Palettes":
					new ImportImagesDialog(mainFrame).setVisible(true);
					break;
				}
			}
		};
		editListener = new MenuListener(){
			@Override
			public void menuClicked(String action) {
				final MainFrame mainFrame = getMainFrame();
				final MainModel model = mainFrame.getModel();
				Sprite sprite = mainFrame.getSprite();
				switch(action){
				case "root.Edit.Duplicate":
					model.duplicateFrames(sprite, mainFrame.getSelectedFrames());
					mainFrame.reload();
					break;
				case "root.Edit.Delete":
					model.deleteFrames(sprite, mainFrame.getSelectedFrames());
					mainFrame.reload();
					mainFrame.listPanel.select(mainFrame.getSelectedFrames()[0]);
					break;
				case "root.Edit.Reverse":
					model.reverseFrames(sprite, mainFrame.getSelectedFrames());
					mainFrame.reload();
					break;
				case "root.Edit.Shift":
					try{
						final int offset = Integer.parseInt(JOptionPane.showInputDialog(
										ViewConfig.getString("ShiftFramesDialog.Label.offset")));
						if (offset > 0){
							final int count = mainFrame.getSelectedFrames().length; 
							final int first = model.shiftFrames(sprite, mainFrame.getSelectedFrames(), offset);
							int[] ids = new int[count];
							for (int i=0; i<count; ++i)
								ids[i] = first + i;
							mainFrame.reload();
							mainFrame.listPanel.select(ids);
						}
					}catch (NumberFormatException e){
					}
					break;
				case "root.Edit.SelAll":
					mainFrame.getListPanel().selectAll();
					break;
				case "root.Edit.SelInv":
					mainFrame.getListPanel().selectInverse();
					break;
				case "root.Edit.MoveAncr":
					mainFrame.mode = MainFrame.MODE_SETANCHOR;
					mainFrame.getCanvas().setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
					break;
				case "root.Edit.SetAncr":
					if (mainFrame.getSprite() != null){
						mainFrame.setEnabled(false);
						final SetAnchorDialog anchorBox = new SetAnchorDialog(mainFrame);
						anchorBox.setConfirmedListener(new ActionListener(){
							@Override
							public void actionPerformed(ActionEvent e) {
								int[] selected = mainFrame.getSelectedFrames();
								Sprite sprite = mainFrame.getSprite();
								model.setAnchor(sprite, selected, anchorBox.type,
										anchorBox.x, anchorBox.y, anchorBox.relative);
								mainFrame.getCanvas().repaint();
							}
						});
						anchorBox.setVisible(true);
					}
					break;
				case "root.Edit.Crop":
					model.cropFrames(mainFrame.getSprite(), mainFrame.getSelectedFrames());
					mainFrame.reload();
					break;
				case "root.Edit.Rotate.FlipH":
					model.flipFrames(mainFrame.getSprite(), 
							mainFrame.getSelectedFrames(), Sprite.FLIP_HORIZONTAL);
					mainFrame.reload();
					break;
				case "root.Edit.Rotate.FlipV":
					model.flipFrames(mainFrame.getSprite(), 
							mainFrame.getSelectedFrames(), Sprite.FLIP_VERTICAL);
					mainFrame.reload();
					break;
				case "root.Edit.Rotate.Rot180":
					model.rotateFrames(mainFrame.getSprite(), 
							mainFrame.getSelectedFrames(), Sprite.ROTATE_180);
					mainFrame.reload();
					break;
				case "root.Edit.Rotate.Rot90":
					model.rotateFrames(mainFrame.getSprite(), 
							mainFrame.getSelectedFrames(), Sprite.ROTATE_CLOCKWISE_90);
					mainFrame.reload();
					break;
				case "root.Edit.Rotate.Rot90c":
					model.rotateFrames(mainFrame.getSprite(), 
							mainFrame.getSelectedFrames(), Sprite.ROTATE_COUNTER_CLOCKWISE_90);
					mainFrame.reload();
					break;
					
				case "root.Edit.Scale":
					ScaleDialog scaleDialog = new ScaleDialog(mainFrame);
					scaleDialog.setConfirmedListener(new ActionListener(){
						@Override
						public void actionPerformed(ActionEvent e) {
							model.scaleFrames(mainFrame.getSprite(), mainFrame.getSelectedFrames(), 
									ScaleDialog.xFactor, ScaleDialog.yFactor, ScaleDialog.interpolate);
							mainFrame.refreshAll();
						}
					});
					scaleDialog.setVisible(true);
					break;
					
				case "root.Edit.Remove.Main":
					model.removeFrameData(sprite, mainFrame.getSelectedFrames(), Sprite.DATA_IMAGE);
					mainFrame.refreshAll();
					break;
				case "root.Edit.Remove.Shadow":
					model.removeFrameData(sprite, mainFrame.getSelectedFrames(), Sprite.DATA_SHADOW);
					mainFrame.refreshAll();
					break;
				case "root.Edit.Remove.Outline":
					model.removeFrameData(sprite, mainFrame.getSelectedFrames(), Sprite.DATA_OUTLINE);
					mainFrame.refreshAll();
					break;
				case "root.Edit.Remove.Smudge":
					model.removeFrameData(sprite, mainFrame.getSelectedFrames(), Sprite.DATA_SMUDGE);
					mainFrame.refreshAll();
					break;
					
				case "root.Edit.Mode.SLP":
					if (mainFrame.getSprite().getVersion() != Sprite.VERSION_SLP){
						SLPSprite sprite1 = new SLPSprite(mainFrame.getSprite());
						mainFrame.loadSprite(sprite1);
					}
					break;
				case "root.Edit.Mode.SMX":
					SMXSprite sprite2 = new SMXSprite(mainFrame.getSprite());
					mainFrame.loadSprite(sprite2);
					break;
					
				case "root.Edit.SetPlrPal.AOK":
					model.setPlayerPalette(sprite, Sprite.PLAYER_PALETTE_AOK, false);
					mainFrame.refreshAll();
					break;
				case "root.Edit.SetPlrPal.AOE":
					model.setPlayerPalette(sprite, Sprite.PLAYER_PALETTE_AOE, false);
					mainFrame.refreshAll();
					break;
				case "root.Edit.SetPlrPal.DE1":
					model.setPlayerPalette(sprite, Sprite.PLAYER_PALETTE_AOEDE, false);
					mainFrame.refreshAll();
					break;
				case "root.Edit.SetPlrPal.DE2":
					model.setPlayerPalette(sprite, Sprite.PLAYER_PALETTE_DE, false);
					mainFrame.refreshAll();
					break;
				case "root.Edit.ConvPlrPal.AOK":
					model.setPlayerPalette(sprite, Sprite.PLAYER_PALETTE_AOK, true);
					mainFrame.refreshAll();
					break;
				case "root.Edit.ConvPlrPal.AOE":
					model.setPlayerPalette(sprite, Sprite.PLAYER_PALETTE_AOE, true);
					mainFrame.refreshAll();
					break;
				case "root.Edit.ConvPlrPal.DE1":
					model.setPlayerPalette(sprite, Sprite.PLAYER_PALETTE_AOEDE, true);
					mainFrame.refreshAll();
					break;
				case "root.Edit.ConvPlrPal.DE2":
					model.setPlayerPalette(sprite, Sprite.PLAYER_PALETTE_DE, true);
					mainFrame.refreshAll();
					break;

				case "root.Tools.ConvShadow":
					ConvertShadowDialog convertShadowDialog = new ConvertShadowDialog(mainFrame);
					convertShadowDialog.setConfirmedListener(new ActionListener(){
						@Override
						public void actionPerformed(ActionEvent e){
							Sprite sprite = mainFrame.getSprite();
							int[] selected = mainFrame.getSelectedFrames();
							FrameFilter filter = new FrameFilter();
							for (int index : selected){
								filter.setFrame(sprite.getFrame(index));
								filter.changeShadowToDithered(
										ConvertShadowDialog.mode, ConvertShadowDialog.levels,
										ConvertShadowDialog.low, ConvertShadowDialog.high);
							}
							mainFrame.refreshAll();
						}
					});
					convertShadowDialog.setVisible(true);
					break;
					
				case "root.Tools.AddOutline":
					int[] selected = mainFrame.getSelectedFrames();
					FrameFilter filter = new FrameFilter();
					for (int index : selected){
						filter.setFrame(sprite.getFrame(index));
						filter.addOutline(mainFrame.getSprite() instanceof SLPSprite, true);
					}
					mainFrame.refreshAll();
					break;
					
				case "root.Tools.BrightContrast":
					selected = mainFrame.getSelectedFrames();
					if (selected.length > 0){
						ImageAdjustDialog dialog = new ImageAdjustDialog(mainFrame, true);
						dialog.setPreviewImage(
								mainFrame.getPreview().getFrameImage(selected[0], Sprite.DATA_IMAGE));
						dialog.setConfirmedListener(new ActionListener(){
							@Override
							public void actionPerformed(ActionEvent e) {
								model.adjustBrightness(mainFrame.getSprite(), mainFrame.getSelectedFrames(),
										ImageAdjustDialog.brightness, ImageAdjustDialog.contrast,
										ImageAdjustDialog.normal ? 0 :-1,
										ImageAdjustDialog.player? 0 :-1,
										mainFrame.getPreview().playerColorId);
								mainFrame.refreshAll();
							}
						});
						dialog.setVisible(true);
					}
					break;	
					
				case "root.Tools.HueSaturate":
					selected = mainFrame.getSelectedFrames();
					if (selected.length > 0){
						ImageAdjustDialog dialog = new ImageAdjustDialog(mainFrame, false);
						dialog.setPreviewImage(
								mainFrame.getPreview().getFrameImage(selected[0], Sprite.DATA_IMAGE));
						dialog.setConfirmedListener(new ActionListener(){
							@Override
							public void actionPerformed(ActionEvent e) {
								model.adjustHue(mainFrame.getSprite(), mainFrame.getSelectedFrames(),
										ImageAdjustDialog.hue, ImageAdjustDialog.saturation, 
										ImageAdjustDialog.value, ImageAdjustDialog.tint,
										ImageAdjustDialog.normal ? 0 :-1,
										ImageAdjustDialog.player? 0 :-1,
										mainFrame.getPreview().playerColorId);
								mainFrame.refreshAll();
							}
						});
						dialog.setVisible(true);
					}
					break;
					
				case "root.Tools.CompAngl": {
					String numStr = JOptionPane.showInputDialog(
							ViewConfig.getString("AdjustAngleDialog.Label.angles"), 
							Integer.toString(AdjustAngleDialog.angleCount));
					if (numStr != null){
						final int angles = Integer.parseInt(numStr);
						model.completeMirrorAngles(sprite, angles);
						AdjustAngleDialog.angleCount = angles;
						mainFrame.reload();
					}
				} break;
				
				case "root.Tools.RemoAngl": {
					String numStr = JOptionPane.showInputDialog(
							ViewConfig.getString("AdjustAngleDialog.Label.angles"), 
							Integer.toString(AdjustAngleDialog.angleCount));
					if (numStr != null){
						final int angles = Integer.parseInt(numStr);
						model.removeMirrorAngles(sprite, angles);
						AdjustAngleDialog.angleCount = angles;
						mainFrame.reload();
					}
				} break;
				
				case "root.Tools.AdjAngl": {
					AdjustAngleDialog dialog = new AdjustAngleDialog(mainFrame);
					dialog.setConfirmedListener(new ActionListener(){
						@Override
						public void actionPerformed(ActionEvent e) {
							model.adjustAngles(mainFrame.getSprite(), AdjustAngleDialog.angleCount,
									AdjustAngleDialog.srcAngle, AdjustAngleDialog.dstAngle,
									AdjustAngleDialog.srcClockwise, AdjustAngleDialog.dstClockwise
									);
							mainFrame.refreshAll();
						}
					});
					dialog.setVisible(true);
				} break;
				
				case "root.Tools.InterpFrames": {
					InterpolateFramesDialog dialog = new InterpolateFramesDialog(mainFrame);
					dialog.setConfirmedListener(new ActionListener(){
						@Override
						public void actionPerformed(ActionEvent e) {
							model.interpolateAngles(mainFrame.getSprite(), InterpolateFramesDialog.angles,
									InterpolateFramesDialog.frames, InterpolateFramesDialog.loop);
							mainFrame.reload();
						}
					});
					dialog.setVisible(true);
				} break;
				
				case "root.Tools.PlayerToNormal": {
					selected = mainFrame.getSelectedFrames();
					filter = new FrameFilter();
					for (int index : selected){
						filter.setFrame(sprite.getFrame(index));
						filter.playerColorToNormal(mainFrame.preview.playerColorId);
					}
					mainFrame.refreshAll();
				} break;
				
				case "root.Tools.TrimFrames": {
					TrimAngleFramesDialog dialog = new TrimAngleFramesDialog(mainFrame);
					dialog.setConfirmedListener(new ActionListener(){
						@Override
						public void actionPerformed(ActionEvent e) {
							model.trimAngleFrames(mainFrame.getSprite(), 
									TrimAngleFramesDialog.angles, TrimAngleFramesDialog.startFrame,
									TrimAngleFramesDialog.frames, TrimAngleFramesDialog.removeSelected);
							mainFrame.reload();
						}
					});
					dialog.setVisible(true);
				} break;
				
				case "root.Tools.ShiftAngleFrames": {
					ShiftAngleFramesDialog dialog = new ShiftAngleFramesDialog(mainFrame);
					dialog.setConfirmedListener(new ActionListener(){
						@Override
						public void actionPerformed(ActionEvent e) {
							model.shiftAngleFrames(mainFrame.getSprite(), 
									ShiftAngleFramesDialog.angles, ShiftAngleFramesDialog.radialOffset,
									ShiftAngleFramesDialog.tangentOffset);
							mainFrame.reload();
						}
					});
					dialog.setVisible(true);
				} break;
					
				}
			}
		};
		helpListener = new MenuListener(){
			@Override
			public void menuClicked(String action) {
				switch (action){
				case "root.Help.Content":
					if (Configuration.getLanguageId() == 2052){
						getMainFrame().getModel().openShellFile("Doc/readme_zh.txt");
					}else{
						getMainFrame().getModel().openShellFile("Doc/readme_en.txt");
					}
					break;
				case "root.Help.About":
					new AboutDialog(getMainFrame()).setVisible(true);
					break;
				case "root.Help.Gc":
					System.gc();
					break;
				default:
					getMainFrame().getModel().setLanguage(action);
				}
			}
		};
		
		setPaletteListener = new MenuListener(){
			@Override
			public void menuClicked(String action) {
				MainFrame mainFrame = getMainFrame(); 
				mainFrame.getModel().setPalette(mainFrame.getSprite(),
						mainFrame.getSelectedFrames(), Integer.parseInt(action), false);
				mainFrame.refreshAll();
			}
		};
		changePaletteListener = new MenuListener(){
			@Override
			public void menuClicked(String action) {
				MainFrame mainFrame = getMainFrame(); 
				mainFrame.getModel().setPalette(mainFrame.getSprite(),
						mainFrame.getSelectedFrames(), Integer.parseInt(action), true);
				mainFrame.refreshAll();
			}
		};
				
		properties = ViewConfig.viewProperties.get(this.getClass());
		for (MenuItem menu : getItemsFromProperties("root")){
			this.add((Menu) menu);
		}
		
		setLanguageMenu();
	}
	
	public void onAddCheckboxMenuItem(final CheckMenuItem menuItem, final String itemKey) {
		switch (itemKey){
		case "root.File.DefaultComment":
			menuItem.setStateListener(new CheckStateListener(){
				@Override
				public boolean getState() {
					return Configuration.isDefaultMemo();
				}
			}); break;
		case "root.View.Main": 
			menuItem.setStateListener(new CheckStateListener(){
				@Override
				public boolean getState() {
					return (getMainFrame().displayFlags & 1) != 0;
				}
			}); break;
		case "root.View.Shadow": 
			menuItem.setStateListener(new CheckStateListener(){
				@Override
				public boolean getState() {
					return (getMainFrame().displayFlags & 2) != 0;
				}
			}); break;
		case "root.View.Outline": 
			menuItem.setStateListener(new CheckStateListener(){
				@Override
				public boolean getState() {
					return (getMainFrame().displayFlags & 4) != 0;
				}
			}); break;
		case "root.View.Smudge": 
			menuItem.setStateListener(new CheckStateListener(){
				@Override
				public boolean getState() {
					return (getMainFrame().displayFlags & 0x8) != 0;
				}
			}); break;
		case "root.View.Border": 
			menuItem.setStateListener(new CheckStateListener(){
				@Override
				public boolean getState() {
					return (getMainFrame().displayFlags & 0x10) != 0;
				}
			}); break;
		case "root.View.Animated": 
			menuItem.setStateListener(new CheckStateListener(){
				@Override
				public boolean getState() {
					return getMainFrame().animated;
				}
			}); break;
		case "root.View.Loop": 
			menuItem.setStateListener(new CheckStateListener(){
				@Override
				public boolean getState() {
					return getMainFrame().animateLoop;
				}
			}); break;
		case "root.View.Sc05": 
			menuItem.setStateListener(new CheckStateListener(){
				@Override
				public boolean getState() {
					return getMainFrame().scaleRate == 50;
				}
			}); break;
		case "root.View.Sc1":
			menuItem.setStateListener(new CheckStateListener(){
				@Override
				public boolean getState() {
					return getMainFrame().scaleRate == 100;
				}
			}); break;
		case "root.View.Sc2":
			menuItem.setStateListener(new CheckStateListener(){
				@Override
				public boolean getState() {
					return getMainFrame().scaleRate == 200;
				}
			}); break;
		case "root.View.Sc4":
			menuItem.setStateListener(new CheckStateListener(){
				@Override
				public boolean getState() {
					return getMainFrame().scaleRate == 400;
				}
			}); break;
		case "root.View.PlrColor.P1":
		case "root.View.PlrColor.P2":
		case "root.View.PlrColor.P3":
		case "root.View.PlrColor.P4":
		case "root.View.PlrColor.P5":
		case "root.View.PlrColor.P6":
		case "root.View.PlrColor.P7":
		case "root.View.PlrColor.P8":
			menuItem.setStateListener(new CheckStateListener(){
				@Override
				public boolean getState() {
					return getMainFrame().preview.playerColorId
							== itemKey.charAt(itemKey.length() - 1) - '1';
				}
			}); break;
		case "root.Edit.Mode.SLP":
			menuItem.setStateListener(new CheckStateListener(){
				@Override
				public boolean getState() {
					if (getMainFrame().sprite != null)
						return getMainFrame().sprite.getVersion() == Sprite.VERSION_SLP;
					return false;
				}
			}); break;
		case "root.Edit.Mode.SMX":
			menuItem.setStateListener(new CheckStateListener(){
				@Override
				public boolean getState() {
					if (getMainFrame().sprite != null)
						return getMainFrame().sprite.getVersion() == Sprite.VERSION_SMX;
					return false;
				}
			}); break;
		case "root.Edit.SetPlrPal.AOK":
			menuItem.setStateListener(new CheckStateListener(){
				@Override
				public boolean getState() {
					if (getMainFrame().sprite != null)
						return getMainFrame().sprite.getPlayerMode() == Sprite.PLAYER_PALETTE_AOK;
					return false;
				}
			}); break;
		case "root.Edit.SetPlrPal.AOE":
			menuItem.setStateListener(new CheckStateListener(){
				@Override
				public boolean getState() {
					if (getMainFrame().sprite != null)
						return getMainFrame().sprite.getPlayerMode() == Sprite.PLAYER_PALETTE_AOE;
					return false;
				}
			}); break;
		case "root.Edit.SetPlrPal.DE1":
			menuItem.setStateListener(new CheckStateListener(){
				@Override
				public boolean getState() {
					if (getMainFrame().sprite != null)
						return getMainFrame().sprite.getPlayerMode() == Sprite.PLAYER_PALETTE_AOEDE;
					return false;
				}
			}); break;
		case "root.Edit.SetPlrPal.DE2":
			menuItem.setStateListener(new CheckStateListener(){
				@Override
				public boolean getState() {
					if (getMainFrame().sprite != null)
						return getMainFrame().sprite.getPlayerMode() == Sprite.PLAYER_PALETTE_DE;
					return false;
				}
			}); break;
		}
	}

	public void onAddListener(MenuItem menuItem, String itemKey){
		if (itemKey.startsWith("root.File")){
			menuItem.addActionListener(fileListener);
		}else if (itemKey.startsWith("root.Edit") | itemKey.startsWith("root.Tools")){
			menuItem.addActionListener(editListener);
		}else if (itemKey.startsWith("root.View")){
			menuItem.addActionListener(viewListener);
		}else if (itemKey.startsWith("root.Help")){
			menuItem.addActionListener(helpListener);
		}
		
		if (itemKey.equals("root.File.Recent")){
			this.rencentFilesMenu = (Menu) menuItem;
		}else if (itemKey.equals("root.Edit.SetPal")){
			this.setPaletteMenu = (Menu) menuItem;
		}else if (itemKey.equals("root.Edit.ConvPal")){
			this.changePaletteMenu = (Menu) menuItem;
		}else if (itemKey.equals("root.Help.Language")){
			this.languageMenu = (Menu) menuItem;
		}else if (itemKey.equals("root.Plugin")){
			Menu parentMenu = ((Menu) menuItem);
			parentMenu.removeAll();
			
			for (String className : Plugin.getPlugins()){
				final Plugin plugin = Plugin.getPlugin(className);
				Menu menu = new Menu(plugin.onGetName());
				parentMenu.add(menu);

				String[] menuItems = plugin.onGetMenuItems();
				for (int i=0; i<menuItems.length; i+=2){
					if (menuItems[i+1].equals("-")){
						menu.addSeparator();
					}else{
						final MenuItem item = new MenuItem(menuItems[i+1]);
						item.setName(menuItems[i]);
						item.addActionListener(new ActionListener(){
							@Override
							public void actionPerformed(ActionEvent e) {
								plugin.onSelectMenu(getMainFrame(), item.getName());
							}
						});
						menu.add(item);
					}
				}
			}
		}
		
	}
	
	public MainFrame getMainFrame(){
		return (MainFrame) getParent();
	}
	
	public void refreshCheckboxMenuItems(){
		for (CheckMenuItem item : checkMenuItems){
			item.refresh();
		}
	}
	
	private MenuItem[] getItemsFromProperties(String name){
		final String[] items = properties.getProperty(name + ".children").split("\\|");
		MenuItem[] menuItems = new MenuItem[items.length]; 
		
		for (int i=0; i<items.length; ++i){
			String item = items[i];
			if (item.length() == 0){ // Separator
				menuItems[i] = null;
				
			}else{
				String itemKey = name + "." + item;
				String itemName = ViewConfig.getString(
						properties.getProperty(itemKey + ".text", 
								itemKey.replaceFirst("^root", this.getClass().getSimpleName())));
				
				//System.out.println(itemKey);
				if (properties.getProperty(itemKey + ".children") != null){
					Menu menuItem = new Menu(itemName);
					for (MenuItem menuItem1 : getItemsFromProperties(itemKey)){
						if (menuItem1 == null)
							menuItem.addSeparator();
						else
							menuItem.add(menuItem1);
					}
					menuItems[i] = menuItem;
					onAddListener(menuItem, itemKey);
					
				}else{
					MenuItem menuItem;
					if (properties.getProperty(itemKey + ".check") != null){
						menuItem = new CheckMenuItem(itemName);
						onAddCheckboxMenuItem((CheckMenuItem) menuItem, itemKey);
					}else{
						menuItem = new MenuItem(itemName);
					}
					
					menuItem.setActionCommand(itemKey);
					String key = properties.getProperty(itemKey + ".key");
					if (key != null)
						menuItem.setShortcut(new MenuShortcut(key.codePointAt(0)));
					menuItems[i] = menuItem;
					onAddListener(menuItem, itemKey);
				}
			}
		}
		return menuItems;
	}
	
	private MenuItem createMenuItem(String name, ActionListener listener, String function, int shortcut){
		MenuItem item = new MenuItem(name);
		item.setActionCommand(function);
		item.addActionListener(listener);
		if (shortcut >= 0)
			item.setShortcut(new MenuShortcut(shortcut, false));
		return item;
	}	
	
	// For Linked Checkbox Menu Items
	private class CheckMenuItem extends CheckboxMenuItem{
		private static final long serialVersionUID = -4095994036482715689L;
		
		private CheckStateListener stateListener;
		public CheckMenuItem(String name){
			super(name);
			checkMenuItems.add(this);
		}
		public CheckMenuItem(String name, ItemListener listener, String function, int shortcut){
			this(name);
			setActionCommand(function);
			addItemListener(listener);
			if (shortcut >= 0)
				setShortcut(new MenuShortcut(shortcut, false));
		}
		public void refresh(){
			this.setState(getReferenceState());
		}
		public boolean getReferenceState(){
			if (stateListener != null)
				return stateListener.getState();
			return getState();
		}
		
		@Override
		public void addActionListener(ActionListener listener){
			if (listener instanceof MenuListener)
				this.addItemListener((MenuListener) listener);
			else
				super.addActionListener(listener);
		}
		
		public void setStateListener(CheckStateListener stateListener){
			this.stateListener = stateListener;
		}
		
	}
	
	private interface CheckStateListener{
		public boolean getState();
	}
	
	abstract private class MenuListener implements ActionListener, ItemListener{

		@Override
		final public void itemStateChanged(ItemEvent e) {
			menuClicked(((CheckMenuItem) e.getItemSelectable()).getActionCommand());
			refreshCheckboxMenuItems();
		}

		@Override
		final public void actionPerformed(ActionEvent e) {
			menuClicked(e.getActionCommand());
		}
		
		abstract public void menuClicked(String action);
	}

}
