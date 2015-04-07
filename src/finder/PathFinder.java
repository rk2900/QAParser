package finder;

import java.util.ArrayList;

import knowledgebase.ClientManagement;

import com.franz.agraph.jena.AGModel;
import com.franz.agraph.jena.AGQuery;
import com.franz.agraph.jena.AGQueryExecutionFactory;
import com.franz.agraph.jena.AGQueryFactory;
import com.franz.agraph.repository.AGRepositoryConnection;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

import paser.Question;
import paser.XMLParser;

public class PathFinder {

	private static AGModel agModel = null;
	private static int segmentThreshold = 5;
	
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
	
	public ArrayList<RDFNode> getPath(String root, String end) {
		ArrayList<RDFNode> path = new ArrayList<>();
		String posPathQuery = "select ?p where {<"+root+"> ?p <"+end+">.}";
		AGQuery agQuery = AGQueryFactory.create(posPathQuery);
		QueryExecution qe = AGQueryExecutionFactory.create(agQuery, agModel);
		ResultSet rs = qe.execSelect();
		while(rs.hasNext()) {
			QuerySolution qs = rs.next();
			RDFNode node = qs.get("p");
			path.add(node);
		}
		return path;
	}
	
	public static void main(String[] args) {
		PathFinder finder = new PathFinder();
		XMLParser parser = new XMLParser("./data/qald-5_train.xml");
		parser.load();
		parser.parse();
		
		ArrayList<Question> qList = parser.getQuestionsPart(0, 301, "resource");
		Question q = qList.get(0);
		
		for (String answer : q.answers) {
			System.out.println(answer);
		}
		
	}

}
