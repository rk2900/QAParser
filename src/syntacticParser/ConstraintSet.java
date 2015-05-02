package syntacticParser;

import java.util.ArrayList;
import java.util.List;

import paser.QuestionFrame;

public class ConstraintSet {
	public List<Constraint> list=new ArrayList<Constraint>();
	public Node ans=new Node();
	public ConstraintSet() {
		
	}
	public void add(Constraint c) {
		list.add(c);
	}
	public String toString() {
		String ret="Ans: "+ans.toString()+"\r\n";
		for (Constraint c:list) {
			ret+=c.getString()+"\r\n";
		}
		return ret;
	}
	public void transform(List<String> w) {
		for (Constraint c:list) {
			if(c.left.isx&&!c.left.transformed) {
				c.left.transform(w);
			}
			if(c.right.isx&&!c.right.transformed) {
				
			}
		}
	}
	public static ConstraintSet getConstraintSet(String question, QuestionFrame qf) {
		return null;
	}
}
