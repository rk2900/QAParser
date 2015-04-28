package longquestion;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import paser.QuestionSingle;
import phrase.EntityPhrase;
import umbc.umbcDB;
import arq.qexpr;
import basic.FileOps;
import finder.Pipeline;

public class observe {
	public static Pipeline pipeline = new Pipeline();
	public static umbcDB db = new umbcDB();

	public static EntityPhrase observeOfEntitySurr(int questionId){
		
		QuestionSingle q = pipeline.preProcess(questionId);
		if(q==null){
//			System.out.println(questionId+"\tnull question");
			return null;
		}
		
		LinkedList<String> words = q.getWordList();
		LinkedList<String> postags = q.getPOSList();
		LinkedList<String> predicts = q.getSurPredicates();
		ArrayList<Integer> entityindexs = new ArrayList<Integer>(q.getEntityPositions());
		Collections.sort(entityindexs);
		int startOffset = entityindexs.get(0);
		int endOffest = entityindexs.get(entityindexs.size()-1);
	
		boolean matched = false;
		int phraseStartOffset = startOffset - 1;
		
		EntityPhrase entityPhrase = new EntityPhrase();
		entityPhrase.setEntityUri(q.entityUri);
		entityPhrase.setQuestionText(q.question);
		LinkedList<String> NNs = new LinkedList<String>();
		if(phraseStartOffset>=0 && postags.get(phraseStartOffset).equals("DT")){
			entityPhrase.setDT2(words.get(phraseStartOffset));
			phraseStartOffset--;
		}
		if(postags.get(phraseStartOffset).equals("IN")){
			entityPhrase.setIN(words.get(phraseStartOffset));
			phraseStartOffset--;
			if(postags.get(phraseStartOffset).startsWith("NN")){
				NNs.add(words.get(phraseStartOffset));
				phraseStartOffset--;
				while(postags.get(phraseStartOffset).startsWith("NN")){
					NNs.add(words.get(phraseStartOffset));
					phraseStartOffset--;
				}
				entityPhrase.setNNs(NNs);
				if(postags.get(phraseStartOffset).startsWith("JJ")){
					entityPhrase.setJJ(words.get(phraseStartOffset));
					phraseStartOffset--;
				}
				if(!postags.get(phraseStartOffset).equals("DT")){
					phraseStartOffset++;
				}else{
					entityPhrase.setDT1(words.get(phraseStartOffset));
				}
				matched = true;
			}
		}
		
		if(matched){
			StringBuilder phrase1 = new StringBuilder("Phrase1: ");
			for(int t=phraseStartOffset; t<=endOffest; ++t){
				phrase1.append(" ");
				phrase1.append(words.get(t));
			}
//			System.out.println(q.question);
//			System.out.println(phrase1.toString());
			
			boolean isTwoStep = false;
			startOffset = phraseStartOffset--;
			
			EntityPhrase twoStepPhrase = new EntityPhrase();
			if(phraseStartOffset>=0 && postags.get(phraseStartOffset).equals("DT")){
				twoStepPhrase.setDT2(words.get(phraseStartOffset));
				phraseStartOffset--;
			}
			if(postags.get(phraseStartOffset).equals("IN")){
				twoStepPhrase.setIN(words.get(phraseStartOffset));
				phraseStartOffset--;
				LinkedList<String> newNNs = new LinkedList<String>(); 
				if(postags.get(phraseStartOffset).startsWith("NN")){
					newNNs.add(words.get(phraseStartOffset));
					phraseStartOffset--;
					while(postags.get(phraseStartOffset).startsWith("NN")){
						newNNs.add(words.get(phraseStartOffset));
						phraseStartOffset--;
					}
					twoStepPhrase.setNNs(newNNs);
					if(postags.get(phraseStartOffset).startsWith("JJ")){
						twoStepPhrase.setJJ(words.get(phraseStartOffset));
						phraseStartOffset--;
					}
					if(!postags.get(phraseStartOffset).equals("DT")){
						phraseStartOffset++;
					}else{
						twoStepPhrase.setDT1(words.get(phraseStartOffset));
					}
					isTwoStep = true;
				}
			}
			
			if(isTwoStep){
				StringBuilder phrase2 = new StringBuilder("Phrase2: ");
				for(int t=phraseStartOffset; t<startOffset; ++t){
					phrase2.append(" ");
					phrase2.append(words.get(t));
				}
				System.out.println();
				System.out.println(questionId);
				System.out.println(q.question);
				System.out.println(phrase1.toString());
				System.out.println(phrase2.toString());
				entityPhrase.setPredictDetails(db, predicts);
				twoStepPhrase.insidEntityPhrase = entityPhrase;
				return twoStepPhrase;
			}
//			System.out.println(phraseStartOffset+"\t"+endOffest);
//			System.out.println(words.get(phraseStartOffset)+"\t"+words.get(endOffest));
//			entityPhrase.setPredictDetails(new umbcDB(), predicts);
			return entityPhrase;
		}else{
			return null;
		}
	}
	
