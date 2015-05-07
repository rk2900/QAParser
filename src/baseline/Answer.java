package baseline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;

import paser.QuestionFrame;

public class Answer {
	
	public String entityUri;
	public QuestionFrame qf;
	public String exceptionString;
	
	public ArrayList<Predicate> predictList;
	public HashMap<Predicate, Double> typeConstrainScore;
	public HashMap<Predicate, Double> entityConstrainScore;
	public HashMap<Predicate, LinkedList<RDFNode>> resources; 
	
	/**
	 * 0 -> one step style
	 * 1 -> pipe style
	 */
	public int answerType;
	public LinkedList<PairPredicate> pairPredicates;
	public HashMap<PairPredicate, LinkedList<RDFNode>> pairResources;
	public HashMap<PairPredicate, Double> pairTypeConstraintScore;
	public HashMap<PairPredicate, Double> pairEntityConstraintScore;
	
	public Answer(){
		answerType = 0;
		exceptionString = "";
	}
	
	public void initial(int type){
		answerType = type;
		switch (type) {
		case 0:
			predictList = new ArrayList<Predicate>();
			typeConstrainScore = new HashMap<>();
			entityConstrainScore  = new HashMap<Predicate, Double>();
			resources = new HashMap<Predicate, LinkedList<RDFNode>>(); 
			break;
		case 1:
			pairPredicates = new LinkedList<PairPredicate>();
			pairResources = new HashMap<PairPredicate, LinkedList<RDFNode>>();
			pairTypeConstraintScore = new HashMap<PairPredicate, Double>();
			pairEntityConstraintScore = new HashMap<PairPredicate, Double>();
			break;
		default:
			break;
		}
	}
	
	public boolean isException(){
		if(exceptionString.length() > 0 ){
			return true;
		}else{
			return false;
		}
	}
	
	public StringBuilder print(){
		StringBuilder sb = new StringBuilder();
		sb.append(qf.id+" "+qf.question);
		sb.append("\n");
		sb.append(qf.query);
		sb.append("\n");
		if(!isException()){
			if(answerType == 0){
				sb.append(entityUri);
				sb.append("\n");
				for (Predicate predict : predictList) {
					sb.append(predict.maxScore + "\t"+ predict.matchedLabel +"\t" + predict.uri);
					LinkedList<RDFNode> res = resources.get(predict);
					for (RDFNode rdfNode : res) {
						sb.append(" " + rdfNode.toString());
					}
					sb.append("\n");
				}
			}
			if(answerType == 1){
				sb.append(entityUri);
				sb.append("\n");
				for (PairPredicate pair : pairPredicates) {
					sb.append(pair.score+"\n");
					sb.append(pair.Predicate1.maxScore + "\t"+ pair.Predicate1.matchedLabel +"\t" + pair.Predicate1.uri + "\n");
					sb.append(pair.Predicate2.maxScore + "\t"+ pair.Predicate2.matchedLabel +"\t" + pair.Predicate2.uri + "\n");
					LinkedList<RDFNode> res = pairResources.get(pair);
					for (RDFNode rdfNode : res) {
						sb.append(" " + rdfNode.toString());
					}
					sb.append("\n");
					break;
				}
			}
			
		}else{
			sb.append(exceptionString);
			sb.append("\n");
		}
		sb.append("\n");
		return sb;
	}
	
	/**
	 * 
	 */
	public StringBuilder numberPrint(){
		StringBuilder sb = new StringBuilder();
		sb.append(qf.id+" "+qf.question);
		sb.append("\n");
		sb.append(qf.query);
		sb.append("\n");
		if(!isException()){
			sb.append(entityUri);
			sb.append("\n");
			
			boolean isOneStep = false;
			RDFNode oneStep = null;
			for (Predicate predict : predictList) {
				sb.append(predict.maxScore + "\t"+ predict.matchedLabel +"\t" + predict.uri);
				LinkedList<RDFNode> res = resources.get(predict);
				for (RDFNode rdfNode : res) {
					sb.append(" " + rdfNode.toString());
				}
				sb.append("\n");
				
				if(!isOneStep && res.size() == 1){
					RDFNode resource = res.get(0);
					if(resource.isLiteral()) {
						Literal literal = resource.asLiteral();
						String resourceString = literal.getLexicalForm();
						int i;
						for(i=0; i<resourceString.length(); ++i){
							if(!Character.isDigit(resourceString.charAt(i)) && resourceString.charAt(i) != '.'){
								break;
							}
						}
						if(i == resourceString.length()){
							isOneStep = true;
							oneStep = resource;
						}
					}
					
				}
			}
			if(isOneStep){
				sb.append("\nResult: ");
				sb.append(oneStep.toString());
				sb.append("\n");
				sb.append("Standard Result: ");
				sb.append(qf.answers);
				sb.append("\n");
			}else{
				if(predictList.size() > 0){
					Predicate predicate = predictList.get(0);
					for(int i=1; i<predictList.size(); ++i){
						if(predictList.get(i).maxScore < predicate.maxScore){
							break;
						}else{
							if(predictList.get(i).uri.toString().startsWith("http://dbpedia.org/ontology/")){
								predicate = predictList.get(i);
								break;
							}
						}
					}
					sb.append("\nCount Result: ");
					sb.append(predicate.uri);
					sb.append(" ");
					LinkedList<RDFNode> res = resources.get(predicate);
					sb.append(res.size());
					sb.append("\n");
					sb.append("Standard Result: ");
					sb.append(qf.answers);
					sb.append("\n");
				}
			}
		}else{
			sb.append(exceptionString);
			sb.append("\n");
		}
		sb.append("\n");
		return sb;
	}
	
}
