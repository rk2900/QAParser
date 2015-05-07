package paser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import type.Type;
import knowledgebase.ClientManagement;

import com.hp.hpl.jena.rdf.model.RDFNode;

import baseline.Answer;
import baseline.Entity;
import baseline.Main;
import baseline.PairPredicate;
import baseline.Predicate;
import finder.Pipeline;

public class FocusConstraint {

	public static final double literalScore = 0.0;
	public static final double lowScore = 0.5;
	
//	public boolean ifTypeMatched(String entityUri, String typeUri) {
//		String askQuery = "ASK WHERE { <"+entityUri+"> rdf:type <"+typeUri+"> }";
//		boolean flag = ClientManagement.ask(askQuery, false);
//		return flag;
//	}
	
	public static boolean isEntityLinked(String leftNodeUri, String rightNodeUri) {
		String askQuery = "ASK WHERE {"
				+ "{<"+leftNodeUri+"> ?p <"+rightNodeUri+">} UNION { <"+rightNodeUri+"> ?p <"+leftNodeUri+"> }"
				+ "}";
		boolean flag = ClientManagement.ask(askQuery, false);
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
				predTypeScores = setPredicateConsScore(answer.predictList, predTypeScores, lowScore);
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
		} else { // 没有focus抽取结果的，predicate类型限制评分均为lowScore
			System.err.println("Focus is empty");
			predTypeScores = setPredicateConsScore(answer.predictList, predTypeScores, lowScore);
		}
		answer.typeConstrainScore = predTypeScores;
		return predTypeScores;
	}
	
	public static HashMap<PairPredicate, Double> getPairPredicateTypeConstraintScore(Answer answer) {
		HashMap<PairPredicate, Double> pairPredicateTypeConstraintScore = new HashMap<>();
		QuestionFrame qf = answer.qf;
		Focus focus = qf.focus;
		if(!focus.isEmpty()) {
			ArrayList<String> extractedTypeList = (ArrayList<String>) Type.getTypeFromFocus(focus.getFocusContent(qf.wordList));
			if(extractedTypeList.isEmpty()) { // 如果focus中 type抽取结果为空， 则所有predicate类型限制评估分数均为 lowScore
				for (PairPredicate pairPredicate : answer.pairPredicates) {
					pairPredicateTypeConstraintScore.put(pairPredicate, lowScore);
				}
			} else { // 如果focus中 type抽取结果 不为空，则针对每个predicate进行评分
				LinkedList<PairPredicate> pairPredicates = answer.pairPredicates;
				for(PairPredicate pairPredicate: pairPredicates) {
					double score = 0.0;
					LinkedList<RDFNode> nodeList = answer.pairResources.get(pairPredicate);
					HashSet<String> pairPredAnswerTypeSet = new HashSet<>();
					for(RDFNode rdfNode: nodeList) {
						if (rdfNode.isLiteral()) {
							score = literalScore;
							break;
						} else {
							pairPredAnswerTypeSet.addAll(ClientManagement.getResourceType(rdfNode.toString()));
						}
					}
					if(pairPredAnswerTypeSet.size() > 0) {
						for(String extracedType: extractedTypeList) {
							if(pairPredAnswerTypeSet.contains(extracedType)) {
								score = 1.0;
								break;
							}
						}
					}
					pairPredicateTypeConstraintScore.put(pairPredicate, score);
				}
			}
		} else { // 没有focus抽取结果的，predicate类型限制评分均为lowScore
			System.err.println("Focus is empty");
			for (PairPredicate pairPredicate : answer.pairPredicates) {
				pairPredicateTypeConstraintScore.put(pairPredicate, lowScore);
			}
		}
		
		answer.pairTypeConstraintScore = pairPredicateTypeConstraintScore;
		return pairPredicateTypeConstraintScore;
	}
	
	/**
	 * To get the confidence of how much predicate is matched along with the entity constraint in Focus
	 * @param answer
	 * @return
	 */
	public static HashMap<Predicate, Double> getPredicateEntityConstraintScore(Answer answer) {
		HashMap<Predicate, Double> predEntityScores = new HashMap<>();
		QuestionFrame qf = answer.qf;
		Focus focus = qf.focus;
		
		if(!focus.hasEntity(qf.entityList)) { // focus中没有entity，直接返回lowScore
			predEntityScores = setPredicateConsScore(answer.predictList, predEntityScores, lowScore);
		} else { // focus中有entity
			Entity entity = focus.getEntityPosition(qf.entityList);
			String entityUri = entity.getUri();
			for (Predicate predicate : answer.predictList) {
				LinkedList<RDFNode> nodeList = answer.resources.get(predicate);
				if(nodeList.size() == 0) { // 该条predicate没有找到宾语
					predEntityScores.put(predicate, 0.0);
					break;
				}
				
				if(nodeList.get(0).isLiteral()) { // predicate连接的是Literal类型
					predEntityScores.put(predicate, literalScore);
					break;
				}
				int linkedCount = 0;
				for (RDFNode rdfNode : nodeList) {
					if(isEntityLinked(rdfNode.toString(), entityUri)) {
						linkedCount++;
					}
				}
				predEntityScores.put(predicate, (double)linkedCount/nodeList.size()); // predicate真实分数为连接到resources中和focus中entity有关系的比例值
			}
		}
		
		answer.entityConstrainScore = predEntityScores;
		return null;
	}
	
	/**
	 * To set type ranking score of each predicate as lowScore (0.0) 
	 * @param predicates
	 * @param predTypeScores
	 * @return
	 */
	public static HashMap<Predicate, Double> setPredicateConsScore(ArrayList<Predicate> predicates, HashMap<Predicate, Double> predScores, double score) {
		for (Predicate predicate : predicates) {
			predScores.put(predicate, score);
		}
		return predScores;
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
