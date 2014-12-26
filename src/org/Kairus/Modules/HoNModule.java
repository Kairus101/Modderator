package org.Kairus.Modules;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.Kairus.Modderator.fileTools;
import org.Kairus.Modderator.mod;
import org.Kairus.Modderator.simpleStringParser;
import org.Kairus.Modderator.Modderator;

public class HoNModule extends GameModule {
	
	// Module info
	public HoNModule(){
		name = "Hon";
		onlineName = "Hon";
		modExtention = "honmod";
		warningOnNoExe = true;
		exeURL = "";
	}
	
	// Game vars
	int archiveNumber = 1;
	
	public String findFile(String name){
		try {
			for (int i = archiveNumber-1;i>=0;i--){
				ZipFile zipFile;
				zipFile = new ZipFile(filePath+"/game/resources"+i+".s2z");
				ZipEntry entry =  zipFile.getEntry(name);
				if (entry != null){
					String r = fileTools.store(zipFile.getInputStream(entry));
					zipFile.close();
					return r;
				}
				zipFile.close();
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return null;
	}


	@Override
	public void applyMod(mod m){
		// Delete old mod files
		fileTools.recursiveDelete(new File(filePath+"/game/mods/"));
		fileTools.recursiveDelete(new File(filePath+"/mods/"+m.name+"/"));
		
		//create output folders.
		findArchives(filePath);
		new File(filePath+"/mods/").mkdirs();
	}

	int findArchives(String path){
		archiveNumber = 1;
		String output = null;
		while (true){
			output = path+"/game/resources"+archiveNumber+".s2z";
			ZipFile zipFile = null;
			try {
				zipFile = new ZipFile(output);
				if ((zipFile.getComment() != null && zipFile.getComment().equals("Long live... ModMan!")) || 
				  (new File(output).length()<10000 && zipFile.size() != 0)
				  ){
					//we've found our guy.
					zipFile.close();
					new File(output).delete(); // REMOVE OUT-DATED S2Z FORMAT!
					//archiveNumber--;
					break;
				}
				zipFile.close();
			} catch (ZipException e){
				new File(output).delete();
				//archiveNumber--;
				return archiveNumber; //corrupt zip file. This is oue guy.
			} catch (java.io.FileNotFoundException e) {
				// perfect, doesn't even exist.
				//archiveNumber--;
				return archiveNumber;
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (zipFile != null)
						zipFile.close();
				} catch (IOException e) {}
			}
			archiveNumber++;
		}
		return archiveNumber;

	}
	
	
	String getLaunchParams(){
		String ret = "game";
		boolean applied = false;
		simpleStringParser parser = new simpleStringParser(Modderator.appliedMods);
		while (true){
			String mod = parser.GetNextString();
			if (mod == null)
				break;
			ret += ";mods/"+mod.replace(" ", "_");
			applied = true;
		}
		if (applied)
			ret += ";moddedHoN"; // Make all configs in this folder
		return ret;
	}

	@Override
	public void launchGame(){
		final ProcessBuilder builder = new ProcessBuilder(filePath+"/hon", 
				"-mod", getLaunchParams()
		);
		try {
			builder.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}


	@Override
	public void onStartApplyMods() {
	}


	@Override
	public void onEndApplyMods() {
		
	}
	
	@Override
	public void init() {
	}
	
	@Override
	public void write(mod m, String path, byte[] data) {
		File f = new File(filePath+"/mods/"+m.name.replace(" ", "_")+"/"+path);
		f.getParentFile().mkdirs();
		try {
			f.createNewFile();
			FileOutputStream fos = new FileOutputStream(f);
			fos.write(data);
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
