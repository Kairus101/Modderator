package org.Kairus.Modules;

import org.Kairus.Modderator.mod;

public abstract class GameModule {
	public String name;
	public String onlineName;
	public String filePath;
	public String modExtention;
	public String exeURL;
	public boolean warningOnNoExe;
	
	public abstract void init();
	public abstract String findFile(String s);
	public abstract void applyMod(mod m);
	public abstract void onStartApplyMods();
	public abstract void onEndApplyMods();
	public abstract void launchGame();
	public abstract void write(mod m, String path, byte[] data);
	
}
