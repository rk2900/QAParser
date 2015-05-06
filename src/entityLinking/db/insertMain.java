package entityLinking.db;

import paser.QuestionFrame;
import entityLinking.client.entityClient;
import entityLinking.client.entityClientConst;
import entityLinking.parse.responseParser;
import finder.Pipeline;
import finder.Pipeline.DataSource;

public class insertMain {
	public final static String trainDBName = "entity";
	public final static String testDBName = "entity_test";
	
	public static void insertMinerToDB(Pipeline pipeline,responseParser parser){
		for (int i=1; i<=pipeline.totalNumber; ++i) {
			QuestionFrame qf = pipeline.xmlParser.getQuestionFrameWithPseudoId(i);
			int id = qf.id;
			String content = qf.question;
			String response = entityClient.queryAPI(content, entityClientConst.TOOLKIT.MINER);
			if(response != null){
				parser.loadMiner(id, response);
				System.err.println(i + " " + id + " finished");
			}else{
				System.err.println(i + " " + id + " null response");
			}
		}
	}
	
	public static void insertDexterToDB(Pipeline pipeline,responseParser parser){
		for (int i=1; i<=3; ++i) {
			QuestionFrame qf = pipeline.xmlParser.getQuestionFrameWithPseudoId(i);
			int id = qf.id;
			String content = qf.question;
			String response = entityClient.queryAPI(content, entityClientConst.TOOLKIT.DEXTER);
//			System.out.println(response);
			if(response != null){
				parser.loadDexter(id, response);
				System.err.println(i + " " + id + " finished");
			}else{
				System.err.println(i + " " + id + " null response");
			}
		}
	}
	
	public static void insertSpotlight1ToDB(Pipeline pipeline,responseParser parser){
		for (int i=1; i<=pipeline.totalNumber; ++i) {
			QuestionFrame qf = pipeline.xmlParser.getQuestionFrameWithPseudoId(i);
			int id = qf.id;
			String content = qf.question;
			String response = entityClient.queryAPI(content, entityClientConst.TOOLKIT.SPOTLIGHT1);
//			System.out.println(response);
			if(response != null){
				parser.loadSpotlight1(id, response);
				System.err.println(i + " " + id + " finished");
			}else{
				System.err.println(i + " " + id + " null response");
			}
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Pipeline pipeline = new Pipeline(DataSource.TEST);
		responseParser parser = new responseParser(testDBName);
		insertDexterToDB(pipeline, parser);
	}

}
