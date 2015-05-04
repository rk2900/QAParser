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
	
	private static LinkedList<String> getLabelWords(String predictLabel){
		LinkedList<String> wordList = new LinkedList<String>();
		String [] words = predictLabel.split(" ");
		for (String word : words) {
			wordList.add(word);
		}
		return wordList;
	}
	
	public static double umbcWordRanking(String predictLabel, String NL){
		double avgScore = 0;
		String [] wordsNL = NL.split(" ");
		LinkedList<String> labelWords = getLabelWords(predictLabel);
		
		for (String labelWord : labelWords) {
			if(labelWord.length() == 0){
				continue;
			}
			double maxScore = 0;
			double score;
			for (String wordNL : wordsNL) {
				if(wordNL.length() == 0){
					continue;
				}
				score = db.getScore(labelWord, wordNL);
				if(score == -1){
					System.err.println("DB Not included: "+labelWord+" "+wordNL);
					db.insertWords(labelWord, wordNL);
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
	
	public static ArrayList<Predict> getSortedPredicts(MatchDetail detail){
		String entityUri = detail.entity.uri;
		String NL = detail.constraint.edge;
		
		ArrayList<Predict> predictList = new ArrayList<Predict>();
		LinkedList<RDFNode> predictUriList = ClientManagement.getSurroundingPred(entityUri);
		
		for (RDFNode predictUri : predictUriList) {
			
			LinkedList<String> predictlabels = ClientManagement.getLabel(predictUri.toString());
			
			double maxScore = 0;
			String matchedLabel = null;
			for (String predictlabel : predictlabels) {
				if (predictlabel.length() == 0) {
					continue;
				}
				double score = SimilarityFunction.umbcWordRanking(predictlabel, NL);
				if(score > maxScore){
					maxScore = score;
					matchedLabel = predictlabel;
				}
			}
			
			if(maxScore > 0){
				Predict predict = new Predict();
				predict.setUri(predictUri.toString());
				predict.setMaxScore(maxScore);
				predict.setMatchedLabel(matchedLabel);
				predictList.add(predict);
			}
			
		}
		Collections.sort(predictList,Collections.reverseOrder());
		return predictList;
	}
	
	public static ArrayList<Predict> getTopNPredicts(MatchDetail detail){
		ArrayList<Predict> predicts = getSortedPredicts(detail);
		ArrayList<Predict> result = new ArrayList<Predict>();
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
