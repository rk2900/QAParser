package similarity;

import java.util.HashSet;
import java.util.LinkedList;

import knowledgebase.ClientManagement;
import paser.QuestionSingle;
import finder.Pipeline;

public class Extraction {
	
	public static void lexical(String word1, String word2){
		
	}

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Pipeline pipeline = new Pipeline();
		int pseudoId = 5;
		
		QuestionSingle q = pipeline.preProcess(pseudoId);
		if(q==null)
			return;
		
		LinkedList<String> words = q.getWordList();
		LinkedList<String> postags = q.getPOSList();
		HashSet<Integer> entityindexes = q.getEntityPositions();
		LinkedList<String> predicts = q.getSurPredicates();
		
		System.out.println(q.getWordList());
		System.out.println(q.getPOSList());
		System.out.println(q.mention);
		System.out.println(q.entityPositions);
		System.out.println(q.surPredicates.size());
		
		for (String predict : predicts) {
			System.out.println(predict);
			LinkedList<String> labels = ClientManagement.getLabel(predict);
			StringBuilder sb = new StringBuilder();
			for (String label : labels) {
				sb.append(label);
				sb.append("\t");
			}
			sb.append("\n");
			System.out.println(sb.toString());
		}

	}

}
