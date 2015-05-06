package paser;

import java.awt.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import tool.OutputRedirector;
import type.Type;
import knowledgebase.ClientManagement;

import com.hp.hpl.jena.rdf.model.RDFNode;

import baseline.Answer;
import baseline.Main;
import baseline.Predicate;
import edu.stanford.nlp.stats.PrecisionRecallStats;
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
	
	public static HashMap<Predicate, Double> getPredicateTypeConstrainScore(Answer answer) {
		HashMap<Predicate, Double> predTypeScores = new HashMap<>();
		QuestionFrame qf = answer.qf;
		Focus focus = qf.focus;
		if(!focus.isEmpty()) { // 有focus结果
			
			ArrayList<String> extractedTypeList = (ArrayList<String>) Type.getTypeFromFocus(focus.getFocusContent(qf.wordList));
			if(extractedTypeList.isEmpty()) { // 如果focus中 type抽取结果为空， 则所有predicate类型限制评估分数均为0.0
				predTypeScores = setPredicateScoreZero(answer.predictList, predTypeScores);
			} else { // 如果focus中 type抽取结果 不为空，则针对每个predicate进行评分
				ArrayList<Predicate> predictList = answer.predictList;
				for (Predicate predicate : predictList) {
					double score = 0.0;
					LinkedList<RDFNode> nodeList = ClientManagement.getNode(answer.entityUri, predicate.getUri());
					HashSet<String> predAnswerTypeSet = new HashSet<>();
					for (RDFNode rdfNode : nodeList) {
						if(rdfNode.isLiteral()) { // 宾语类型为 Literal的谓语predicate，predicate类型限制评估分数均为0.0
							score = 0.0; 
							break;
						} else {
							predAnswerTypeSet.addAll(ClientManagement.getResourceType(rdfNode.toString()));
						}
					}
					if(predAnswerTypeSet.size() > 0) {
						for (String extracedType : extractedTypeList) {
							if(predAnswerTypeSet.contains(extracedType)) {
								score = 1.0;
								break;
							}
						}
					}
					predTypeScores.put(predicate, score);
				}
			}
		} else { // 没有focus抽取结果的，predicate类型限制评分均为0.0
			System.err.println("Focus is empty");
			predTypeScores = setPredicateScoreZero(answer.predictList, predTypeScores);
		}
		answer.typeConstrainScore = predTypeScores;
		return predTypeScores;
	}
	
	public static HashMap<Predicate, Double> setPredicateScoreZero(ArrayList<Predicate> predicates, HashMap<Predicate, Double> predTypeScores) {
		for (Predicate predicate : predicates) {
			predTypeScores.put(predicate, 0.0);
		}
		return predTypeScores;
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
