package paser;

import java.awt.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

import tool.OutputRedirector;
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
		OutputRedirector.openFileOutput("./data/type_of_answer_and extracted_types.txt");
		for (QuestionFrame questionFrame : resourceQuestions) {
			System.out.println(questionFrame.id+"\t"+questionFrame.question);
			Focus focus = questionFrame.focus;
			if(!focus.isEmpty() && questionFrame.answerType.equalsIgnoreCase("resource")) {
				String focusString = focus.getFocusContent(questionFrame.wordList);
				System.out.println("Focus: "+"\t"+focusString);
				ArrayList<String> typeUriList = (ArrayList<String>)Type.getTypeFromFocus(focusString);
				System.out.println("Type: "+"\t"+typeUriList);
				ArrayList<String> answers = questionFrame.answers;
				HashSet<String> typeSet = new HashSet<>();
				for (String string : answers) {
					typeSet.addAll(ClientManagement.getResourceType(string));
				}
				Boolean containsFlag = false;
				for (String string : typeUriList) {
					if(typeSet.contains(string)) {
						containsFlag = true;
						break;
					}
				}
				System.out.println("Contains? "+"\t"+containsFlag);
				System.out.println(typeSet);
			} else {
				System.out.println("Answer type is not a resource.");
			}
			System.out.println();
		}
		OutputRedirector.closeFileOutput();
	}

}
