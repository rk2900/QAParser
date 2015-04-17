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
	
	public static Pipeline pipeline;
	public static int minLength = 3;
	public static double threshold = 0.0;
	public static double precision = 0.59;
	public static double recall = 0.59;
	static{
		pipeline = new Pipeline();
	}
	
	/*
	 * return the max common prefix length of two string
	 */
	public static int getMaxComPreLength(String str1, String str2){
		int maxLength = 0;
		str1 = str1.toLowerCase();
		str2 = str2.toLowerCase();
		
		int size = (str1.length() < str2.length())? str1.length():str2.length();
		for(int i=0; i<size; ++i){
			if(str1.charAt(i) == str2.charAt(i)){
				++maxLength;
			}else{
				break;
			}
		}
		return maxLength;
	}
	
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

	public static int getMaxComPreLength4List(String str1,LinkedList<String> list){
		int maxLength = 0;
		int currentLength;
		for (String str2 : list) {
			currentLength = getMaxComPreLength(str1, str2);
			if(currentLength > maxLength){
				double rate1 = (double)currentLength / str2.length();
				double rate2 = (double)currentLength / str1.length();
				if(currentLength > minLength && rate1 > precision && rate2 > recall){
					maxLength = currentLength;
				}
			}
		}
		return maxLength;
	}

	public static double calLexRate4List(LinkedList<String> words, LinkedList<Integer> lengths){
		double rate = 0;
		if(words.size() != lengths.size()){
			System.err.println("***calculateRate4List wrong***");
		}
		
		int lengthSum = 0;
		int sum = 0;
		for (String word : words) {
			sum += word.length();
		}
		for (Integer length : lengths) {
			lengthSum += length;
		}
		
		rate = (double)lengthSum/sum;
		return rate;
	}
	
	public static double[] calSemRate(String str,ArrayList<String> nP2,ArrayList<String> vP){
		double[] semRates = new double[8];
		for (int i=0; i<semRates.length; ++i) {
			semRates[i] = 0;
		}
		
		nP2.addAll(vP);
		for (String np : nP2) {
			LinkedList<String> npLemmas = pipeline.getLemma(np);
			for (String npLemma : npLemmas) {
				double[] tmpRates = wsfj.similarity(str, npLemma);
				for(int i=0; i<semRates.length; ++i){
					if(semRates[i] <tmpRates[i]){
						semRates[i] = tmpRates[i];
					}
				}
			}
		}
		return semRates;
	}
	
	public static HashMap<String, Double> topRanking(int pseudoId){
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
		System.out.println(result);
		return result;
	}
	
	public static void comSub() {
		// TODO Auto-generated method stub
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
					
					boolean first = true;
					while(i<postags.size() && (postag = postags.get(i)).startsWith("NN")){
						if(!first){
							phrase.append(" ");
						}
						phrase.append(words.get(i));
						first = false;
						++i;
					}
					if(phrase.length() > 0){
						NP.add(phrase.toString());
						phrase.setLength(0);
					}else{
						while(i<postags.size() && (postag = postags.get(i)).startsWith("VB")){
							if(!stopwords.contains(words.get(i).toLowerCase())){
								if(!first){
									phrase.append(" ");
								}
								phrase.append(words.get(i));
								first = false;
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
//						String tlabel = label.replace(" ", "");
						String tlabel = label;
						for(int k=0; k<NP.size(); ++k){
							comLength = getCommonStrLength(tlabel,NP.get(k));
							if(comLength < minLength){
								comLength = 0;
							}
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
							if(comLength < minLength){
								comLength = 0;
							}
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
				boolean isIn = false;
				for(int k=0; count<5 && k<ranking.size(); ++k){
					double currentRanking = ranking.get(k);
					if(currentRanking > threshold){
						for (String key : rateMap.keySet()) {
							if(rateMap.get(key).equals(currentRanking)){
								++count;
								fout.write(key);
								fout.write("\t");
								fout.write(ranking.get(k).toString());
								fout.write("\n");
								for (int j = 1; j < list.length; j++) {
									if(list[j].equals(key)){
										isIn = true;
									}
								}
							}
						}
					}else{
						break;
					}
				}
				fout.write("\n");
				if(isIn){
					++matched;
				}
			}
			fout.close();
			System.out.println(matched);
			System.out.println(questions.size());
			System.out.println((double)matched/questions.size());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void comPre(){
		String questionPath = "./data/zch/question-semantic.txt";
		LinkedList<String> questions = FileOps.LoadFilebyLine(questionPath);
		
		HashSet<String> stopwords = new HashSet<String>();
		LinkedList<String> stopwordslist = FileOps.LoadFilebyLine("./data/zch/stopwords.txt");
		for (String stopword : stopwordslist) {
			stopwords.add(stopword);
		}
		
		try {
			BufferedWriter fout = new BufferedWriter(new FileWriter("./data/zch/semantic-com-prefix.txt"));
			int matched = 0;
			System.out.println(questions.size());
			for (String question : questions) {
				String[] list = question.split("\t");
				int questionId = Integer.parseInt(list[0]);
				QuestionSingle q = pipeline.preProcess(questionId);
				if(q==null)
					return;
				
//				fout.write(q.question);
//				fout.write("\n");
				
				LinkedList<String> words = q.getWordList();
				LinkedList<String> postags = q.getPOSList();
				HashSet<Integer> entityindexes = q.getEntityPositions();
				LinkedList<String> predicts = q.getSurPredicates();
							
				ArrayList<String> NP = new ArrayList<String>();
				ArrayList<String> VP = new ArrayList<String>();
				
//				fout.write(words.toString());
//				fout.write("\n");
//				fout.write(postags.toString());
//				fout.write("\n");
				
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
					
					boolean first = true;
					while(i<postags.size() && (postag = postags.get(i)).startsWith("NN")){
						if(!first){
							phrase.append(" ");
						}
						phrase.append(words.get(i));
						first = false;
						++i;
					}
					if(phrase.length() > 0){
						NP.add(phrase.toString());
						phrase.setLength(0);
					}else{
						while(i<postags.size() && (postag = postags.get(i)).startsWith("VB")){
							if(!stopwords.contains(words.get(i).toLowerCase())){
								if(!first){
									phrase.append(" ");
								}
								phrase.append(words.get(i));
								first = false;
							}
							++i;
						}
						if(phrase.length() > 0){
							VP.add(phrase.toString());
							phrase.setLength(0);
						}
					}
				}
//				fout.write(NP.toString());
//				fout.write("\n");
//				fout.write(VP.toString());
//				fout.write("\n");
//				fout.write(question);
//				fout.write("\n");
				
				double rate;
				HashMap<String, Double> rateMap = new HashMap<String, Double>();
				HashSet<Double> rankingSet = new HashSet<Double>();
				
				System.out.println("predicts:\t"+predicts.size());
				for (String predict : predicts) {
					LinkedList<String> labels = ClientManagement.getLabel(predict);
					System.out.println("labels:+\t"+labels.size());
					for (String label : labels) {
						rate = 0;
//						String tlabel = label.replace(" ", "");
						LinkedList<String> tlabels = pipeline.getLemma(label);
						LinkedList<Integer> preLength = new LinkedList<Integer>(); 
//						for (String tlabel : tlabels) {
//							int currentLength = getMaxComPreLength4List(tlabel, pipeline.getLemma(q.question));
//							preLength.add(currentLength);
//						}
//						rate = calLexRate4List(tlabels, preLength);
						
						for (String tlable : tlabels) {
							double [] rates = calSemRate(tlable, NP, VP);
//							System.out.println(tlable);
							fout.write(tlable);
							fout.write("\n");
							for (int j = 0; j < rates.length; j++) {
//								System.out.println(rates[j]);
								fout.write(String.valueOf(rates[j]));
								fout.write("\t");
							}
//							System.out.println();
							fout.write("\n");
						}
						rankingSet.add(rate);
						rateMap.put(predict, rate);
					}
				}
//				ArrayList<Double> ranking = new ArrayList<Double>(rankingSet);
//				Collections.sort(ranking);
//				Collections.reverse(ranking);
//				
//				int count = 0;
//				boolean isIn = false;
//				for(int k=0; count<5 && k<ranking.size(); ++k){
//					double currentRanking = ranking.get(k);
//					if(currentRanking > threshold){
//						for (String key : rateMap.keySet()) {
//							if(rateMap.get(key).equals(currentRanking)){
//								++count;
//								fout.write(key);
//								fout.write("\t");
//								fout.write(ranking.get(k).toString());
//								fout.write("\n");
//								for (int j = 1; j < list.length; j++) {
//									if(list[j].equals(key)){
//										isIn = true;
//									}
//								}
//							}
//						}
//					}else{
//						break;
//					}
//				}
//				fout.write("\n");
//				if(isIn){
//					++matched;
//				}
			}
			fout.close();
			System.out.println(matched);
			System.out.println(questions.size());
			System.out.println((double)matched/questions.size());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void semWSFJ(){
		String questionPath = "./data/zch/question-semantic.txt";
		LinkedList<String> questions = FileOps.LoadFilebyLine(questionPath);
		
		HashSet<String> stopwords = new HashSet<String>();
		LinkedList<String> stopwordslist = FileOps.LoadFilebyLine("./data/zch/stopwords.txt");
		for (String stopword : stopwordslist) {
			stopwords.add(stopword);
		}
		
		try {
			BufferedWriter fout = new BufferedWriter(new FileWriter("./data/zch/semantic-com-prefix.txt"));
			int matched = 0;
//			System.out.println(questions.size());
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
					
					boolean first = true;
					while(i<postags.size() && (postag = postags.get(i)).startsWith("NN")){
						if(!first){
							phrase.append(" ");
						}
						phrase.append(words.get(i));
						first = false;
						++i;
					}
					if(phrase.length() > 0){
						NP.add(phrase.toString());
						phrase.setLength(0);
					}else{
						while(i<postags.size() && (postag = postags.get(i)).startsWith("VB")){
							if(!stopwords.contains(words.get(i).toLowerCase())){
								if(!first){
									phrase.append(" ");
								}
								phrase.append(words.get(i));
								first = false;
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
				
				double rate;
				HashMap<String, double[]> rateMap = new HashMap<String, double[]>();
				ArrayList<double[]> rankingSet = new ArrayList<double[]>();
				
				for (String predict : predicts) {
					LinkedList<String> labels = ClientManagement.getLabel(predict);
					for (String label : labels) {
						rate = 0;
						LinkedList<String> tlabels = pipeline.getLemma(label);
						for (String tlable : tlabels) {
							double [] rates = calSemRate(tlable, NP, VP);
							rankingSet.add(rates);
							rateMap.put(predict, rates);
						}
					}
				}
				
				for(int t=0; t<8; ++t){
					HashSet<Double> tArraySet = new HashSet<Double>();
					for (double[] rankingList : rankingSet) {
						tArraySet.add(rankingList[t]);
					}
					
					ArrayList<Double> tArrayList = new ArrayList<Double>(tArraySet);
					Collections.sort(tArrayList);
					Collections.reverse(tArrayList);
					
					for(int tmp=0; tmp<10&&tmp<tArrayList.size(); ++tmp){
						if(tArrayList.get(tmp) > 0){
							for (String key : rateMap.keySet()) {
								for (double dd : rateMap.get(key)) {
									if(dd == tArrayList.get(tmp)){
										fout.write(key);
										fout.write("\t");
										fout.write(tArrayList.get(tmp).toString());
										fout.write("\n");
									}
								}
							}
						}
					}
					fout.write("\n");
				}
			}
			fout.close();
			System.out.println(matched);
			System.out.println(questions.size());
			System.out.println((double)matched/questions.size());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	
	public static void main(String [] args){
//		comSub();
//		comPre();
		semWSFJ();
	}
}
