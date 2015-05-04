package baseline;

import java.util.ArrayList;

import paser.QuestionFrame;

public class Answer {
	public ArrayList<Predict> predictList;
	public String entityUri;
	public QuestionFrame qf;
	public String exceptionString;
	
	public boolean isException(){
		if(exceptionString.length() > 0 ){
			return true;
		}else{
			return false;
		}
	}
}
