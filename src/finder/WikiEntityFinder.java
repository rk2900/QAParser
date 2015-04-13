package finder;

import java.util.LinkedList;

import knowledgebase.ClientManagement;
import basic.FileOps;

import com.franz.agraph.jena.AGModel;
import com.franz.agraph.jena.AGQuery;
import com.franz.agraph.jena.AGQueryExecutionFactory;
import com.franz.agraph.jena.AGQueryFactory;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

/**
 * To find entities in DBpedia linking with wiki URL and save in files.
 * @author kren
 *
 */
public class WikiEntityFinder {
	
//	private HashMap<Integer, String> readWikiUrl() {
//		LinkedList<String> lines = FileOps.LoadFilebyLine("./data/mark.txt", "utf-8");
//		HashMap<Integer, String> wikiMaps = new HashMap<Integer, String>();
//		for (String line : lines) {
//			String[] items = line.split("\t");
//			int id = Integer.parseInt(items[0]);
//			String url = items[3];
//			wikiMaps.put(id, url);
//		}
//		return wikiMaps;
//	}
	
	private LinkedList<RDFNode> getEntityOfWiki(String wikiUrl) {
		LinkedList<RDFNode> entityNodes = new LinkedList<RDFNode>();
		AGModel model = null;
		try {
			model = ClientManagement.getAgModel();
		} catch (Exception e) {
			System.err.println("Error in model assertion: "+e);
			e.printStackTrace();
		}
		try {
			String queryString = "SELECT ?s ?p  WHERE {"
					+ "?s ?p <" + wikiUrl + ">.} LIMIT 10";
			AGQuery sparql = AGQueryFactory.create(queryString);
			QueryExecution qe = AGQueryExecutionFactory.create(sparql, model);
			try {
				ResultSet results = qe.execSelect();
				while (results.hasNext()) {
					QuerySolution result = results.next();
					RDFNode s = result.get("s");
					entityNodes.add(s);
				}
			} finally {
				qe.close();
			}
		} finally {
			ClientManagement.clearAll();
		}
		return entityNodes;
	}

	public void wikiLinkEntity(String inputFile, String outputFile) {
		LinkedList<String> lines = FileOps.LoadFilebyLine(inputFile);
		LinkedList<String> linesToOutput = new LinkedList<String>();
		for (String line : lines) {
			System.out.println(line);
			String[] items = line.split("\t");
			String wikiUrl = items[3];
			LinkedList<RDFNode> entities = getEntityOfWiki(wikiUrl);
			StringBuilder sb = new StringBuilder();
			sb.append(line);
			for (RDFNode rdfNode : entities) {
				sb.append("\t").append(rdfNode);
			}
			linesToOutput.add(sb.toString());
		}
		FileOps.SaveList(outputFile, linesToOutput);
	}
	
	public static void main(String[] args) {
		WikiEntityFinder finder = new WikiEntityFinder();
		finder.wikiLinkEntity("./data/q-e/all-mark-wiki.txt", 
				"./data/q-e/all-mark-wiki-entity.txt");
	}

}
