package baseline;

public class Predicate implements Comparable<Predicate>{
	String uri;
	double maxScore;
	String matchedLabel;
	
	public Predicate(){
		
	}
	public Predicate(String uri, double maxScore, String matchedLabel) {
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
	 * 然后按照ontology的优先
	 * 对于score相同的，取label较长的
	 */
	public int compareTo(Predicate o) {
		// TODO Auto-generated method stub
		if(maxScore > o.maxScore){
			return 1;
		}
		if(maxScore < o.maxScore){
			return -1;
		}
		if(uri.startsWith("http://dbpedia.org/ontology/") && !o.uri.startsWith("http://dbpedia.org/ontology")){
			return 1;
		}
		if(!uri.startsWith("http://dbpedia.org/ontology/") && o.uri.startsWith("http://dbpedia.org/ontology")){
			return -1;
		}
		return matchedLabel.length()-o.matchedLabel.length();
	}
	
//	public boolean equals(Object o){ 
//		if(!(o instanceof Predicate)) {
//			return false;
//		}
//		Predicate p = (Predicate)o;
//		return p.uri.equals(uri);
//	} 

	
}
