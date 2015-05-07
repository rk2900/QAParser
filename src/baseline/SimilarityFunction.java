package baseline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import knowledgebase.ClientManagement;
import baseline.Classification.CLASSIFICATION;

import com.hp.hpl.jena.rdf.model.RDFNode;

import umbc.umbcDB;

public class SimilarityFunction {
	private static umbcDB db = new umbcDB();
	public static int predictNum = 5;
	public static double minSimilarityScore = 0.1;
	/**
	 * threshold 表示 当谓语的结果即使不满足type类型的，也要删除
	 */
	public static double threshold = 0.9;
	
	/**
	 * 直接对空格切分
	 * @param predictLabel
	 * @return
	 */
//	private static LinkedList<String> getLabelWords(String predictLabel){
//		LinkedList<String> wordList = new LinkedList<String>();
//		String [] words = predictLabel.split(" ");
//		for (String word : words) {
//			wordList.add(word);
//		}
//		return wordList;
//	}
	
	/**
	 * 空格切分后转化大小写
	 * @param predictLabel
	 * @return
	 */
	private static LinkedList<String> getLabelWords(String predictLabel){
		LinkedList<String> wordList = new LinkedList<String>();
		if(predictLabel.contains(" ")){
			String [] words = predictLabel.split(" ");
			for (String word : words) {
				wordList.add(word);
			}
		}else{
			boolean isLetter = true;
			for(int i=0; i<predictLabel.length(); ++i){
				if(!Character.isLetter(predictLabel.charAt(i))){
					isLetter = false;
					break;
				}
			}
			if(isLetter){
				if(predictLabel.length()>1 && Character.isUpperCase(predictLabel.charAt(0)) && Character.isLowerCase(predictLabel.charAt(1))){
					int start = 0;
					for(int i=1; i<predictLabel.length(); ++i){
						if(Character.isUpperCase(predictLabel.charAt(i))){
							wordList.add(predictLabel.substring(start, i));
							start = i;
						}

						if(i == predictLabel.length()-1){
							wordList.add(predictLabel.substring(start, i+1));
						}
					}
				}else{
					wordList.add(predictLabel);
				}
			}else{
				wordList.add(predictLabel);
			}
			
		}
		return wordList;
	}
	
	public static void main(String [] args){
//		System.out.println(getLabelWords("AdmittanceDate"));
		System.out.println(umbcWordRanking( "person that first ascented a mountain", "climb", ""));
	}
	
//	private static double umbcWordRanking(String predictLabel, String NL, String focusString){
//		double avgScore = 0;
//		
//		LinkedList<String> labelWords = getLabelWords(predictLabel);
////		System.out.println(labelWords);
//		LinkedList<String> resWords = new LinkedList<String>();
//		
//		String [] wordsNL = NL.split(" ");
//		for (String word : wordsNL) {
//			if(word.length() > 0){
//				resWords.add(word);
//			}
//		}
//		
//		if(focusString.length() > 0){
//			String [] focStrings = focusString.split(" ");
//			for (String word : focStrings) {
//				if(word.length() > 0){
//					resWords.add(word);
//				}
//			}
//		}
//		
//		for (String labelWord : resWords) {
//			if(labelWord.length() == 0){
//				continue;
//			}
//			double maxScore = 0;
//			double score;
//			for (String res : labelWords) {
//				score = db.getScore(labelWord, res);
//				if(score == -1){
//					System.err.println("DB Not included: "+labelWord+" "+res);
//					db.insertWords(labelWord, res);
//				}
//				if(maxScore < score){
//					maxScore = score;
//				}
//			}
//			avgScore += maxScore;
//		}
//		
//		avgScore /= (resWords.size()+1);
//		return avgScore;
//	}
	private static double umbcWordRanking(String predictLabel, String NL, String focusString){
		double avgScore = 0;
		
		LinkedList<String> labelWords = getLabelWords(predictLabel);
		LinkedList<String> resWords = new LinkedList<String>();
		
		String [] wordsNL = NL.split(" ");
		for (String word : wordsNL) {
			if(word.length() > 0){
				resWords.add(word);
			}
		}
		
		if(focusString.length() > 0){
			String [] focStrings = focusString.split(" ");
			for (String word : focStrings) {
				if(word.length() > 0){
					resWords.add(word);
				}
			}
		}
		
		for (String labelWord : labelWords) {
			if(labelWord.length() == 0){
				continue;
			}
			double maxScore = 0;
			double score;
			for (String res : resWords) {
				score = db.getScore(labelWord, res);
				if(score == -1){
					System.err.println(predictLabel+"\t"+NL+"\t"+focusString);
					System.err.println("DB Not included: "+labelWord+" "+res);
					db.insertWords(labelWord, res);
				}
				if(maxScore < score){
					maxScore = score;
				}
			}
			avgScore += maxScore;
		}
		
		avgScore /= labelWords.size();
		return avgScore;
	}
	
