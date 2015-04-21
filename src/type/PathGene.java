package type;

import java.io.BufferedReader;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import knowledgebase.ClientManagement;

import com.franz.agraph.jena.AGModel;
import com.franz.agraph.jena.AGQuery;
import com.franz.agraph.jena.AGQueryExecutionFactory;
import com.franz.agraph.jena.AGQueryFactory;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;


public class PathGene {
	public IDictionary dict;
	StanfordCoreNLP pipeline;
	public static PathGene pg=new PathGene();
	public HashMap<String,Integer> map=new HashMap<String,Integer>();
	public PathGene() {
		
		try {
			BufferedReader br=new BufferedReader(new FileReader("./data/lx/depth.txt"));
			while(true) {
				String str=br.readLine();
				if(str==null) break;
				String[] tmp=str.split(" ");
				tmp[0]=tmp[0].substring(tmp[0].lastIndexOf("/")+1);
				map.put(tmp[0],Integer.valueOf(tmp[1]));
			}
			br.close();
			String path="./data/lx/dict";
			URL url=new URL("file",null,path);
		
			dict=new Dictionary(url);
			dict.open();
		} catch (Exception e){
			System.err.println(e);
		}
		Properties props = new Properties(); 
        props.put("annotators", "tokenize, ssplit, pos, lemma"); 
        pipeline = new StanfordCoreNLP(props, false);
	}
	public static ArrayList<ArrayList<ISynset>> synpath(ISynset s) {
		ArrayList<ArrayList<ISynset>> ans=new ArrayList<ArrayList<ISynset>>();
		ArrayList<ISynset> tmp=new ArrayList<ISynset>();
		tmp.add(s);
		dfs(s,tmp,ans);
		return ans;
	}
	public static void dfs(ISynset s, ArrayList<ISynset> tmp,ArrayList<ArrayList<ISynset>> ans) {
		List<ISynsetID> hypernyms=s.getRelatedSynsets(Pointer.HYPERNYM);
		if(hypernyms.size()==0) {
			ans.add(tmp);
			return;
		} else {
			for (int i=0;i<hypernyms.size()-1;i++) {
				ArrayList<ISynset> tmpp=new ArrayList<ISynset>(tmp);
				
				tmpp.add(pg.dict.getSynset(hypernyms.get(i)));
				dfs(pg.dict.getSynset(hypernyms.get(i)),tmpp,ans);
			}
			tmp.add(pg.dict.getSynset(hypernyms.get(hypernyms.size()-1)));
			dfs(pg.dict.getSynset(hypernyms.get(hypernyms.size()-1)),tmp,ans);
		}
	}
	public static ArrayList<ArrayList<ISynset>> getPathbyString(String str) {
		IIndexWord idxWord=pg.dict.getIndexWord(str,POS.NOUN);
		if(idxWord==null) return null;
		ArrayList<ArrayList<ISynset>> ans=new ArrayList<ArrayList<ISynset>>();
		for (IWordID wid:idxWord.getWordIDs()) {
			ISynsetID w=wid.getSynsetID();
			ISynset set=pg.dict.getSynset(w);
			ans.addAll(synpath(set));
		}
		return ans;
	}
	public static double getscore(String a, String b) {
		ArrayList<ArrayList<ISynset>> sa=getPathbyString(a);
		ArrayList<ArrayList<ISynset>> sb=getPathbyString(b);
		double ans=-1.0;
		for (ArrayList<ISynset> aa:sa) {
			for (ArrayList<ISynset> bb:sb) {
				double tmp=score(aa,bb);
				if(tmp>ans) ans=tmp;
			}
		}
		return ans;
	}
	private static  double score(ArrayList<ISynset> a, ArrayList<ISynset> b) {
		int alen=a.size();
		int blen=b.size();
		int lca=1;
		while(true) {
			if(lca<=alen&&lca<=blen&&a.get(alen-lca).getID().toString().equals(b.get(blen-lca).getID().toString())) ++lca;
			else break;
		}
		--lca;
		int tmp=(alen<blen)?alen:blen;
		System.out.println("alen:"+alen+" blen:"+blen+" lca:"+lca+" "+1.0/(tmp+1-lca)+" "+((double)2*lca)/(alen+blen));
		return (1.0/Math.pow(2,(tmp-lca))*((double)2*lca)/(alen+blen));
	}
	public static void show(String str) {
		ArrayList<ArrayList<ISynset>> ans=getPathbyString(str);
		for (ArrayList<ISynset> a: ans) {
			for (ISynset b:a) {
				System.out.print(b.getWords().get(0)+" ");
			}
			System.out.println();
		}
	}
	public static boolean exist(String str) {
		if(pg.dict.getIndexWord(str,POS.NOUN)==null) return false;
		else return true;
	}
	public static String[] origin(String text) {
		Annotation document = pg.pipeline.process(text);  
		CoreMap sentence=document.get(SentencesAnnotation.class).get(0);
		List<CoreLabel> mid=sentence.get(TokensAnnotation.class);
		String[] ans=new String[mid.size()];
		for(int i=0;i<mid.size();i++)
		{       
			CoreLabel token=mid.get(i);
			//String word = token.get(TextAnnotation.class);      
			String lemma = token.get(LemmaAnnotation.class);
			ans[i]=lemma;
		}
        return ans;
	}
	public static String linkedorigin(String text) {
		String[] txt=origin(text);
		if(txt.length==0) return null;
		String ans=txt[0];
		for (int i=1;i<txt.length;i++) ans+=(" "+txt[i]);
		return ans;
	}
	public static void main(String[] args) throws Exception {
		//String src="http://dbpedia.org/resource/Pope_Benedict_XVI";
		//String type="mother";
		//System.out.println(haoge(src,type));
		System.out.println(haoge("http://dbpedia.org/resource/China","country"));
	}
	public static double haoge(String src,String type) {
		String tmp=extractType(src);
		if(tmp=="") {
			System.out.println("extract Type failed");
			return -1.0;
		}
		type=linkedorigin(type);
		double s=1.0;
		if(!exist(type)) {
			type=type.substring(type.lastIndexOf(" ")+1);
			s*=0.9;
		}
		if(!exist(type)) return -1;
		if(!exist(tmp)) {
			tmp=tmp.substring(tmp.lastIndexOf(" ")+1);
			s*=0.9;
		}
		if(!exist(tmp)) return -1;
		double p=s*getscore(type,tmp);
		return p;
	}
	public static String extractType(String src) {
		AGModel model=null;
		try {
			model = ClientManagement.getAgModel();
		} catch (Exception e) {
			System.err.println(e);
		}
		String ans="";
		int ansdep=-1;
		try {
			String queryString="PREFIX dbo: <http://dbpedia.org/ontology/> "
					+"PREFIX dbp: <http://dbpedia.org/property/> "
					+"PREFIX res: <http://dbpedia.org/resource/> ";
			queryString +="SELECT DISTINCT ?o ?p WHERE { <"+src+"> rdf:type ?o . ?o rdfs:label ?p . } ";
			System.out.println(queryString);
			AGQuery sparql = AGQueryFactory.create(queryString);
			QueryExecution qe = AGQueryExecutionFactory.create(sparql, model);
			try {
				ResultSet results = qe.execSelect();
				while (results.hasNext()) {
					QuerySolution result = results.next();
					
					RDFNode o = result.get("o");
					RDFNode p = result.get("p");
					if(p.toString().endsWith("@en")&&o.toString().startsWith("http://dbpedia.org/ontology/")) {
						String u=o.toString().substring(o.toString().lastIndexOf("/")+1);
						Integer i=pg.map.get(u);
						if(i==null) continue;
						if(i>ansdep) {
							ansdep=i;
							ans=p.toString().substring(0,p.toString().indexOf("@"));
						}
					}
				}
			} finally {
				qe.close();
			}
		} finally {
			model.close();
		}
		return ans;
	}
}
