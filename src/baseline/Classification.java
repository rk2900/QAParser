package baseline;

import java.util.LinkedList;
import java.util.List;

import paser.QuestionFrame;
import syntacticParser.Constraint;
import syntacticParser.ConstraintSet;
import syntacticParser.Node;
import tool.OutputRedirector;
import knowledgebase.ClientManagement;
import finder.Pipeline;

public class Classification {
	public static enum CLASSIFICATION {NORMAL, DATE, WHERE, WHO, NUMBER, RESOURCE};
	
	public static Answer getAnswer(QuestionFrame qf){
		return getAnswer(qf,CLASSIFICATION.NORMAL);
	}
	
	public static Answer getAnswer(QuestionFrame qf, CLASSIFICATION type){
		Answer answer = new Answer();
		answer.qf = qf;
		LinkedList<Entity> entityList = qf.getEntityList();
		ConstraintSet constraintSet=ConstraintSet.getConstraintSet(qf.question, qf);
		List<Constraint> constraintList = constraintSet.list;
		String focusString = qf.getFocusStringForPredicate();
		switch (type) {
		case DATE:
			focusString = "date";
			break;
		default:
			break;
		}
		
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
					e = Main.getEntity(entityList, left);
					location = 0;
				}else{
					e = Main.getEntity(entityList, right);
					location = 1;
				}
				if(e == null){
					exceptionString += "No matched entity in the Node.\n";
				}else{
					MatchDetail onestep = new MatchDetail(e, constraint, location,focusString);
					Main.stepAnswer(onestep, answer,type);
				}
			}
			
			if(constraintList.size() == 2){
//				exceptionString += "constraintList size equals 2\n";
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
					MatchDetail pipe;
					if(cs1Location >= 0){//cs1存在已知情况
						if(cs1Location == 0){
							e = Main.getEntity(entityList, cs1.left);
						}else{
							e = Main.getEntity(entityList, cs1.right);
						}
					}else{
						if(cs2Location == 0){
							e = Main.getEntity(entityList, cs2.left);
						}else{
							e = Main.getEntity(entityList, cs2.right);
						}
					}
					if(e == null){
						exceptionString = qf.id + " constraintList size equals 2 && pipeline style &&No matched entity";
					}else{
						if(cs1Location >= 0){
							pipe = new MatchDetail(e, cs1, cs1Location,focusString);
							Main.pipe(pipe, cs2, answer, type);
						}else{
							pipe = new MatchDetail(e, cs2, cs2Location, focusString);
							Main.pipe(pipe, cs1, answer, type);
						}
						exceptionString += answer.exceptionString;
					}
				}
				
				if(eCount == 2){
					exceptionString = qf.id + " constraintList size equals 2 && map style && No matched entity";
//					MatchDetail step1,step2;
//					Entity e;
//					if(cs1Location == 0){
//						e = getEntity(entityList, cs1.left);
//					}else{
//						e = getEntity(entityList, cs1.right);
//					}
//					step1 = new MatchDetail(e, cs1, cs1Location);
//					
//					if(cs2Location == 0){
//						e = getEntity(entityList, cs2.left);
//					}else{
//						e = getEntity(entityList, cs2.right);
//					}
//					step2 = new MatchDetail(e, cs2, cs2Location);
//					map(step1, step2);
				}
				
				if(eCount > 2){
					exceptionString = qf.id + " constraintList size equals 2 && entity Num > 2 && No matched entity";
				}
			}
			if(constraintList.size() > 2){
				exceptionString += "constraintList size > 2\n";
			}
		}
		answer.exceptionString = exceptionString;
		return answer;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Pipeline pipeline = new Pipeline();
		Main.setEntity(pipeline);
		
		try {
			ClientManagement.getAgModel();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
//		OutputRedirector.openFileOutput("./data/zch_classification/date-addFocus-newDivision-restriction.txt");
//		LinkedList<QuestionFrame> whenQF = pipeline.date;
//		System.err.println(whenQF.size());
//		for (QuestionFrame qf : whenQF) {
//			Answer answer = getAnswer(qf,map.get("date"));
//			System.out.println(answer.print().toString());
//		}
//		OutputRedirector.closeFileOutput();
		
//		OutputRedirector.openFileOutput("./data/zch_classification/where/where-basic.txt");
//		LinkedList<QuestionFrame> whereQF = pipeline.where;
//		System.err.println(whereQF.size());
//		for (QuestionFrame qf : whereQF) {
//			Answer answer = getAnswer(qf,map.get("where"));
//			System.out.println(answer.print().toString());
//		}
//		OutputRedirector.closeFileOutput();
		
//		OutputRedirector.openFileOutput("./data/zch_classification/who/who-basic.txt");
//		LinkedList<QuestionFrame> whoQF = pipeline.who;
//		System.err.println(whoQF.size());
//		for (QuestionFrame qf : whoQF) {
//			Answer answer = getAnswer(qf,map.get("who"));
//			System.out.println(answer.print().toString());
//		}
//		OutputRedirector.closeFileOutput();
		
		OutputRedirector.openFileOutput("./data/zch_classification/resource/resource-basic-addTypeScore-1-0-5.txt");
		LinkedList<QuestionFrame> resourceQF = pipeline.resource;
		System.err.println(resourceQF.size());
		int originalNum = SimilarityFunction.predictNum;
		int count = 0;
		SimilarityFunction.predictNum = 10;
		for (QuestionFrame qf : resourceQF) {
			++count;
			Answer answer = getAnswer(qf,CLASSIFICATION.RESOURCE);
			if(!answer.isException()){
				System.out.println(answer.print().toString());
			}
			System.err.println(count + " finished.*****" + qf.id);
//			if(count == 5){
//				break;
//			}
		}
		SimilarityFunction.predictNum = originalNum;
		OutputRedirector.closeFileOutput();
		
//		OutputRedirector.openFileOutput("./data/zch_classification/number/number-basic-result.txt");
//		LinkedList<QuestionFrame> numberQF = pipeline.number;
//		System.err.println(numberQF.size());
//		for (QuestionFrame qf : numberQF) {
//			Answer answer = getAnswer(qf,map.get("number"));
//			System.out.println("Original focus: "+qf.focus.getFocusContent(qf.wordList));
//			System.out.println(answer.numberPrint().toString());
//		}
//		OutputRedirector.closeFileOutput();
	}
	
}
