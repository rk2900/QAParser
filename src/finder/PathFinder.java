package finder;

import java.util.LinkedList;

import knowledgebase.ClientManagement;

import com.franz.agraph.jena.AGModel;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

import paser.Path;

public class PathFinder {

	private static AGModel agModel = null;
	
	private static void AGInit() {
		try {
			agModel = ClientManagement.getAgModel();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public PathFinder() {
		AGInit();
	}
	
	public AGModel getAgModel() {
		return agModel;
	}
	
	/**
	 * To get the path between root entity and end entity.
	 * @param root
	 * @param end
	 * @param length the length of path (counting edges)
	 * @return
	 */
	public LinkedList<Path> getPath(String root, String end, int length) {
		LinkedList<String> vars = new LinkedList<>();
		String rootLabel = "A";
		String endLabel = "B";
		LinkedList<Path> pathList = new LinkedList<>();
		
		StringBuilder sparqlBuilder = new StringBuilder();
		sparqlBuilder.append("SELECT ");
		for(int i=0; i<length*2-1; i++) {
			String n = new String("?n"+i);
			vars.add(n);
			sparqlBuilder.append(n).append(" ");
		}
		vars.add(0, rootLabel);
		vars.add(vars.size(), endLabel);
		
		sparqlBuilder.append("WHERE { ");
		String lastNode = vars.get(0);
		for(int i=0; i<length; i++) {
			sparqlBuilder.append(lastNode).append(" ");
			sparqlBuilder.append(vars.get(i*2+1)).append(" ");
			sparqlBuilder.append(vars.get(i*2+1+1)).append(". ");
			lastNode = new String(vars.get(i*2+1+1));
		}
		sparqlBuilder.append("}");
		
		sparqlBuilder.replace(sparqlBuilder.indexOf(rootLabel), 
				sparqlBuilder.indexOf(rootLabel)+1, 
				"<"+root+">");
		sparqlBuilder.replace(sparqlBuilder.indexOf(endLabel), 
				sparqlBuilder.indexOf(endLabel)+1,
				"<"+end+">");
		
//		System.out.println(sparqlBuilder.toString());
		ResultSet rs = ClientManagement.query(sparqlBuilder.toString());
		while(rs.hasNext()) {
			Path path = new Path(root, end);
			QuerySolution qSolution = rs.next();
			LinkedList<RDFNode> list = new LinkedList<>();
			for(int i=0; i<length*2-1; i++) {
				String n = new String("n"+i);
				list.add(qSolution.get(n));
			}
			path.extend(list);
			pathList.add(path);
		}
		return pathList;
	}
	
	public static void main(String[] args) {
//		XMLParser parser = new XMLParser("./data/qald-5_train.xml");
//		parser.load();
//		parser.parse();
		
//		ArrayList<Question> qList = parser.getQuestionsPart(0, 301, "resource");
//		Question q = qList.get(0);
//		
//		for (String answer : q.answers) {
//			System.out.println(answer);
//		}
		
		PathFinder finder = new PathFinder();
		LinkedList<Path> pathList = finder.getPath("http://dbpedia.org/resource/China", "http://dbpedia.org/resource/Beijing", 1);
		for (Path path : pathList) {
			System.out.println(path);
		}
	}

}
