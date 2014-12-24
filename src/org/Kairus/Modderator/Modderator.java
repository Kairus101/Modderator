package org.Kairus.Modderator;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import java.util.LinkedHashSet;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

import org.Kairus.Modules.*;

import name.fraser.neil.plaintext.diff_match_patch;
import name.fraser.neil.plaintext.diff_match_patch.Patch;

public class Modderator {
	private static final long serialVersionUID = 1L;
	String version = "1.16.3";
	GameModule gameModule = new StrifeModule();

	public static void main(String[] args) {
		if (args.length>0 && args[0].equals("launchStrife")){
			Modderator mm = new Modderator();
			mm.loadFromConfig();
			StrifeModule strifeM = new StrifeModule();
			strifeM.launchGame();
		}else
			new Modderator().init();
	}
	GUI gui;
	downloadsGUI downloadsGui;
	String repoPath = "http://REPO.ADDRESS/"+gameModule.onlineName+"/";
	public static String appliedMods = "";
	ArrayList<mod> appliedModsList = new ArrayList<mod>();
	boolean isDeveloper = false;
	ArrayList<mod> mods = new ArrayList<mod>();
	byte[] buffer = new byte[1024];

	HashMap<String, String> toBeAdded = new HashMap<String, String>();
	HashMap<String, Boolean> alreadyDone = new HashMap<String, Boolean>();

