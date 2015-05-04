package baseline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import knowledgebase.ClientManagement;

import com.hp.hpl.jena.rdf.model.RDFNode;

import umbc.umbcDB;

public class SimilarityFunction {
	private static umbcDB db = new umbcDB();
	private static int predictNum = 5;
	private static double minSimilarityScore = 0.1;
	
	/**
	 * 直接对空格切分
	 * @param predictLabel
	 * @return
	 */
	private static LinkedList<String> getLabelWords(String predictLabel){
		LinkedList<String> wordList = new LinkedList<String>();
		String [] words = predictLabel.split(" ");
		for (String word : words) {
			wordList.add(word);
		}
		return wordList;
	}
	
	/**
	 * 空格切分后转化大小写
	 * @param predictLabel
	 * @return
	 */
//	private static LinkedList<String> getLabelWords(String predictLabel){
//		LinkedList<String> wordList = new LinkedList<String>();
//		return wordList;
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
	
	private static ArrayList<Predicate> getSortedPredicts(MatchDetail detail){
		String entityUri = detail.entity.uri;
		String NL = detail.constraint.edge;
		String focusString = detail.focusString;
		
		ArrayList<Predicate> predictList = new ArrayList<Predicate>();
		LinkedList<RDFNode> predictUriList = ClientManagement.getSurroundingPred(entityUri);
		
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
		ArrayList<Predicate> predicts = getSortedPredicts(detail);
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
