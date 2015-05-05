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

	public static Answer getAnswer(QuestionFrame qf){
		Answer answer = new Answer();
		LinkedList<Entity> entityList = qf.getEntityList();
		ConstraintSet constraintSet=ConstraintSet.getConstraintSet(qf.question, qf);
		List<Constraint> constraintList = constraintSet.list;
		String focusString = qf.getFocusStringForPredicate();
//		focusString = "date";
		
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
					Main.stepAnswer(onestep, answer);
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
		
		OutputRedirector.openFileOutput("./data/zch_classification/date-addFocus.txt");
		LinkedList<QuestionFrame> whenQF = pipeline.date;
		System.err.println(whenQF.size());
		for (QuestionFrame qf : whenQF) {
			Answer answer = getAnswer(qf);
			System.out.println(answer.print().toString());
		}
		
		OutputRedirector.closeFileOutput();
	}

}
