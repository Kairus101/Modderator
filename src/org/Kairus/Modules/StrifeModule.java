package org.Kairus.Modules;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.Kairus.Modderator.fileTools;
import org.Kairus.Modderator.mod;
import org.Kairus.Modderator.simpleStringParser;
import org.Kairus.Modderator.Modderator;

public class StrifeModule extends GameModule {
	
	// Module info
	public StrifeModule(){
		name = "Strife";
		onlineName = "Strife";
		imagePath = "data/images/Strife";
		modExtention = "strifemod";
	}
	
	// Game vars
	String strifeVersion;
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
			ret += ";moddedStrife"; // Make all configs in this folder
		return ret;
	}

	@Override
	public void launchGame(){
		try {
			// Create our lua script
			// It's much easier to hard-code the lua into the modman code.
			String modmanLoader = "--Long live... ModMan!\nlibThread.threadFunc(function()\n	wait(500)\n	if GetCvarString('host_version') ~= '{version}' then\n		GenericDialog(\n			Translate('Outdated Mods'), Translate('^rYour mods are out of date!\\n^*Do you want to shut down Strife, so you can open modman and re-apply mods?\\nOtherwise you may have game-breaking bugs!'), '', Translate('general_ok'), Translate('I\\\'ll deal'), \n			function()\n				Cmd('Quit')\n			end,\n			function()\n				--Cmd('Quit')\n			end,\n			nil,\n			nil,\n			true\n		)\n	end\nend)";
			modmanLoader = modmanLoader.replace("{version}", strifeVersion);
			PrintWriter pout;
			pout = new PrintWriter(new File(filePath+"/mods/modman.lua"));
			pout.print(modmanLoader);
			pout.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		final ProcessBuilder builder = new ProcessBuilder(filePath+"/bin/strife", 
				"-mod", getLaunchParams(),
				"-execute", "set host_autoexec \"\"\"script \\\\\\\"dofile 'mods/modman.lua'\\\\\\\"\"\"\"\""//,
		);
		try {
			builder.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}


	@Override
	public void applyMod() {
	}


	@Override
	public void onStartApplyMods() {
	}


	@Override
	public void onEndApplyMods() {
		
	}


	@Override
	public void init() {
		strifeVersion = fileTools.getStrifeVersionFromFile(filePath+"/strife.version");
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
