package com.imwg.smxworkshop.view;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.ScrollPane;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import com.imwg.smxworkshop.sprite.Sprite;
import com.imwg.smxworkshop.sprite.SpritePreview;

public class FrameListPanel extends ScrollPane{

	private static final long serialVersionUID = 5012857938535801357L;
	
	public static final int ITEM_SELECT_PRIME = 0;
	public static final int ITEM_SELECT = 1;
	public static final int ITEM_DESELECT = 2;
	public static final int ITEM_TOGGLE = 3;
	
	private boolean[] selected;	
	private Panel listChild;
	private SpritePreview preview;
	private FrameListItem[] items = new FrameListItem[0];
	private int perHeight;
	private int pressedKey = MouseEvent.NOBUTTON;
	
	public FrameListPanel(MainFrame mainFrame){
		super();
		this.preview = mainFrame.preview;
		listChild = new Panel();
		listChild.setLayout(new GridLayout(1, 1, 0, 1));
		this.add(listChild);
	}
	
	
	public void reload(){
		Sprite sprite = preview.getSprite();
		
		if (sprite != null){
			int frameCount = sprite.getFrameCount();
			if (frameCount == 0){
				listChild.removeAll();
				items = new FrameListItem[0];
				listChild.setPreferredSize(new Dimension(listChild.getWidth(), 0));
				
			}else{
				int frameCount0 = items.length;
				
				if (frameCount != frameCount0){
					perHeight = Math.min(64, 32767 / frameCount);
					listChild.setPreferredSize(new Dimension(listChild.getWidth(), perHeight * frameCount));
					
					GridLayout gl = (GridLayout) listChild.getLayout();
					gl.setRows(frameCount);
					listChild.setLayout(gl);
					
					FrameListItem[] items0 = items;
					items = new FrameListItem[frameCount];
					if (frameCount > frameCount0){
						for (int i=0; i<frameCount0; ++i){
							items[i] = items0[i];
							items[i].index = i;
						}
						for (int i=frameCount0; i<frameCount; ++i){ // Add
							FrameListItem item = new FrameListItem(i);
							item.setBackground(ViewConfig.backgroundColor);
							item.addMouseListener(new ItemMouseListener(i));
							listChild.add(item, i);
							items[i] = item;
						}
					}else{
						for (int i=0; i<frameCount; ++i){
							items[i] = items0[i];
							items[i].index = i;
						}
						for (int i=frameCount; i<frameCount0; ++i){ // Remove
							listChild.remove(items0[i]);
						}
					}
				}
			}
			selected = new boolean[frameCount];
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
			for (int i=0; i<selected.length; ++i){
				if (selected[i] && i < items.length)
					items[i].setBackground(ViewConfig.backgroundSelectedColor);
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
		if (index >= 0 && index < items.length){ 
			switch (mode){
			case ITEM_SELECT_PRIME:
				for (int i=0; i<items.length; ++i)
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
		setScrollPosition(0, perHeight * index);
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
		for (int i=0; i<items.length; ++i){
			if (selected[i])
				++length;
		}
		int[] ids = new int[length];
		int id = 0;
		for (int i=0; i<items.length; ++i){
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
			if (preview.getSprite().getFrame(index).getWidth(Sprite.DATA_IMAGE) > 0)
				im = preview.getFrameImage(index, Sprite.DATA_IMAGE);
			else
				im = preview.getFrameImage(index, Sprite.DATA_SHADOW);
				
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
			String indexString = Integer.toString(index); 
			g.drawString(indexString, 4, 12);
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
			if (pressedKey == MouseEvent.BUTTON1){
				if (e.isShiftDown()){ // Select continous
					int first = 0, last = selected.length - 1;
					for (; first < last && !selected[first]; ++first);
					
					if (first == last && !selected[last]){ // No one selected yet
						onSelect(index, ITEM_SELECT_PRIME);
						
					}else{		
						for (; !selected[last]; --last);
						
						if (index < first){ // Append from this to first
							for (int i=index; i<first; ++i)
								onSelect(i, ITEM_SELECT);
						}else if (index > last){ // Append from last to this
							for (int i=last+1; i<=index; ++i)
								onSelect(i, ITEM_SELECT);
						}else{ // Cut from first to this
							for (int i=first+1; i<=index; ++i)
								onSelect(i, ITEM_SELECT);
							for (int i=index+1; i<=last; ++i)
								onSelect(i, ITEM_DESELECT);
						}
					}
					
				}else if (e.isControlDown()){ // Select multi
					onSelect(index, ITEM_TOGGLE);
					
				}else{ // Select only one
					onSelect(index, ITEM_SELECT_PRIME);
					
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
		for (int i=0; i<items.length; ++i){
			selected[i] = true;
		}
		reselect();
	};
	
	public void selectInverse() {
		for (int i=items.length-1; i>=0; --i){
			selected[i] = !selected[i];
		}
		reselect();
	};

	
}
