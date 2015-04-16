package similarity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import finder.Pipeline;

public class test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Pipeline pipeline = new Pipeline();
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
		Extraction.topRanking(pipeline, 67);

//		String sentence = "writers";
//		LinkedList<String> lemmas = pipeline.getLemma(sentence);
//		for (String lemma : lemmas) {
//			System.out.println(lemma);
//		}
//		String s = "In which U.S. state is Fort Knox located?";
//		LinkedList<String> postags = pipeline.getPOSTag(s);
//		System.out.println(postags);
	}

}
