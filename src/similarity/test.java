package similarity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import finder.Pipeline;

public class test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		int l = Extraction.getCommonStrLength("writer", "wrote");
//		System.out.println(l);
		
//		ArrayList<Double> d = new ArrayList<Double>();
//		d.add(1.1);
//		d.add(2.0);
//		d.add(1.3);
//		Collections.sort(d);
//		Collections.reverse(d);
//		System.out.println(d);
//		for (Double double1 : d) {
//			if(double1 == 2.0){
//				System.out.println("vv");
//			}
//		}
		Extraction.topRanking(new Pipeline(), 5);
	}

}
