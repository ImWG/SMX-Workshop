package com.imwg.smxworkshop.view;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Label;
import java.awt.Panel;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.AWTEventListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.filechooser.FileView;

import com.imwg.smxworkshop.model.Configuration;
import com.imwg.smxworkshop.model.MainModel;
import com.imwg.smxworkshop.sprite.Sprite;
import com.imwg.smxworkshop.sprite.SpriteIO;
import com.imwg.smxworkshop.sprite.SpritePreview;

@SuppressWarnings("serial")
public class MainFrame extends Frame {
	
	private MainModel model;
	private Dialog processDialog;
	
	Sprite sprite;
	SpritePreview preview = new SpritePreview();
	Label fileLabel, statusLabel;
	FrameListPanel listPanel;
	NumberField speedField;
	Canvas canvas;
	
	public int displayFlags = 0x3;
	public int scaleRate = 100;
	public double canvasCenterX = .5, canvasCenterY = .5;
	public double dragX = 0, dragY = 0;
	public boolean dragging = false;
	public int mode = MODE_NORMAL;
	public int current = 0;
	public boolean animated = false;
	public boolean animateLoop = true;
	public String currentFile = null;
	private boolean processing;
	
	public boolean hasShownSLPHintDialog = false;
	
	static private String processString;
	static private boolean processBreakable;
	
	static public String currentSpritePath = ".";
	static public String currentImagePath = ".";
	
	static final public int MODE_NORMAL = 0;
	static final public int MODE_SETANCHOR = 1; 
	
	static final public void setProcessString(String s){ // For loading message
		processString = s;
		processBreakable = false;
	}
	
	public MainModel getModel(){
		return model;
	}
	
	public Sprite getSprite(){
		return sprite;
	}
	
	public SpritePreview getPreview(){
		return preview;
	}
	
	public FrameListPanel getListPanel(){
		return listPanel;
	}
	
	public Canvas getCanvas(){
		return canvas;
	}
	
