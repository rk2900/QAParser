package entityLinking.client;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import baseline.Entity;
import entityLinking.client.entityClientConst.TOOLKIT;


public class entityClient {

	private static HttpClient client;
	static{
		client = new HttpClient();
	}
	
	public static String queryResult(String query) {
		String responseBody = null;
		GetMethod getMethod = new GetMethod(query);
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
				new DefaultHttpMethodRetryHandler());

		int statusCode;
		try {
			statusCode = client.executeMethod(getMethod);
			if (statusCode != HttpStatus.SC_OK) {
				System.err.println("Method failed: "
						+ getMethod.getStatusLine());
			}
			responseBody = getMethod.getResponseBodyAsString();

		} catch (HttpException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			getMethod.releaseConnection();
		}
		return responseBody;
	}
	
	public static String queryAPI(String content, TOOLKIT toolkit){
		StringBuilder query = new StringBuilder();
		try {
			switch (toolkit) {
			case MINER:
				query.append(entityClientConst.miner);
				query.append(URLEncoder.encode(content,"UTF-8"));
				query.append(entityClientConst.minerParas);
				break;
			case DEXTER:
				query.append(entityClientConst.dexter);
				query.append(URLEncoder.encode(content,"UTF-8"));
				query.append(entityClientConst.dexterParas);
				break;
			case SPOTLIGHT1:
				query.append(entityClientConst.spotlight1);
				query.append(URLEncoder.encode(content,"UTF-8"));
				query.append(entityClientConst.spotlightParas);
				break;
			case SPOTLIGHT2:
				query.append(entityClientConst.spotlight2);
				query.append(URLEncoder.encode(content,"UTF-8"));
				query.append(entityClientConst.spotlightParas);
				break;
			case MINERDIS:
				query.append(entityClientConst.miner);
				query.append(URLEncoder.encode(content,"UTF-8"));
				query.append(entityClientConst.minerDisParas);
			default:
				break;
			}
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(query.length() > 0){
			return queryResult(query.toString());
		}else{
			return null;
		}
	}
	
	public static void main(String [] args){
		String test = "Which soccer players were born on Malta?";
		test = "Which books by Kerouac were published by Viking Press?";
		test ="Give me the currency of China.";
		test = "Which soccer players were born on Malta?";
		test = "Who is the mayor of Berlin?";
//		test = "Which movies starring Brad Pitt were directed by Guy Ritchie?";
//		test = "To which artistic movement did the painter of The Three Dancers belong?";
//		test = "In which countries can you pay using the West African CFA franc?";
		test = "When were the Hells Angels founded?";
		test = "In which U.S. state is Fort Knox located?";
		test = "Give me all breeds of the German Shepherd dog.";
		test = "Give me all members of Prodigy."; 
//		test = "Who painted The Storm on the Sea of Galilee?";
		test = "In which city was the former Dutch queen Juliana buried?";
		test = "Who invented the zipper?";
//		test = "Who painted The Storm on the Sea of Galilee?";
//		test = "How many official languages are spoken on the Seychelles?";
//		test = "In which military conflicts did Lawrence of Arabia participate?";
//		String result = entityClient.queryAPI(test, entityClientConst.TOOLKIT.MINERDIS);
//		String title = "Vrije Universiteit";
//		Entity entity = new Entity(title, 0, 0);
//		System.out.println(entity.getUri());
		System.out.println(entityClient.queryAPI(test, entityClientConst.TOOLKIT.MINERDIS));
//		System.out.println(result);
//		System.out.println(entityClient.queryAPI(test, entityClientConst.TOOLKIT.DEXTER));
//		System.out.println(entityClient.queryAPI(test, entityClientConst.TOOLKIT.SPOTLIGHT1));
//		System.out.println(entityClient.queryAPI(test, entityClientConst.TOOLKIT.SPOTLIGHT2));
	}
}
