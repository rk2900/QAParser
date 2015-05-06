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
	public static List<String> getType(String str) {
		String tmp=lemma(str);
		List<String> ret=new ArrayList<String>();
		for (int i=0;i<type.labellist.size();i++) {
			if(tmp.toLowerCase().replace(" ", "").equals(lemma(type.labellist.get(i)).toLowerCase().replace(" ", ""))||WordNet.isSynset(tmp, type.labellist.get(i))) {
				ret.add(type.ontolist.get(i));
			}
		}
		return ret;
	}
	public static List<String> getTypeFromFocus(String str) {
		List<String> ret=new ArrayList<String>();
		while(true) {
			List<String> tmp=getType(str);
			ret.addAll(tmp);
			int index=str.indexOf(' ');
			if(index==-1) break;
			str=str.substring(index+1,str.length());
		}
		return ret;
	}
	public static void main(String[] args) {
		System.out.println(getTypeFromFocus("sister cities"));
	}
}
