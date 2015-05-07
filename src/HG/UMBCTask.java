package HG;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

public class UMBCTask implements Callable<String> {
	private List<StringPair> ins;
	private List<Double> outs;
	public UMBCTask(List<StringPair> in, List<Double> out) {
		ins=in;
		outs=out;
	}
	public String call() {
		for (StringPair sp:ins) {
			outs.add(UMBCSimilarity(sp.left,sp.right));
			System.out.println(sp.left);
		}
		return "";
	}
	public static double UMBCSimilarity(String a, String b) {
		//http://swoogle.umbc.edu/SimService/GetSimilarity?operation=api&phrase1=car&phrase2=bike
		String aa=a.replace(" ", "%20");
		String bb=b.replace(" ", "%20");
		String ret=sendGet("http://swoogle.umbc.edu/SimService/GetSimilarity","operation=api&phrase1="+aa+"&phrase2="+bb);
		//System.out.println(ret);
		return Double.parseDouble(ret);
	}
	public static String sendGet(String url, String param) {
        String result = "";
        BufferedReader in = null;
        try {
            String urlNameString = url + "?" + param;
            URL realUrl = new URL(urlNameString);
            URLConnection connection = realUrl.openConnection();
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.connect();
            Map<String, List<String>> map = connection.getHeaderFields();
           /* for (String key : map.keySet()) {
                System.out.println(key + "--->" + map.get(key));
            }*/
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
        return result;
    }
	public static String sendPost(String url, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            for (int i=0;i<10;i++) {
            	out.print(param+"?");
            	
            }
            out.flush();
            // flush输出流的缓冲
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            System.out.println("发送 POST 请求出现异常！"+e);
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally{
            try{
                if(out!=null){
                    out.close();
                }
                if(in!=null){
                    in.close();
                }
            }
            catch(IOException ex){
                ex.printStackTrace();
            }
        }
        return result;
    }
	public static void main(String[] args) {
		System.out.println(sendPost("http://swoogle.umbc.edu/SimService/GetSimilarity","operation=api&phrase1=car&phrase2=car"));
	}
}
