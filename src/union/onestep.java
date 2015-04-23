package union;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import com.hp.hpl.jena.rdf.model.RDFNode;

import knowledgebase.ClientManagement;
import paser.Question;
import paser.QuestionSingle;
import pattern.RuleBasedType;
import type.PathGene;
import umbc.umbcDB;
import basic.FileOps;
import finder.Pipeline;

public class onestep {

	public static Pipeline pipeline;
	public static int rankingSize;
	public static umbcDB db;
//	public static double UMBC_threshold = 0.1;
	
	static {
		pipeline = new Pipeline();
		rankingSize = 5;
		db = new umbcDB();
	}
	
	
	public static HashSet<String> getTypes(int id){
		RuleBasedType rbType = new RuleBasedType();
//		rbType.initializeXMLParser();
		Question q = rbType.xmlParser.getQuestionWithPseudoId(id);
		String qText = q.question;
		HashSet<String> types = rbType.typeExtractor(qText, false);
		return types;
	}
	
	//cal the predicts' scores
	public static double rankingUMBC(String predict,LinkedList<String> questionList){
		LinkedList<String> labels = ClientManagement.getLabel(predict);
		double maxScore = 0;
		double currentScore;
		for (String label : labels) {
			if(label.length() == 0){
				continue;
			}
			currentScore = 0;
			String [] labelWords = label.split(" ");
			for (String labelWord : labelWords) {
				double wordScore = 0;
				for (String questionWord : questionList) {
					double tmpScore = db.getScore(labelWord, questionWord);
					wordScore = (wordScore<tmpScore)?tmpScore:wordScore;
				}
				currentScore += wordScore;
			}
			currentScore = currentScore / labelWords.length;
			
			if(maxScore < currentScore){
				maxScore = currentScore;
			}
		}
		return maxScore;
	}
	
	public static void main(String[] args) {
		String questionPath = "./data/zch/manual-selected-question.txt";
//		questionPath = "./data/zch/question-semantic-1.txt";
		LinkedList<String> questions = FileOps.LoadFilebyLine(questionPath);
//		System.out.println(questions.size());
		try {
			BufferedWriter fout = new BufferedWriter(new FileWriter("./data/zch/manual-selected-question-top5-result.txt"));
			for (String question : questions) {
				String[] list = question.split("\t");
				int questionId = Integer.parseInt(list[0]);
				QuestionSingle q = pipeline.preProcess(questionId);
				if(q==null){
					System.out.println("Null question:\t");
					continue;
				}
				String entityUri = q.getEntityUri();
				HashSet<String> types = getTypes(questionId);
				System.out.println(q.question+"\tbegin");
				fout.write(list[0]);
				fout.write("\n");
				fout.write(q.question);
				fout.write("\n");
				
				LinkedList<String> words = q.getWordList();
//				LinkedList<String> postags = q.getPOSList();
				HashSet<Integer> entityindexes = q.getEntityPositions();
				LinkedList<String> predicts = q.getSurPredicates();
							
//				fout.write(words.toString());
//				fout.write("\n");
//				fout.write(postags.toString());
//				fout.write("\n");
				
				LinkedList<String> questionContext = new LinkedList<String>();
				for(int i=0; i<words.size(); ++i){
					if(!entityindexes.contains(i)){
						questionContext.add(words.get(i));
					}
				}
				
				HashMap<Double, ArrayList<String>> scoreMap = new HashMap<Double, ArrayList<String>>();
				
				for (String predict : predicts) {
					double score = rankingUMBC(predict, questionContext);
					if(!scoreMap.containsKey(score)){
						scoreMap.put(score, new ArrayList<String>());
					}
					scoreMap.get(score).add(predict);
				}
				
				
				ArrayList<Double> scores = new ArrayList<Double>(scoreMap.keySet());
				Collections.sort(scores,Collections.reverseOrder());
				
				int count = 0;
				for(int i=0; i<scores.size() && count<rankingSize;++i){
					double score = scores.get(i);
					if(score == 0){
						break;
					}
					for (String p : scoreMap.get(score)) {
						StringBuilder sb = new StringBuilder();
						sb.append("Predict:\t");
						sb.append(p);
						sb.append("\t");
						sb.append(score);
						sb.append("\n");
						LinkedList<RDFNode> resourceNodes = ClientManagement.getNode(entityUri, p);
						
						for(String type:types){
							sb.append("\ttype:\t");
							sb.append(type);
							sb.append("\n");
							for(RDFNode resourceNode:resourceNodes){
								if(!resourceNode.isResource()){
									continue;
								}
								
								String resource = resourceNode.toString();
								sb.append("\t\t");
								System.out.println(p+"\t"+resource);
								double typeScore = PathGene.haoge(resource,type);
								sb.append(resource);
								sb.append("\t");
								sb.append(typeScore);
								sb.append("\t");
							}
							sb.append("\n");
						}
						fout.write(sb.toString());
						++count;
					}
				}fout.write("\n");
				fout.flush();
				System.out.println("***end***");
			}
			fout.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
