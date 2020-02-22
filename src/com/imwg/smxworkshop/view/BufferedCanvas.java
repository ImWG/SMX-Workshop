package com.imwg.smxworkshop.view;

import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;

public class BufferedCanvas extends Canvas {

	private static final long serialVersionUID = 5095349676802449881L;
	
	private BufferedImage surface;
	public BufferedCanvas(){
		super();
		this.addComponentListener(new ComponentListener(){

			@Override
			public void componentResized(ComponentEvent e) {
				if (surface != null)
					if (getWidth() < surface.getWidth() && getHeight() < surface.getHeight())
						return;

				surface = new BufferedImage(getWidth()+64, getHeight()+64, BufferedImage.TYPE_INT_ARGB);
			}

			@Override
			public void componentMoved(ComponentEvent e) {}
			@Override
			public void componentShown(ComponentEvent e) {}
			@Override
			public void componentHidden(ComponentEvent e) {}
			
		});
	}
	
	@Override
	public void update(Graphics g) { // Prevent from flashing
		Graphics surfaceGraphics = surface.getGraphics(); 
		surfaceGraphics.setColor(getBackground());
		surfaceGraphics.fillRect(0, 0, getWidth(), getHeight());
		paint(surfaceGraphics);
		getGraphics().drawImage(surface, 0, 0, null);
	}
	
}
