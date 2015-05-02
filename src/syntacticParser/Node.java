package syntacticParser;

public class Node {
	public boolean isx;
	public String str;
	public int leftchar;
	public int rightchar;
	public Node(String s, int l, int r) {
		str=s;
		leftchar=l;
		rightchar=r;
		isx=false;
	}
	public Node() {
		isx=true;
	}
	public String getString() {
		if(isx) {
			return this.toString();
		} else return str+"["+leftchar+","+rightchar+"]";
	}
}
