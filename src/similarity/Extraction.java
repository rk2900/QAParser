package similarity;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import basic.FileOps;
import knowledgebase.ClientManagement;
import paser.QuestionSingle;
import finder.Pipeline;

public class Extraction {
	
	public static int getCommonStrLength(String str1, String str2){
		str1 = str1.toLowerCase();
		str2 = str2.toLowerCase();
         
		int len1 = str1.length();  
		int len2 = str2.length(); 
		String min = null;  
        String max = null;  
        String target = null;
        
        min = len1 <= len2 ? str1 : str2;
        max = len1 >  len2 ? str1 : str2;
        
        for (int i = min.length(); i >= 1; i--) {
            for (int j = 0; j <= min.length() - i; j++) {  
                target = min.substring(j, j + i);  
                for (int k = 0; k <= max.length() - i; k++) {  
                    if (max.substring(k,k + i).equals(target)) {  
                        return i;  
                    }
                }
            }
        }
        return 0;  
	}

	public static HashMap<String, Double> topRanking(Pipeline pipeline,int pseudoId){
		QuestionSingle q = pipeline.preProcess(pseudoId);
		if(q==null)
			return null;
		
		LinkedList<String> words = q.getWordList();
		LinkedList<String> postags = q.getPOSList();
		HashSet<Integer> entityindexes = q.getEntityPositions();
		LinkedList<String> predicts = q.getSurPredicates();
		
		System.out.println(words);
		System.out.println(postags);
		System.out.println(entityindexes);
		System.out.println(q.mention);
		
		ArrayList<String> NP = new ArrayList<String>();
		ArrayList<String> VP = new ArrayList<String>();
		
		/*
		 * load the stop words
		 */
		HashSet<String> stopwords = new HashSet<String>();
		LinkedList<String> stopwordslist = FileOps.LoadFilebyLine("./data/zch/stopwords.txt");
		for (String stopword : stopwordslist) {
			stopwords.add(stopword);
		}
//		System.out.println("file loaded!");
		
		/*
		 * get the NP list and VP list;
		 */
		int i = 0;
		String postag;
		StringBuilder phrase = new StringBuilder();
		
		while(i<postags.size()){
			postag = postags.get(i);
			if(entityindexes.contains(i)){
				++i;
				continue;
			}
			if(!postag.startsWith("VB") && !postag.startsWith("NN")){
				++i;
				continue;
			}
			
			while(i<postags.size() && (postag = postags.get(i)).startsWith("NN")){
				phrase.append(words.get(i));
				++i;
			}
			if(phrase.length() > 0){
				NP.add(phrase.toString());
				phrase.setLength(0);
			}else{
				while(i<postags.size() && (postag = postags.get(i)).startsWith("VB")){
					if(!stopwords.contains(words.get(i).toLowerCase())){
						phrase.append(words.get(i));
					}
					++i;
				}
				if(phrase.length() > 0){
					VP.add(phrase.toString());
					phrase.setLength(0);
				}
			}
		}
		
//		System.out.println(NP);
//		System.out.println(VP);
		
		int comLength;
		double rate;
		HashMap<String, Double> rateMap = new HashMap<String, Double>();
		HashSet<Double> rankingSet = new HashSet<Double>();
		for (String predict : predicts) {
//			System.out.println(predict);
			LinkedList<String> labels = ClientManagement.getLabel(predict);
			for (String label : labels) {
				comLength = 0;
				rate = 0;
				String tlabel = label.replace(" ", "");
				for(int k=0; k<NP.size(); ++k){
					comLength = getCommonStrLength(tlabel,NP.get(k));
					double p = (double)comLength / tlabel.length();
					double r = (double)comLength / NP.get(k).length();
					if((p+r) > 0){
						rate += 2*p*r/(p+r);
					}else{
						rate += 0;
					}
					
				}
				for(int k=0; k<VP.size(); ++k){
					comLength = getCommonStrLength(tlabel, VP.get(k));
					double p = (double)comLength / tlabel.length();
					double r = (double)comLength / VP.get(k).length();
					if((p+r) > 0){
						rate += 2*p*r/(p+r);
					}else{
						rate += 0;
					}
				}
				rankingSet.add(rate);
				rateMap.put(predict, rate);
			}
//			System.out.println("******");
		}
		ArrayList<Double> ranking = new ArrayList<Double>(rankingSet);
		Collections.sort(ranking);
		Collections.reverse(ranking);
		
//		System.out.println(ranking);
		
		int count = 0;
		HashMap<String, Double> result = new HashMap<String, Double>();
		for(int k=0; count<5 && k<ranking.size(); ++k){
			for (String key : rateMap.keySet()) {
				if(rateMap.get(key).equals(ranking.get(k))){
					++count;
//					System.out.println(ranking.get(k)+"\t"+key);
					result.put(key, ranking.get(k));
				}
			}
		}
		return result;
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Pipeline pipeline = new Pipeline();
		String questionPath = "./data/zch/question.txt";
		LinkedList<String> questions = FileOps.LoadFilebyLine(questionPath);
		
		HashSet<String> stopwords = new HashSet<String>();
		LinkedList<String> stopwordslist = FileOps.LoadFilebyLine("./data/zch/stopwords.txt");
		for (String stopword : stopwordslist) {
			stopwords.add(stopword);
		}
		
		try {
			BufferedWriter fout = new BufferedWriter(new FileWriter("./data/zch/detail-lexical-result.txt"));
			int matched = 0;
			for (String question : questions) {
				String[] list = question.split("\t");
				int questionId = Integer.parseInt(list[0]);
				QuestionSingle q = pipeline.preProcess(questionId);
				if(q==null)
					return;
				
				fout.write(q.question);
				fout.write("\n");
				
				LinkedList<String> words = q.getWordList();
				LinkedList<String> postags = q.getPOSList();
				HashSet<Integer> entityindexes = q.getEntityPositions();
				LinkedList<String> predicts = q.getSurPredicates();
							
				ArrayList<String> NP = new ArrayList<String>();
				ArrayList<String> VP = new ArrayList<String>();
				
				fout.write(words.toString());
				fout.write("\n");
				fout.write(postags.toString());
				fout.write("\n");
				
				int i = 0;
				String postag;
				StringBuilder phrase = new StringBuilder();
				
				while(i<postags.size()){
					postag = postags.get(i);
					if(entityindexes.contains(i)){
						++i;
						continue;
					}
					if(!postag.startsWith("VB") && !postag.startsWith("NN")){
						++i;
						continue;
					}
					
					while(i<postags.size() && (postag = postags.get(i)).startsWith("NN")){
						phrase.append(words.get(i));
						++i;
					}
					if(phrase.length() > 0){
						NP.add(phrase.toString());
						phrase.setLength(0);
					}else{
						while(i<postags.size() && (postag = postags.get(i)).startsWith("VB")){
							if(!stopwords.contains(words.get(i).toLowerCase())){
								phrase.append(words.get(i));
							}
							++i;
						}
						if(phrase.length() > 0){
							VP.add(phrase.toString());
							phrase.setLength(0);
						}
					}
				}
				fout.write(NP.toString());
				fout.write("\n");
				fout.write(VP.toString());
				fout.write("\n");
				fout.write(question);
				fout.write("\n");
				
				int comLength;
				double rate;
				HashMap<String, Double> rateMap = new HashMap<String, Double>();
				HashSet<Double> rankingSet = new HashSet<Double>();
				for (String predict : predicts) {
					LinkedList<String> labels = ClientManagement.getLabel(predict);
					for (String label : labels) {
						comLength = 0;
						rate = 0;
						String tlabel = label.replace(" ", "");
						for(int k=0; k<NP.size(); ++k){
							comLength = getCommonStrLength(tlabel,NP.get(k));
							double p = (double)comLength / tlabel.length();
							double r = (double)comLength / NP.get(k).length();
							if((p+r) > 0){
								rate += 2*p*r/(p+r);
							}else{
								rate += 0;
							}
							
						}
						for(int k=0; k<VP.size(); ++k){
							comLength = getCommonStrLength(tlabel, VP.get(k));
							double p = (double)comLength / tlabel.length();
							double r = (double)comLength / VP.get(k).length();
							if((p+r) > 0){
								rate += 2*p*r/(p+r);
							}else{
								rate += 0;
							}
						}
						rankingSet.add(rate);
						rateMap.put(predict, rate);
					}
				}
				ArrayList<Double> ranking = new ArrayList<Double>(rankingSet);
				Collections.sort(ranking);
				Collections.reverse(ranking);
				
				int count = 0;
//				HashMap<String, Double> result = new HashMap<String, Double>();
				for(int k=0; count<5 && k<ranking.size(); ++k){
					for (String key : rateMap.keySet()) {
						if(rateMap.get(key).equals(ranking.get(k))){
							++count;
							fout.write(key);
							fout.write("\t");
							fout.write(ranking.get(k).toString());
							fout.write("\n");
//							result.put(key, ranking.get(k));
						}
					}
				}
				fout.write("\n");
				
//				for(int k=1; k<list.length; ++k){
//					if(result.containsKey(list[k])){
//						++matched;
//						System.out.println(questionId+"\t"+list[k]);
//						break;
//					}
//				}
//				System.out.println(matched);
//				System.out.println(questions.size());
//				System.out.println((double)matched/questions.size());
			}
			fout.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
