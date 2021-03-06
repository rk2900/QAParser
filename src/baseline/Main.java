package baseline;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import knowledgebase.ClientManagement;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;

import paser.FocusConstraint;
import paser.QuestionFrame;
import syntacticParser.Constraint;
import syntacticParser.ConstraintSet;
import syntacticParser.Node;
import finder.Pipeline;
import baseline.Classification.CLASSIFICATION;
import basic.FileOps;

public class Main {
	public static double threshold = 0.5;
	public static double pairThreshold = 0.8;
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
//		for (Entity entity : entityList) {
//			if(node.left <= entity.start && node.right >= entity.end){
//				return entity;
//			}
//		}
//		return null;
		//===========================================================
		Entity e = null;
		for (Entity entity : entityList) {
			if(node.left <= entity.start && node.right >= entity.end){
				if(e == null){
					e = entity;
				}else{
					if(entity.start > e.end){
						e = entity;
					}
				}
			}
		}
		return e;
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
		stepAnswer(step, answer,CLASSIFICATION.NORMAL);
	}
	
	public static void stepAnswer(MatchDetail step, Answer answer, CLASSIFICATION type){
		answer.entityUri = step.entity.uri;
		answer.predictList = SimilarityFunction.getTopNPredicts(step,type);
		for (Predicate predicate : answer.predictList) {
			LinkedList<RDFNode> resources = ClientManagement.getNode(answer.entityUri, predicate.uri);
			answer.resources.put(predicate,resources);
		}
		addTypeConstraint(answer, type);
	}
	
	public static Answer dealWithNumberConstraint(Answer answer){
		LinkedList<RDFNode> res;
		boolean isOneStep = false;
		RDFNode oneStep = null;
		Predicate p = null;
		PairPredicate pa = null;
		int i;
		if(answer.answerType == 0){
			for(i=0; i<answer.predictList.size() && i<SimilarityFunction.predictNum; ++i){
				p = answer.predictList.get(i);
				res = answer.resources.get(p);
				if(res.size() > 1){
					continue;
				}
				
				RDFNode resource = res.get(0);
				if(resource.isLiteral()){
					Literal literal = resource.asLiteral();
					String resourceString = literal.getLexicalForm();
					int t;
					for(t=0; t<resourceString.length(); ++t){
						if(!Character.isDigit(resourceString.charAt(t)) && resourceString.charAt(t) != '.'){
							break;
						}
					}
					if(t == resourceString.length()){
						isOneStep = true;
						oneStep = resource;
						break;
					}
				}
			}
			if(isOneStep){
				answer.predictList = new ArrayList<Predicate>();
				answer.predictList.add(p);
			}else{
				if(i == answer.predictList.size() || i == SimilarityFunction.predictNum){
//					answer.predictList = new ArrayList<Predicate>();
					//===============================================================
					if(answer.predictList.get(0).getMaxScore() > threshold){
						p = answer.predictList.get(0);
						answer.predictList.clear();
						answer.predictList.add(p);
					}
				}
			}
		}
		
		if(answer.answerType == 1){
//			for (i=0; i<answer.pairPredicates.size() && i<SimilarityFunction.predictNum; ++i) {
			for (i=0; i<answer.pairPredicates.size() && i<25; ++i) {
				pa = answer.pairPredicates.get(i);
				res = answer.pairResources.get(pa);
				if(res.size() > 1){
					continue;
				}
				
				RDFNode resource = res.get(0);
				if(resource.isLiteral()){
					Literal literal = resource.asLiteral();
					String resourceString = literal.getLexicalForm();
					int t;
					for(t=0; t<resourceString.length(); ++t){
						if(!Character.isDigit(resourceString.charAt(t)) && resourceString.charAt(t) != '.'){
							break;
						}
					}
					if(t == resourceString.length()){
						isOneStep = true;
						oneStep = resource;
						break;
					}
				}
			}
			if(isOneStep){
				answer.pairPredicates = new LinkedList<PairPredicate>();
				answer.pairPredicates.add(pa);
			}else{
				if(i == answer.pairPredicates.size() || i == SimilarityFunction.predictNum){
//					answer.pairPredicates = new LinkedList<PairPredicate>();
					if(answer.pairPredicates.get(0).score > pairThreshold){
						pa = answer.pairPredicates.get(0);
						answer.pairPredicates.clear();
						answer.pairPredicates.add(pa);
					}
				}
			}
		}
		return answer;
	}
	
	public static void addTypeConstraint(Answer answer, CLASSIFICATION type){
		switch (type) {
		case NUMBER:
			dealWithNumberConstraint(answer);
			break;
		case RESOURCE:
			switch (answer.answerType) {
			case 0:
				answer.typeConstrainScore = FocusConstraint.getPredicateTypeConstraintScore(answer);
				ArrayList<Predicate> newPredicates = new ArrayList<Predicate>();
				for (Predicate predicate : answer.predictList) {
					double typeScore = answer.typeConstrainScore.get(predicate);
					if(typeScore > 0 || predicate.maxScore > SimilarityFunction.threshold){
						predicate.maxScore += typeScore;
						newPredicates.add(predicate);
					}
				}
				Collections.sort(newPredicates,Collections.reverseOrder());
				answer.predictList = newPredicates;
				break;
			case 1:
				answer.pairTypeConstraintScore = FocusConstraint.getPairPredicateTypeConstraintScore(answer);
				LinkedList<PairPredicate> pairPredicates = new LinkedList<PairPredicate>();
				for (PairPredicate pair : answer.pairPredicates) {
					double typeScore = answer.pairTypeConstraintScore.get(pair);
					if(typeScore > 0 || pair.Predicate2.maxScore > SimilarityFunction.threshold){
						pair.score = (pair.Predicate1.maxScore+pair.Predicate2.maxScore) / 2 + typeScore;
						pairPredicates.add(pair);
					}
				}
				Collections.sort(pairPredicates,Collections.reverseOrder());
				answer.pairPredicates = pairPredicates;
				break;
			case 2:
				answer.pairTypeConstraintScore = FocusConstraint.getPairPredicateTypeConstraintScore(answer);
				LinkedList<PairPredicate> pairP = new LinkedList<PairPredicate>();
				for (PairPredicate pair : answer.pairPredicates) {
					double typeScore = answer.pairTypeConstraintScore.get(pair);
					if(typeScore > 0 || (pair.Predicate1.maxScore + pair.Predicate2.maxScore)/2 > SimilarityFunction.threshold){
						pair.score = (pair.Predicate1.maxScore+pair.Predicate2.maxScore) / 2 + typeScore;
						pairP.add(pair);
					}
				}
				Collections.sort(pairP, new PairPredicate());
				answer.pairPredicates = pairP;
				break;
			default:
				break;
			}
			break;
		default:
			break;
		}
	}
	//链式问题
	public static void pipe(MatchDetail pipe, Constraint cs, Answer answer, CLASSIFICATION type){
		answer.entityUri = pipe.entity.uri;
		
		String focusString = pipe.focusString;
		String NL = cs.edge;
		
		pipe.focusString = "";
		Answer firstAnswer = new Answer();
		firstAnswer.initial(0);
		firstAnswer.entityUri = answer.entityUri;
		firstAnswer.qf = answer.qf;
		stepAnswer(pipe, firstAnswer, CLASSIFICATION.NORMAL);
		
		if(firstAnswer.isException()){
			answer.exceptionString = "pipe style, step 1 error, " + firstAnswer.exceptionString;
		}else{
//			System.out.println(firstAnswer.print());
			if(firstAnswer.predictList.size() == 0){
				answer.exceptionString = "pipe style, step 1 error, predicateList size equals: 0";
			}else{
				LinkedList<RDFNode> step1Result = firstAnswer.resources.get(firstAnswer.predictList.get(0));
				if(step1Result.size() == 0){
					answer.exceptionString = "pipe style, step 1 error, resources size equals: 0";
				}else{
					HashMap<Predicate, HashSet<String>> pipePredicates = ClientManagement.getPredicatePipe(answer.entityUri, firstAnswer.predictList, type);
//					for (Predicate p : pipePredicates.keySet()) {
//						System.out.println(p.getUri());
//						System.out.println(pipePredicates.get(p));
//						System.out.println("===========");
//					}
					HashMap<String, Predicate> p2Map = new HashMap<String, Predicate>();
					for (Predicate p1 : pipePredicates.keySet()) {
						for (String p2String : pipePredicates.get(p1)) {
							if(!p2Map.containsKey(p2String)){
								Predicate p2 = SimilarityFunction.getPredicate(p2String, NL, focusString);
								p2Map.put(p2String, p2);
							} 
							if(p2Map.get(p2String).maxScore > SimilarityFunction.minSimilarityScore){
								PairPredicate pairP = new PairPredicate(p1,p2Map.get(p2String));
								answer.pairPredicates.add(pairP);
							}
						}
					}
					Collections.sort(answer.pairPredicates, Collections.reverseOrder());
					
					for (PairPredicate pairP : answer.pairPredicates) {
						LinkedList<RDFNode> res = ClientManagement.getPipeNode(answer.entityUri, pairP.Predicate1.uri, pairP.Predicate2.uri);
						answer.pairResources.put(pairP, res);
						
						//=============================
//						System.out.println(pairP.Predicate1.getUri()+"\t"+pairP.Predicate2.getUri()+"\t"+pairP.score);
					}
					addTypeConstraint(answer, type);
				}
			}
		}
	}
	
	//2对1的映射问题
	public static void map(Answer answer, MatchDetail step1, MatchDetail step2, CLASSIFICATION type){
		String eUri1 = step1.entity.uri;
		String eUri2 = step2.entity.uri;
		
		answer.entityUri = eUri1 + "\t" + eUri2;
		
		HashMap<String, HashSet<String>> pairStringMap = ClientManagement.getPredicateCross(eUri1, eUri2, type);
		HashMap<String, Predicate> p2Map = new HashMap<String, Predicate>();
		
		for (String s1 : pairStringMap.keySet()) {
			Predicate p1 = SimilarityFunction.getPredicate(s1, step1.constraint.edge, step1.focusString);
			for (String s2 : pairStringMap.get(s1)) {
				if(!p2Map.containsKey(s2)){
					Predicate p2 = SimilarityFunction.getPredicate(s2, step2.constraint.edge, step2.focusString);
					p2Map.put(s2, p2);
				}
				if(p1.maxScore > SimilarityFunction.minSimilarityScore && p2Map.get(s2).maxScore > SimilarityFunction.minSimilarityScore){
					answer.pairPredicates.add(new PairPredicate(p1,p2Map.get(s2)));
				}
			}
		}
		Collections.sort(answer.pairPredicates, new PairPredicate());
		for (PairPredicate pairP : answer.pairPredicates) {
			LinkedList<RDFNode> res = ClientManagement.getCrossNode(eUri1, pairP.Predicate1.uri, eUri2, pairP.Predicate2.uri);
			answer.pairResources.put(pairP, res);
		}
		addTypeConstraint(answer, type);
		
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
	}
}
