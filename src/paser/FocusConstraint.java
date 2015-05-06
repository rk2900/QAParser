package paser;

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
import finder.Pipeline;

public class FocusConstraint {

	public static final double literalScore = 0.0;
	public static final double lowScore = 0.5;
	
	public boolean ifTypeMatched(String entityUri, String typeUri) {
		String askQuery = "ASK WHERE { <"+entityUri+"> rdf:type <"+typeUri+"> }";
		boolean flag = ClientManagement.ask(askQuery, true);
		return flag;
	}
	
	/**
	 * To get the score of each predicate with type information in focus
	 * @param answer each score of specified predicate has been stored in HashMap<Predicate, Double>
	 * @return
	 */
	public static HashMap<Predicate, Double> getPredicateTypeConstraintScore(Answer answer) {
		HashMap<Predicate, Double> predTypeScores = new HashMap<>();
		QuestionFrame qf = answer.qf;
		Focus focus = qf.focus;
		if(!focus.isEmpty()) { // 有focus结果
			ArrayList<String> extractedTypeList = (ArrayList<String>) Type.getTypeFromFocus(focus.getFocusContent(qf.wordList));
			if(extractedTypeList.isEmpty()) { // 如果focus中 type抽取结果为空， 则所有predicate类型限制评估分数均为 lowScore
				predTypeScores = setPredicateLowScore(answer.predictList, predTypeScores);
			} else { // 如果focus中 type抽取结果 不为空，则针对每个predicate进行评分
				ArrayList<Predicate> predictList = answer.predictList;
				for (Predicate predicate : predictList) {
					double score = 0.0;
					LinkedList<RDFNode> nodeList = answer.resources.get(predicate);//ClientManagement.getNode(answer.entityUri, predicate.getUri());
					HashSet<String> predAnswerTypeSet = new HashSet<>();
					for (RDFNode rdfNode : nodeList) {
						if(rdfNode.isLiteral()) { // 宾语类型为 Literal的谓语predicate，predicate类型限制评估分数均为0.0
							score = literalScore; 
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
			predTypeScores = setPredicateLowScore(answer.predictList, predTypeScores);
		}
		answer.typeConstrainScore = predTypeScores;
		return predTypeScores;
	}
	
	/**
	 * To set type ranking score of each predicate as lowScore (0.0) 
	 * @param predicates
	 * @param predTypeScores
	 * @return
	 */
	public static HashMap<Predicate, Double> setPredicateLowScore(ArrayList<Predicate> predicates, HashMap<Predicate, Double> predTypeScores) {
		for (Predicate predicate : predicates) {
			predTypeScores.put(predicate, lowScore);
		}
		return predTypeScores;
	}
	
	public static void main(String[] args) {
		Pipeline pipeline = new Pipeline();
		Main.setEntity(pipeline);
		QuestionFrame qf = pipeline.xmlParser.getQuestionFrameWithId(121);
		String focusString = qf.focus.getFocusContent(qf.wordList);
		System.out.println(focusString);
		System.out.println(Type.getTypeFromFocus(focusString));
	}

}
