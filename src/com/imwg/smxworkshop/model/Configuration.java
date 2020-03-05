package com.imwg.smxworkshop.model;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import com.imwg.smxworkshop.view.ViewConfig;

final public class Configuration {
	public static final String configName = "config.properties";
	public static final String gitHubPath = "https://github.com/ImWG/SMX-Workshop.git";
	public static final String VERSION = "1.4 Beta";
	
	static private Properties properties;
	static private int languageId = 1033;
	
	public static int getLanguageId(){
		return languageId;
	}
	static void setLanguageId(int languageId){
		Configuration.languageId = languageId;
	}
	
	static public void loadConfig(){
		properties = new Properties();
		try {
			properties.load(new FileInputStream(configName));
			
			if (properties.getProperty("backgroundColor") != null)
				ViewConfig.backgroundColor = 
						new Color(Integer.parseInt(properties.getProperty("backgroundColor"), 16) | 0xff000000);
			
			if (properties.getProperty("backgroundSelectedColor") != null)
				ViewConfig.backgroundSelectedColor = 
						new Color(Integer.parseInt(properties.getProperty("backgroundSelectedColor"), 16) | 0xff000000);
			
			if (properties.getProperty("language") != null)
				languageId = Integer.parseInt(properties.getProperty("language"));
			
		} catch (IOException e) {
			
		}
	}
	
	static public void saveConfig(){
		properties = new Properties();
		properties.setProperty("language", Integer.toString(languageId));
		properties.setProperty("backgroundColor", 
				Integer.toHexString(ViewConfig.backgroundColor.getRGB() & 0xffffff));
		properties.setProperty("backgroundSelectedColor", 
				Integer.toHexString(ViewConfig.backgroundSelectedColor.getRGB() & 0xffffff));
		
		try {
			properties.store(new FileOutputStream(configName), null);
		} catch (IOException e) {
			
		}
	}
}
