package baseline;

import paser.QuestionFrame;
import finder.Pipeline;

public class test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		Pipeline pipeline = new Pipeline();
		QuestionFrame qf = pipeline.xmlParser.getQuestionFrameWithId(296);
		String test = qf.question;
//		System.out.println(test.substring(beginIndex, endIndex));
		System.out.println(qf.wordList);
		
	}	

}
