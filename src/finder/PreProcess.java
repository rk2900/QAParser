package finder;

import java.util.LinkedList;

import paser.QuestionFrame;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

public class PreProcess {
	
	public static void processWordAndPOS(QuestionFrame q) {
		LinkedList<String> wordList = new LinkedList<>();
		LinkedList<String> posList = new LinkedList<>();
		String sentence = q.question;
		Annotation annotation = new Annotation(sentence);
		Pipeline.pipeline.annotate(annotation);
		CoreMap labeledSentence = annotation.get(SentencesAnnotation.class).get(0);
		for(CoreLabel label: labeledSentence.get(TokensAnnotation.class)) {
			wordList.add(label.get(TextAnnotation.class));
			posList.add(label.get(PartOfSpeechAnnotation.class));
		}
		if(wordList.size() != posList.size()) {
			System.err.println("Word list is not as long as POS list!");
			return ;
		}
		
		// to remove the last punctuation
		wordList.remove(wordList.size()-1);
		posList.remove(posList.size()-1);
		
		q.setWordList(wordList);
		q.setPOSList(posList);
	}
	
	public static void main(String[] args) {
		LinkedList<String> wordList = new LinkedList<>();
		LinkedList<String> posList = new LinkedList<>();
		String sentence = "\nDoes the new Battlestar Galactica series have more episodes than the old one?";
		Annotation annotation = new Annotation(sentence);
		Pipeline.pipeline.annotate(annotation);
		CoreMap labeledSentence = annotation.get(SentencesAnnotation.class).get(0);
		for(CoreLabel label: labeledSentence.get(TokensAnnotation.class)) {
			wordList.add(label.get(TextAnnotation.class));
			posList.add(label.get(PartOfSpeechAnnotation.class));
		}
		if(wordList.size() != posList.size()) {
			System.err.println("Word list is not as long as POS list!");
			return ;
		}
		
		// to remove the last punctuation
		wordList.remove(wordList.size()-1);
		posList.remove(posList.size()-1);
		
		System.out.println(wordList);
		System.out.println(posList);
		
	}

}
