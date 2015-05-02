package syntacticParser;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreePrint;

public class stringParser {
	LexicalizedParser lp;
	public static stringParser sp=new stringParser();
	public static Tree p;
	public static void main(String[] args) throws IOException {
		Tree tr=parse("In which country does the Ganges start?");
		p=tr;
		dfs(tr);
	}
	public static void dfs(Tree t) {
		if(t.isLeaf()) {
			System.out.println("isLeaf "+t.label());
		} else if(t.isPhrasal()){
			System.out.println("isPhrasal "+t.label());
		} else  if(t.isPrePreTerminal()) {
			System.out.println("isPrePreTerminal"+t.label());
		} else  if(t.isPreTerminal()) {
			System.out.println("isPreTerminal"+t.label());
		}
		System.out.println(t.getChildrenAsList().size()+"\t"+p.rightCharEdge(t));
		for (Tree tr: t.getChildrenAsList()) dfs(tr);
	}
	public stringParser() {
		String parserModel = "edu/stanford/nlp/models/lexparser/englishRNN.ser.gz";
		lp = LexicalizedParser.loadModel(parserModel);
	}
	public static Tree parse(String sentence) {
		TokenizerFactory<CoreLabel> tokenizerFactory =
		        PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
		Tokenizer<CoreLabel> tok =
		        tokenizerFactory.getTokenizer(new StringReader(sentence));
		List<CoreLabel> Words = tok.tokenize();
		Tree tree=sp.lp.apply(Words);
		return tree;
	}
	public static String getString(Tree t) {
		String ans="";
		for (Tree tr:t.getChildrenAsList()) {
			if(tr.isLeaf()) ans+=(tr.value()+" ");
			else ans+=getString(tr);
		}
	//	if(ans.length()!=0) ans=ans.substring(0,ans.length()-1);
		return ans;
	}
}
