package knowledgebase;

import java.util.HashSet;

public class BlackList {

	public static HashSet<String> predicateSet = new HashSet<String>();
	
	static {
		predicateSet.add("http://dbpedia.org/ontology/wikiPageExternalLink");
		predicateSet.add("http://dbpedia.org/ontology/wikiPageID");
		predicateSet.add("http://dbpedia.org/ontology/wikiPageRevisionID");
		predicateSet.add("http://dbpedia.org/ontology/thumbnail");
		predicateSet.add("http://dbpedia.org/ontology/abstract");
		predicateSet.add("http://dbpedia.org/ontology/wikiPageRedirects");
		predicateSet.add("http://dbpedia.org/ontology/wikiPageRevisionID");
		
		predicateSet.add("http://dbpedia.org/property/hasPhotoCollection");
		predicateSet.add("http://dbpedia.org/property/id");
		predicateSet.add("http://dbpedia.org/property/imageCaption");
		predicateSet.add("http://dbpedia.org/property/imageFlag");
		predicateSet.add("http://dbpedia.org/property/imageMap");
		predicateSet.add("http://dbpedia.org/property/imageShield");
		predicateSet.add("http://dbpedia.org/property/imageSkyline");
		predicateSet.add("http://dbpedia.org/property/link");
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
