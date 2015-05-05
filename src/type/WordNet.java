package type;

import java.net.URL;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;

public class WordNet {
	public static WordNet wordnet=new WordNet();
	public IDictionary dict;
	public WordNet() {
		try {
			String path="./data/lx/dict";
			URL url=new URL("file",null,path);
		
			dict=new Dictionary(url);
			dict.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static boolean isSynset(String str1, String str2) {
		IIndexWord idxWord1=wordnet.dict.getIndexWord(str1,POS.NOUN);
		IIndexWord idxWord2=wordnet.dict.getIndexWord(str2,POS.NOUN);
		if(idxWord1==null||idxWord2==null) return false;
		for (int i=0;i<idxWord1.getWordIDs().size();i++) {
			IWordID wid1=idxWord1.getWordIDs().get(i);
			ISynsetID w1=wid1.getSynsetID();
			ISynset set1=wordnet.dict.getSynset(w1);
			for (int j=0;j<idxWord2.getWordIDs().size();j++) {
				IWordID wid2=idxWord2.getWordIDs().get(j);
				ISynsetID w2=wid2.getSynsetID();
				ISynset set2=wordnet.dict.getSynset(w2);
				if(set1.getOffset()==set2.getOffset()) return true;
			}
		}
		return false;
	}
	public static void main(String[] args) {
		System.out.println(isSynset("program","mission"));
	}
}
