package baseline;

import java.util.ArrayList;
import java.util.LinkedList;

import knowledgebase.ClientManagement;

import com.hp.hpl.jena.rdf.model.RDFNode;

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
}
