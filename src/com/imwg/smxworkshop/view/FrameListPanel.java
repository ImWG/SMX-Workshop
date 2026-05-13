package com.imwg.smxworkshop.view;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.ScrollPane;
import java.awt.Scrollbar;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;

import com.imwg.smxworkshop.sprite.Sprite;
import com.imwg.smxworkshop.sprite.SpritePreview;

public class FrameListPanel extends Panel {

	private static final long serialVersionUID = 5012857938535801357L;
	
	public static final int ITEM_SELECT_PRIME = 0;
	public static final int ITEM_SELECT = 1;
	public static final int ITEM_DESELECT = 2;
	public static final int ITEM_TOGGLE = 3;
	
	private boolean[] selected;	
	private Panel listChild;
	private Scrollbar scrollbar;
	private SpritePreview preview;
	private FrameListItem[] items = new FrameListItem[ROWS];
	private int pressedKey = MouseEvent.NOBUTTON;
	private int currentScrollIndex = 0;
	
	static private final int ROWS = 10;
	
	public FrameListPanel(MainFrame mainFrame){
		super();
		this.preview = mainFrame.preview;
		listChild = new Panel();
		listChild.setLayout(new GridLayout(ROWS, 1, 0, 1));
		this.add(listChild);
		scrollbar = new Scrollbar();
		scrollbar.setOrientation(Scrollbar.VERTICAL);
		this.add(scrollbar);
		BorderLayout layout = new BorderLayout();
		this.setLayout(layout);
		layout.addLayoutComponent(listChild, BorderLayout.CENTER);
		layout.addLayoutComponent(scrollbar, BorderLayout.EAST);
		scrollbar.addAdjustmentListener(new AdjustmentListener() {
			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				scrollTo(scrollbar.getValue());
			}
		});
		this.addMouseWheelListener(new MouseWheelListener() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				scrollTo(currentScrollIndex + e.getWheelRotation());
			}
		});

		for (int i=0; i<ROWS; ++i){ // Add
			FrameListItem item = new FrameListItem(i);
			item.setBackground(ViewConfig.backgroundColor);
			item.addMouseListener(new ItemMouseListener(i));
			listChild.add(item, i);
			item.setVisible(false);
			items[i] = item;
		}
	}
	
	
	public void reload(){
		Sprite sprite = preview.getSprite();
		
		if (sprite != null){
			int frameCount = sprite.getFrameCount();
			int gridCount = Math.min(ROWS, frameCount);
			scrollbar.setMinimum(0);
			scrollbar.setMaximum(Math.max(1, frameCount - ROWS + 1));
			for (int i=0; i<gridCount; ++i){
				items[i].setVisible(true);
			}
			for (int i=gridCount; i<ROWS; ++i){
				items[i].setVisible(false);
			}
			selected = new boolean[frameCount];
			scrollTo(scrollbar.getValue());
		}else{
			listChild.removeAll();
			items = new FrameListItem[0];
			selected = new boolean[0];
		}
		listChild.revalidate();
	}
	
	public void refresh(){
		for (int i=0; i<items.length; ++i){
			items[i].repaint();
		}
	}
	
	public void refresh(int i){// TODO
		if (i >= 0 && i < items.length)
			items[i].repaint();
	}
	
	public void reselect(){
		if (items.length > 0){
			for (FrameListItem item : items){
				if (item != null)
					if (item.getBackground() != ViewConfig.backgroundColor)
						item.setBackground(ViewConfig.backgroundColor);
			}
			for (FrameListItem item : items){
				int index = item.index + this.currentScrollIndex;
				if (index < selected.length && selected[index])
					item.setBackground(ViewConfig.backgroundSelectedColor);
			}
		}
	}
	
	@Override
	public void setBackground(Color c){
		super.setBackground(Color.GRAY);
		for (int i=0; i<items.length; ++i){
			items[i].setBackground(c);
		}
	}
	
	/**
	 * Select one item.
	 * @param index Index of item to select 
	 * @param mode Select mode: ITEM_SELECT_PRIME, ITEM_SELECT,
	 * 		ITEM_DESELECT or ITEM_TOGGLE.
	 */
	public void onSelect(int index, int mode){
		if (index >= 0 && index < selected.length){ 
			switch (mode){
			case ITEM_SELECT_PRIME:
				for (int i=0; i<selected.length; ++i)
					selected[i] = false;
				selected[index] = true;
				break;
			case ITEM_SELECT:		
				selected[index] = true;
				break;
			case ITEM_DESELECT:
				selected[index] = false;
				break;
			case ITEM_TOGGLE:
				selected[index] = !selected[index];
				break;
			}
			reselect();
		}
	}
	
	public void scrollTo(int index){
		index = Math.max(0, Math.min(index, scrollbar.getMaximum() - 1));
		scrollbar.setValue(index);
		if (this.currentScrollIndex != index) {
			this.currentScrollIndex = index;
			refresh();
			reselect();	
		}
	}
	
	public void deselectAll(){
		if (selected.length > 0){
			for (int i=0; i<selected.length; ++i)
				selected[i] = false;
		}
	}
	
	public void select(int id){
		deselectAll();
		if (id < selected.length)
			selected[id] = true;
		reselect();
	}
	
	public void select(int[] ids){
		deselectAll();
		for (int i=0; i<ids.length; ++i){
			if (ids[i] < selected.length)
				selected[ids[i]] = true;
		}
		reselect();
	}
	
	int[] getSelected(){
		int length = 0;
		for (int i=0; i<selected.length; ++i){
			if (selected[i])
				++length;
		}
		int[] ids = new int[length];
		int id = 0;
		for (int i=0; i<selected.length; ++i){
			if (selected[i]){
				ids[id] = i; ++id;
			}
		}
		return ids;
	}
	
	public class FrameListItem extends Canvas{

		private static final long serialVersionUID = -7117197427867872103L;
		
		int index;
		public FrameListItem(int index){
			super();
			this.index = index;
		}
		
		public void paint(Graphics g) {
			BufferedImage im;
			int actualIndex = index + currentScrollIndex;
			if (preview.getSprite().getFrameCount() <= actualIndex) {
				g.clearRect(0, 0, this.getWidth(), this.getHeight());
			}else {
				if (preview.getSprite().getFrame(actualIndex).getWidth(Sprite.DATA_IMAGE) > 0)
					im = preview.getFrameImage(actualIndex, Sprite.DATA_IMAGE);
				else
					im = preview.getFrameImage(actualIndex, Sprite.DATA_SHADOW);
					
				if (im != null){ 
					int width = im.getWidth(), height = im.getHeight();
					int myWidth = this.getWidth() - 16; // Subtracted by scroll bar width
					if (width > myWidth || height > this.getHeight()){
						double rate = Math.min((double)myWidth / width, 
								(double)this.getHeight() / height);
						int width1 = (int)(width*rate);
						g.drawImage(im, (myWidth - width1) / 2,	0, width1, (int)(height*rate), null);
					}else{
						g.drawImage(im, (myWidth - width) / 2, 0, null);
					}
					//g.drawImage(SpritePreview.paletteImages[preview.sprite.getFrame(index).palette],
					//		0, 0, null);
				}
				g.setColor(Color.WHITE);
				String indexString = Integer.toString(actualIndex); 
				g.drawString(indexString, 4, 12);
			}
		}
	}
	
	private class ItemMouseListener implements MouseListener{
		private int index;

		public ItemMouseListener(int index){
			this.index = index;
		}
		
		@Override
		public void mouseClicked(MouseEvent e) {}

		@Override
		public void mousePressed(MouseEvent e) {
			pressedKey = e.getButton();
			int actualIndex = index + currentScrollIndex;
			if (pressedKey == MouseEvent.BUTTON1){
				if (e.isShiftDown()){ // Select continous
					int first = 0, last = selected.length - 1;
					for (; first < last && !selected[first]; ++first);
					
					if (first == last && !selected[last]){ // No one selected yet
						onSelect(actualIndex, ITEM_SELECT_PRIME);
						
					}else{		
						for (; !selected[last]; --last);
						
						if (actualIndex < first){ // Append from this to first
							for (int i=actualIndex; i<first; ++i)
								onSelect(i, ITEM_SELECT);
						}else if (actualIndex > last){ // Append from last to this
							for (int i=last+1; i<=actualIndex; ++i)
								onSelect(i, ITEM_SELECT);
						}else{ // Cut from first to this
							for (int i=first+1; i<=actualIndex; ++i)
								onSelect(i, ITEM_SELECT);
							for (int i=actualIndex+1; i<=last; ++i)
								onSelect(i, ITEM_DESELECT);
						}
					}
					
				}else if (e.isControlDown()){ // Select multi
					onSelect(actualIndex, ITEM_TOGGLE);
					
				}else{ // Select only one
					onSelect(actualIndex, ITEM_SELECT_PRIME);
					
				}
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			pressedKey = MouseEvent.NOBUTTON;
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			if (pressedKey == MouseEvent.BUTTON1){
				if (e.isControlDown()) {// Select multi
					onSelect(index, ITEM_TOGGLE);
				}
			}
		}

		@Override
		public void mouseExited(MouseEvent e) {}
	}

	public void selectAll() {
		for (int i=0; i<selected.length; ++i){
			selected[i] = true;
		}
		reselect();
	};
	
	public void selectInverse() {
		for (int i=selected.length-1; i>=0; --i){
			selected[i] = !selected[i];
		}
		reselect();
	};

	
}
