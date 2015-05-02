package syntacticParser;

public class Constraint {
	public Node left;
	public String edge;
	public Node right;
	public Constraint(Node l, String e, Node r) {
		left=l;
		edge=e;
		right=r;
	}
	public String getString() {
		return "\""+left.getString()+"\"\t\""+edge+"\"\t\""+right.getString()+"\"";
	}
}
