package baseline;

public class Predict implements Comparable<Predict>{
	String uri;
	double maxScore;
	String matchedLabel;
	
	public Predict(){
		
	}
	public Predict(String uri, double maxScore, String matchedLabel) {
		super();
		this.uri = uri;
		this.maxScore = maxScore;
		this.matchedLabel = matchedLabel;
	}
	
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public double getMaxScore() {
		return maxScore;
	}
	public void setMaxScore(double maxScore) {
		this.maxScore = maxScore;
	}
	public String getMatchedLabel() {
		return matchedLabel;
	}
	public void setMatchedLabel(String matchedLabel) {
		this.matchedLabel = matchedLabel;
	}
	@Override
	/**
	 * 首先按照score排名，
	 * 对于score相同的，取label较长的
	 */
	public int compareTo(Predict o) {
		// TODO Auto-generated method stub
		if(maxScore > o.maxScore){
			return 1;
		}else{
			if(maxScore < o.maxScore){
				return -1;
			}else{
				return matchedLabel.length()-o.matchedLabel.length();
			}
		}
	}
	
	
}
