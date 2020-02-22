package com.imwg.smxworkshop.view;

import java.awt.Checkbox;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class ScaleDialog extends PropDialog {
	
	private static final long serialVersionUID = -1266686594291975337L;
	private NumberField xField, yField;
	private Checkbox ratioCheckbox, interpolateCheckbox;
	static public double xFactor = 1, yFactor = 1;
	static public boolean interpolate;
	
	public ScaleDialog(MainFrame owner) {
		super(owner, ScaleDialog.class);
		setBounds();
		loadDefaultEvents();
		
		this.add(xField = new NumberField(false), "TextField.x");
		this.add(yField = new NumberField(false), "TextField.y");
		yField.setEnabled(false);
		
		this.addLabel("Label.x");
		this.addLabel("Label.y");
		
		ratioCheckbox = this.addCheckbox("Checkbox.ratio");
		ratioCheckbox.setState(true);
		ratioCheckbox.addItemListener(new ItemListener(){
			@Override
			public void itemStateChanged(ItemEvent e) {
				yField.setEnabled(!((Checkbox) e.getItemSelectable()).getState());
			}
		});
		interpolateCheckbox = this.addCheckbox("Checkbox.interpolate");
		
		xField.setText(xFactor);
		yField.setText(yFactor);
		interpolateCheckbox.setState(interpolate);
		
	}
	
	public void onConfirmed() {
		xFactor = xField.getDouble();
		if (ratioCheckbox.getState())
			yFactor = xFactor;
		else
			yFactor = yField.getDouble();
		
		interpolate = interpolateCheckbox.getState();
	}

}
