package paser;

import java.util.LinkedList;

import com.hp.hpl.jena.rdf.model.RDFNode;

public class Path {
	private String root;
	private String end;
	
	private LinkedList<RDFNode> pathChain;
	
	public Path(String r, String e) {
		root = new String(r);
		end = new String(e);
		pathChain = new LinkedList<>();
	}
	
	public void extend(RDFNode edge) {
		pathChain.add(edge);
	}
	
	public void extend(RDFNode node, RDFNode edge) {
		pathChain.add(node);
		pathChain.add(edge);
	}
	
	public void extend(LinkedList<RDFNode> list) {
		pathChain.addAll(list);
	}
	
	public String getRoot() {
		return root;
	}
	
	public String getEnd() {
		return end;
	}
	
	public RDFNode getNode(int index) {
		if(2*index > pathChain.size()) {
			System.out.println("Error: Length exceed!");
			return null;
		} else {
			return pathChain.get(2*index-1);
		}
	}
	
	public RDFNode getEdge(int index) {
		if(2*index-1 > pathChain.size()) {
			System.out.println("Error: Length exceed!");
			return null;
		} else {
			return pathChain.get(2*index-2);
		}
	}
	
	public LinkedList<RDFNode> getPathChain() {
		return pathChain;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(root);
		for (RDFNode rdfNode : pathChain) {
			sb.append("\t");
			sb.append(rdfNode);
		}
		sb.append("\t");
		sb.append(end);
		return sb.toString();
	}
	
	public Integer getLengh() {
		return (pathChain.size()+1)/2;
	}
}
