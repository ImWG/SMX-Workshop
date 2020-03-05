package com.imwg.smxworkshop.plugin;

import java.lang.reflect.InvocationTargetException;

import com.imwg.smxworkshop.view.MainFrame;

abstract public class Plugin {
	
	abstract public String onGetName();
	abstract public String[] onGetMenuItems();
	abstract public void onSelectMenu(MainFrame mainFrame, String name);
	
	static final private String[] plugins = new String[]{
		"com.imwg.smxworkshop.plugin.cnc.CNCPlugin"
	};
	static final public String[] getPlugins(){
		return plugins;
	}
	
	static final public Plugin getPlugin(String className){
		try {
			Class<?> cl = Class.forName(className);
			if (Plugin.class.isAssignableFrom(cl)){
				return (Plugin) cl.getConstructor().newInstance();
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
			
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
