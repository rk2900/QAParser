package syntacticParser;

import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.trees.Tree;
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
			if(!c.left.isx&&!c.left.transformed) {
				c.left.transform(w);
			}
			if(!c.right.isx&&!c.right.transformed) {
				c.right.transform(w);
			}
		}
	}
	public static ConstraintSet getConstraintSet(String question, QuestionFrame qf) {
		ConstraintSet cs=new ConstraintSet();
		Tree tree=stringParser.parse(question);
		constraintExtractor.extract(qf,tree,tree,cs.ans,cs);
		for (Constraint l:cs.list) {
			l.left.POS(cs);
			l.right.POS(cs);
		}
		cs.transform(qf.wordList);
		return cs;
	}
}
