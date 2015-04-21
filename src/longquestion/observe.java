package longquestion;

import java.util.LinkedList;

import finder.Pipeline;

public class observe {
	public static Pipeline pipeline = new Pipeline();
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		LinkedList<String> postag;
		String sentence;
		
		sentence = "Who are the parents of the wife of Juan Carlos I?";
		sentence = "What is the total amount of men and women serving in the FDNY?";
		sentence = "Which U.S.state has been admitted latest?";
		sentence = "Which other weapons did the designer of the Uzi develop?";
		postag = pipeline.getPOSTag(sentence);
		
		System.out.println(sentence);
		System.out.println(postag);
	}

}
