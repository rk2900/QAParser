package finder;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Properties;

import org.w3c.dom.Node;

import com.hp.hpl.jena.rdf.model.RDFNode;

import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import arq.qparse;
import basic.FileOps;
import paser.Question;
import paser.QuestionSingle;
import paser.XMLParser;
import knowledgebase.ClientManagement;

public class Pipeline {
	public static final String questionFile = "./data/qald-5_train.xml";
	public static final String wikiEntityFilePath = "./data/q-e/all-mark-wiki-entity.txt";
	public static final double matchThreshold = 0.5;
	public static final int edThreshold = 2;
	
	private XMLParser xmlParser;
	
	private static StanfordCoreNLP pipeline;
	
	static {
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos");
		pipeline = new StanfordCoreNLP(props);
	}
	
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
		System.out.println(pseudoId+"\t"+questionText);
		String splitText = splitSentence(questionText, beginOffset, endOffset);
		
		// Get entity and surrounding predicates
		String entityUri = itemsOfQe[5];
		LinkedList<RDFNode> predicates = ClientManagement.getSurroundingPred(entityUri);
		
		// Insert matched predicates into new list
		HashSet<String> matchedPredicates = new HashSet<>();
		for (RDFNode predicate : predicates) {
			double matchScore = getMatchScore(splitText, predicate);
			if(matchScore > matchThreshold) {
				matchedPredicates.add(predicate.toString());
			}
		}
		
		// Print results
		System.out.println("\t"+matchedPredicates);
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
					matchCount += editDistance(wordL, wordT, false)<edThreshold?1:0;
			if(matchedFlag = (matchCount==wordsInLabel.length?true:false) )
				break;
		}
		return matchedFlag?1:0;
	}

	private int editDistance(String wordL, String wordT, boolean visible) {
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
		
		if(visible)
			System.out.println("\t"+wordL+"\t"+wordT+"\t"+distance[lenL][lenT]);
		return distance[lenL][lenT];
	}

	public static void main(String[] args) {
		Pipeline pipeline = new Pipeline();
		int pseudoId = 2;
		
		QuestionSingle q = pipeline.preProcess(pseudoId);
		if(q==null)
			return;
		System.out.println(q.getWordList());
		System.out.println(q.getPOSList());
		System.out.println(q.mention);
		System.out.println(q.entityPositions);
		System.out.println(q.surPredicates);
		
	}

	private QuestionSingle preProcess(int pseudoId) {
		// Read question text and entity inside
		LinkedList<String> qeLines = FileOps.LoadFilebyLine("./data/q-e/all-mark-wiki-entity.txt");
		int lineCount = 0;
		String qe = "";
		while( lineCount < qeLines.size() && !(qe = qeLines.get(lineCount++)).startsWith(pseudoId+"\t") );
		String[] itemsOfQe = qe.split("\t");
		if( lineCount == qeLines.size() || itemsOfQe.length < 5)
			return null;
		
		// Start to load question information
		QuestionSingle question = xmlParser.getQuestionWithPseudoId(pseudoId).toQuestionSingle();
		String sentence = question.question;
		int beginOffset = Integer.parseInt(itemsOfQe[1]);
		int endOffset = Integer.parseInt(itemsOfQe[2]);
		
		// Get entity mention
		String mention = question.question.substring(beginOffset, endOffset);
		question.mention = mention;
		
		// Get question words list
		String[] wordList = sentence.split(" ");
		// Eliminate punctuation
		String lastWord = wordList[wordList.length-1];
		wordList[wordList.length-1] = lastWord.substring(0, lastWord.length()-1);
		for (String string : wordList) {
			question.qWordList.add(string);
			if(mention.contains(string))
				question.entityPositions.add(question.qWordList.size()-1);
		}
		
		// Get POS tags
		LinkedList<String> POSList = getPOSTag(sentence);
		// Eliminate punctuation of POS tag
		POSList.remove(POSList.size()-1);
		question.qPOSList = POSList;
		
		// Get entity and surrounding predicates
		String entityUri = itemsOfQe[4];
		LinkedList<RDFNode> predicates = ClientManagement.getSurroundingPred(entityUri);
		for (RDFNode predNode : predicates)
			question.surPredicates.add(predNode.toString());
		
		return question;
	}
	
	public LinkedList<String> getPOSTag(String sentence) {
		LinkedList<String> tags = new LinkedList<String>();
		Annotation annotation = new Annotation(sentence);
		pipeline.annotate(annotation);
		CoreMap labeledSentence = annotation.get(SentencesAnnotation.class).get(0);
		for (CoreLabel label : labeledSentence.get(TokensAnnotation.class)) {
			tags.add(label.get(PartOfSpeechAnnotation.class));
		}
		return tags;
	}

}