	private Runnable runAnimation = new Runnable(){
		@Override
		public void run() {
			try {
				if (!animateLoop && current >= sprite.getFrameCount() - 1){
					current = 0;
					canvas.repaint();
					listPanel.select(current);
					listPanel.scrollTo(current);
					refreshStatusBar();
				}
				while (animated){
					Thread.sleep(Configuration.getAnimationSpeed());
					++current;
					if (current >= sprite.getFrameCount()){
						if (!animateLoop){
							current = sprite.getFrameCount() - 1;
							animated = false;
							return;
						}else{
							current = 0;
						}
					}
					canvas.repaint();
					listPanel.select(current);
					listPanel.scrollTo(current);
					refreshStatusBar();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	};
	
	private AWTEventListener keyListener = new AWTEventListener(){

		@Override
		public void eventDispatched(AWTEvent event) {
			if (!isActive())
				return;
			
			if (event.getID() == KeyEvent.KEY_PRESSED){
				KeyEvent keyEvent = (KeyEvent) event;
				int pressedKey = keyEvent.getKeyCode();
				switch (pressedKey){
				case KeyEvent.VK_PAGE_UP:
					if (current > 0){
						if (keyEvent.isControlDown())
							current = 0;
						else
							--current;
						listPanel.onSelect(current, FrameListPanel.ITEM_SELECT_PRIME);
						listPanel.scrollTo(current);
						canvas.repaint(); 
					}
					break;
				case KeyEvent.VK_PAGE_DOWN:
					if (current < sprite.getFrameCount() - 1){
						if (keyEvent.isControlDown())
							current = sprite.getFrameCount() - 1;
						else
							++current;
						listPanel.onSelect(current, FrameListPanel.ITEM_SELECT_PRIME);
						listPanel.scrollTo(current);
						canvas.repaint(); 
					}
					break;
				case KeyEvent.VK_LEFT:
					if (keyEvent.isControlDown()){
						model.setAnchor(sprite, getSelectedFrames(), -1, 1, 0, true);
						canvas.repaint(); 
					}
					break;
				case KeyEvent.VK_RIGHT:
					if (keyEvent.isControlDown()){
						model.setAnchor(sprite, getSelectedFrames(), -1, -1, 0, true);
						canvas.repaint(); 
					}
					break;
				case KeyEvent.VK_UP:
					if (keyEvent.isControlDown()){
						model.setAnchor(sprite, getSelectedFrames(), -1, 0, 1, true);
						canvas.repaint();
					}
					break;
				case KeyEvent.VK_DOWN:
					if (keyEvent.isControlDown()){
						model.setAnchor(sprite, getSelectedFrames(), -1, 0, -1, true);
						canvas.repaint(); 
					}
					break;
				case KeyEvent.VK_ESCAPE:
					if (MainFrame.this.mode == MainFrame.MODE_SETANCHOR) {
						MainFrame.this.mode = MainFrame.MODE_NORMAL;
						MainFrame.this.canvas.setCursor(null);
					}
					break;
				}
				
			}
		}
		
	};
	
	
	public void loadSprite(File file){
		if (file != null){
			this.loadSprite(SpriteIO.loadFromFile(file));
			currentFile = file.getAbsolutePath(); 
			refreshStatusBar();
		}
	}
	
	public void loadSprite(Sprite sprite){
		this.sprite = sprite;
		preview.setSprite(sprite);
		currentFile = null;
		this.reload();
	}
	
	public void reload(){
		if (sprite == null)
			return;
		
		int frameCount = sprite.getFrameCount();
		if (current >= frameCount)
			current = frameCount - 1;
		else if (current < 0)
			current = 0;
		listPanel.reload();
		this.refreshAll();
	}
		
	public void refreshStatusBar(){
		fileLabel.setText(currentFile);
		if (sprite.getFrameCount() == 0)
			statusLabel.setText("No frames");
		else
			statusLabel.setText(String.format("F#%d/%d, P#%d", current,
					sprite.getFrameCount(), sprite.getFrame(current).getPalette()));
	}
	
	public void refreshAll(){
		if (sprite != null){
			refreshStatusBar();
			preview.refresh();
			canvas.repaint();
			((MainMenu)getMenuBar()).refreshCheckboxMenuItems();
			new Thread(new Runnable(){
				public void run(){
					if (sprite.getFrameCount() > 0){
						for (int i = 0; i < sprite.getFrameCount(); ++i){
							while (!preview.getFrameStatus(i));
							
							listPanel.refresh(i);
							if (i == current){
								canvas.repaint();
							}
						}
					}
				}
				
			}).start();
		}
	}
	
	public int[] getSelectedFrames(){
		if (sprite.getFrameCount() == 0)
			return new int[0];
		
		int[] ids = listPanel.getSelected();
		if (ids.length == 0)
			return new int[]{current};
		return ids;
	}
	
	public void toggleAnimationMode(){
		if (sprite != null){
			if (animated = !animated)
				new Thread(runAnimation).start();
			((MainMenu) this.getMenuBar()).refreshCheckboxMenuItems();
		}
	}
		
	public void popupProcessDialog(){
		processDialog = new Dialog(this);
		processDialog.setBounds(this.getX() + getWidth() / 2 - 100,
				getY() + getHeight() / 2 - 50, 200, 100);
		processDialog.setUndecorated(true);
		
		processDialog.setVisible(true);
		processing = true;
		processBreakable = false;
		this.setEnabled(false);
		new Thread(new Runnable(){
			@Override
			public void run() {
				final int delayLimit = 200;
				int delayed = 0;
				Label processInfo = new Label();
				processInfo.setBounds(16, 38, 168, 24);
				Label processHint = new Label();
				processHint.setBounds(16, 64, 168, 24);
				processHint.setText("Press Pause to Break...");
				processHint.setVisible(false);
				processDialog.add(processInfo);
				processDialog.add(processHint);
				processDialog.getToolkit().addAWTEventListener(new AWTEventListener(){
					@Override
					public void eventDispatched(AWTEvent event) {
						if (processBreakable){
							if (((KeyEvent) event).getKeyCode() == KeyEvent.VK_PAUSE)
								closeProcessDialog();
						}
					}
				}, AWTEvent.KEY_EVENT_MASK);
				try {
					while (processing){
						processInfo.setText(processString);
						if (!processBreakable){
							if (++delayed >= delayLimit){ // Bug splat
								processBreakable = true;
								processHint.setVisible(true);
								delayed = 0;
							}else{
								processHint.setVisible(false);
							}
						}
						Thread.sleep(20);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}
	public void closeProcessDialog(){
		processDialog.setVisible(true);
		processDialog.dispose();
		processing = false;
		this.setEnabled(true);
	}
	
	public void addRecentFile(String file){
		Configuration.addRecentFile(file);
		((MainMenu) getMenuBar()).setRencentFilesMenu();
	}
	
	
	public MainFrame() {
		super("SMX Workshop " + Configuration.VERSION + " By WAIFor");
		
		final MainFrame mainFrame = this;
		this.model = new MainModel(this);
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((int)screenSize.getWidth()/2 - 320, (int)screenSize.getHeight()/2 - 240, 640, 480);
		setLayout(new BorderLayout());
		
		addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
            	exit();
            }
        });
		
		canvas = new BufferedCanvas(){
			@Override
			public void paint(Graphics g){
				if (sprite == null)
					return;
				
				Sprite.Frame frame = sprite.getFrame(current);
				if (frame != null){
					int x0 = (int) (canvasCenterX * this.getWidth());
					int y0 = (int) (canvasCenterY * this.getHeight());
					g.setPaintMode();
					if ((displayFlags & 1) != 0){
						g.drawImage(preview.getFrameImage(current, Sprite.DATA_IMAGE),
								x0 - frame.getAnchorX(Sprite.DATA_IMAGE) * scaleRate / 100,
								y0 - frame.getAnchorY(Sprite.DATA_IMAGE) * scaleRate / 100,
								frame.getWidth(Sprite.DATA_IMAGE) * scaleRate / 100,
								frame.getHeight(Sprite.DATA_IMAGE) * scaleRate / 100,
								null);
					}
					if ((displayFlags & 8) != 0){
						g.drawImage(preview.getFrameImage(current, Sprite.DATA_SMUDGE),
								x0 - frame.getAnchorX(Sprite.DATA_SMUDGE) * scaleRate / 100,
								y0 - frame.getAnchorY(Sprite.DATA_SMUDGE) * scaleRate / 100,
								frame.getWidth(Sprite.DATA_SMUDGE) * scaleRate / 100,
								frame.getHeight(Sprite.DATA_SMUDGE) * scaleRate / 100,
								null);
					}
					if ((displayFlags & 2) != 0){
						g.drawImage(preview.getFrameImage(current, Sprite.DATA_SHADOW),
								x0 - frame.getAnchorX(Sprite.DATA_SHADOW) * scaleRate / 100,
								y0 - frame.getAnchorY(Sprite.DATA_SHADOW) * scaleRate / 100,
								frame.getWidth(Sprite.DATA_SHADOW) * scaleRate / 100,
								frame.getHeight(Sprite.DATA_SHADOW) * scaleRate / 100,
								null);
					}
					if ((displayFlags & 4) != 0){
						g.drawImage(preview.getFrameImage(current, Sprite.DATA_OUTLINE),
								x0 - frame.getAnchorX(Sprite.DATA_OUTLINE) * scaleRate / 100,
								y0 - frame.getAnchorY(Sprite.DATA_OUTLINE) * scaleRate / 100,
								frame.getWidth(Sprite.DATA_OUTLINE) * scaleRate / 100,
								frame.getHeight(Sprite.DATA_OUTLINE) * scaleRate / 100,
								null);
					}
					if ((displayFlags & 0x10) != 0){ // Borders
						if ((displayFlags & 1) != 0){
							g.setColor(new Color(0xff00ff));
							g.drawRect(x0 - frame.getAnchorX(Sprite.DATA_IMAGE) * scaleRate / 100 - 1,
									y0 - frame.getAnchorY(Sprite.DATA_IMAGE) * scaleRate / 100 - 1,
									frame.getWidth(Sprite.DATA_IMAGE) * scaleRate / 100 + 1,
									frame.getHeight(Sprite.DATA_IMAGE) * scaleRate / 100 + 1);
						}
						if ((displayFlags & 2) != 0){
							g.setColor(new Color(0xffff00));
							g.drawRect(x0 - frame.getAnchorX(Sprite.DATA_SHADOW) * scaleRate / 100 - 1,
									y0 - frame.getAnchorY(Sprite.DATA_SHADOW) * scaleRate / 100 - 1,
									frame.getWidth(Sprite.DATA_SHADOW) * scaleRate / 100 + 1,
									frame.getHeight(Sprite.DATA_SHADOW) * scaleRate / 100 + 1);
						}
						if ((displayFlags & 4) != 0){
							g.setColor(new Color(0x00ffff));
							g.drawRect(x0 - frame.getAnchorX(Sprite.DATA_OUTLINE) * scaleRate / 100 - 1,
									y0 - frame.getAnchorY(Sprite.DATA_OUTLINE) * scaleRate / 100 - 1,
									frame.getWidth(Sprite.DATA_OUTLINE) * scaleRate / 100 + 1,
									frame.getHeight(Sprite.DATA_OUTLINE) * scaleRate / 100 + 1);
						}
					}
					g.setColor(Color.BLACK);
					g.setXORMode(Color.WHITE);
					int anchorSize = Configuration.getAnchorSize();
					g.drawLine(x0-anchorSize*2, y0-anchorSize, x0+anchorSize*2, y0+anchorSize);
					g.drawLine(x0+anchorSize*2, y0-anchorSize, x0-anchorSize*2, y0+anchorSize);
				}
			}
		};
		canvas.setBackground(ViewConfig.backgroundColor);
		
		Panel CenterPanel = new Panel(new BorderLayout());
		Panel rightPanel = new Panel(new BorderLayout());
		
		listPanel = new FrameListPanel(this){
			@Override
			public void onSelect(int index, int mode){
				super.onSelect(index, mode);
				if (mode == 0){
					mainFrame.current = index;
					((MainMenu)mainFrame.getMenuBar()).refreshCheckboxMenuItems();
					canvas.repaint();
					refreshStatusBar();
				}
			}
		};
		listPanel.setVisible(true);
		listPanel.setPreferredSize(new Dimension(150, getHeight()));
		
		listPanel.setBackground(ViewConfig.backgroundColor);
		speedField = new NumberField(true);
		speedField.setRange(0, null);
		speedField.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				Configuration.setAnimationSpeed(speedField.getInteger());
			}
		});
		speedField.addMouseWheelListener(new MouseWheelListener(){
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				speedField.setText(speedField.getInteger() - e.getWheelRotation() * 5);
				speedField.adjust();
				Configuration.setAnimationSpeed(speedField.getInteger());
			}
			
		});
		speedField.setText(Configuration.getAnimationSpeed());
		rightPanel.add(listPanel, BorderLayout.CENTER);
		rightPanel.add(speedField, BorderLayout.SOUTH);
		
		
		canvas.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON1){
					if (mode == MODE_SETANCHOR){ // Set Anchor by Click
						mode = MODE_NORMAL;
						int dx = (int) (e.getX() - canvasCenterX * canvas.getWidth()) * 100 / scaleRate;
						int dy = (int) (e.getY() - canvasCenterY * canvas.getHeight()) * 100 / scaleRate;
						model.setAnchor(sprite, getSelectedFrames(), -1, dx, dy, true);
						canvas.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
						canvas.repaint();
						
					}else{
						toggleAnimationMode();
					}
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3){ // Start Dragging
					dragX = e.getX(); dragY = e.getY();
					if (mode == MODE_SETANCHOR)
						canvas.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
					else if (mode == MODE_NORMAL)
						canvas.setCursor(new Cursor(Cursor.MOVE_CURSOR));
				}
			}
			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3){ // Stop Dragging
					if (mode == MODE_SETANCHOR){ // Drag Anchor
						mode = MODE_NORMAL;
						int dx = (int)(dragX - e.getX());
						int dy = (int)(dragY - e.getY());
						model.setAnchor(sprite, getSelectedFrames(), -1, dx, dy, true);
						canvas.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
						canvas.repaint();
						
					}else if (mode == MODE_NORMAL){ // Drag Viewport
						canvasCenterX += (e.getX() - dragX) / canvas.getWidth();
						canvasCenterY += (e.getY() - dragY) / canvas.getHeight();
						canvas.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
						canvas.repaint();
						
					}
				}
			}
			@Override
			public void mouseEntered(MouseEvent e) {}
			@Override
			public void mouseExited(MouseEvent e) {}
			
		});
		canvas.addMouseWheelListener(new MouseWheelListener(){
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				scaleRate -= e.getWheelRotation() * 5;
				scaleRate = Math.max(scaleRate, 0);
				canvas.repaint();
			}
			
		});
		
		CenterPanel.add(canvas, BorderLayout.CENTER);
		CenterPanel.add(rightPanel, BorderLayout.EAST);
				
		add(CenterPanel, BorderLayout.CENTER);

		new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, new DropTargetListener(){

			@Override
			public void dragEnter(DropTargetDragEvent dtde) {
			}

			@Override
			public void dragOver(DropTargetDragEvent dtde) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void dropActionChanged(DropTargetDragEvent dtde) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void dragExit(DropTargetEvent dte) {
				// TODO Auto-generated method stub
				
			}

			@Override // TODO
			public void drop(DropTargetDropEvent dtde) {
				dtde.acceptDrop(DnDConstants.ACTION_REFERENCE);
		        Transferable tf = dtde.getTransferable();
		        try {
		            @SuppressWarnings("unchecked")
		            final java.util.List<File> list = 
							(List<File>) tf.getTransferData(DataFlavor.javaFileListFlavor);
		            File firstFile = list.get(0);
		            String firstName = firstFile.getName().toUpperCase(); 
		            if (firstName.endsWith(".BMP") || firstName.endsWith(".JPG") || firstName.endsWith(".PNG")
		            		|| firstName.endsWith(".GIF")){ // Import from images
		            	
						ImportImagesDialog dialog = new ImportImagesDialog(mainFrame);
						dialog.setConfirmedListener(new ActionListener(){
							@Override
							public void actionPerformed(ActionEvent e) {
								File[] files = (File[]) list.toArray();
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
						
		            }else{ // Open sprtie file
						String fname = firstFile.getAbsolutePath();
						popupProcessDialog();
						loadSprite(firstFile);
						closeProcessDialog();
						currentSpritePath = fname;
						mainFrame.addRecentFile(fname);
		            }
		            
	        	} catch (Exception e) {
	        		//e.printStackTrace();
	        	}

			}
			
		}, true, null);
		
		Panel statusBar = new Panel(new BorderLayout());
		statusBar.setBackground(Color.LIGHT_GRAY);
		fileLabel = new Label("");
		statusBar.add(fileLabel, BorderLayout.CENTER);
		statusLabel = new Label("Ready");
		statusBar.add(statusLabel, BorderLayout.EAST);
		statusLabel.setPreferredSize(new Dimension(160, 20));
		this.add(statusBar, BorderLayout.SOUTH);
		
		MainMenu menu = new MainMenu();
		menu.setPaletteMenu();
		menu.setRencentFilesMenu();
		this.setMenuBar(menu);
		menu.refreshCheckboxMenuItems();
		
		this.setIconImage(SpritePreview.paletteImages[0]);
		
		getToolkit().addAWTEventListener(keyListener, AWTEvent.KEY_EVENT_MASK);
		
		setVisible(true);
		
	}
	
	static public String currentFileFormat = "smx"; 
	
	static private File getFileDirectory(String path) {
		// If the file is lost, it'd jump to app path.
		return new File(path.replaceFirst("[^/\\\\]+$", ""));
	}
	
	public File popupChooseSpriteFile(int type){
		final JFileChooser fd = new JFileChooser();
		fd.setDialogType(type);
		fd.setFileView(new FileView() {
		    public Icon getIcon(File f) {
		    	return fd.getFileSystemView().getSystemIcon(f);
		    }
		});
		
		FileNameExtensionFilter ff = new FileNameExtensionFilter("Supported Files", "smx", "slp","smp");
		Map<FileFilter, String> formats = new LinkedHashMap<FileFilter, String>(); // Ordered
		formats.put(ff, "");
		formats.put(new FileNameExtensionFilter("SLP File", "SLP"), "slp");
		formats.put(new FileNameExtensionFilter("SMP File", "SMP"), "smp");
		formats.put(new FileNameExtensionFilter("SMX File", "SMX"), "smx");
		
		for (Entry<FileFilter, String> filter : formats.entrySet()){
			fd.setFileFilter(filter.getKey());
		} 
		fd.setFileFilter(ff);
		
		fd.setCurrentDirectory(getFileDirectory(MainFrame.currentSpritePath));
		fd.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fd.setMultiSelectionEnabled(false);
		
		if (fd.showDialog(this, null) == JFileChooser.APPROVE_OPTION){
			if (fd.getSelectedFile() != null) {
				File file = fd.getSelectedFile();
				if (type == JFileChooser.SAVE_DIALOG){
					String filePath = file.getAbsolutePath();
					currentFileFormat = formats.get(fd.getFileFilter());
					if (!file.getName().contains(".")){ // Auto complete
						if (fd.getFileFilter() instanceof FileNameExtensionFilter)
							filePath += "." + ((FileNameExtensionFilter)fd.getFileFilter()).getExtensions()[0];
						else // Default
							filePath += ".smx";
						
						file = new File(filePath);
					}
				}
				return file;
			}
		}
		return null;
	}
	
	public File[] popupChooseImagesFile(int type){
		final JFileChooser fd = new JFileChooser();
		fd.setDialogType(type);
		fd.setFileFilter(new FileNameExtensionFilter("Image Files", "BMP", "JPG", "PNG", "GIF"));
		fd.setFileView(new FileView() {
		    public Icon getIcon(File f) {
		    	return fd.getFileSystemView().getSystemIcon(f);
		    }
		});
		fd.setCurrentDirectory(getFileDirectory(MainFrame.currentImagePath));
		fd.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fd.setMultiSelectionEnabled(type == JFileChooser.OPEN_DIALOG);
		
		if (fd.showDialog(this, null) == JFileChooser.APPROVE_OPTION){
			if (type == JFileChooser.SAVE_DIALOG){
				if (fd.getSelectedFile() != null);
					return new File[]{fd.getSelectedFile()};
			}else{
				File[] files = fd.getSelectedFiles();
				if (files != null) {
					if (files.length > 0)
						return files;
				}
			}
		}
		return null;
	}

	public void exit() {
		MainModel.exit();
        System.exit(0);		
	}

}
