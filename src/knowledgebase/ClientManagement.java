package knowledgebase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import baseline.Predicate;

import com.franz.agraph.jena.AGGraph;
import com.franz.agraph.jena.AGGraphMaker;
import com.franz.agraph.jena.AGModel;
import com.franz.agraph.jena.AGQuery;
import com.franz.agraph.jena.AGQueryExecution;
import com.franz.agraph.jena.AGQueryExecutionFactory;
import com.franz.agraph.jena.AGQueryFactory;
import com.franz.agraph.repository.AGCatalog;
import com.franz.agraph.repository.AGRepository;
import com.franz.agraph.repository.AGRepositoryConnection;
import com.franz.agraph.repository.AGServer;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class ClientManagement {
	public enum Server{
		LOCAL, ONLINE
	}
	public static Server serverType = Server.LOCAL; //0: locale 1: online
	
	public static final String SERVER_URL = "http://172.16.2.21:10035";//"http://agraph.apexlab.org";
	public static final String CATALOG_ID = "dbpedia2014";
	public static final String REPOSITORY_ID = "dbpedia_full";
	public static final String USERNAME = "dbpedia";
	public static final String PASSWORD = "apex";
	public static final String TEMPORARY_DIRECTORY = "";
	
	public static final String DBPEDIA_SERVER = "http://dbpedia.org/sparql";
	
	
	static final String RDFS_NS = "http://www.w3.org/2000/01/rdf-schema#";
	static final String DBPEDIA_NS = "http://dbpedia.org/resource/";
	static final String DBPEDIA_OWL_NS = "http://dbpedia.org/ontology/";
	static final String DBPEDIA_PROP_NS = "http://dbpedia.org/property/";
	
	private static AGModel model = null;
	
	private static LinkedList<AGRepositoryConnection> toCloseConnection = new LinkedList<AGRepositoryConnection>();
	
	protected static void closeBeforeExit(AGRepositoryConnection conn) {
		toCloseConnection.add(conn);
	}
	
	static void close(AGRepositoryConnection conn) {
		try {
			conn.close();
		} catch (Exception e) {
			System.err.println("Error closing repository connection: "+e);
			e.printStackTrace();
		}
	}
	
	protected static void closeAllConnections() {
		for (AGRepositoryConnection conn : toCloseConnection) {
			close(conn);
		}
	}
	
	/**
	 * To get AGraph model structure
	 * @return
	 * @throws Exception
	 */
	public static AGModel getAgModel() throws Exception{
		if(ClientManagement.model == null) {
			AGServer server = new AGServer(SERVER_URL, USERNAME, PASSWORD);
			System.out.println("Available catalogs: "+server.listCatalogs());
			AGCatalog catalog = server.getCatalog(CATALOG_ID);
			AGRepository repository = catalog.openRepository(REPOSITORY_ID);
			AGRepositoryConnection connection = repository.getConnection();
			AGGraphMaker maker = new AGGraphMaker(connection);
			System.out.println("\nGot a graph maker for the connection.");
			AGGraph graph = maker.getGraph();
			AGModel model = new AGModel(graph);
			ClientManagement.model = model;
		}
		return model;
	}
	
	public static AGRepositoryConnection getConnection() {
		AGRepositoryConnection conn = null;
		if(!toCloseConnection.isEmpty()) {
			return toCloseConnection.getLast();
		} else {
			//TODO
		}
		return conn;
	}
	
	public static void setServerType(Server s) {
		serverType = s;
	}
	
	/**
	 * To execute a specific query.
	 * @param sparql
	 * @param visible whether print out real SPARQL query string
	 * @return
	 */
	public static ResultSet query(String sparql, boolean visible) {
		ResultSet rs = null;
		if(serverType == Server.LOCAL) {
			try {
				getAgModel();
			} catch (Exception e) {
				e.printStackTrace();
			}
			if (visible) {
				System.err.println("\t-"+sparql);
			}
			AGQuery query = AGQueryFactory.create(sparql);
			QueryExecution qe = AGQueryExecutionFactory.create(query, model);
			rs = qe.execSelect();
		} else if(serverType == Server.ONLINE) {
			if(DBPEDIA_SERVER != null && DBPEDIA_SERVER.length() > 0) {
	            QueryExecution qe = QueryExecutionFactory.sparqlService(DBPEDIA_SERVER, sparql);
	            if(visible)
	            	System.err.println(sparql);
	            rs = qe.execSelect();
	        }
		} 
		return rs;
	}
	
	/**
	 * To ask if some triple exists 
	 * @param sparql ASK query
	 * @param visible
	 * @return whether exist
	 */
	public static boolean ask(String sparql, boolean visible) {
		AGQuery query = AGQueryFactory.create(sparql);
		if(visible)
			System.err.println(sparql);
		boolean flag = false;
		try {
			AGQueryExecution qe = AGQueryExecutionFactory.create(query, getAgModel());
			flag = qe.execAsk();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return flag;
	}
	
	public static void clearAll() {
		closeAllConnections();
		ClientManagement.model.close();
	}
	
	public static RDFNode getEntityRedirectUri(RDFNode node) {
		RDFNode rtn = node;
		String queryString = "SELECT ?o WHERE {"
				+ "<"+node.toString()+"> dbo:wikiPageRedirects ?o."
				+ "}";
		ResultSet rs = query(queryString, false);
		if (rs.hasNext()) {
			RDFNode redirectNode = rs.next().get("o");
			rtn = redirectNode;
		}
		return rtn;
	}
	
	public static RDFNode getEntityOfWiki(String wikiUrl) {
		RDFNode node = null;
		
		String queryString = "SELECT ?s ?p  WHERE {"
				+ "?s ?p <" + wikiUrl + ">."
						+ "} LIMIT 1";
		ResultSet results = query(queryString, false);
		if (results.hasNext()) {
			QuerySolution result = results.next();
			RDFNode s = result.get("s");
			node = getEntityRedirectUri(s);
		}
		return node;
	}
	
	public static LinkedList<RDFNode> getSurroundingPred(String entityUri) {
		LinkedList<RDFNode> predList = new LinkedList<>();
		
		// Forward & Backward
		String fSPO = "<"+entityUri+">" + " ?p ?o.";
		String bSPO = "?s ?p " + "<"+entityUri+">";
		
		String sparql = "SELECT DISTINCT ?p WHERE { "
				+"{" +fSPO+ "} UNION {" +bSPO+ "}"
						+ "}";
		ResultSet rs = ClientManagement.query(sparql, false);
		while(rs.hasNext()) {
			RDFNode p = rs.next().get("p");
			if(!BlackList.predicateSet.contains(p.toString()))
				predList.add(p);
		}
		
		return predList;
	}
	
	/**
	 * To get the direction of the node and predicate
	 * @param nodeUri
	 * @param predUri
	 * @return -1: do not exist; 0: outward; 1: inward; 2: bidirectional 
	 */
	public static int getDirection(String nodeUri, String predUri) {
		String fSPO = "<"+nodeUri+">" + " <"+predUri+"> ?o.";
		String bSPO = "?s <"+predUri+"> " + "<"+nodeUri+">";
		
		int directionType = -1;
		String outSparql = "ASK WHERE { " +fSPO + "}";
		String inSparql = "ASK WHERE {"+bSPO+"}";
		if(ClientManagement.ask(outSparql, false)) {
			directionType++;
		}
		if(ClientManagement.ask(inSparql, false)) {
			directionType+=2;
		}
		return directionType;
	}

	/**
	 * To get the label of given URI in English
	 * @param uri
	 * @return
	 */
	public static LinkedList<String> getLabel(String uri) {
		LinkedList<String> labels = new LinkedList<>();
		String sparql = "SELECT ?label WHERE { "
				+ "<" +uri+">" + " rdfs:label ?label ."
						+ "FILTER(LANGMATCHES(LANG(?label), \"en\"))"
						+ "}";
		ResultSet rs = ClientManagement.query(sparql, false);
		while (rs.hasNext()) {
			RDFNode label = rs.next().get("label");
			labels.add(label.asLiteral().getString());
		}
		return labels;
	}
	
	public static LinkedList<String> getLabel(RDFNode node) {
		return getLabel(node.toString());
	}
	
	/**
	 * To get the other node of given predicate with regard to one node
	 * @param node
	 * @param predicate
	 * @return
	 */
	public static LinkedList<RDFNode> getNode(String node, String predicate) {
		LinkedList<RDFNode> nodes = new LinkedList<>();
		String query = "SELECT DISTINCT ?node WHERE { "
				+ "{<" + node + "> " + "<" + predicate + "> " +"?node."
						+ "} UNION {"
						+ "?node " + "<" + predicate + "> <" + node + "> ."
						+ "}" + "}";
		ResultSet rs = ClientManagement.query(query, false);
		while(rs.hasNext()) {
			RDFNode rdfNode = rs.next().get("node");
			nodes.add(rdfNode);
		}
		return nodes;
	}
	
	/**
	 * To get the domain of given predicate
	 * @param predicate
	 * @return null if the predicate does not have domains 
	 */
	public static RDFNode getDomain(String predicate) {
		RDFNode domain = null;
		String query = "SELECT ?domain WHERE { "
				+ "<" + predicate +"> rdfs:domain ?domain."
						+ "}";
		ResultSet rs = ClientManagement.query(query, false);
		if(rs.hasNext()) {
			domain = rs.next().get("domain");
		}
		return domain;
	}
	
	/**
	 * To get range of given predicate
	 * @param predicate
	 * @return null if the predicate does not have ranges
	 */
	public static RDFNode getRange(String predicate) {
		RDFNode range = null;
		String query = "SELECT ?range WHERE { "
				+ "<" + predicate +"> rdfs:range ?range."
						+ "}";
		ResultSet rs = ClientManagement.query(query, false);
		if(rs.hasNext()) {
			range = rs.next().get("range");
		}
		return range;
	}
	
	/**
	 * To get the cross path node
	 * @param leftNode
	 * @param rightNode
	 * @return node list
	 */
	public static LinkedList<RDFNode> getCrossNode(String e1, String p1, String e2, String p2) {
		LinkedList<RDFNode> nodeList = new LinkedList<>();
		String query = "SELECT DISTINCT ?node WHERE {"
				+ "{<"+e1+"> <"+p1+"> ?node} UNION {?node <"+p1+"> <"+e1+">} "
				+ "{<"+e2+"> <"+p2+"> ?node} UNION {?node <"+p2+"> <"+e2+">}"
				+ "}";
		ResultSet rs = ClientManagement.query(query, false);
		while(rs.hasNext()) {
			RDFNode node = rs.next().get("node");
			nodeList.add(node);
		}
		return nodeList;
	}
	
	public static LinkedList<RDFNode> getPipeNode(String entity, String firstPred, String secondPred) {
		LinkedList<RDFNode> nodeList = new LinkedList<>();
		String sparql = "SELECT DISTINCT ?o1 WHERE {"
				+ "{<"+entity+"> <"+firstPred+"> ?o. ?o <"+secondPred+"> ?o1.} " //out - out
				+ "UNION {<"+entity+"> <"+firstPred+"> ?o. ?o1 <"+secondPred+"> ?o.} " // out - in
				+ "UNION {?o <"+firstPred+"> <"+entity+">. ?o <"+secondPred+"> ?o1.} " // in - out
				+ "UNION {?o <"+firstPred+"> <"+entity+">. ?o1 <"+secondPred+"> ?o.} " // in - in
				+ "}";
		ResultSet rs = ClientManagement.query(sparql, false);
		while (rs.hasNext()) {
			RDFNode node = rs.next().get("o1");
			nodeList.add(node);
		}
		return nodeList;
	}
	
	public static HashMap<Predicate, HashSet<String>> getPredicatePipe(String entity, ArrayList<Predicate> predList) {
		HashMap<Predicate, HashSet<String>> predMap = new HashMap<>();
		for (Predicate predicate : predList) {
			String predUri = predicate.getUri();
			HashSet<String> predSet = new HashSet<>();
			boolean literalFlag = false; // judge if this predicate 
			
			String firstQuery = "SELECT DISTINCT ?o WHERE { {<"+entity+"> <"+predUri+"> ?o} UNION {?o <"+predUri+"> <"+entity+">}}";
			ResultSet rs = ClientManagement.query(firstQuery, true);
			while (rs.hasNext()) {
				RDFNode node = rs.next().get("o");
				if(node.isLiteral()) { // literal case
					literalFlag = true;
					break;
				} else { // resource case
					LinkedList<RDFNode> secondPredList = getSurroundingPred(node.toString());
					for (RDFNode secondPredicate : secondPredList) {
						if(!secondPredicate.equals(predicate))
							predSet.add(secondPredicate.toString());
					}
				}
			}
			if(literalFlag) {
				continue;
			}
			predMap.put(predicate, predSet);
		}
		return predMap;
	}
	
	public static HashMap<String, HashSet<String>> getPredicateCross(String e1, String e2) {
		HashMap<String, HashSet<String>> predMap = new HashMap<>();
		String sparql = "SELECT DISTINCT ?p1 ?p2 WHERE {"
				+ "{{<"+e1+"> ?p1 ?o} UNION {?o ?p1 <"+e1+">}} "
				+ "{{<"+e2+"> ?p2 ?o} UNION {?o ?p2 <"+e2+">}}"
				+ "}";
		
		ResultSet rs = ClientManagement.query(sparql, false);
		while (rs.hasNext()) {
			QuerySolution qs = rs.next();
			RDFNode p1 = qs.get("p1");
			RDFNode p2 = qs.get("p2");
			if(predMap.containsKey(p1.toString())) {
				predMap.get(p1.toString()).add(p2.toString());
			} else {
				HashSet<String> predSet = new HashSet<>();
				predSet.add(p2.toString());
				predMap.put(p1.toString(), predSet);
			}
		}
		
		return predMap;
	}
	
	public static LinkedList<RDFNode> getPredicateWho(String entityUri) {
		LinkedList<RDFNode> predList = new LinkedList<>();
		
		// Forward & Backward
		String fSPO = "<"+entityUri+">" + " ?p ?o.";
		String bSPO = "?o ?p " + "<"+entityUri+">";
		
		String sparql = "SELECT DISTINCT ?p WHERE { "
				+"{" +fSPO+ "} UNION {" +bSPO+ "}"
						+ "{?o rdf:type dbo:Person} UNION {?o rdf:type dbo:Organisation}."
						+ "}";
		ResultSet rs = ClientManagement.query(sparql, false);
		while(rs.hasNext()) {
			RDFNode p = rs.next().get("p");
			if(!BlackList.predicateSet.contains(p.toString()))
				predList.add(p);
		}
		return predList;
	}
	
	public static LinkedList<RDFNode> getPredicateWhere(String entityUri) {
		LinkedList<RDFNode> predList = new LinkedList<>();
		
		// Forward & Backward
		String fSPO = "<"+entityUri+">" + " ?p ?o.";
		String bSPO = "?o ?p " + "<"+entityUri+">";
		
		String sparql = "SELECT DISTINCT ?p WHERE { "
				+"{" +fSPO+ "} UNION {" +bSPO+ "}"
						+ "?o rdf:type dbo:Place."
						+ "}";
		ResultSet rs = ClientManagement.query(sparql, false);
		while(rs.hasNext()) {
			RDFNode p = rs.next().get("p");
			if(!BlackList.predicateSet.contains(p.toString()))
				predList.add(p);
		}
		return predList;
	}
	
	public static LinkedList<RDFNode> getPredicateDate(String entityUri) {
		LinkedList<RDFNode> predList = new LinkedList<>();
		
		// Forward & Backward
		String fSPO = "<"+entityUri+">" + " ?p ?o.";
		
		String sparql = "SELECT DISTINCT ?p WHERE { "
						+fSPO
						+ "FILTER(isLiteral(?o))"
						+ "}";
		ResultSet rs = ClientManagement.query(sparql, false);
		while(rs.hasNext()) {
			RDFNode p = rs.next().get("p");
			if(!BlackList.predicateSet.contains(p.toString()))
				predList.add(p);
		}
		return predList;
	}
	
	/**
	 * To get predicates with specified type URI
	 * @param entityUri
	 * @param typeUri
	 * @return
	 */
	public static LinkedList<RDFNode> getPredicateType(String entityUri, String typeUri) {
		LinkedList<RDFNode> predList = new LinkedList<>();
		
		// Forward & Backward
		String fSPO = "<"+entityUri+">" + " ?p ?o.";
		String bSPO = "?o ?p " + "<"+entityUri+">";
		
		String sparql = "SELECT DISTINCT ?p WHERE { "
				+"{" +fSPO+ "} UNION {" +bSPO+ "}"
						+ "?o rdf:type <"+typeUri+">."
						+ "}";
		ResultSet rs = ClientManagement.query(sparql, false);
		while(rs.hasNext()) {
			RDFNode p = rs.next().get("p");
			if(!BlackList.predicateSet.contains(p.toString()))
				predList.add(p);
		}
		
		
		return predList;
	}
	
	public static HashSet<String> getResourceType(String entityUri) {
		HashSet<String> typeSet = new HashSet<>();
		String query = "SELECT ?type WHERE {"
				+ "<"+entityUri+"> rdf:type ?type."
				+ "}";
		ResultSet rs = ClientManagement.query(query, false);
		while(rs.hasNext()) {
			RDFNode type = rs.next().get("type");
			typeSet.add(type.toString());
		}
		return typeSet;
	}
	
	public static void main(String[] args) throws Exception {
		/**/
		
		
	}
	
	/**
//	String query = "select distinct ?s where {?s ?p ?o} limit 50";
	String query = "SELECT DISTINCT ?o1 WHERE {<http://dbpedia.org/resource/Beijing> <http://dbpedia.org/ontology/country> ?o. ?o1 <http://dbpedia.org/ontology/stateOfOrigin> ?o}";
//	String query = "SELECT (COUNT(DISTINCT ?p) AS ?count) WHERE {{<http://dbpedia.org/resource/Beijing> <http://dbpedia.org/property/areaCode> ?o. ?o ?p ?o1.} UNION {<http://dbpedia.org/resource/Beijing> <http://dbpedia.org/property/areaCode> ?o. ?o1 ?p ?o.} UNION {?o <http://dbpedia.org/property/areaCode> <http://dbpedia.org/resource/Beijing>. ?o ?p ?o2.} UNION {?o <http://dbpedia.org/property/areaCode> <http://dbpedia.org/resource/Beijing>. ?o2 ?p ?o.} }";//"SELECT DISTINCT ?s WHERE { ?s ?p ?o } LIMIT 5";
	ResultSet rs = ClientManagement.query(query, true);
	System.out.println("OVER");
	while(rs.hasNext()) {
		System.out.println(rs.next().get("o1"));
	}
	/**/
}
