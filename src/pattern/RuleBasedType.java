package pattern;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import paser.Question;
import tool.OutputRedirector;
import tool.Tool;

public class RuleBasedType extends TypeExtraction{

	private static String[] prepositionWords = {"list", "all", "which", "what", "many"};
	private static String[] interrogativeWords = {"who", "when", "where"};
	private static HashSet<String> prepositionRules;
	private static HashMap<String, String[]> interrogativeRules;
	
	private static StanfordCoreNLP pipeline;
	
	static {
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
		pipeline = new StanfordCoreNLP(props);
	}
	
	static {
		prepositionRules = new HashSet<>();
		interrogativeRules = new HashMap<>();
		for (String string : prepositionWords)
			prepositionRules.add(string);
		String[] who = {"person", "organisation"};
		interrogativeRules.put("who", who);
		String[] when = {"date"};
		interrogativeRules.put("when", when);
		String[] where = {"place"};
		interrogativeRules.put("where", where);
	}

	@Override
	public HashSet<String> typeExtractor(String text, boolean visible) {
		// Initialize
		HashSet<String> types = new HashSet<>();
		LinkedList<String> words = new LinkedList<>();
		HashMap<String, String> posMapping = new HashMap<>();
		
		// Annotation
		Annotation annotation = new Annotation(text);
		pipeline.annotate(annotation);
		CoreMap sentence = annotation.get(SentencesAnnotation.class).get(0);
		for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
			String word = token.get(TextAnnotation.class);
			String pos = token.get(PartOfSpeechAnnotation.class);
			words.add(word);
			posMapping.put(word, pos);
			if(visible)
				System.out.println("\t"+word+"\t"+pos);
		}
		
		boolean prepositionFlag = false;
		boolean interregativeFlag = false;
		String interregativeKey = "";
		StringBuilder type = new StringBuilder();
		for (String word : words) {
			if(prepositionFlag) {
				if(posMapping.get(word).startsWith("NN")) {
					if(type.length()>0) type.append(" ");
					type.append(word);
				} else if(type.length()>0){
					break;
				} else {
					continue;
				}
			}
			
			for (String prePhrase : prepositionRules) {
				if(prePhrase.startsWith(word.toLowerCase())) {
					prepositionFlag = true;
					break;
				}
			}
			
			for (String interPhrase : interrogativeRules.keySet()) {
				if(interPhrase.startsWith(word.toLowerCase())) {
					interregativeFlag = true;
					interregativeKey = interPhrase;
					break;
				}
			}
		}
		if(type.length()>0) types.add(type.toString());
		if(interregativeFlag) 
			for (String t : interrogativeRules.get(interregativeKey)) {
				types.add(t);
			}
		return types;
	}
	
	public static void main(String[] args) {
		RuleBasedType rbType = new RuleBasedType();
		rbType.initializeXMLParser();
		
		OutputRedirector.openFileOutput("./data/type_in_text.txt");
		
		for(int i=1; i<=300; i++) {
			Question q = rbType.xmlParser.getQuestionWithPseudoId(i);
			String qText = q.question;
			HashSet<String> types = rbType.typeExtractor(qText, false);
			if(types.size()>0) {
				System.out.println(i+"\t"+qText);
				Tool.printList(types);
				Tool.printList(q.answers);
				System.out.println("------------------------------");
			}
			
			
		}
		
		OutputRedirector.closeFileOutput();
		System.out.println("Annotation finished.");
	}

}
