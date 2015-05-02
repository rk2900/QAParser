package baseline;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;

import finder.Pipeline;
import basic.FileOps;

public class Main {

	public static void entityExtraction(String inPath,String outPath){
		LinkedList<String> wikiLines = FileOps.LoadFilebyLine(inPath);
		HashMap<String, ArrayList<Entity>> map = new HashMap<String, ArrayList<Entity>>();
		for (String wikiLine : wikiLines) {
			String content;
			LinkedList<Entity> entityList = new LinkedList<Entity>();
			
			String [] wikiText = wikiLine.split("\t");
			if(wikiText.length != 5){
				System.err.println("Wrong wiki-entity format");
				System.err.println(wikiLine);
				continue;
			}
			
			if(!map.containsKey(wikiText[0])){
				map.put(wikiText[0], new ArrayList<Entity>());
			}
			
			//需要将char index 转化为 word index
			map.get(wikiText[0]).add(new Entity(wikiText[4],wikiText[3],Integer.parseInt(wikiText[1]),Integer.parseInt(wikiText[2])));
		}
		System.out.println("Load finished");
		
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
				Entity beforeEntity = questionEntityList.get(0);
				
				while(index <= questionEntityList.size()){
//					System.out.println(index + "\t"+ questionEntityList.size());
					if(index == questionEntityList.size()){
						result.add(beforeEntity);
						break;
					}
//					System.out.println("...");
					Entity currentEntity = questionEntityList.get(index);
					if(currentEntity.start < beforeEntity.end){
						if(!currentEntity.wiki.equals(beforeEntity.wiki) || !currentEntity.uri.equals(beforeEntity.uri)){
							System.err.println(question);
						}
						
						beforeEntity.setEnd(currentEntity.end);
						beforeEntity.setUri(currentEntity.uri);
						beforeEntity.setWiki(currentEntity.wiki);
					}else{
						result.add(beforeEntity);
//						System.out.println(beforeEntity.uri);
						beforeEntity = currentEntity;	
					}
					++index;
				}
//				System.out.println("OK");
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
//				System.out.println(count+"****");
//				break;
			}
			fout.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String wikiPath = "./data/q-e/all-mark-wiki-entity.txt";
		String outPath = "./data/zch/newEntity.txt";
		Pipeline pipeline = new Pipeline();
//		QuestionFrame qf = pipeline.xmlParser.getQuestionFrameWithPseudoId(i);
		entityExtraction(wikiPath,outPath);
//		LinkedList<String> wikiLines = FileOps.LoadFilebyLine(wikiPath);
//		
//		for (String wikiLine : wikiLines) {
//			String content;
//			LinkedList<Entity> entityList = new LinkedList<Entity>();
//			
//			String [] wikiText = wikiLine.split("\t");
//			if(wikiText.length != 5){
//				System.err.println("Wrong wiki-entity format");
//				System.err.println(wikiLine);
//				continue;
//			}
//		}
	}

}
