package similarity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

public class UMBC {
	private static HttpClient client;
	static{
		client = new HttpClient();
	}
	
	private static String queryResult(String query) {
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
	
	private static String queryBuilder(String phrase1, String phrase2){
		StringBuilder query = new StringBuilder();
//		"http://swoogle.umbc.edu/SimService/GetSimilarity?operation=api&phrase1=XXX&phrase2=YYY"
		String base_uri = "http://swoogle.umbc.edu/SimService/GetSimilarity?operation=api&phrase1=";
		query.append(base_uri);
		try {
			query.append(URLEncoder.encode(phrase1,"UTF-8"));
			query.append("&phrase2=");
			query.append(URLEncoder.encode(phrase2,"UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return query.toString();
	}
	
	public static Double getSimilarity(String phrase1,String phrase2){ 
		String query = queryBuilder(phrase1, phrase2);
		String response = queryResult(query);
		while(response == null || response.length() == 0){
			response = queryResult(query);
		}
		Double result = Double.parseDouble(response);
		return result;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
