package HG;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class UMBC {
	public static void main(String[] args) {
		List<StringPair> s=new ArrayList<StringPair>();
		for (int i=0;i<10000;i++) s.add(new StringPair(Integer.toString(i),Integer.toString(i)));
		List<Double> ret=similarity(s);
		for (Double d:ret) {
			System.out.print(d);
		}
	}
	public static int threadnum=200;
	public static List<Double> similarity(List<StringPair> l) {
		int tmp=l.size();
		if(tmp>threadnum) tmp=threadnum; 
		ExecutorService exec=Executors.newCachedThreadPool();
		ArrayList<List<Double>> s=new ArrayList<List<Double>>();
		ArrayList<Future<String>> results=new ArrayList<Future<String>>();
		for (int i=0;i<tmp;i++) {
			List<StringPair> ins=new ArrayList<StringPair>();
			int j=i;
			while(j<l.size()) {
				ins.add(l.get(j));
				j+=tmp;
			}
			List<Double> outs=new ArrayList<Double>();
			s.add(outs);
			results.add(exec.submit(new UMBCTask(ins,outs)));
		}
		exec.shutdown();
		for (Future<String> fs:results) {
			try {
				fs.get();
			} catch (InterruptedException e) {
				return null;
			} catch(ExecutionException e) {
				e.printStackTrace();
			} finally {
				exec.shutdown();
			}
		}
		
		ArrayList<Double> ret=new ArrayList<Double>();
		for (int i=0;i<l.size();i++) {
			ret.add(s.get(i%tmp).get(i/tmp));
		}
		return ret;
	}
}
