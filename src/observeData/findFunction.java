package observeData;

import java.util.LinkedList;

import basic.FileOps;

public class findFunction {

	public static void findHowMany(){
		LinkedList<String> questions = FileOps.LoadFilebyLine("./data/questions.txt");
		
		int count = 0;
		for (String question : questions) {
//			question = question.toLowerCase();
			if(question.startsWith("When")){
				++count;
				System.out.println(question);
			}
		}
		System.out.println(count);
	}
	
	public static void main(String args[]){
		findHowMany();
	}
}