	public Modderator(){}
	public void init(){
		try {
			//UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); //platform dependent
			/*
		    	NimRODTheme nt = new NimRODTheme();
		    	nt.setPrimary( new Color(205,235,255));
		    	nt.setSecondary( new Color(235,245,255));

		    	NimRODLookAndFeel NimRODLF = new NimRODLookAndFeel();
		    	NimRODLF.setCurrentTheme( nt);
		    	UIManager.setLookAndFeel( NimRODLF);
			 */
			UIManager.setLookAndFeel(ch.randelshofer.quaqua.QuaquaManager.getLookAndFeel());
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		gui = new GUI(this);
		downloadsGui = new downloadsGUI(this);

		//update the main program
		checkForModderatorUpdate();

		//load config
		loadFromConfig();
		
		//init GUI
		loadModFiles();

		//load enabled mods
		setModStatuses();

		gui.init();
		downloadsGui.init();
		
		gameModule.init();

		if ( mods.size()>0 && gui.showYesNo("Update mods?", "Would you like to update your mods?") == 0){ //0 is yes.
			//update mods
			int updated = checkForModUpdates();
			if (updated>0){
				gui.showMessage("Updated:\n"+updated);
			}else if(updated == 0)
				gui.showMessage("All mods up to date!");
		}
	}
	
	
	boolean applyMod(mod m) throws java.io.IOException	{
		appliedMods += m.name+"|";
		m.patchesToSave.clear();
		
		
		
		ZipFile sourceZip = new ZipFile(m.fileName);
		for (String s: m.fileNames){

			String foundFile = gameModule.findFile(s);
			ZipEntry patchFile = sourceZip.getEntry("patch_"+s);//patch
			
			//source
			if ((foundFile==null || m.replaceWithoutPatchCheck) && patchFile == null){ //new file
				ZipEntry sourceFile = sourceZip.getEntry(s);
				InputStream zis = sourceZip.getInputStream(sourceFile);

				//output
				if (alreadyDone.get(s) != null){
					if (m.requirements.size()==0)
						gui.showMessage("Warning ("+m.name+"): Duplicate file with no originals:\n");
					continue;
				}
				alreadyDone.put(s, true);
				
				DataInputStream dis = new DataInputStream(zis);
				byte[] data = new byte[(int) sourceFile.getSize()];
				dis.readFully(data);
				gameModule.write(m, s, data);
				
			}else{
				//we need to perform a diff.
				//step 1, check for a patch file, if so, skip to step 3
				//step 2, check for original files, if so, make patches
				//step 3, apply patch to current file.
				//step 4, apply any xml modifications.
				//step 5, put new file in resources

				//setup
				diff_match_patch differ = new diff_match_patch();
				LinkedList<Patch> patch = null;
				String current = "";

				if (toBeAdded.get(s) != null){ // not the first mod
					current = toBeAdded.get(s);
				}else
					current = foundFile; //current

				//step 1
				//check for a patch file, if so, skip to step 3
				String potentialPatch = m.patches.get(s);
				if (potentialPatch != null){ //We have a patch! continuing
					patch = (LinkedList<Patch>)differ.patch_fromText(potentialPatch);
				}else{ //no patch, check for original files
					//step 2
					//check for original files, if so, make patches and update mod later
					ZipEntry sourceFile = sourceZip.getEntry("original/"+s);//original
					if (sourceFile == null){
						gui.showMessage("Problem in "+m.name+"!\n"+s+"\nFound in resources, but no patch and no original file.\nIf you are developing this mod, put the official file in your mod pack, under \"original/"+s+"\".");
						continue;
					}

					InputStream zis = sourceZip.getInputStream(sourceFile);//original
					String original = fileTools.store(zis); //original
					sourceFile = sourceZip.getEntry(s);//modified
					zis = sourceZip.getInputStream(sourceFile);//modified
					String modified = fileTools.store(zis); //modified

					LinkedList<diff_match_patch.Diff> diffs1 = differ.diff_main(original, modified);
					patch = differ.patch_make(original, diffs1);

					if (isDeveloper){
						String patchText = differ.patch_toText(patch);
						m.patchesToSave.put(s, patchText);
					}
				}

				//step 3
				//apply patch to current file.
				Object[] result = differ.patch_apply(patch, current);
				boolean good = true;
				int error = 0;
				for (error = 0; error < ((boolean[])result[1]).length;error++)
					if (!((boolean[])result[1])[error]){
						good=false;
						break;
					}
				if (!good){
					gui.showMessage("Problem in "+m.name+":"+s+". So I won't apply it.\n"+(isDeveloper?"Applying diff: "+patch.get(error):""));
					continue;
				}
				toBeAdded.remove(s);
				toBeAdded.put(s, (String)result[0]);

				gameModule.write(m, s, ((String)result[0]).getBytes());
			}
		}
		sourceZip.close();
		if (m.patchesToSave.size() > 0){
			int n = gui.showYesNo("Compress", "Save new official "+gameModule.name+" mod?");
			if (n==0){
				String[] filesToDelete = new String[2*m.patchesToSave.size()+1];
				filesToDelete[2*m.patchesToSave.size()] = "original/";
				String[] filesToAdd = new String[m.patchesToSave.size()];
				String[] content = new String[m.patchesToSave.size()];
				int i = 0;
				Iterator<?> it = m.patchesToSave.entrySet().iterator();
				while (it.hasNext()) {
					@SuppressWarnings("unchecked")
					Map.Entry<String, String> pairs = (Map.Entry<String, String>)it.next();		        
					filesToDelete[2*i] = pairs.getKey();
					filesToDelete[2*i+1] = "original/"+pairs.getKey();
					filesToAdd[i] = "patch_"+pairs.getKey();
					content[i] = pairs.getValue();
					it.remove(); // avoids a ConcurrentModificationException
					i++;
				}
				fileTools.remakeZipEntry(m.name+"_official."+gameModule.modExtention, new File(m.fileName), filesToDelete, filesToAdd, content);
				gui.showMessage("Created official mod at: "+new File(m.name+"_official."+gameModule.modExtention).getAbsolutePath());
			}
		}
		appliedModsList.add(m);
		return true;
	}

	LinkedHashSet<mod> insertModRequirements(mod Mod){
		LinkedHashSet<mod> returnArray = new LinkedHashSet<mod>();
		for(String requirement : Mod.requirements){
			boolean requirementFound = false;
			for(mod m : mods){
				if(!m.equals(Mod) && m.name.toLowerCase().equals(requirement.toLowerCase())){
					requirementFound = true;
					returnArray.addAll(insertModRequirements(m));
				}
			}
			if(!requirementFound){
				//internet
				for(onlineModDescription m : onlineModList){
					if(m.name.toLowerCase().equals(requirement.toLowerCase())){
						requirementFound = true;
						downloadsGUI.downloadMod modDownload = downloadMod((repoPath + m.link).replace(" ", "%20"), System.getProperty("user.dir") + "/mods/" + m.name + "."+gameModule.modExtention, m.name);
						try{
							//Wait for the download to complete
							modDownload.get();
						}catch(Exception e){
							System.out.println(e);
						}
						File modFile = new File(System.getProperty("user.dir") + "/mods/" + m.name + "."+gameModule.modExtention);
						if(modFile.exists()){
							mod newMod = fileTools.loadModFile(modFile, this);
							returnArray.add(newMod);
							this.gui.tableData[this.gui.tableData.length - 1][0] = true;
						}
						break;
					}
				}
			}
			if(!requirementFound){
				//Panic?
				//return empty list to not apply this mod nor any of its requirements?
				this.gui.showMessage("Could not find mod " + requirement + " requirement for " + Mod.name);
			}
		}
		returnArray.add(Mod);

		return returnArray;
	}
	
	LinkedHashSet<mod> getModsToApply(){
		int o = 0;
		LinkedHashSet<mod> modsToApply = new LinkedHashSet<mod>();
		ArrayList<mod> currentMods = new ArrayList<mod>(this.mods);
		
		// Add framework mods 
		for (mod m: currentMods){
			if (!m.framework && (Boolean)gui.tableData[o++][0] == true)	{
				modsToApply.addAll(insertModRequirements(m));
			}
		}
		return modsToApply;
	}
	
	void applyMods(){

		//populateOnlineModsTable();

		toBeAdded.clear();
		alreadyDone.clear();

		boolean success = true;
		
		// Get all requirements
		LinkedHashSet<mod> modsToApply = getModsToApply();
		
		try {
			// Add all mods to be added
			appliedMods = "";
			appliedModsList.clear();
			
			// Apply all mods
			for (mod m: modsToApply)
			{
				if(!applyMod(m))
				{
					success = false;
					break;
				}
			}
			
			//step 4
			//apply any xml modifications.
			for (mod m: modsToApply){

				//System.out.println("looking for modifications: "+m.name);
				simpleStringParser parser = new simpleStringParser(m.xmlModifications.toString());
				while (true){
					String modification = parser.GetNextString();
					if (modification == null)
						break;

					//Is this a valid command?
					if (modification.toLowerCase().trim().equals("replace") || modification.toLowerCase().trim().equals("add before") || modification.toLowerCase().trim().equals("add after")){
						String file = parser.GetNextString();
						addFileIfNotAdded(toBeAdded, file);
						String fileText = toBeAdded.get(file);
						String text1 = parser.GetNextString();
						String text2 = parser.GetNextString();
						String newText = null;

						if (modification.toLowerCase().trim().equals("replace")){//replacement
							newText = fileText.replace(text1, text2);
							if (fileText == newText)
								gui.showMessage("Warning ("+m.name+"): \n\n"+text1+"\n\nnot found in "+file+"\n continuing anyway.", "Warning: couldn't find text.", 3);

						}else if (modification.toLowerCase().trim().equals("add before")){
							int insertPosition = fileText.indexOf(text1);
							if (insertPosition == -1)
								gui.showMessage("Warning ("+m.name+"): "+newText+" not found in "+file+"\n continuing anyway.", "Warning: couldn't find text.", 3);
							else
								newText = fileText.substring(0, insertPosition) + text2 + fileText.substring(insertPosition);

						}else if (modification.toLowerCase().trim().equals("add after")){
							int insertPosition = fileText.indexOf(text1)+text1.length();
							if (insertPosition == text1.length()-1)
								gui.showMessage("Warning ("+m.name+"): "+newText+" not found in "+file+"\n continuing anyway.", "Warning: couldn't find text.", 3);
							else
								newText = fileText.substring(0, insertPosition) + text2 + fileText.substring(insertPosition);
						}
						toBeAdded.remove(file);
						toBeAdded.put(file, newText);
						
						gameModule.write(m, file, (newText).getBytes());
					}
				}
			}

			saveConfig();

			gameModule.onEndApplyMods();
			
			if (success && gui.showYesNo("Success.", "Mod merge successful.\n\nLaunch "+gameModule.name) == 0){ //0 is yes.
				gameModule.launchGame();
			}

		} catch (java.io.FileNotFoundException e){
			e.printStackTrace();
			gui.showMessage("Failure, archive open or non-existant\nAre you running "+gameModule.name+"? Close it.\nHave you got a mod/resource file open? Close it.", "Failed to open files warning", JOptionPane.ERROR_MESSAGE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addFileIfNotAdded(HashMap<String, String> toBeAdded, String file) throws IOException{
		if (toBeAdded.get(file) == null){//we need to add the file to toBeAdded
			String foundFile = gameModule.findFile(file);
			if (foundFile == null){
				gui.showMessage("Error: file: "+file+" not found! Mod won't be applied.", "Mod merge unsuccessful", 1);
				System.out.println("Error: file: "+file+" not found! Mod won't be applied.");
				throw new IOException();
			}else{
				toBeAdded.put(file, foundFile);
			}
		}
	}
	public int numMods = 0;
	void loadModFiles(){
		mods.clear();
		final File folder = new File(System.getProperty("user.dir")+"/mods");
		if(!folder.exists())
			folder.mkdirs();

		for (final File fileEntry : folder.listFiles()) {
			if (fileEntry.getName().toLowerCase().endsWith("."+gameModule.modExtention)) {
				mods.add(fileTools.loadModFile(fileEntry, this));
			}
		}
		
		int i = 0;
		int numFrameworks = 0;
		while (i<mods.size()-numFrameworks){
			if (mods.get(i).framework){
				numFrameworks++;
				mods.add(mods.remove(i));//move to end
			}else
				i++;
		}
		if (!isDeveloper)
			numMods = mods.size()-numFrameworks;
		else
			numMods = mods.size();
		
		gui.tableData = new Object[numMods][5];
		for (i = 0;i<numMods;i++){
			gui.tableData[i]=mods.get(i).getData();
		}
		
		if (gui.table != null){
			String[] columnNames = {"Enabled", "Icon", "Name", "Author", "Version"};
			gui.table.setModel(new DefaultTableModel(gui.tableData, columnNames));
			gui.table.revalidate();
		}
	}

	public void loadFromConfig(){
		try {
			BufferedReader reader = new BufferedReader(new FileReader("config.txt"));
			//S2 path
			gameModule.filePath = reader.readLine();
			if (gameModule.filePath == null)
				gameModule.filePath = "";
			//Developer mode
			if (reader.readLine().equals("1"))
				isDeveloper = true;
			else
				isDeveloper = false;
			//applied mods
			appliedMods = reader.readLine();
			if (appliedMods == null)
				appliedMods = "";

			reader.close();
		} catch (FileNotFoundException e1) {
			gui.showMessage("Welcome to Modderator!\nPlease select your GAME-TODO.",
					"Welcome!", JOptionPane.PLAIN_MESSAGE);

			JFileChooser chooser = new JFileChooser();
			chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY);
			int returnVal = chooser.showOpenDialog(null);

			if(returnVal == JFileChooser.APPROVE_OPTION) {
				gameModule.filePath = chooser.getSelectedFile().getAbsolutePath();
				saveConfig();
			}else
				System.exit(0);

		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public void setModStatuses(){
		for (Object[] o: gui.tableData){
			String tmp = (String)o[2];
			if (appliedMods.contains(tmp.substring(6, tmp.length()-7))){
				o[0] = true;
			}
		}
	}

	public void saveConfig(){
		try {
			PrintWriter pr = new PrintWriter(new File("config.txt"));
			pr.println(gameModule.filePath);
			if (isDeveloper)
				pr.println(1);
			else
				pr.println(0);
			pr.println(appliedMods);
			pr.close();
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
		}
	}

	private void checkForModderatorUpdate(){
		try {
			BufferedReader webIn;
			webIn = new BufferedReader(new InputStreamReader(new URL("http://pastebin.com/raw.php?i=aXuwRyFM").openStream()));
			String version = webIn.readLine();
			if (version.startsWith("<")) throw new Exception();
			String link = webIn.readLine();
			if (!version.equals(this.version)){
				gui.showMessage("Update found, automatically updating!");

				int kbChunks = 1 << 10; //1kb

				java.io.BufferedInputStream in = new java.io.BufferedInputStream(new java.net.URL(link).openStream());
				java.io.FileOutputStream fos = new java.io.FileOutputStream("ModManager.jar");
				java.io.BufferedOutputStream bout = new BufferedOutputStream(fos,kbChunks*1024);
				byte[] data = new byte[kbChunks*1024];
				int x=0;
				while((x=in.read(data,0,kbChunks*1024))>=0)
				{
					bout.write(data,0,x);
				}
				bout.flush();
				bout.close();
				in.close();

				gui.showMessage("Restarting!");

				try {
					final ArrayList<String> command = new ArrayList<String>();
					command.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
					command.add("-jar");
					command.add(new File("ModManager.jar").getAbsolutePath());
					System.out.println(command);
					final ProcessBuilder builder = new ProcessBuilder(command);
					builder.start();
				} catch (Exception e) {
					throw new IOException("Error while trying to restart the application", e);
				}
				System.exit(0);
			}
		} catch (Exception e) {
			gui.showMessage("Failed to update modman.\nThis could be because:\n\n"/*+"You aren't connected to the internet\nYou are using a proxy\n"*/+"MOD REPO is down.","Failure updateing Modderator",0);
		}
	}

	ArrayList<onlineModDescription> onlineModList = new ArrayList<onlineModDescription>();
	private onlineModDescription getOnlineModDescription(String name){
		for (onlineModDescription i:onlineModList)
			if (i.name.equals(name))
				return i;
		return null;
	}

	public boolean populateOnlineModsTable(){
		if (onlineModList.size()==0){
			
			//populate the online mods table.
			try {
				BufferedReader webIn;
				webIn = new BufferedReader(new InputStreamReader(new URL(repoPath+"rawModList.php").openStream()));
				String input;
				while ((input=webIn.readLine())!=null){
					if (input.startsWith("<")) throw new Exception();
					onlineModList.add(new onlineModDescription(input));
				}
			} catch (Exception e) {
				gui.showMessage("Failed to get mods.\nThis could be because:\n\n"/*+"You aren't connected to the internet\nYou are using a proxy\n"*/+"mod repo is down.","Failure updateing Modderator",0);
				return false;
			}
		}
		return true;
	}

	private boolean purgedOnlineList = false;
	public int numOnlineMods = 0;
	public void purgeOnlineModsTable(){
		if (purgedOnlineList) return;
		for (int i = 0;i<mods.size();i++){
			for (int o = 0;o<onlineModList.size();o++){
				if (mods.get(i).name.equals(onlineModList.get(o).name)){
					onlineModList.remove(o);
					o--;
				}
			}	
		}
		
		int i = 0;
		int numFrameworks = 0;
		while (i<onlineModList.size()-numFrameworks){
			if (onlineModList.get(i).framework.toLowerCase().equals("true")){
				numFrameworks++;
				onlineModList.add(onlineModList.remove(i));//move to end
			}else
				i++;
		}

		if (!isDeveloper)
			numOnlineMods = onlineModList.size()-numFrameworks;
		else
			numOnlineMods = onlineModList.size();
		
		purgedOnlineList = true;
	}

	private int checkForModUpdates(){

		if (!populateOnlineModsTable()) return -1;
		purgedOnlineList = false;

		int updated=0;
		for (int i = 0; i < mods.size(); i++){
			mod m = mods.get(i);

			String latestVersion = null;
			String latestLink = null;

			//check mod repo
			onlineModDescription onlineModDesc = getOnlineModDescription(m.name);
			if (onlineModDesc!=null){
				latestVersion = onlineModDesc.version;
				latestLink = repoPath+onlineModDesc.link.replace(" ", "%20");
			}
			/*// Disabled external(uncheckable) updating
			//check mod update link
			if (m.updateLink != null){
				BufferedReader webIn;
				webIn = new BufferedReader(new InputStreamReader(new URL(m.updateLink).openStream()));
				String version = webIn.readLine();
				String link = webIn.readLine();
				//if we are out of date, and the link version is higher than the repo version
				if (!version.equals(m.version) && (latestVersion==null || version.compareTo(latestVersion) > 0)){
					latestVersion = version;
					latestLink = link;
				}
			}
			*/
			//we have an update, grab it.
			if (latestVersion!=null && m.version.compareTo(latestVersion) < 0){
				updated++;
				downloadMod(latestLink, m.fileName, m.name);
				if (!m.framework){
					gui.removeFromTable1(mods.indexOf(m));
				}
				mods.remove(i);
				i--;
			}
		}
		return updated;
	}

	downloadsGUI.downloadMod downloadMod(String link, String filename, String name){
		return downloadsGui.downloadMod(link, filename, name);
	}

	
}