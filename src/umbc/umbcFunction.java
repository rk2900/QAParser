package umbc;

import java.net.URLDecoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedList;

import knowledgebase.ClientManagement;
import paser.QuestionSingle;
import similarity.UMBC;
import finder.Pipeline;

public class umbcFunction {
	public static Pipeline pipeline = new Pipeline();
	public static umbcDB db = new umbcDB();
	
	public static void insertAllPairs( ){
		for(int questionId=1; questionId<=300; ++questionId){
			QuestionSingle q = pipeline.preProcess(questionId);
			if(q==null){
				continue;
			}
			
			LinkedList<String> words = q.getWordList();
			HashSet<Integer> entityindexes = q.getEntityPositions();
			LinkedList<String> predicts = q.getSurPredicates();
			
			LinkedList<String> questionContext = new LinkedList<String>();
			for(int i=0; i<words.size(); ++i){
				if(!entityindexes.contains(i)){
					questionContext.add(words.get(i));
				}
			}
			for (String predict : predicts){
				LinkedList<String> labels = ClientManagement.getLabel(predict);
				for (String label : labels) {
					String [] labelWords = label.split(" ");
					for (String labelWord : labelWords) {
						if(labelWord.length() == 0){
							continue;
						}
						for (String queString : questionContext) {
							if(queString.length() == 0){
								continue;
							}
							db.insertWords(labelWord, queString);
						}
					}
				}
			}
			
			System.out.println(questionId+"\t"+words);
		}
	}

	public static void updateScores(){
		int count = 0;
		ResultSet r = db.getAllPairs();
		try {
			while(r.next()){
				String word1 = r.getString(1);
				String word2 = r.getString(2);
				double value = r.getDouble(3);
				if(value > -1){
					continue;
				}
				++count;
				word1 = URLDecoder.decode(word1);
				word2 = URLDecoder.decode(word2);
//				System.out.println(word1+"\t"+word2+"\t"+value);
				
				double score = UMBC.getSimilarity(word1, word2);
				db.updateValue(word1, word2, score);
				if(count%100 == 0){
					System.err.println(count);
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String [] args){
		updateScores();
	}
}
