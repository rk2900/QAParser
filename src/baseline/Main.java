package baseline;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import knowledgebase.ClientManagement;

import com.hp.hpl.jena.rdf.model.RDFNode;

import paser.FocusConstraint;
import paser.QuestionFrame;
import syntacticParser.Constraint;
import syntacticParser.ConstraintSet;
import syntacticParser.Node;
import tool.OutputRedirector;
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
		
		for(int i=1; i<=pipeline.totalNumber; ++i){
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
	
	
	//一步走
	public static void step(MatchDetail step){
		String entityUri = step.entity.uri;
		ArrayList<Predicate> predictList = SimilarityFunction.getTopNPredicts(step);
		System.out.println(entityUri);
		for (Predicate predict : predictList) {
			System.out.print(predict.maxScore + "\t"+ predict.matchedLabel +"\t" + predict.uri);
			LinkedList<RDFNode> resources = ClientManagement.getNode(entityUri, predict.uri);
			for (RDFNode rdfNode : resources) {
				System.out.print("\t" + rdfNode.toString());
			}
			System.out.println();
		}
		System.out.println();
	}
	
	public static void stepAnswer(MatchDetail step, Answer answer){
		stepAnswer(step, answer,0);
	}
	
	public static void stepAnswer(MatchDetail step, Answer answer, int type){
		answer.entityUri = step.entity.uri;
		answer.predictList = SimilarityFunction.getTopNPredicts(step,type);
		for (Predicate predicate : answer.predictList) {
			LinkedList<RDFNode> resources = ClientManagement.getNode(answer.entityUri, predicate.uri);
			answer.resources.put(predicate,resources);
		}
		if(type == 5){
			answer.typeConstrainScore = FocusConstraint.getPredicateTypeConstraintScore(answer);
			ArrayList<Predicate> newPredicates = new ArrayList<Predicate>();
			for (Predicate predicate : answer.predictList) {
				double typeScore = answer.typeConstrainScore.get(predicate);
				if(typeScore > 0){
					predicate.maxScore += typeScore;
					newPredicates.add(predicate);
				}
			}
			answer.predictList = newPredicates;
		}
	}
	
	//链式问题
	public static void pipe(MatchDetail pipe, Constraint cs, Answer answer, int type){
		MatchDetail secondMD = new MatchDetail();
		secondMD.constraint = cs;
		secondMD.focusString = pipe.focusString;
		if(pipe.location == 1){
			if(pipe.constraint.left.equals(cs.left)){
				secondMD.location = 0;
			}else{
				secondMD.location = 1;
			}
		}else{
			if(pipe.constraint.right.equals(cs.left)){
				secondMD.location = 0;
			}else{
				secondMD.location = 1;
			}
		}
		
		Answer fisrtAnswer = new Answer();
		fisrtAnswer.qf = answer.qf;
		pipe.focusString = "";
		stepAnswer(pipe, fisrtAnswer, 0);
		
		if(fisrtAnswer.isException()){
			answer.exceptionString = "pipe style, step 1 error, " + fisrtAnswer.exceptionString;
		}else{
			LinkedList<RDFNode> step1Result = fisrtAnswer.resources.get(fisrtAnswer.predictList.get(0));
			if(step1Result.size() != 1){
				answer.exceptionString = "pipe style, step 1 error, resources size equals: " + step1Result.size();
			}else{
				secondMD.entity = new Entity(step1Result.get(0).toString());
				stepAnswer(secondMD, answer, type);
			}
		}
	}
	
	
	//2对1的映射问题
	public static void map(MatchDetail step1, MatchDetail step2){
		
	}
	
	//在此之间你应该已经 set entityList
	public static Answer getAnswer(Pipeline pipeline,int id){
		Answer answer = new Answer();
		QuestionFrame qf = pipeline.xmlParser.getQuestionFrameWithPseudoId(id);
		LinkedList<Entity> entityList = qf.getEntityList();
		ConstraintSet constraintSet=ConstraintSet.getConstraintSet(qf.question, qf);
		List<Constraint> constraintList = constraintSet.list;
		String focusString = qf.getFocusStringForPredicate();
		
		String exceptionString="";
		if(constraintList.size() == 0){
			exceptionString += "ConstraintList size equals 0.\n";
		}
		
		if(entityList.size() == 0){
			exceptionString += "entityList size equals 0.\n";
		}
		
		if(entityList.size() > 0 && constraintList.size() > 0){
			if(constraintList.size() == 1){
				Constraint constraint = constraintList.get(0);
				Entity e;
				Node left = constraint.left;
				Node right = constraint.right;
				int location;
				
				if(!left.isx && !right.isx){
					exceptionString += "Both nodes in Constraint are Strings.\n";
				}
				
				if(!left.isx){
					e = getEntity(entityList, left);
					location = 0;
				}else{
					e = getEntity(entityList, right);
					location = 1;
				}
				if(e == null){
					exceptionString += "No matched entity in the Node.\n";
				}else{
					MatchDetail onestep = new MatchDetail(e, constraint, location,focusString);
					stepAnswer(onestep, answer);
				}
			}
			
			if(constraintList.size() == 2){
				exceptionString += "constraintList size equals 2\n";
//				int eCount = 0;
//				int cs1Location = -1;
//				int cs2Location = -1;
//				
//				Constraint cs1 = constraintList.get(0);
//				Constraint cs2 = constraintList.get(1);
//				
//				if(!cs1.left.isx){
//					cs1Location = 0;
//					++eCount;
//				}
//				if(!cs1.right.isx){
//					cs1Location = 1;
//					++eCount;
//				}
//				if(!cs2.left.isx){
//					cs2Location = 0;
//					++eCount;
//				}
//				if(!cs2.right.isx){
//					cs2Location = 1;
//					++eCount;
//				}
//				
//				if(eCount == 1){
//					Entity e;
//					MatchDetail pipe1;
//					if(cs1Location >= 0){
//						if(cs1Location == 0){
//							e = getEntity(entityList, cs1.left);
//						}else{
//							e = getEntity(entityList, cs1.right);
//						}
//						pipe1 = new MatchDetail(e, cs1, cs1Location,focusString);
////						pipe(pipe1, cs2, answer);
//					}else{
//						if(cs2Location == 0){
//							e = getEntity(entityList, cs2.left);
//						}else{
//							e = getEntity(entityList, cs2.right);
//						}
//						pipe1 = new MatchDetail(e, cs2, cs2Location,focusString);
//						pipe(pipe1, cs1, answer);
//					}
//				}
//				
//				if(eCount == 2){
//					MatchDetail step1,step2;
//					Entity e;
//					if(cs1Location == 0){
//						e = getEntity(entityList, cs1.left);
//					}else{
//						e = getEntity(entityList, cs1.right);
//					}
//					step1 = new MatchDetail(e, cs1, cs1Location,focusString);
//					
//					if(cs2Location == 0){
//						e = getEntity(entityList, cs2.left);
//					}else{
//						e = getEntity(entityList, cs2.right);
//					}
//					step2 = new MatchDetail(e, cs2, cs2Location,focusString);
//					map(step1, step2);
//				}
			}
			
			if(constraintList.size() > 2){
				exceptionString += "constraintList size > 2\n";
			}
		}
		answer.exceptionString = exceptionString;
		answer.qf = qf;
		return answer;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Pipeline pipeline = new Pipeline();
		setEntity(pipeline);
		try {
			ClientManagement.getAgModel();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		OutputRedirector.openFileOutput("./data/zch_classfication/When.txt");
	}
}
