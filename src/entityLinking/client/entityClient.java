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
		String test = "How many children does Eddie Murphy have?";
		System.out.println(entityClient.queryAPI(test, entityClientConst.TOOLKIT.MINER));
		System.out.println(entityClient.queryAPI(test, entityClientConst.TOOLKIT.DEXTER));
		System.out.println(entityClient.queryAPI(test, entityClientConst.TOOLKIT.SPOTLIGHT1));
		System.out.println(entityClient.queryAPI(test, entityClientConst.TOOLKIT.SPOTLIGHT2));
	}
}
