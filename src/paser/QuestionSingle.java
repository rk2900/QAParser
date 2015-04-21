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
	public String entityUri;
	
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
	
	public QuestionSingle(int id, boolean onlydbo, boolean aggregation,
			boolean hybrid, String answerType, ArrayList<String> keywords,
			String question, ArrayList<String> answers, String query,
			String mention, LinkedList<String> qWordList,
			LinkedList<String> qPOSList, HashSet<Integer> entityPositions,
			LinkedList<String> surPredicates, String entityUri) {
		super(id, onlydbo, aggregation, hybrid, answerType, keywords, question,
				answers, query);
		this.mention = mention;
		this.qWordList = qWordList;
		this.qPOSList = qPOSList;
		this.entityPositions = entityPositions;
		this.surPredicates = surPredicates;
		this.entityUri = entityUri;
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
	
	public String getEntityUri(){
		return entityUri;
	}
	
}
