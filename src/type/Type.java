package type;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import finder.Pipeline;

public class Type {
	public static Type type=new Type();
	List<String> ontolist=new ArrayList<String>();
	List<String> labellist=new ArrayList<String>();
	Type() {
		try {
			BufferedReader br=new BufferedReader(new FileReader("./data/lx/TypeWithLabel.txt"));
			while(true) {
				String str=br.readLine();
				if(str==null) break;
				String label=br.readLine();
				ontolist.add(str);
				labellist.add(label);
			}
			br.close();
		} catch (Exception e){
			e.printStackTrace();
		}
		
	}
	public static String lemma(String str) {
		Annotation document = Pipeline.pipeline.process(str);  
		CoreMap sentence=document.get(SentencesAnnotation.class).get(0);
		List<CoreLabel> mid=sentence.get(TokensAnnotation.class);
		String ans="";
		for(int i=0;i<mid.size();i++)
		{       
			CoreLabel token=mid.get(i);
			//String word = token.get(TextAnnotation.class);      
			String lemma = token.get(LemmaAnnotation.class);
			ans+=lemma;
			if(i!=mid.size()-1) ans+=" ";
		}
		return ans;
	}
	public static String getType(String str) {
		String tmp=lemma(str);
		for (int i=0;i<type.labellist.size();i++) {
			if(WordNet.isSynset(tmp, type.labellist.get(i))) {
				return type.ontolist.get(i);
			}
		}
		return null;
	}
	public static void main(String[] args) {
		System.out.println(getType("Movies"));
	}
}
