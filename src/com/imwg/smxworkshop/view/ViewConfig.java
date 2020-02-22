package com.imwg.smxworkshop.view;

import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.imwg.smxworkshop.model.Configuration;

final public class ViewConfig {

	static public Color backgroundColor = new Color(0x406840);
	static public Color backgroundSelectedColor = new Color(0x80D080);
	static Map<Class<?>, Properties> viewProperties;
	static Map<Integer, String> languages;
	static private Properties stringTable;
	static private final String LANGUAGE_PACKAGE = "/com/imwg/smxworkshop/resource/lang/"; 
	
	private ViewConfig(){
	}
	
	static public void loadViewConfig(){
		viewProperties = new HashMap<Class<?>, Properties>();
		
		Class<?>[] classes = new Class<?>[]{
			 SetAnchorDialog.class,
			 ScaleDialog.class,
			 ImportImagesDialog.class,
			 ConvertShadowDialog.class,
			 AboutDialog.class,
			 ExportImagesDialog.class,
			 
			 MainMenu.class
		};
		
		for (Class<?> c : classes){
			try {
				Properties prop = new Properties();
				prop.load(c.getResourceAsStream(c.getSimpleName() + ".properties"));
				viewProperties.put(c, prop);
				
			} catch (NullPointerException e){
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		setStringTable(Configuration.languageId);
		
		
//		try {
//			viewProperties.load(ViewConfig.class.getResourceAsStream(".properties"));
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		
		loadLanguages();
	}
	
	static public void setStringTable(int languageId){
		try {
			stringTable = new Properties();
			stringTable.load(ViewConfig.class.getResourceAsStream(LANGUAGE_PACKAGE
					 + languageId + ".properties"));
			//stringTable.load(new FileInputStream("lang/" + languageId + ".properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	static public void loadLanguages(){ // TODO
		languages = new HashMap<Integer, String>();
		Properties langList = new Properties();
		try {
			langList.load(ViewConfig.class.getResourceAsStream(LANGUAGE_PACKAGE + "lang.properties"));
			for (int i=0; langList.getProperty(Integer.toString(i)) != null; ++i){
				int key = Integer.parseInt(langList.getProperty(Integer.toString(i)));
				String fileName = LANGUAGE_PACKAGE + key + ".properties";
				Properties prop = new Properties();
				try {
					prop.load(ViewConfig.class.getResourceAsStream(fileName));
					languages.put(key, prop.getProperty("__Name__"));
				} catch (IOException e) {
				}
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	static public String getString(String key){
		return stringTable.getProperty(key);
	}
}
