package paser;

import java.util.ArrayList;
import java.util.LinkedList;

import type.Type;
import knowledgebase.ClientManagement;

import com.hp.hpl.jena.rdf.model.RDFNode;

import baseline.Answer;
import baseline.Main;
import baseline.Predicate;
import finder.Pipeline;

public class FocusConstraint {

	public boolean ifTypeMatched(String entityUri, String typeUri) {
		String askQuery = "ASK WHERE { <"+entityUri+"> rdf:type <"+typeUri+"> }";
		boolean flag = ClientManagement.ask(askQuery, true);
		return flag;
	}
	
	public boolean judgeTypeConstraint(RDFNode resource, QuestionFrame qf) {
		return false;
	}
	
	public static LinkedList<RDFNode> getPredicateType(String focus) {
		LinkedList<RDFNode> predList = new LinkedList<>();
		String typeUri = Type.getType(focus);
		
		
		return predList;
	}

	
	//TODO 
	public LinkedList<String> getSurroundingLabels(RDFNode resource) {
		LinkedList<String> labels = new LinkedList<>();
		return null;
	}
	
	public static void main(String[] args) {
		Pipeline pipeline = new Pipeline();
		Main.setEntity(pipeline);
		Answer answer = Main.getAnswer(pipeline, 2);
		ArrayList<Predicate> predicates = answer.predictList;
		
		QuestionFrame qf = pipeline.xmlParser.getQuestionFrameWithId(271);
		
		FocusConstraint constraint = new FocusConstraint();
		
//		String focus = qf.focus.getFocusContent(qf.wordList);
//		String typeUri = Type.getTypeFromFocus(focus);
//		
//		System.out.print(focus);
//		System.out.println(typeUri);
//		
//		String entityUri = "http://dbpedia.org/resource/China";
//		System.out.println(constraint.ifTypeMatched(entityUri, typeUri));
		
		System.out.println(qf.question);
		System.out.println(qf.posList);
		
	}

}
