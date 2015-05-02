package syntacticParser;

import java.util.ArrayList;
import java.util.List;

public class ConstraintSet {
	List<Constraint> list=new ArrayList<Constraint>();
	Node ans=new Node();
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
}