	public static Predicate getPredicate(String predict,String NL, String focusString){
		LinkedList<String> predictlabels = ClientManagement.getLabel(predict);
		double maxScore = 0;
		Predicate p = new Predicate();
		p.setMaxScore(0);
		p.setUri(predict);
		p.setMatchedLabel("");
		String matchedLabel = null;
		for (String predictlabel : predictlabels) {
			if (predictlabel.length() == 0) {
				continue;
			}
			double score = SimilarityFunction.umbcWordRanking(predictlabel, NL, focusString);
			if(score > maxScore){
				maxScore = score;
				matchedLabel = predictlabel;
			}
		}
		
		if(maxScore > 0){
			p.setMaxScore(maxScore);
			p.setMatchedLabel(matchedLabel);
		}
		return p;
	}
	
	private static ArrayList<Predicate> getSortedPredicts(MatchDetail detail,CLASSIFICATION type){
		String entityUri = detail.entity.uri;
		String NL = detail.constraint.edge;
		String focusString = detail.focusString;
		
		ArrayList<Predicate> predictList = new ArrayList<Predicate>();
		LinkedList<RDFNode> predictUriList;
		
		switch (type) {
		case DATE:
			predictUriList = ClientManagement.getPredicateDate(entityUri);
			break;
		case WHERE:
			predictUriList = ClientManagement.getPredicateWhere(entityUri);
			break;
		case WHO:
			predictUriList = ClientManagement.getPredicateWho(entityUri);
			break;
//		case 5:
//			if(focusString.length() > 0) {
//				String typeUri = Type.getType(focusString);
//				predictUriList = ClientManagement.getPredicateType(entityUri, typeUri);
//			}else{
//				predictUriList = ClientManagement.getSurroundingPred(entityUri);
//			}
//			break;
		default:
			predictUriList = ClientManagement.getSurroundingPred(entityUri);
			break;
		}
		
		for (RDFNode predictUri : predictUriList) {
			
			LinkedList<String> predictlabels = ClientManagement.getLabel(predictUri.toString());
			
			double maxScore = 0;
			String matchedLabel = null;
			for (String predictlabel : predictlabels) {
				if (predictlabel.length() == 0) {
					continue;
				}
				double score = SimilarityFunction.umbcWordRanking(predictlabel, NL, focusString);
				if(score > maxScore){
					maxScore = score;
					matchedLabel = predictlabel;
				}
			}
			
			if(maxScore > 0){
				Predicate predict = new Predicate();
				predict.setUri(predictUri.toString());
				predict.setMaxScore(maxScore);
				predict.setMatchedLabel(matchedLabel);
				predictList.add(predict);
			}
			
		}
		Collections.sort(predictList,Collections.reverseOrder());
		return predictList;
	}
	
	public static ArrayList<Predicate> getTopNPredicts(MatchDetail detail){
		return getTopNPredicts(detail, CLASSIFICATION.NORMAL);
	}
	
	public static ArrayList<Predicate> getTopNPredicts(MatchDetail detail, CLASSIFICATION type){
		ArrayList<Predicate> predicts = getSortedPredicts(detail,type);
		ArrayList<Predicate> result = new ArrayList<Predicate>();
		for(int i=0; i<predictNum && i<predicts.size(); ++i){
			if(predicts.get(i).maxScore > minSimilarityScore){
				result.add(predicts.get(i));
			}else{
				break;
			}
		}
		return result;
	}
}
