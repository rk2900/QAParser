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
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Pipeline pipeline = new Pipeline(DataSource.TEST);
		responseParser parser = new responseParser("entity_test");
		for (int i=1; i<pipeline.totalNumber; ++i) {
			QuestionFrame qf = pipeline.xmlParser.getQuestionFrameWithPseudoId(i);
			int id = qf.id;
			String content = qf.question;
			String response = entityClient.queryAPI(content, entityClientConst.TOOLKIT.MINER);
			parser.loadMiner(id, response);
			
//			System.out.println(entityClient.queryAPI(test, entityClientConst.TOOLKIT.MINER));
//			System.out.println(entityClient.queryAPI(test, entityClientConst.TOOLKIT.DEXTER));
//			System.out.println(entityClient.queryAPI(test, entityClientConst.TOOLKIT.SPOTLIGHT1));
//			System.out.println(entityClient.queryAPI(test, entityClientConst.TOOLKIT.SPOTLIGHT2));
			break;
		}
	}

}
