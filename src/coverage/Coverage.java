package coverage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import knowledgebase.ClientManagement;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

import paser.Question;
import tool.OutputRedirector;
import baseline.Main;
import edu.mit.jwi.item.IVerbFrame;
import finder.Pipeline;

public class Coverage {

	static {
		try {
			ClientManagement.getAgModel();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Pipeline pipeline;
	
	public Coverage() {
		Initialize();
	}
	
	private void Initialize() {
		pipeline = new Pipeline();
		Main.setEntity(pipeline);
	}
	
	public Pipeline getPipeline() {
		return pipeline;
	}
	
	public void assessment(int questionId) {
		Question q = pipeline.xmlParser.getQuestionWithId(questionId);
		assessment(q);
	}
	
	public void assessment(Question q) {
		String textQuestion = new String(q.question);
		ArrayList<String> answerList = q.answers;
		
		// judge if it is a question asking for resource
		if(!q.answerType.equalsIgnoreCase("resource")) {
			System.out.println("Question "+q.id+" is not a resource question.");
			return;
		}
		
		System.out.println("Question:\t"+textQuestion);
		System.out.println("Answers:\t"+answerList);
		
		// judge if it has no answers in knowledge base
		if(answerList.size() == 0) {
			System.out.println("Question "+q.id+" has no answer in knowledge base.");
			return;
		}
		
		// for each ANSWER entity
		String ansUri = answerList.get(0);
		SubGraph graph = new SubGraph(q, ansUri);
		graph.expand(false);
		
		// assessment
		surEntityCoverage(graph, true);
	}

	private double surEntityCoverage(SubGraph graph, boolean visible) {
		double coverage = 0.0;
		HashMap<String, LinkedList<String>> invertedIndex = new HashMap<>();
		
		// get labels and construct inverted index
		for (String rscUri : graph.surResourceSet) {
			String sparql = "SELECT ?label WHERE {"
					+ "<"+rscUri+"> rdfs:label ?label."
					+ "FILTER(lang(?label) = \"en\")"
					+ "}";
			ResultSet rs = ClientManagement.query(sparql, false);
			while(rs.hasNext()) {
				String label = rs.next().get("label").asLiteral().getLexicalForm();
				if(invertedIndex.containsKey(label)) {
					invertedIndex.get(label).add(rscUri);
				} else {
					LinkedList<String> rscList = new LinkedList<>();
					rscList.add(rscUri);
					invertedIndex.put(label, rscList);
				}
			}
		}
		
		if(visible) {
			for (String label: invertedIndex.keySet()) {
				System.out.println(label);
				for (String rsc : invertedIndex.get(label)) {
					System.out.println("\t"+rsc);
				}
			}
		}
		
		return coverage;
	}

	public static void main(String[] args) {
		// initialization
		OutputRedirector.openFileOutput("./data/coverage/invertedIndex.txt");
		Coverage cov = new Coverage();
		cov.assessment(21);
		OutputRedirector.closeFileOutput();
	}

}