	public static void observeOfRule(){
		LinkedList<String> sentences = FileOps.LoadFilebyLine("./data/questions.txt");
		
		int JJ = 0;
		int count = 0;
		boolean isJJ,isMatched;
		for(String sentence:sentences){
			if(sentence.startsWith("Give") || sentence.startsWith("List")){
				continue;
			}
			LinkedList<String> postags = pipeline.getPOSTag(sentence);
			
			int i = 0;
			while(i < postags.size()){
				isJJ = false;
				isMatched = false;
				if(postags.get(i).equals("DT")){
					++i;
					if(i<postags.size() && postags.get(i).equals("JJ")){
						isJJ = true;
						++i;
					}
					if(i<postags.size() && postags.get(i).startsWith("NN")){
						while(++i < postags.size()){
							if(!postags.get(i).startsWith("NN")){
								break;
							}
						}
						if(i<postags.size() && postags.get(i).equals("IN")){
							++i;
							if(i<postags.size() && postags.get(i).startsWith("NN")){
								isMatched = true;
							}
						}
					}
				}else{
					++i;
				}
				if(isMatched){
					++count;
					System.out.println(isJJ);
					System.out.println(sentence);
					System.out.println(postags);
				}
			}
		}
		System.out.println("***");
		System.out.println(count);
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		LinkedList<String> postag;
		String sentence;
		
//		sentence = "Who are the parents of the wife of Juan Carlos I?";
//		sentence = "What is the total amount of men and women serving in the FDNY?";
//		sentence = "Which U.S.state has been admitted latest?";
//		sentence = "Which other weapons did the designer of the Uzi develop?";
//		sentence = "What is the official language of Suriname?";
//		sentence = "Who was the wife of U.S. president Lincoln?";
//		sentence = "Give me all people that were born in Vienna and died in Berlin.";
//		postag = pipeline.getPOSTag(sentence);
//		System.out.println(sentence);
//		System.out.println(postag);
//		observeOfRule();
//		observeOfEntitySurr(77);
		
//		PrintStream old = System.out;
		
//		FileOutputStream fout;
//		try {
//			fout = new FileOutputStream("./data/zch/entity-phrase-pattern.txt");
//			System.setOut(new PrintStream(fout));
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
//		int count = 0;
		
		for(int i=1; i<=300; ++i){
			EntityPhrase entityPhrase = observeOfEntitySurr(i);
			if(entityPhrase != null){
				if(entityPhrase.insidEntityPhrase != null){
					System.out.println(entityPhrase.insidEntityPhrase.getPredictDetails().get(0).getPredictUri());
				}
			}
//			if(entityPhrase != null){
//				++count;
//				entityPhrase.printEntityPhrase();
////				entityPhrase.ranking(db, predictUris);
//			}
		}
//		System.setOut(old);
//		System.out.println(count);
	}

}
