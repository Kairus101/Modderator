package org.Kairus.Modderator;

public class onlineModDescription {
	String name;
	String author;
	String version;
	String rating;
	String category;
	String description;
	String link;
	String framework;
	onlineModDescription(String text){
		simpleStringParser ssp = new simpleStringParser(text);
		name=ssp.GetNextString();
		author=ssp.GetNextString();
		version=ssp.GetNextString();
		rating=ssp.GetNextString();
		category=ssp.GetNextString();
		description=ssp.GetNextString();
		link=ssp.GetNextString();
		framework=ssp.GetNextString();
	}
}
