package entityLinking.client;

import java.util.HashMap;

public class entityClientConst {
	public static enum TOOLKIT {MINER,DEXTER,SPOTLIGHT1,SPOTLIGHT2};
	public static String miner = "http://wikipedia-miner.cms.waikato.ac.nz/services/wikify";
	public static String dexter = "http://dexterdemo.isti.cnr.it:8080/dexter-webapp/api/rest/spot-entities";
	public static String spotlight1 = "";
	public static String spotlight2 = "";
	public static HashMap<TOOLKIT,String> queryUriMap;
	
	public final static String spotlightSpotURI = "http://spotlight.dbpedia.org/rest/spot/";
	public final static String spotlightURI = "http://spotlight.dbpedia.org/rest/annotate/";
	public final static String spotlightCandURI = "http://spotlight.dbpedia.org/rest/candidates/";
	public final static String spotlightURI2 = "http://spotlight.sztaki.hu:2222/rest/candidates/";
	public final static String minerURI = "http://wikipedia-miner.cms.waikato.ac.nz/services/wikify";
	public final static String dexterURI = "http://localhost:8080/rest/annotate/";
	public final static String dexterURI2 = "";
	public final static String tagmeURI = "http://tagme.di.unipi.it/tag";
	
	
	public final static String [] serviceList = {spotlightSpotURI,spotlightURI,spotlightCandURI,spotlightURI2,minerURI,dexterURI,dexterURI2,tagmeURI};
	
//	public final static String spotlightParas = "";
//	public final static String minerParas = "&responseFormat=JSON&sourceMode=HTML&references=true&disambiguationPolicy=loose&linkFormat=html_id_weight";
//	public final static String dexterParas = "&n=10";
//	public final static String tagmeParas = "&key=abAnBGgAqA2015&include_categories=true&include_all_spots=true";
	
	public final static String spotlightParas = "&confidence=0&support=0";
	public final static String minerParas = "&responseFormat=JSON&sourceMode=HTML&references=true&minProbability=0&disambiguationPolicy=loose";
	public final static String dexterParas = "&wn=false&debug=false&format=text";
	public final static String tagmeParas = "&key=abAnBGgAqA2015";
	public final static String [] servicetParas = {spotlightParas,spotlightParas,spotlightParas,spotlightParas,minerParas,dexterParas,dexterParas,tagmeParas};

	public final static String dexterURI2ID = "http://dexterdemo.isti.cnr.it:8080/dexter-webapp/api/rest/get-desc?id=";
	static{
		
	}
}
