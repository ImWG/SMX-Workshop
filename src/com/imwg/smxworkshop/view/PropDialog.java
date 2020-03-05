package com.imwg.smxworkshop.view;

import java.awt.Button;
import java.awt.Checkbox;
import java.awt.Choice;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.Label;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.util.Properties;

public class PropDialog extends Dialog {
	
	private static final long serialVersionUID = 1L;
	private Class<?> dialogClass;
	protected Properties properties;
	private boolean confirmed = false;
	private ActionListener confirmedListener;
	
	protected void loadProperties(Class<?> c){
		properties = ViewConfig.viewProperties.get(c);
		this.dialogClass = c;
	}
	
	// For plug-in only
	protected void loadExternalProperties(Class<?> c){
		try {
			properties = new Properties();
			properties.load(c.getResourceAsStream(c.getSimpleName() + ".properties"));
			this.dialogClass = c;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public PropDialog(Frame owner) {
		super(owner);
	}
	
	protected PropDialog(Frame owner, Class<?> dialogClass) {
		super(owner);
		loadProperties(dialogClass);
		
		String langTitle = ViewConfig.getString(dialogClass.getSimpleName());
		if (langTitle != null)
			this.setTitle(langTitle);
		else
			this.setTitle(getPropertyText("root"));
	}
	
	final public Class<?> getDialogClass(){
		return dialogClass;
	}
	
	public void setBounds(){
		if (properties.getProperty("root.bounds") != null){
			String[] bounds = properties.getProperty("root.bounds").split("\\s+");
			Component owner = this.getParent();
			int width = Integer.parseInt(bounds[0]);
			int height = Integer.parseInt(bounds[1]);
			
			setBounds(owner.getX() + (owner.getWidth() - width) / 2,
					owner.getY() + (owner.getHeight() - height) / 2,
					width, height);
		}
		setLayout(null);
		setResizable(false);
	}
	
	public void loadDefaultEvents(){
		// Load Default buttons and events
		if (properties.getProperty("Button.ok.bounds") != null){
			this.addButton("Button.ok").addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					confirmed = true;
					dispose();
				}
			});
		}
		if (properties.getProperty("Button.cancel.bounds") != null){
			this.addButton("Button.cancel").addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					confirmed = false;
					dispose();
				}
			});
		}
		
		addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
            	confirmed = false;
            	dispose();
            }
        });
		
		addWindowListener(new WindowListener(){
			@Override
			public void windowOpened(WindowEvent e) {
				getOwner().setEnabled(false);
			}
			@Override
			public void windowClosed(WindowEvent e) {
				getOwner().setEnabled(true);
				getOwner().setVisible(true);
				if (confirmed){	
					onConfirmed();
					if (confirmedListener != null)
						confirmedListener.actionPerformed(null);
				}
			}
			
			@Override
			public void windowClosing(WindowEvent e) {}
			@Override
			public void windowIconified(WindowEvent e) {}
			@Override
			public void windowDeiconified(WindowEvent e) {}
			@Override
			public void windowActivated(WindowEvent e) {}
			@Override
			public void windowDeactivated(WindowEvent e) {}

		});
	}
	
	
	public void onConfirmed() {
		// To be overridden
	}
	
	public void setConfirmedListener(ActionListener listener){
		this.confirmedListener = listener;
	}
	
	
	public Component add(Component component, String name){
		if (properties.getProperty(name+".bounds") != null){
			String[] bounds = properties.getProperty(name+".bounds").split("\\s+");
			component.setBounds(
					Integer.parseInt(bounds[0]), Integer.parseInt(bounds[1]),
					Integer.parseInt(bounds[2]), Integer.parseInt(bounds[3]));
		}
		this.add(component);
		return component;
	}
	
	protected String getPropertyText(String name){
		if (properties.getProperty(name+".text") != null){
			return properties.getProperty(name+".text");
		}
		return "";
	}
	
	protected String getLanguageText(String name){
		if (name.startsWith("Common.")){
			String langText = ViewConfig.getString(name);
			if (langText != null)
				return langText; 
		}else{
			String langText = ViewConfig.getString(dialogClass.getSimpleName() + "." + name);
			if (langText != null){
				return langText;
			}else if (getPropertyText(name).length() > 0){
				name = getPropertyText(name);
				if (name.startsWith("Common.")){
					String langText1 = ViewConfig.getString(name);
					if (langText1 != null){
						return langText1;
					}
				}else if (ViewConfig.getString(name) != null){
					return ViewConfig.getString(name);
				}
			}
		}
		return name;
	}
	
	public Label addLabel(String name){
		Label label = new Label();
		label.setText(getLanguageText(name));
		this.add(label, name);
		return label;
	}
	
	public TextField addTextField(String name){
		TextField textField = new TextField();
		textField.setText(getLanguageText(name));
		this.add(textField, name);
		return textField;
	}
	
	public Checkbox addCheckbox(String name){
		Checkbox checkbox = new Checkbox();
		checkbox.setLabel(getLanguageText(name));
		this.add(checkbox, name);
		return checkbox;
	}
	
	public Button addButton(String name){
		Button button = new Button();
		button.setLabel(getLanguageText(name));
		this.add(button, name);
		return button;
	}
	
	public Choice addChoice(String name){
		Choice choice = new Choice();
		for (String item : getPropertyText(name).split("\\|")){
			choice.add(getLanguageText(item));
		}
		this.add(choice, name);
		return choice;
	}
	
}
