package com.imwg.smxworkshop.view;

import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class NumberField extends TextField {

	private static final long serialVersionUID = 1L;
	private boolean integer;
	private Number min, max;
	
	public boolean isInteger(){
		return integer;
	}
	
	private void addListener(){
		this.addFocusListener(new FocusListener(){
			@Override
			public void focusGained(FocusEvent e) {}
			@Override
			public void focusLost(FocusEvent event) { // Convert current string to integer
				try{
					String s = getText();
					if (s.length() == 0){
						setText("0");
						
					} else if (integer) { // Integer
						char c = s.charAt(0);
						int len = 1;
						if (c == '+' || c == '-' || c >= '0' && c <= '9'){
							for (; len<s.length(); ++len){
								c = s.charAt(len);
								if (!(c >= '0' && c <= '9'))
									break;
							}
							setText(Integer.toString(Integer.parseInt(s.substring(0, len))));
						}else{
							setText("0");
						}
						
					}else{ // Float
						char c = s.charAt(0);
						int len = 1;
						if (c == '+' || c == '-' || c == '.' || c >= '0' && c <= '9'){
							for (; len<s.length(); ++len){
								c = s.charAt(len);
								if (!(c >= '0' && c <= '9' || c == '.'))
									break;
							}
							setText(Double.toString(Double.parseDouble(s.substring(0, len))));
						}else{
							setText("0");
						}
						
					}
				}catch(NumberFormatException e){
					setText("0");
				}
				
				adjust();
			}
		});
		
		this.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				adjust();
			}
		});
	}
	
	public int getInteger(){
		if (getText().length() == 0)
			return 0; 
		return Integer.parseInt(getText());
	}
	public double getDouble(){
		if (getText().length() == 0)
			return 0;
		return Double.parseDouble(getText());
	}
	public void setText(Number number){
		if (integer)
			setText(Integer.toString((Integer) number));
		else
			setText(Double.toString((Double) number));
	}
	
	public void setRange(Number min, Number max){
		this.min = min;
		this.max = max;
		adjust();
	}
	
	public void adjust(){
		if (min != null){
			if (integer)
				setText(Math.max(getInteger(), (Integer) min));
			else
				setText(Math.max(getDouble(), (Double) min));
		}
		if (max != null){
			if (integer)
				setText(Math.min(getInteger(), (Integer) max));
			else
				setText(Math.min(getDouble(), (Double) max));
		}
	}
	
	@Deprecated
	public NumberField(){
		super();
		this.integer = true;
		addListener();
	}
	public NumberField(boolean integer){
		super();
		this.integer = integer;
		addListener();
	}

}
