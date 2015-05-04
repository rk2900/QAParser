package type;

import java.util.LinkedList;

import baseline.Entity;
import baseline.Main;
import paser.Focus;
import paser.QuestionFrame;
import pattern.QuestionClassifier;
import pattern.QuestionClassifier.Category;
import pattern.QuestionClassifier.Label;
import finder.Pipeline;

public class TypeConstraint {

	public static Pipeline pipeline;
	
	static {
		pipeline = new Pipeline();
		Main.setEntity(pipeline);
	}
	
	public static void main(String[] args) {
		for(int id=1; id<=300; id++) {
			System.out.print(id+"\t");
			QuestionFrame qf = pipeline.xmlParser.getQuestionFrameWithId(id);
			QuestionClassifier qc = qf.questionClassifier;
			System.out.print(qc.category+"\t");
			System.out.println(qc.label);
			if(qc.category == Category.RESOURCE && !qc.label.get(Label.COMPARISON)) {
				LinkedList<Entity> entities = qf.getEntityList();
				Focus focus = qf.focus;
				System.out.println(focus.getFocusContent(qf.wordList));
				System.out.println(entities);
				for (Entity entity : entities) {
					if(focus.getLeftIndex() <= entity.getStart() && focus.getRightIndex() >= entity.getEnd()) {
						// entity in focus
						System.out.println(id);
					}
				}
			}
			System.out.println("-------------------------------------");
		}
		
	}

}
