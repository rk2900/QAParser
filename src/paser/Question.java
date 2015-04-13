package paser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import com.hp.hpl.jena.rdf.model.RDFNode;

public class Question {
	//public enum answerType {resource, date, number, string, bool, list};
	public int id;
	public boolean onlydbo;
	public boolean aggregation;
	public boolean hybrid;
	public String answerType;
	public ArrayList<String> keywords;
	
	public String question;
	public ArrayList<String> answers;
	
	public String query;
	
	public LinkedList<String> qWordList;
	public LinkedList<String> qPOSList;
	public HashSet<Integer> entityPositions;
	public LinkedList<String> surPredicates;
	
	public Question(int id, boolean onlydbo, boolean aggregation, boolean hybrid, String answerType,
			ArrayList<String> keywords, String question, ArrayList<String> answers, String query) {
		this.id = id;
		this.onlydbo = onlydbo;
		this.aggregation = aggregation;
		this.hybrid = hybrid;
		this.answerType = answerType;
		this.keywords = keywords;
		this.question = question;
		this.answers = answers;
		this.query = query;
	}
	
	
	public void print() {
		System.out.println("-------------------------");
		System.out.println("ID: "+id);
		System.out.println("Hybrid: "+hybrid);
		System.out.println("OnlyDBO: "+onlydbo);
		System.out.println("Aggregation: "+aggregation);
		System.out.println("Answer Type: "+answerType);
		System.out.print("Keyword: ");
		for(String key: keywords) {
			System.out.print(key+"\t");
		}
		System.out.println();
		System.out.println("Question: "+question);
		System.out.println("Query: ");
		System.out.println(query);
		System.out.print("Answers: ");
		for(String a: answers) {
			System.out.print(a+"\t");
		}
		System.out.println();
		System.out.println("-------------------------");
	}

	public HashSet<Integer> getEntityPositions() {
		// TODO Auto-generated method stub
		return entityPositions;
	}

	public LinkedList<String> getPOSList() {
		// TODO Auto-generated method stub
		return qPOSList;
	}

	public LinkedList<String> getWordList() {
		// TODO Auto-generated method stub
		return qWordList;
	}


	public LinkedList<String> getSurPredicates() {
		// TODO Auto-generated method stub
		return surPredicates;
	}
	
}
