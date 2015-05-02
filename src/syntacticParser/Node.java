package syntacticParser;

import java.util.List;

public class Node {
	public boolean isx;
	public String str;
	public int left;
	public int right;
	public boolean transformed;
	public Node(String s, int l, int r) {
		str=s;
		left=l;
		right=r;
		isx=false;
		transformed=false;
	}
	public Node() {
		isx=true;
		transformed=false;
	}
	public String getString() {
		if(isx) {
			return this.toString();
		} else return str+"["+left+","+right+"]";
	}
	public void transform(List<String> w) {
		int loc=0;
		int tl=left;
		int tr=right;
		for (int i=0;i<w.size();i++) {
			if(tl==loc) left=i;
			loc+=w.get(i).length();
			if(tr==loc) right=i;
		}
		transformed=true;
	}
}
