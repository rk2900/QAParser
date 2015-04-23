package phrase;

import java.util.ArrayList;
import java.util.Comparator;

public class PredictDetail implements Comparator<PredictDetail>, Comparable<PredictDetail>{
	private String predictUri;
	private String label;
	private double score;
	private ArrayList<WordDetail> wordDetails;
	
	public String getPredictUri() {
		return predictUri;
	}
	public void setPredictUri(String predictUri) {
		this.predictUri = predictUri;
	}
	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	public ArrayList<WordDetail> getWordDetails() {
		return wordDetails;
	}
	public void setWordDetails(ArrayList<WordDetail> wordDetails) {
		this.wordDetails = wordDetails;
	}
	
	public int compareTo(PredictDetail p){
		if(this.score > p.score){
			return -1;
		}
		if(this.score < p.score){
			return 1;
		}
		return 0;
	}
	
	public int compare(PredictDetail p1, PredictDetail p2){
		if(p1.score > p2.score){
			return -1;
		}
		if(p1.score < p2.score){
			return 1;
		}
		return 0;
	}
	
	public void print(){
		
	}
}
