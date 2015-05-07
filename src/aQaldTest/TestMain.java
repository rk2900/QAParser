package aQaldTest;

import baseline.Entity;
import paser.QuestionFrame;
import knowledgebase.ClientManagement;
import entityLinking.client.entityClientConst.TOOLKIT;
import entityLinking.parse.responseParser;
import finder.Pipeline;
import finder.Pipeline.DataSource;

public class TestMain {

	public static void main(String[] args) {
		Pipeline pipeline = new Pipeline(DataSource.TRAIN);
		responseParser parser = new responseParser();
		
		for(int i=1; i<=pipeline.totalNumber; i++) {
			QuestionFrame qf = pipeline.xmlParser.getQuestionFrameWithPseudoId(i);
			System.out.println(qf.wordList);
			System.out.println(qf.question);
			parser.setEntityList(qf, TOOLKIT.MINERDIS);
			
			
//			break;
		}
	}
}
