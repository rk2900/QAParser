package type;

import java.util.HashMap;
import java.util.LinkedList;

import knowledgebase.ClientManagement;

import com.hp.hpl.jena.rdf.model.RDFNode;

import baseline.Answer;
import baseline.Main;
import baseline.Predicate;
import paser.QuestionFrame;
import tool.OutputRedirector;
import finder.Pipeline;

public class TypeConstraint {

	public static Pipeline pipeline;
	
	static {
		pipeline = new Pipeline();
		Main.setEntity(pipeline);
	}
	
	public static void main(String[] args) {
		OutputRedirector.openFileOutput("./data/temp.txt");
		System.out.println("===================================================");
		for(int id=1; id<=20; id++) {
			Answer stepAnswer = Main.getAnswer(pipeline, id);
			QuestionFrame qf = stepAnswer.qf;
			
			if(stepAnswer.isException()) {
				System.out.println(qf.id+"\t"+qf.question);
				System.out.println(stepAnswer.exceptionString);
				continue;
			}
			
//			//rank resources in answer structure
//			HashMap<Predicate, LinkedList<RDFNode>> answerMap = new HashMap<>();
//			for (Predicate predicate : stepAnswer.predictList) {
//				System.out.println("\t"+predicate.getUri());
//				LinkedList<RDFNode> nodes = ClientManagement.getNode(stepAnswer.entityUri, predicate.getUri());
//				for (RDFNode rdfNode : nodes) {
//					System.out.println("\t\t"+rdfNode.toString());
//				}
//				answerMap.put(predicate, nodes);
//			}
		}
		OutputRedirector.closeFileOutput();
	}

}
