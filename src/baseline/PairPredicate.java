package baseline;

import java.util.Comparator;

public class PairPredicate implements Comparable<PairPredicate>,Comparator<PairPredicate> {
	public Predicate Predicate1;
	public Predicate Predicate2;
	public double score;
	
	public PairPredicate(){
		
	}
	
	public PairPredicate(Predicate p1, Predicate p2){
		Predicate1 = p1;
		Predicate2 = p2;
		score = (p1.maxScore+p2.maxScore)/2;
	}
	
	/**
	 * 从小到大排序
	 */
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

	/**
	 * 从大到小排序
	 */
	@Override
	public int compare(PairPredicate o1, PairPredicate o2) {
		// TODO Auto-generated method stub
		if(o1.score < o2.score){
			return 1;
		}
		if(o1.score > o2.score){
			return -1;
		}
		
		int count1 = 0;
		int count2 = 0;
		
		if(o2.Predicate1.uri.startsWith("http://dbpedia.org/ontology/")){
			++count2;
		}
		if(o2.Predicate2.uri.startsWith("http://dbpedia.org/ontology/")){
			++count2;
		}
		if(o1.Predicate1.uri.startsWith("http://dbpedia.org/ontology/")){
			++count1;
		}
		if(o1.Predicate2.uri.startsWith("http://dbpedia.org/ontology/")){
			++count1;
		}
		
		return count2 - count1;
	}

}
