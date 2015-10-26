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
		
		Answer answer = Classification.getAnswer(qf,CLASSIFICATION.WHO);
//		System.out.println(answer.answerType);
		System.out.println(answer.entityUri);
		if(answer.predictList != null && answer.predictList.size() > 0){
			System.out.println(answer.predictList.get(0).getUri() + "\t" + answer.predictList.get(0).getMaxScore());
			System.out.println(answer.resources.get(answer.predictList.get(0)));
		}
		if(answer.pairPredicates != null && answer.pairPredicates.size() > 0){
			System.out.println(answer.pairPredicates.get(0).Predicate1.getUri() + "\t" + answer.pairPredicates.get(0).Predicate2.getUri() + "\t" + answer.pairPredicates.getFirst().score);
			System.out.println(answer.pairResources.get(answer.pairPredicates.getFirst()));
		}
		System.err.println("******************************************");
//		for (Entity entity : qf.entityList) {
//			entity.print();
//		}
		System.out.println(syntacticParser.Main.get(qf,qf.question));
//		System.out.println(qf.answers);
	}
	public static void main(String[] args) {
		Pipeline pipeline = new Pipeline(DataSource.TEST);
		responseParser parser = new responseParser();
		
		showSingleQF(pipeline, parser, 96);
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
