package finder;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Properties;

import com.hp.hpl.jena.rdf.model.RDFNode;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import basic.FileOps;
import paser.Question;
import paser.QuestionFrame;
import paser.QuestionSingle;
import paser.XMLParser;
import knowledgebase.ClientManagement;

public class Pipeline {
	public static final String questionFile = "./data/qald-5_train.xml";
	public static final String wikiEntityFilePath = "./data/q-e/all-mark-wiki-entity.txt";
	public static final double matchThreshold = 0.5;
	public static final int edThreshold = 2;
	
	public XMLParser xmlParser;
	
	public static StanfordCoreNLP pipeline;
	
	static {
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma");
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
	
	@Deprecated
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
	
	@Deprecated
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

	@Deprecated
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
//		int pseudoId = 72;
//		
//		QuestionSingle q = pipeline.preProcess(pseudoId);
//		if(q==null)
//			return;
//		System.out.println(q.getWordList());
//		System.out.println(q.getPOSList());
//		System.out.println(q.mention);
//		System.out.println(q.entityPositions);
//		System.out.println(q.surPredicates);
//		
//		System.out.println(pipeline.getLemma(q.question));
		
//		ArrayList<Question> qList = pipeline.xmlParser.getQuestions();
//		System.err.println(qList.size());
//		OutputRedirector.openFileOutput("./data/question-pos.txt");
//		for(int i=1; i<=300; i++) {
//			QuestionSingle qs = pipeline.xmlParser.getQuestionWithPseudoId(i).toQuestionSingle();
//			qs.qPOSList = pipeline.getPOSTag(qs);
//			System.out.println(i);
//			System.out.println(qs.question);
//			System.out.println(qs.getWordList());
//			System.out.println(qs.getPOSList());
//			System.out.println();
//		}
//		OutputRedirector.closeFileOutput();
		
//		for(int i=1; i<=300; i++) {
////			Question q = pipeline.xmlParser.getQuestionWithPseudoId(i);
//			QuestionFrame qf = pipeline.xmlParser.getQuestionFrameWithPseudoId(i);
//			qf.print();
//		}
		
	}
	
	public QuestionSingle preProcess(int pseudoId) {
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
		int beginOffset = Integer.parseInt(itemsOfQe[1]);
		int endOffset = Integer.parseInt(itemsOfQe[2]);
		
		// Get entity mention
		String mention = question.question.substring(beginOffset, endOffset);
		question.mention = mention;
		
		// Get POS tags
		LinkedList<String> POSList = getPOSTag(question);
		// Eliminate punctuation
		if(question.qWordList.size() == 0)
			System.err.println("qWord list error: size is 0 !");
		else {
			question.qWordList.remove(question.qWordList.size()-1);
			POSList.remove(POSList.size()-1);
			question.qPOSList = POSList;
		}
		
		// Get mention words position
		for (String string : question.qWordList)
			if(mention.contains(string))
				question.entityPositions.add(question.qWordList.indexOf(string));
		
		// Get entity and surrounding predicates
		String entityUri = itemsOfQe[4];
		question.entityUri = entityUri;
		LinkedList<RDFNode> predicates = ClientManagement.getSurroundingPred(entityUri);
		for (RDFNode predNode : predicates)
			question.surPredicates.add(predNode.toString());
		
		return question;
	}
	
	public LinkedList<String> getPOSTag(QuestionSingle question) {
		LinkedList<String> tags = new LinkedList<String>();
		Annotation annotation = new Annotation(question.question);
		pipeline.annotate(annotation);
		CoreMap labeledSentence = annotation.get(SentencesAnnotation.class).get(0);
		for (CoreLabel label : labeledSentence.get(TokensAnnotation.class)) {
			question.qWordList.add(label.get(TextAnnotation.class));
			tags.add(label.get(PartOfSpeechAnnotation.class));
		}
		question.qPOSList = tags;
		return tags;
	}
	
	/**
	 * Get POS tag results of each word in sentence
	 * @param sentence
	 * @return list of POS tags
	 */
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
	
	/**
	 * Get lemma form of words in sentence.
	 * @param sentence
	 * @return
	 */
	public LinkedList<String> getLemma(String sentence) {
		LinkedList<String> lemma = new LinkedList<>();
		Annotation annotation = new Annotation(sentence);
		pipeline.annotate(annotation);
		CoreMap labeledSentence = annotation.get(SentencesAnnotation.class).get(0);
		for (CoreLabel label : labeledSentence.get(TokensAnnotation.class)) {
			lemma.add(label.get(LemmaAnnotation.class));
		}
		return lemma;
	}
	
	public XMLParser getXMLParser() {
		return xmlParser;
	}

}
