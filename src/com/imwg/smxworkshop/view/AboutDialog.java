package com.imwg.smxworkshop.view;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Label;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import com.imwg.smxworkshop.model.Configuration;
import com.imwg.smxworkshop.model.MainModel;

public class AboutDialog extends PropDialog {

	private static final long serialVersionUID = 8114907285012892008L;
	
	public AboutDialog(MainFrame owner) {
		super(owner, AboutDialog.class);
		setBounds();
		loadDefaultEvents();
		
		Canvas logo = new Canvas(){
			private static final long serialVersionUID = -5270379074129130301L;
			Image logoImage = Toolkit.getDefaultToolkit().createImage(
					AboutDialog.class.getResource("resource/about.png"));
			@Override
			public void paint(Graphics g){
				g.drawImage(logoImage, 0, 0, this);
			}
		};
		this.add(logo, "Canvas.logo");
		
		this.addLabel("Label.info");
		
		Label nameLabel = this.addLabel("Label.name");
		nameLabel.setText(nameLabel.getText() + Configuration.VERSION);
		
		this.addLabel("Label.git");
		Label gitLabel = this.addLabel("Label.gitLink");
		gitLabel.setText(Configuration.gitHubPath);
		gitLabel.setForeground(Color.BLUE);
		gitLabel.addMouseListener(new MouseListener(){

			@Override
			public void mouseClicked(MouseEvent e) {
				// Open My Repository at GitHub
				// Referred from https://blog.csdn.net/weixin_43445841/article/details/88769621
				if (java.awt.Desktop.isDesktopSupported()) {
					try {
						java.net.URI uri = java.net.URI.create(Configuration.gitHubPath);
						java.awt.Desktop dp = java.awt.Desktop.getDesktop();
						if (dp.isSupported(java.awt.Desktop.Action.BROWSE)) {
							dp.browse(uri);
						}
					} catch (Exception er) {
						er.printStackTrace();
					}
				}
			}

			@Override
			public void mousePressed(MouseEvent e) {}

			@Override
			public void mouseReleased(MouseEvent e) {}

			@Override
			public void mouseEntered(MouseEvent e) {
				setCursor(new Cursor(Cursor.HAND_CURSOR));
			}

			@Override
			public void mouseExited(MouseEvent e) {
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
			
		});

	}


}
