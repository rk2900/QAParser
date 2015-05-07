package entityLinking.parse;

import java.util.LinkedList;

import paser.QuestionFrame;

public class IndexTransform {
	public int start;
	public int end;
	public boolean isMatched;
	
	public IndexTransform(int s, int e, QuestionFrame qf){
		isMatched = false;
		
		String curEntity = qf.question.substring(s,e);
		curEntity = curEntity.replace(" ", "");
		LinkedList<String> wordList = qf.wordList;
		for(int i=0; i<wordList.size();++i){
			if(curEntity.startsWith(wordList.get(i))){
				StringBuilder sb = new StringBuilder();
				sb.append(wordList.get(i));
				int k=i+1;
				for(; k<wordList.size();++k){
					if(curEntity.startsWith(sb.toString()+wordList.get(k))){
						sb.append(wordList.get(k));
					}else{
						--k;
						break;
					}
				}
				if(k == wordList.size()){
					--k;
				}
				if(sb.toString().equals(curEntity)){
					start = i;
					end = k;
					isMatched = true;
					break;
				}
			}
		}
	}
}
