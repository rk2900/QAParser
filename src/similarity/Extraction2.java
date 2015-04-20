package similarity;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import paser.QuestionSingle;
import umbc.umbcDB;
import basic.FileOps;
import knowledgebase.ClientManagement;
import finder.Pipeline;

public class Extraction2 {

	public static Pipeline pipeline;
	public static int rankingSize;
	public static umbcDB db;
//	public static double UMBC_threshold = 0.1;
	static {
		pipeline = new Pipeline();
		rankingSize = 10;
		db = new umbcDB();
	}
	
//	public static LinkedList<String> getNNPhrase(String text){
//		
//	}
//	
//	public static LinkedList<String> getVBPhrase(String text){
//		
//	}
	
	//¼ÆËãpredictµÄscore
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
//					double tmpScore = UMBC.getSimilarity(labelWord, questionWord);
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
		LinkedList<String> questions = FileOps.LoadFilebyLine(questionPath);
		
		try {
			BufferedWriter fout = new BufferedWriter(new FileWriter("./data/zch/manual-selected-question-top10.txt"));
			for (String question : questions) {
				String[] list = question.split("\t");
				int questionId = Integer.parseInt(list[0]);
				QuestionSingle q = pipeline.preProcess(questionId);
				if(q==null){
					continue;
				}
				System.out.println(q.question+"\tbegin");
				fout.write(q.question);
				fout.write("\n");
				
				LinkedList<String> words = q.getWordList();
				LinkedList<String> postags = q.getPOSList();
				HashSet<Integer> entityindexes = q.getEntityPositions();
				LinkedList<String> predicts = q.getSurPredicates();
							
				fout.write(words.toString());
				fout.write("\n");
				fout.write(postags.toString());
				fout.write("\n");
				
				LinkedList<String> questionContext = new LinkedList<String>();
				for(int i=0; i<words.size(); ++i){
					if(!entityindexes.contains(i)){
						questionContext.add(words.get(i));
					}
				}
				
				HashMap<Double, ArrayList<String>> scoreMap = new HashMap<Double, ArrayList<String>>();
				
//				int predictNum = 0;
				for (String predict : predicts) {
					double score = rankingUMBC(predict, questionContext);
					if(!scoreMap.containsKey(score)){
						scoreMap.put(score, new ArrayList<String>());
					}
					scoreMap.get(score).add(predict);
//					LinkedList<String> lls = ClientManagement.getLabel(predict);
//					++predictNum;
//					System.out.println(predictNum+"\t"+predict+"\t"+lls);
				}
				System.out.println("***end***");
				
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
						sb.append(score);
						sb.append("\t");
						sb.append(p);
						sb.append("\n");
						fout.write(sb.toString());
						++count;
					}
				}fout.write("\n");
			}
			fout.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
