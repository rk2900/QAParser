package syntacticParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import baseline.Entity;
import paser.QuestionFrame;
import edu.stanford.nlp.trees.Tree;
import finder.Pipeline;

public class Main {
	public static void main(String[] args) throws IOException {
		
		BufferedWriter bw=new BufferedWriter(new FileWriter("./data/lx/199_ans_RNNchar.txt"));
		Pipeline pipeline=new Pipeline();
		baseline.Main.setEntity(pipeline);
		for (int i=1;i<=300;i++) {
			System.out.println(i);
			QuestionFrame qf = pipeline.xmlParser.getQuestionFrameWithPseudoId(i);
			bw.write(get(qf,qf.question));
			for (Entity e:qf.getEntityList()) {
				bw.write(e.getUri()+"["+e.getStart()+","+e.getEnd()+"]\r\n");
			}
			bw.write("\r\n");
		}
		bw.close();
	}
	public static String get(QuestionFrame qf, String str) {
		String ret=str+"\r\n";
		Tree t=stringParser.parse(str);
		ret+=t.pennString();
		ConstraintSet c=ConstraintSet.getConstraintSet(str,qf);
		ret+=c.toString();
		return ret;
	}
}
