package com.imwg.smxworkshop.model;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Stack;

import com.imwg.smxworkshop.view.ViewConfig;

final public class Configuration {
	public static final String configName = "config.properties";
	public static final String gitHubPath = "https://github.com/ImWG/SMX-Workshop.git";
	public static final String VERSION = "1.6 Beta";
	public static final int RECENT_FILE_COUNT = 10;
	
	static private Properties properties;
	static private int languageId = 1033;
	static private final Stack<String> recentFiles = new Stack<String>();
	static private int animationSpeed = 10;
	static private int anchorSize = 6;
	static private boolean defaultMemo = true;

	
	private Configuration(){
		recentFiles.setSize(RECENT_FILE_COUNT);
	}
	
	public static int getLanguageId(){
		return languageId;
	}
	static void setLanguageId(int languageId){
		Configuration.languageId = languageId;
	}
	
	public static String[] getRecentFiles(){
		String[] files = new String[recentFiles.size()];
		for (int i = 0; i < recentFiles.size(); ++i){
			files[i] = recentFiles.get(recentFiles.size() - i - 1);
		}
		return files;
	}
	
	public static int getAnimationSpeed(){
		return animationSpeed;
	}
	public static void setAnimationSpeed(int animationSpeed){
		Configuration.animationSpeed = animationSpeed;
	}
	
	public static int getAnchorSize(){
		return anchorSize;
	}
	public static void setAnchorSize(int anchorSize){
		Configuration.anchorSize = anchorSize;
	}
	
	public static boolean isDefaultMemo(){
		return defaultMemo;
	}
	public static void setDefaultMemo(boolean defaultMemo){
		Configuration.defaultMemo = defaultMemo;
	} 
	
	static public void addRecentFile(String file){
		for (String fileName : recentFiles){
			if (file.equals(fileName)){
				recentFiles.remove(fileName);
				break;
			}
		}
		recentFiles.push(file);
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
			
			if (properties.getProperty("animationSpeed") != null)
				animationSpeed = Integer.parseInt(properties.getProperty("animationSpeed"));

			if (properties.getProperty("anchorSize") != null)
				anchorSize = Integer.parseInt(properties.getProperty("anchorSize"));
			
			if (properties.getProperty("defaultMemo") != null)
				defaultMemo = Boolean.parseBoolean(properties.getProperty("defaultMemo"));
			
			for (int i = RECENT_FILE_COUNT - 1; i >= 0; --i){
				String file = properties.getProperty("recentFile" + i);
				if (file != null)
					if (!file.isEmpty())
						recentFiles.push(file);
			}
			
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
		properties.setProperty("animationSpeed", Integer.toString(animationSpeed));
		properties.setProperty("anchorSize", Integer.toString(anchorSize));
		properties.setProperty("defaultMemo", Boolean.toString(defaultMemo));
		
		for (int i = 0; i < recentFiles.size(); ++i)
			properties.setProperty("recentFile" + i, recentFiles.get(i));
		for (int i = recentFiles.size(); i < RECENT_FILE_COUNT; ++i)
			properties.setProperty("recentFile" + i, "");
		
		
		try {
			properties.store(new FileOutputStream(configName), null);
		} catch (IOException e) {
			
		}
	}
}
