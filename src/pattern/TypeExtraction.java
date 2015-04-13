package pattern;

import java.util.HashSet;
import java.util.LinkedList;

import paser.Question;
import paser.XMLParser;

public class TypeExtraction {

	protected String trainFile = "./data/qald-5_train.xml";
	protected XMLParser xmlParser;
	
	public TypeExtraction() {
		initializeXMLParser();
	}
	
	protected void initializeXMLParser() {
		xmlParser = new XMLParser();
		xmlParser.setFilePath(trainFile);
		xmlParser.load();
		xmlParser.parse();
	}
	
	public HashSet<String> typeExtractor(String text){
		return new HashSet<>();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TypeExtraction extraction = new TypeExtraction();
		Question q = extraction.xmlParser.getQuestionWithId(1);
		q.print();
	}

}
