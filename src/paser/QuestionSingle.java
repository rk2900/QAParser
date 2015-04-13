package paser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

public class QuestionSingle extends Question{

	public String mention;
	public LinkedList<String> qWordList;
	public LinkedList<String> qPOSList;
	public HashSet<Integer> entityPositions;
	public LinkedList<String> surPredicates;
	
	public QuestionSingle(int id, boolean onlydbo, boolean aggregation,
			boolean hybrid, String answerType, ArrayList<String> keywords,
			String question, ArrayList<String> answers, String query) {
		super(id, onlydbo, aggregation, hybrid, answerType, keywords, question,
				answers, query);
		qWordList = new LinkedList<String>();
		qPOSList = new LinkedList<String>();
		entityPositions = new HashSet<Integer>();
		surPredicates = new LinkedList<String>();
	}
	
	public HashSet<Integer> getEntityPositions() {
		return entityPositions;
	}

	public LinkedList<String> getPOSList() {
		return qPOSList;
	}

	public LinkedList<String> getWordList() {
		return qWordList;
	}


	public LinkedList<String> getSurPredicates() {
		return surPredicates;
	}
	
}
