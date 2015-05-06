package aQaldTest;

import paser.QuestionFrame;
import knowledgebase.ClientManagement;
import finder.Pipeline;
import finder.Pipeline.DataSource;

public class TestMain {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Pipeline pipeline = new Pipeline(DataSource.TEST);
		for(int i=1; i<=pipeline.totalNumber; i++) {
			QuestionFrame qf = pipeline.xmlParser.getQuestionFrameWithPseudoId(i);
			qf.print();
		}
//		baseline.Main.setEntity(pipeline);
//		try {
//			ClientManagement.getAgModel();
//		} catch (Exception e1) {
//			e1.printStackTrace();
//		}
		
		
	}
}
