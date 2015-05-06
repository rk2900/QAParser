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
	
	private static String queryBuilder(String content, TOOLKIT toolkit){
		StringBuilder query = new StringBuilder();
		try {
			switch (toolkit) {
			case MINER:
				query.append(entityClientConst.miner);
				query.append(URLEncoder.encode(content,"UTF-8"));
				query.append(entityClientConst.minerParas);
				break;
			default:
				break;
			}
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
//		try {
//			query.append(URLEncoder.encode(phrase1,"UTF-8"));
//			query.append("&phrase2=");
//			query.append(URLEncoder.encode(phrase2,"UTF-8"));
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		}
		return query.toString();
	}
	
	public static void main(String [] args){
		
	}
}