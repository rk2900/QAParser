package paser;

import java.util.ArrayList;
import java.util.HashSet;
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
	
	//TODO 
	public LinkedList<String> getSurroundingLabels(RDFNode resource) {
		LinkedList<String> labels = new LinkedList<>();
		return null;
	}
	
	public static void main(String[] args) {
		Pipeline pipeline = new Pipeline();
		Main.setEntity(pipeline);
		LinkedList<QuestionFrame> resourceQuestions = pipeline.resource;
		for (QuestionFrame questionFrame : resourceQuestions) {
			System.out.println(questionFrame.id+"\t"+questionFrame.question);
			Focus focus = questionFrame.focus;
			if(!focus.isEmpty()) {
				String focusString = focus.getFocusContent(questionFrame.wordList);
				System.out.println("Focus: "+"\t"+focusString);
				String typeUri = Type.getTypeFromFocus(focusString);
				System.out.println("Type: "+"\t"+typeUri);
				ArrayList<String> answers = questionFrame.answers;
				HashSet<String> typeSet = new HashSet<>();
				for (String answer : answers) {
					System.err.println(answer);
					if(!typeSet.addAll(ClientManagement.getResourceType(answer)))
						System.err.println("ADD ERROR");
				}
				System.out.println(typeSet);
			}
			System.out.println();
		}
		
	}

}
