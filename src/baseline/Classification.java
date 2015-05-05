package baseline;

import java.util.HashMap;
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
	public static HashMap<String, Integer> map;
	static{
		map = new HashMap<String, Integer>();
		map.put("normal", 0);
		map.put("date", 1);
		map.put("where", 2);
		map.put("who", 3);
		map.put("number", 4);
		map.put("resource", 5);
	}
	
	public static Answer getAnswer(QuestionFrame qf){
		return getAnswer(qf,0);
	}
	public static Answer getAnswer(QuestionFrame qf,int type){
		Answer answer = new Answer();
		LinkedList<Entity> entityList = qf.getEntityList();
		ConstraintSet constraintSet=ConstraintSet.getConstraintSet(qf.question, qf);
		List<Constraint> constraintList = constraintSet.list;
		String focusString = qf.getFocusStringForPredicate();
//		focusString = "date";
		if(type == 1){
			focusString = "date";
		}
		if(type == 2){
//			focusString = "place";
		}
		if(type == 3){
			
		}
		if(type == 4){
//			if(focusString.length() > 0){
//				focusString += " number total";
//			}
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
		
//		OutputRedirector.openFileOutput("./data/zch_classification/resource/resource-basic.txt");
//		LinkedList<QuestionFrame> resourceQF = pipeline.resource;
//		System.err.println(resourceQF.size());
//		for (QuestionFrame qf : resourceQF) {
//			Answer answer = getAnswer(qf,map.get("resource"));
//			System.out.println(answer.print().toString());
//		}
//		OutputRedirector.closeFileOutput();
		
//		OutputRedirector.openFileOutput("./data/zch_classification/number/number-addFocus-number-total.txt");
		LinkedList<QuestionFrame> numberQF = pipeline.number;
		System.err.println(numberQF.size());
		for (QuestionFrame qf : numberQF) {
			Answer answer = getAnswer(qf,map.get("number"));
//			System.out.println("Original focus: "+qf.focus.getFocusContent(qf.wordList));
//			System.out.println(answer.print().toString());
			System.out.println(answer.numberPrint().toString());
			
		}
//		OutputRedirector.closeFileOutput();
	}
	
}
