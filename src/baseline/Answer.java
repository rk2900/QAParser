package baseline;

import java.util.ArrayList;
import java.util.LinkedList;

import knowledgebase.ClientManagement;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

import paser.QuestionFrame;

public class Answer {
	public ArrayList<Predicate> predictList;
	public String entityUri;
	public QuestionFrame qf;
	public String exceptionString;
	
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
				LinkedList<RDFNode> resources = ClientManagement.getNode(entityUri, predict.uri);
				for (RDFNode rdfNode : resources) {
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
	 * 针对number类型的问题的处理结果
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
				LinkedList<RDFNode> resources = ClientManagement.getNode(entityUri, predict.uri);
				for (RDFNode rdfNode : resources) {
					sb.append(" " + rdfNode.toString());
				}
				sb.append("\n");
				
				if(!isOneStep && resources.size() == 1){
					RDFNode resource = resources.get(0);
					String resourceString = resource.toString();
					int i;
					for(i=0; i<resourceString.length(); ++i){
						if(!Character.isDigit(resourceString.charAt(i)) && resourceString.charAt(i) != '.'){
							break;
						}
					}
					if(i == resourceString.length()){
						isOneStep = true;
						oneStep = resource;
						break;
					}
				}
			}
			if(isOneStep){
				sb.append("Result: ");
				sb.append(oneStep.toString());
				sb.append("\n");
			}else{
				if(predictList.size() > 0){
					Predicate predicate = predictList.get(0);
					for(int i=1; i<predictList.size(); ++i){
						if(predictList.get(i).maxScore < predicate.maxScore){
							break;
						}else{
							if(predictList.get(i).toString().startsWith("http://dbpedia.org/ontology/")){
								predicate = predictList.get(i);
								break;
							}
						}
					}
					sb.append("Result: ");
					sb.append(predicate.toString());
					sb.append("\n");
					LinkedList<RDFNode> resources = ClientManagement.getNode(entityUri, predicate.uri);
					sb.append(resources.size());
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
