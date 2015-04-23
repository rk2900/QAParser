package phrase;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import umbc.umbcDB;
import knowledgebase.ClientManagement;

/**
 * pattern 
 * 	(the) (official) language of (the) China
 * 	DT		JJ*			NN*	  IN  DT    E
 * @author chzhu
 *
 */
public class EntityPhrase {
	public static double minScore = 0.1;
	
	private String questionText;
	private String DT1;
	private String JJ;
	private LinkedList<String> NNs;
	private String IN;
	private String DT2;
	private String entityUri;
	private ArrayList<PredictDetail> predictDetails;
	
	public String getQuestionText() {
		return questionText;
	}
	public void setQuestionText(String questionText) {
		this.questionText = questionText;
	}
	public String getDT1() {
		return DT1;
	}
	public void setDT1(String dT1) {
		DT1 = dT1;
	}
	public String getJJ() {
		return JJ;
	}
	public void setJJ(String jJ) {
		JJ = jJ;
	}
	public LinkedList<String> getNNs() {
		return NNs;
	}
	public void setNNs(LinkedList<String> nNs) {
		NNs = nNs;
	}
	public String getIN() {
		return IN;
	}
	public void setIN(String iN) {
		IN = iN;
	}
	public String getDT2() {
		return DT2;
	}
	public void setDT2(String dT2) {
		DT2 = dT2;
	}
	public String getEntityUri() {
		return entityUri;
	}
	public void setEntityUri(String entityUri) {
		this.entityUri = entityUri;
	}
	public ArrayList<PredictDetail> getPredictDetails() {
		return predictDetails;
	}
	public void setPredictDetails(ArrayList<PredictDetail> predictDetails) {
		this.predictDetails = predictDetails;
	}
	
	public void setPredictDetails(umbcDB db, LinkedList<String> predictUris){
		this.predictDetails = ranking(db, predictUris);
	}
	
	public void printEntityPhrase(){
//		StringBuilder sb = new StringBuilder();
//		sb.append(questionText);
//		sb.append("\nDT1\t");
//		sb.append(DT1);
//		sb.append("\nJJ\t");
//		sb.append(JJ);
//		sb.append("\nNNs");
//		for(String nn:NNs){
//			sb.append("\t");
//			sb.append(nn);
//		}
//		sb.append("\nIN\t");
//		sb.append(IN);
//		sb.append("\nDT2\t");
//		sb.append(DT2);
//		sb.append("\n");
//		
//		System.out.println(sb.toString());
		System.out.println();
		System.out.println(questionText);
//		System.out.println(predictDetails.toString());
		for (PredictDetail p : predictDetails) {
			StringBuilder sb = new StringBuilder();
			sb.append(p.getScore());
			sb.append("\t");
			sb.append(p.getPredictUri());
			sb.append("\t");
			for (WordDetail w : p.getWordDetails()) {
				sb.append("<");
				sb.append(w.getWordInLabel());
				sb.append(", ");
				sb.append(w.getWordInQuestion());
				sb.append(">");
				sb.append("\t");
				sb.append(w.getScore());
				sb.append("\t");
				sb.append(w.getPosInQuestion());
				sb.append("\t");
			}
			System.out.println(sb.toString());
		}
	}

	/**
	 * calculate the max score of a predict's label and the phrase 
	 * @param db
	 * @param label
	 * @return
	 */
	private ArrayList<WordDetail> ranking(umbcDB db, String label){
		if(label.length() == 0){
			return null;
		}
		
		ArrayList<WordDetail> list = new ArrayList<WordDetail>();
		
		String [] labelWords = label.split(" ");
		for (String labelWord : labelWords) {
			WordDetail wordDetail = new WordDetail();
			wordDetail.setWordInLabel(labelWord);
			double wordScore = 0;
			if(JJ != null && JJ.length() > 0){
				wordScore = db.getScore(labelWord, JJ);
				wordDetail.setScore(wordScore);
				wordDetail.setWordInQuestion(JJ);
				wordDetail.setPosInQuestion("JJ");
			}
			for (String nn : NNs) {
				double tmpScore = db.getScore(labelWord, nn);
				if(tmpScore > wordScore){
					wordScore = tmpScore;
					wordDetail.setScore(wordScore);
					wordDetail.setWordInQuestion(nn);
					wordDetail.setPosInQuestion("NN");
				}
			}
			list.add(wordDetail);
		}
		return list;
	}
	
	public ArrayList<PredictDetail> ranking(umbcDB db, LinkedList<String> predictUris){
		ArrayList<PredictDetail> list = new ArrayList<PredictDetail>();
		
		for(String predictUri:predictUris){
			PredictDetail predictDetail = new PredictDetail();
			predictDetail.setPredictUri(predictUri);
			
			LinkedList<String> labels = ClientManagement.getLabel(predictUri);
			double maxScore = 0;
			for (String label : labels) {
				ArrayList<WordDetail> wordDetails = ranking(db, label);
				double score = 0;
				for (WordDetail wordDetail : wordDetails) {
					score += wordDetail.getScore();
				}
				score = score/wordDetails.size();
				if(score > maxScore){
					maxScore = score;
					predictDetail.setLabel(label);
					predictDetail.setScore(score);
					predictDetail.setWordDetails(wordDetails);
				}
			}
			if(maxScore > minScore){
				list.add(predictDetail);
			}
		}
		Collections.sort(list);
		return list;
	}
}
