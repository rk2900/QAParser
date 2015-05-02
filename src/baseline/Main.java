package baseline;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import paser.QuestionFrame;
import finder.Pipeline;
import basic.FileOps;

public class Main {

	public static void entityExtraction(String inPath,String outPath, Pipeline pipeline){
		LinkedList<String> wikiLines = FileOps.LoadFilebyLine(inPath);
		HashMap<String, ArrayList<Entity>> map = new HashMap<String, ArrayList<Entity>>();
		for (String wikiLine : wikiLines) {
			String content;
			LinkedList<Entity> entityList = new LinkedList<Entity>();
			
			String [] wikiText = wikiLine.split("\t");
			if(wikiText.length != 5){
				System.err.println("Wrong wiki-entity format"+"\t"+wikiLine);
				continue;
			}
			
			if(!map.containsKey(wikiText[0])){
				map.put(wikiText[0], new ArrayList<Entity>());
			}
			
			//需要将char index 转化为 word index
			QuestionFrame qf = pipeline.xmlParser.getQuestionFrameWithPseudoId(Integer.parseInt(wikiText[0]));
			content = qf.question;
			LinkedList<String> wordList = qf.getWordList();
			String curEntity = content.substring(Integer.parseInt(wikiText[1]),Integer.parseInt(wikiText[2]));
			
			curEntity = curEntity.replace(" ", "");
			
			boolean isMatched = false;
			
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
						map.get(wikiText[0]).add(new Entity(wikiText[4],wikiText[3],i,k));
						isMatched = true;
						break;
					}
				}
			}
			
			if(!isMatched){
				System.out.println(content);
				System.out.println(curEntity);
			}
			
//			String [] wList = curEntity.split(" ");
//			int i;
//			for(i=0; i<wordList.size(); ++i){
//				boolean mark = true;
//				if(wordList.get(i).equals(wList[0])){
//					for(int k=1;k<wList.length; ++k){
//						if(!wordList.get(i+k).equals(wList[k])){
//							mark = false;
//							break;
//						}
//					}
//					if(mark){
//						map.get(wikiText[0]).add(new Entity(wikiText[4],wikiText[3],i,i+wList.length-1));
//						break;
//					}
//				}
//			}
		}
//		System.out.println("Load finished");
//		
		try {
			BufferedWriter fout = new BufferedWriter(new FileWriter(outPath));
			int count = 0;
			for (String question : map.keySet()) {
				
				ArrayList<Entity> questionEntityList = map.get(question);
				Collections.sort(questionEntityList);
//				System.out.println("sort finished");
				LinkedList<Entity> result = new LinkedList<Entity>();
				int index = 1;
//				System.out.println(questionEntityList.size());
				if(questionEntityList.size() == 0){
					System.err.println(question+"\tszie = 0");
					continue;
				}
				Entity beforeEntity = questionEntityList.get(0);
				
				while(index <= questionEntityList.size()){
					if(index == questionEntityList.size()){
						result.add(beforeEntity);
						break;
					}
					Entity currentEntity = questionEntityList.get(index);
					if(currentEntity.start <= beforeEntity.end){
						if(!currentEntity.wiki.equals(beforeEntity.wiki) || !currentEntity.uri.equals(beforeEntity.uri)){
							System.err.println("different entities for intersection\t"+question);
						}
						
						beforeEntity.setEnd(currentEntity.end);
						
						if(currentEntity.start == beforeEntity.start){
							beforeEntity.setUri(currentEntity.uri);
							beforeEntity.setWiki(currentEntity.wiki);
						}
					}else{
						result.add(beforeEntity);
						beforeEntity = currentEntity;	
					}
					++index;
				}
				
				StringBuilder sb = new StringBuilder();
				sb.append(question);
				sb.append("\n");
				sb.append(result.size());
				sb.append("\n");
				for (Entity entity : result) {
					sb.append(entity.start);
					sb.append(" ");
					sb.append(entity.end);
					sb.append(" ");
					sb.append(entity.wiki);
					sb.append(" ");
					sb.append(entity.uri);
					sb.append("\n");
				}
				fout.write(sb.toString());
				++count;
			}
			fout.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void setEntity(Pipeline pipeline){
		String inPath = "./data/zch/newEntity.txt";
		LinkedList<String> lines = FileOps.LoadFilebyLine(inPath);
		
		HashMap<Integer, LinkedList<Entity>> map = new HashMap<Integer, LinkedList<Entity>>();
		int index = 0;
		while(index < lines.size()){
			int id = Integer.parseInt(lines.get(index++));
			int size = Integer.parseInt(lines.get(index++));
			map.put(id, new LinkedList<Entity>());
			for(int k=0; k<size; ++k){
				String [] texts = lines.get(index++).split(" ");
				map.get(id).add(new Entity(texts[3], texts[2], Integer.parseInt(texts[0]), Integer.parseInt(texts[1])));
			}
		}
		
		for(int i=1; i<=300; ++i){
			QuestionFrame qf = pipeline.xmlParser.getQuestionFrameWithPseudoId(i);
			if(map.containsKey(i)){
				qf.setEntityList(map.get(i));
			}else{
				qf.setEntityList(new LinkedList<Entity>());
			}
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String wikiPath = "./data/q-e/all-mark-wiki-entity.txt";
		String outPath = "./data/zch/newEntity.txt";
		 
		Pipeline pipeline = new Pipeline();
//		entityExtraction(wikiPath,outPath,pipeline);
		
		setEntity(pipeline);
		
		int [] intersection = {51,174,296,103,108};
		for(int i=0; i<intersection.length; ++i){
			System.out.println("*************");
			QuestionFrame qf = pipeline.xmlParser.getQuestionFrameWithPseudoId(intersection[i]);
			
			System.out.println(qf.question);
			System.out.println(qf.wordList);
			
			for(Entity e:qf.getEntityList()){
				for(int k=e.getStart(); k<=e.getEnd(); ++k){
					System.out.print(qf.wordList.get(k)+" ");
				}
				System.out.println();
				System.out.println(e.start+" "+e.end+" "+e.uri);
			}
		}
	}

}
