package entityLinking.client;

import java.util.HashMap;

public class entityClientConst {
	public static enum TOOLKIT {MINER,DEXTER,SPOTLIGHT1,SPOTLIGHT2};
	public final static String miner = "http://wikipedia-miner.cms.waikato.ac.nz/services/wikify?text=";
	public final static String dexter = "http://dexterdemo.isti.cnr.it:8080/dexter-webapp/api/rest/spot-entities?source=";
	public final static String spotlight1 = "http://spotlight.dbpedia.org/rest/candidates?source=";
	public final static String spotlight2 = "http://spotlight.sztaki.hu:2222/rest/candidates?source=";
	
	public final static String spotlightParas = "&confidence=0&support=0";
	public final static String minerParas = "&responseFormat=JSON&sourceMode=HTML&references=true&minProbability=0&disambiguationPolicy=loose";
	public final static String dexterParas = "&wn=false&debug=false&format=text";
//	public final static String tagmeParas = "&key=abAnBGgAqA2015";

	public final static String dexterURI2ID = "http://dexterdemo.isti.cnr.it:8080/dexter-webapp/api/rest/get-desc?id=";
	
	static{
		
	}
}