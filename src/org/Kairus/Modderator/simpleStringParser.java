package org.Kairus.Modderator;

public class simpleStringParser{
	String text;
	public simpleStringParser(String text){
		this.text = text;
	}
	public String GetNextString(){
		if (text.length() == 0) return null;
		int nextSeperator = text.indexOf("|");
		if (nextSeperator == -1) return null;
		String currentString = text.substring(0, nextSeperator);
		text = text.substring(nextSeperator+1);
		if (currentString.startsWith("\n")) currentString = currentString.substring(1);
		if (currentString.endsWith("\n")) currentString = currentString.substring(0,currentString.length()-1);
		return currentString;
	}
}