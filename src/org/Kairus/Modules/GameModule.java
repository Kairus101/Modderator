package org.Kairus.Modules;

import org.Kairus.Modderator.mod;

public abstract class GameModule {
	public String name;
	public String onlineName;
	public String imagePath;
	public String filePath;
	public String modExtention;
	

	public abstract void init();
	public abstract String findFile(String s);
	public abstract void applyMod();
	public abstract void onStartApplyMods();
	public abstract void onEndApplyMods();
	public abstract void launchGame();
	public abstract void write(mod m, String path, byte[] data);
	
}
