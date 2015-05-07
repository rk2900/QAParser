package baseline;

public class PairPredicate implements Comparable<PairPredicate> {
	public Predicate Predicate1;
	public Predicate Predicate2;
	public double score;
	
	public PairPredicate(){
		
	}
	
	public PairPredicate(Predicate p1, Predicate p2){
		Predicate1 = p1;
		Predicate2 = p2;
		score = calScore();
	}
	
	private double calScore(){
		return Predicate1.maxScore * Predicate2.maxScore;
	}

	@Override
	public int compareTo(PairPredicate o) {
		// TODO Auto-generated method stub
		if(score > o.score){
			return 1;
		}
		if(score < o.score){
			return -1;
		}
		
		if(Predicate1.uri.startsWith("http://dbpedia.org/ontology/") && !o.Predicate1.uri.startsWith("http://dbpedia.org/ontology")){
			return 1;
		}
		
		if(!Predicate1.uri.startsWith("http://dbpedia.org/ontology/") && o.Predicate1.uri.startsWith("http://dbpedia.org/ontology")){
			return -1;
		}
		
		if(Predicate2.uri.startsWith("http://dbpedia.org/ontology/") && !o.Predicate2.uri.startsWith("http://dbpedia.org/ontology")){
			return 1;
		}
		
		if(!Predicate2.uri.startsWith("http://dbpedia.org/ontology/") && o.Predicate2.uri.startsWith("http://dbpedia.org/ontology")){
			return -1;
		}
		
		return Predicate1.uri.length() - o.Predicate1.uri.length();
	}

}
