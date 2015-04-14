package similarity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;

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
        
        //最外层：min子串的长度，从最大长度开始
        for (int i = min.length(); i >= 1; i--) {
            //遍历长度为i的min子串，从0开始
            for (int j = 0; j <= min.length() - i; j++) {  
                target = min.substring(j, j + i);  
                //遍历长度为i的max子串，判断是否与target子串相同，从0开始
                for (int k = 0; k <= max.length() - i; k++) {  
                    if (max.substring(k,k + i).equals(target)) {  
                        return i;  
                    }
                }
            }
        }
        return 0;  
	}


	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Pipeline pipeline = new Pipeline();
		int pseudoId = 5;
		
		QuestionSingle q = pipeline.preProcess(pseudoId);
		if(q==null)
			return;
		
		LinkedList<String> words = q.getWordList();
		LinkedList<String> postags = q.getPOSList();
		HashSet<Integer> entityindexes = q.getEntityPositions();
		LinkedList<String> predicts = q.getSurPredicates();
		
		System.out.println(q.getWordList());
		System.out.println(q.getPOSList());
		System.out.println(q.mention);
		System.out.println(q.entityPositions);
		System.out.println(q.surPredicates.size());
		
		for (String predict : predicts) {
//			System.out.println(predict);
			LinkedList<String> labels = ClientManagement.getLabel(predict);
			StringBuilder sb = new StringBuilder();
			for (String label : labels) {
				sb.append(label);
				sb.append("\t");
			}
			sb.append("\n");
			System.out.println(sb.toString());
		}

	}

}
