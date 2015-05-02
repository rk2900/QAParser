package baseline;

import java.util.LinkedList;

import basic.FileOps;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String wikiPath = "./data/q-e/all-mark-wiki-entity.txt";
		LinkedList<String> wikiLines = FileOps.LoadFilebyLine(wikiPath);
		for (String wikiLine : wikiLines) {
			String content;
			LinkedList<Entity> entityList = new LinkedList<Entity>();
			
			String [] wikiText = wikiLine.split("\t");
			if(wikiText.length != 5){
				System.err.println("Wrong wiki-entity format");
				System.err.println(wikiLine);
				continue;
			}
			
			
			
		}
	}

}
