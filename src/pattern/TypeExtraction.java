package pattern;

import java.util.HashSet;
import java.util.Properties;

import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import finder.Pipeline.DataSource;
import paser.Question;
import paser.XMLParser;

public class TypeExtraction {

	protected String trainFile = "./data/qald-5_train.xml";
	public XMLParser xmlParser;
	
	
	
	public TypeExtraction() {
		initializeXMLParser();
	}
	
	protected void initializeXMLParser() {
		xmlParser = new XMLParser();
		xmlParser.setFilePath(trainFile);
		xmlParser.load();
		xmlParser.parse(DataSource.TRAIN);
	}
	
	public HashSet<String> typeExtractor(String text, boolean visible){
		return new HashSet<>();
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TypeExtraction extraction = new TypeExtraction();
		Question q = extraction.xmlParser.getQuestionWithId(1);
		q.print();
	}

}
