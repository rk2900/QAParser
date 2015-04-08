package finder;

import java.util.LinkedList;

import com.hp.hpl.jena.rdf.model.RDFNode;

import basic.FileOps;
import paser.Question;
import paser.XMLParser;
import knowledgebase.ClientManagement;

public class Pipeline {
	public static final String questionFile = "./data/qald-5_train.xml";
	public static final double matchThreshold = 0.5;
	public static final int edThreshold = 2;
	
	private XMLParser xmlParser;
	
	/**
	 * To initialize AGraph model and XML parser.
	 */
	public Pipeline() {
		xmlParser = new XMLParser();
		xmlParser.setFilePath(questionFile);
		xmlParser.load();
		xmlParser.parse();
	}
	
	public String splitSentence(String sentence, int begin, int end) {
		String questionSplitted = sentence.substring(0, begin)+sentence.substring(end+1, sentence.length());
		return questionSplitted;
	}
	
	public void proceed(int pseudoId) {
		// Read question text and entity inside
		String wikiEntityFilePath = "./data/q-e/question-wiki-entity-all.txt";
		LinkedList<String> qeLines = FileOps.LoadFilebyLine(wikiEntityFilePath);
		int lineCount = 0;
		String qe = "";
		while( lineCount < qeLines.size() && !(qe = qeLines.get(lineCount++)).startsWith(pseudoId+"\t") );
		String[] itemsOfQe = qe.split("\t");
		if( lineCount == qeLines.size() || itemsOfQe.length < 6)
			return;
		
		// Start to load question information
		Question question = xmlParser.getQuestionWithPseudoId(pseudoId);
		int beginOffset = Integer.parseInt(itemsOfQe[1]);
		int endOffset = Integer.parseInt(itemsOfQe[2]);
		
		// Get split question text
		String questionText = question.question;
		String splitText = splitSentence(questionText, beginOffset, endOffset);
		
		// Get entity and surrounding predicates
		String entityUri = itemsOfQe[5];
		LinkedList<RDFNode> predicates = ClientManagement.getSurroundingPred(entityUri);
		
		// Insert matched predicates into new list
		LinkedList<RDFNode> matchedPredicates = new LinkedList<>();
		for (RDFNode predicate : predicates) {
			double matchScore = getMatchScore(splitText, predicate);
			if(matchScore > matchThreshold) {
				matchedPredicates.add(predicate);
			}
		}
		
		// Print results
		System.out.println(matchedPredicates);
	}
	
	private double getMatchScore(String splitText, RDFNode predicate) {
		LinkedList<String> labels = ClientManagement.getLabel(predicate.toString());
		String[] wordsInText = splitText.split(" ");
		boolean matchedFlag = false;
		
		for (String label : labels) {
			String[] wordsInLabel = label.split(" ");
			int matchCount = 0;
			for(String wordL: wordsInLabel)
				for(String wordT: wordsInText)
					matchCount += editDistance(wordL, wordT)<edThreshold?1:0;
			if(matchedFlag = (matchCount==wordsInLabel.length?true:false) )
				break;
		}
		return matchedFlag?1:0;
	}

	private int editDistance(String wordL, String wordT) {
		int lenL = wordL.length();
		int lenT = wordT.length();
		int[][] distance = new int[lenL+1][lenT+1];
		for(int i=0; i<lenL+1; i++) {
			distance[i][0] = i;
		}
		for(int j=0; j<lenT+1; j++) {
			distance[0][j] = j;
		}
		
		for(int i=0; i<lenL; i++) {
			char L = wordL.charAt(i);
			for(int j=0; j<lenT; j++) {
				char T = wordT.charAt(j);
				if(L == T) {
					distance[i+1][j+1] = distance[i][j];
				} else {
					int replace = distance[i][j] + 1;
					int insert = distance[i][j+1] + 1;
					int delete = distance[i+1][j] + 1;
					
					int min = replace>insert?insert:replace;
					min = min>delete?delete:min;
					
					distance[i+1][j+1] = min;
				}
			}
		}
		
		System.out.println(wordL+"\t"+wordT+"\t"+distance[lenL][lenT]);
		return distance[lenL][lenT];
	}

	public static void main(String[] args) {
		Pipeline pipeline = new Pipeline();
		pipeline.proceed(169);
	}

}
