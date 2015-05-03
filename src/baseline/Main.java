package baseline;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import paser.QuestionFrame;
import syntacticParser.Constraint;
import syntacticParser.ConstraintSet;
import syntacticParser.Node;
import finder.Pipeline;
import basic.FileOps;

public class Main {
	
	public static void entityExtraction(String inPath,String outPath, Pipeline pipeline){
		LinkedList<String> wikiLines = FileOps.LoadFilebyLine(inPath);
		HashMap<String, ArrayList<Entity>> map = new HashMap<String, ArrayList<Entity>>();
		for (String wikiLine : wikiLines) {
			String content;
			String [] wikiText = wikiLine.split("\t");
			if(wikiText.length != 5){
				System.err.println("Wrong wiki-entity format"+"\t"+wikiLine);
				continue;
			}
			
			if(!map.containsKey(wikiText[0])){
				map.put(wikiText[0], new ArrayList<Entity>());
			}
			
			//将char index 转化为 word index
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
				System.err.print("UnMatched\t");
				System.err.print(wikiText[0]+" ");
				System.err.println(content);
			}
			
		}
		try {
			BufferedWriter fout = new BufferedWriter(new FileWriter(outPath));
			for (String question : map.keySet()) {
				
				ArrayList<Entity> questionEntityList = map.get(question);
				Collections.sort(questionEntityList);
				LinkedList<Entity> result = new LinkedList<Entity>();
				int index = 1;
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
	
	//entityList 已经排过序了 
	public static Entity getEntity(LinkedList<Entity> entityList, Node node){
		for (Entity entity : entityList) {
			if(node.left <= entity.start && node.right >= entity.end){
				return entity;
			}
		}
		return null;
	}
	
	public static void stat(){
		Pipeline pipeline = new Pipeline();
		
		/*		String wikiPath = "./data/q-e/all-mark-wiki-entity.txt";
				String outPath = "./data/zch/newEntity.txt";
				entityExtraction(wikiPath,outPath,pipeline);
		*/		
				setEntity(pipeline);
		/*	
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
		*/		
				LinkedList<String> nullConstrainListQuestion = new LinkedList<String>();
				LinkedList<String> nullEntityQuestion = new LinkedList<String>();
				HashMap<Integer, Integer> statMap = new HashMap<Integer, Integer>();
				
				for(int id=1; id<=300; ++id){
					
					
					QuestionFrame qf = pipeline.xmlParser.getQuestionFrameWithPseudoId(id);
					LinkedList<Entity> entityList = qf.getEntityList();
					
					ConstraintSet constraintSet=ConstraintSet.getConstraintSet(qf.question, qf);
					Node answer = constraintSet.ans;
					List<Constraint> constraintList = constraintSet.list;
					
					if(!statMap.containsKey(constraintList.size())){
						statMap.put(constraintList.size(), 1);
					}else{
						statMap.put(constraintList.size(),statMap.get(constraintList.size())+1);
					}
					
					if(constraintList.size() == 0){
						nullConstrainListQuestion.add(qf.question);
//						continue;
					}
					
					if(entityList.size() == 0){
						nullEntityQuestion.add(qf.question);
//						continue;
					}
					
					if(entityList.size() > 0 && constraintList.size() > 0){
						//首先将匹配有实体的constraint封装 放入queue中
//						Queue<MatchDetail> queue = new LinkedList<MatchDetail>();
//						for (Constraint constraint : costraintList) {
//							Node left = constraint.left;
//							Node right = constraint.right;
							
							
							
//							if(!left.isx && !right.isx){
//								System.err.println("Both nodes in Constraint are Strings");
//								continue;
//							}
//							
//							if(!left.isx){
//								Entity e = getEntity(entityList, left);
//								if(e == null){
//									System.err.println("No matched entity in the left Node");
//								}else{
//									queue.add(new MatchDetail(e, left, constraint, 0));
//								}
//							}
//							
//							if(!right.isx){
//								Entity e = getEntity(entityList, right);
//								if(e == null){
//									System.err.println("No matched entity in the right Node");
//								}else{
//									queue.add(new MatchDetail(e, right, constraint, 1));
//								}
//							}
//						}
						
//						while(!queue.isEmpty()){
//							MatchDetail curMatchDetail = queue.poll();
//							
//						}
					}
				}
				System.out.println("null entity question Num: "+nullEntityQuestion.size());
//				System.out.println("null constrainList question Num: "+nullConstrainListQuestion.size());
				
				for (Integer size : statMap.keySet()) {
					System.out.println("ConstrainList size = "+size+" Num = "+statMap.get(size));
				}
	}
	
	
	//一步走
	public static void step(MatchDetail step){
		String entityUri = step.entity.uri;
		ArrayList<Predict> predictList = SimilarityFunction.getTopNPredicts(step);
	}
	
	//链式问题
	public static void pipe(MatchDetail pipe1, Constraint cs){
		ArrayList<Predict> predictList = SimilarityFunction.getTopNPredicts(pipe1);
	}
	
	//2对1的映射问题
	public static void map(MatchDetail step1, MatchDetail step2){
		
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Pipeline pipeline = new Pipeline();
		setEntity(pipeline);
		
		LinkedList<String> nullConstrainListQuestion = new LinkedList<String>();
		LinkedList<String> nullEntityQuestion = new LinkedList<String>();
		
		for(int id=1; id<=300; ++id){
			QuestionFrame qf = pipeline.xmlParser.getQuestionFrameWithPseudoId(id);
			LinkedList<Entity> entityList = qf.getEntityList();
			
			ConstraintSet constraintSet=ConstraintSet.getConstraintSet(qf.question, qf);
			Node answer = constraintSet.ans;
			List<Constraint> constraintList = constraintSet.list;
			
			if(constraintList.size() == 0){
				nullConstrainListQuestion.add(qf.question);
//				continue;
			}
			
			if(entityList.size() == 0){
				nullEntityQuestion.add(qf.question);
//				continue;
			}
			
			if(entityList.size() > 0 && constraintList.size() > 0){
				if(constraintList.size() == 1){
					Constraint constraint = constraintList.get(0);
					Entity e;
					Node left = constraint.left;
					Node right = constraint.right;
					int location;
					
					if(!left.isx && !right.isx){
						System.err.println(id + ": Both nodes in Constraint are Strings");
						continue;
					}
					
					if(!left.isx){
						e = getEntity(entityList, left);
						location = 0;
					}else{
						e = getEntity(entityList, right);
						location = 1;
					}
					if(e == null){
						System.err.println(id + ": No matched entity in the Node");
						System.out.println(constraint.getString());
						continue;
					}
					
					MatchDetail onestep = new MatchDetail(e, constraint, location);
					step(onestep);
					onestep.print();
				}
				
				if(constraintList.size() == 2){
					int eCount = 0;
					int cs1Location = -1;
					int cs2Location = -1;
					
					Constraint cs1 = constraintList.get(0);
					Constraint cs2 = constraintList.get(1);
					
					if(!cs1.left.isx){
						cs1Location = 0;
						++eCount;
					}
					if(!cs1.right.isx){
						cs1Location = 1;
						++eCount;
					}
					if(!cs2.left.isx){
						cs2Location = 0;
						++eCount;
					}
					if(!cs2.right.isx){
						cs2Location = 1;
						++eCount;
					}
					
					if(eCount == 1){
						Entity e;
						MatchDetail pipe1;
						if(cs1Location >= 0){
							if(cs1Location == 0){
								e = getEntity(entityList, cs1.left);
							}else{
								e = getEntity(entityList, cs1.right);
							}
							pipe1 = new MatchDetail(e, cs1, cs1Location);
							pipe(pipe1,cs2);
						}else{
							if(cs2Location == 0){
								e = getEntity(entityList, cs2.left);
							}else{
								e = getEntity(entityList, cs2.right);
							}
							pipe1 = new MatchDetail(e, cs2, cs2Location);
							pipe(pipe1, cs1);
						}
					}
					
					if(eCount == 2){
						MatchDetail step1,step2;
						Entity e;
						if(cs1Location == 0){
							e = getEntity(entityList, cs1.left);
						}else{
							e = getEntity(entityList, cs1.right);
						}
						step1 = new MatchDetail(e, cs1, cs1Location);
						
						if(cs2Location == 0){
							e = getEntity(entityList, cs2.left);
						}else{
							e = getEntity(entityList, cs2.right);
						}
						step2 = new MatchDetail(e, cs2, cs2Location);
						map(step1, step2);
					}
					
					if(eCount > 2){
						System.err.println(id + ": eCount > 2");
						continue;
					}
				}
				
				if(constraintList.size() > 2){
					System.err.println("constraintList size > 2");
				}
			}
		}
	}
}
