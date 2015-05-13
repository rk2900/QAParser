package coverage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import paser.Question;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

import knowledgebase.ClientManagement;

public class SubGraph {
	public Question question;
	public String rootUri;
	public HashMap<String, LinkedList<RDFNode>> surPropertyList;
	public HashSet<String> surResourceSet;
	
	public SubGraph(Question q, String uri) {
		question = q;
		rootUri = new String(uri);
		surResourceSet = new HashSet<>();
		surPropertyList = new HashMap<>();
	}

	public void expand(boolean visible) {
		System.err.println(rootUri);
		String sparql = "SELECT ?p ?e WHERE {"
				+ "{<"+rootUri+"> ?p ?e} UNION {?e ?p <"+rootUri+">}"
				+ "}";
		
		ResultSet rs = ClientManagement.query(sparql, visible);
		String lastProperty = "";
		while(rs.hasNext()) {
			QuerySolution qs = rs.next();
			RDFNode e = qs.get("e");
			RDFNode p = qs.get("p");
			String prop = p.toString();
			if(surPropertyList.containsKey(prop))
				surPropertyList.get(prop).add(e);
			else {
				LinkedList<RDFNode> entityList = new LinkedList<>();
				entityList.add(e);
				surPropertyList.put(prop, entityList);
			}
			
			if(e.isResource()) {
				surResourceSet.add(e.toString());
			}
			
			// Print
			if(visible) {
				if(prop.equalsIgnoreCase(lastProperty));
				else {
					System.out.println("\t"+prop);
					lastProperty = prop;
				}
				System.out.println("\t\t"+e.toString());
			}
		}
		System.err.println("Construction finished.");
	}
}
