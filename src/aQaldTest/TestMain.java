package aQaldTest;

import baseline.Answer;
import baseline.Classification;
import baseline.Entity;
import baseline.Classification.CLASSIFICATION;
import paser.QuestionFrame;
import entityLinking.client.entityClientConst.TOOLKIT;
import entityLinking.parse.responseParser;
import finder.Pipeline;
import finder.Pipeline.DataSource;

public class TestMain {

	public static void showSingleQF(Pipeline pipeline, responseParser parser, int qid){
		QuestionFrame qf = pipeline.xmlParser.getQuestionFrameWithId(qid);
		parser.setEntityList(qf, TOOLKIT.MINERDIS);
		
		Answer answer = Classification.getAnswer(qf,CLASSIFICATION.RESOURCE);
		System.out.println(answer.print());
		System.err.println("******************************************");
		for (Entity entity : qf.entityList) {
			entity.print();
		}
		System.out.println(syntacticParser.Main.get(qf,qf.question));
		System.out.println(qf.answers);
	}
	public static void main(String[] args) {
		Pipeline pipeline = new Pipeline(DataSource.TRAIN);
		responseParser parser = new responseParser();
		
		showSingleQF(pipeline, parser, 181);
//		OutputRedirector.openFileOutput("./data/api_classification/normal-type-pipe.txt");
//		for(int i=1; i<=pipeline.totalNumber; i++) {
//			
//			QuestionFrame qf = pipeline.xmlParser.getQuestionFrameWithPseudoId(i);
//			parser.setEntityList(qf, TOOLKIT.MINERDIS);
//			
//			Answer answer = Classification.getAnswer(qf,0);
//			System.out.println(answer.print());
////			break;
//			System.err.println(qf.id + ": Finished.");
//		}
//		OutputRedirector.closeFileOutput();
	}
}
