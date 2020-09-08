package com.imwg.smxworkshop.view;

import java.awt.Component;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class AdjustAngleDialog extends PropDialog {
	
	private NumberField anglesField; 
	private ClockPanel srcClock, dstClock;
	
	static public int angleCount = 8;
	static public int srcAngle = 0;
	static public int dstAngle = 0;
	static public boolean srcClockwise = true;
	static public boolean dstClockwise = true;

	public AdjustAngleDialog(Frame owner) {
		super(owner, AdjustAngleDialog.class);
		setBounds();
		loadDefaultEvents();
		
		add(srcClock = new ClockPanel(), "Canvas.srcClock");
		add(dstClock = new ClockPanel(), "Canvas.dstClock");
		add(anglesField = new NumberField(true), "TextField.angles");
		
		addLabel("Label.angles");
		anglesField.setRange(1, 256);
		
		anglesField.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				angleCount = anglesField.getInteger();
				adjust();
			}
		});
		
		anglesField.setText(angleCount);
		srcClock.setValue(srcAngle);
		dstClock.setValue(dstAngle);
		srcClock.setClockwise(srcClockwise);
		dstClock.setClockwise(dstClockwise);
		
		adjust();
	}
	
	private void adjust(){
		int angleCount = anglesField.getInteger();
		srcClock.setAngles(angleCount);
		dstClock.setAngles(angleCount);
	}
	
	@Override
	public void onConfirmed(){
		angleCount = anglesField.getInteger();
		srcAngle = srcClock.getValue();
		dstAngle = dstClock.getValue();
		srcClockwise = srcClock.isClockwise();
		dstClockwise = dstClock.isClockwise();
	}
	
	private class ClockPanel extends Component{
		static private final int CENTER_RADIUS = 32;
		static private final int MARKER_SIZE = 4;
		
		private int angles = 8;
		private int value = 0;
		private boolean clockwise = true;
		
		public int getAngles(){
			return angles;
		}
		public int getValue(){
			return value;
		}
		public boolean isClockwise(){
			return clockwise;
		}
		
		public void setAngles(int angles){
			this.angles = angles;
			repaint();
		}
		public void setValue(int value){
			this.value = value;
			repaint();
		}
		public void setClockwise(boolean clockwise){
			this.clockwise = clockwise;
			repaint();
		}
		
		ClockPanel(){
			super();
			addMouseListener(new MouseListener(){

				@Override
				public void mouseClicked(MouseEvent e) {
					int width = getWidth(), height = getHeight();
					int x0 = width / 2, y0 = height / 2;
					int dx = e.getX() - x0, dy = e.getY() - y0;
					if (dx * dx + dy * dy <= CENTER_RADIUS * CENTER_RADIUS){
						clockwise = !clockwise;
					}else{
						value = (int) (Math.round(
								Math.atan2(dy, dx) * angles / (2 * Math.PI)));
					}
					repaint();
				}

				@Override
				public void mousePressed(MouseEvent e) {}

				@Override
				public void mouseReleased(MouseEvent e) {}

				@Override
				public void mouseEntered(MouseEvent e) {}

				@Override
				public void mouseExited(MouseEvent e) {}
				
			});
		}
		
		@Override
		public void paint(Graphics g){
			int width = getWidth(), height = getHeight();
			int x0 = width / 2, y0 = height / 2;
			g.drawRoundRect(0, 0, width - 1, height - 1, width, height);
			g.drawRoundRect(x0 - CENTER_RADIUS, y0 - CENTER_RADIUS,
					CENTER_RADIUS * 2, CENTER_RADIUS * 2,
					CENTER_RADIUS * 2, CENTER_RADIUS * 2);
			
			for (int i = 0; i < angles; ++i){
				double angle = 2 * Math.PI * i/ angles;
				g.fillRect(
						x0 + (int) (Math.cos(angle) * (width / 2 - MARKER_SIZE / 2)) - MARKER_SIZE / 2,
						y0 + (int) (Math.sin(angle) * (width / 2 - MARKER_SIZE / 2)) - MARKER_SIZE / 2,
						MARKER_SIZE, MARKER_SIZE);
			}
			
			double angle = 2 * Math.PI * value / angles;
			g.drawLine(x0, y0, 
					x0 + (int) (Math.cos(angle) * width / 2),
					y0 + (int) (Math.sin(angle) * height / 2));
			
			int dangle = (int) (angle * 180 / Math.PI);
			int tangle = clockwise ? -90 : 90;  
			g.drawArc(x0 - width * 3 / 8, y0 - height * 3 / 8,
					width * 3 / 4, height * 3 / 4, -dangle, tangle);
		}
	}

}
