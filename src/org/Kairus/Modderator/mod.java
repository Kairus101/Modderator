package org.Kairus.Modderator;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ImageIcon;

public class mod{
	public ArrayList<String> fileNames = new ArrayList<String>();
	public ArrayList<String> requirements = new ArrayList<String>();
	public String fileName;
	public String name = "defaultModName";
	public String author = "defaultModAuthor";
	public String category = "defaultModCategory";
	public String version = "0";
	public ImageIcon image = null;
	public StringWriter xmlModifications = new StringWriter();
	public HashMap<String, String> patches = new HashMap<String, String>();
	public HashMap<String, String> patchesToSave = new HashMap<String, String>();
	public Modderator modman;
	boolean replaceWithoutPatchCheck = false;
	boolean framework = false;


	mod(String fileName, Modderator mm){
		this.fileName = fileName;
		modman = mm;
	}
	Object[] getData(){
		return new Object[]{false, image!=null?image:modman.gui.defaultIcon, "<html>"+name+"</html>", author, version, category};
	}
}