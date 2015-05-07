package baseline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;

import paser.QuestionFrame;

public class Answer {
	public ArrayList<Predicate> predictList;
	public String entityUri;
	public QuestionFrame qf;
	public String exceptionString;
	public HashMap<Predicate, Double> typeConstrainScore;
	public HashMap<Predicate, Double> entityConstrainScore;
	public HashMap<Predicate, LinkedList<RDFNode>> resources; 
	
	public Answer(){
		predictList = new ArrayList<Predicate>();
		typeConstrainScore = new HashMap<>();
		resources = new HashMap<Predicate, LinkedList<RDFNode>>(); 
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
