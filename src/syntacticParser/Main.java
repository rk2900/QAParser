package syntacticParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import paser.QuestionFrame;
import edu.stanford.nlp.trees.Tree;

public class Main {
	public static void main(String[] args) throws IOException {
		BufferedReader br=new BufferedReader(new FileReader("./data/lx/all.txt"));
		BufferedWriter bw=new BufferedWriter(new FileWriter("./data/lx/199_ans_RNNchar.txt"));
		int k=0;
		while(true) {
			
			System.out.println(++k);
			String str=br.readLine();
			if(str==null) break;
			str=str.split("\t")[1];
		//	bw.write(get(str)+"\r\n");
			
		}
		bw.close();
	}
	public static String get(QuestionFrame qf, String str) {
		String ret=str+"\r\n";
		Tree t=stringParser.parse(str);
		ret+=t.pennString();
		ConstraintSet c=new ConstraintSet();
		constraintExtractor.extract(qf, t, t, c.ans, c);
		ret+=c.toString()+"\r\n";
		return ret;
	}
}
