package finder;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.LinkedList;

import knowledgebase.ClientManagement;
import basic.FileOps;

import com.franz.agraph.jena.AGModel;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.RDFNode;

import finder.Pipeline.DataSource;
import paser.Path;
import paser.Question;
import paser.XMLParser;

public class PathFinder {

	private static AGModel agModel = null;
	private static int lengthThreshold = 2;
	private static boolean printQuery = false;
	
	private static void AGInit() {
		try {
			agModel = ClientManagement.getAgModel();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public PathFinder() {
		AGInit();
	}
	
	public AGModel getAgModel() {
		return agModel;
	}
	
	/**
	 * To get the path between root entity and end entity.
	 * @param root
	 * @param end
	 * @param length the length of path (counting edges)
	 * @return
	 */
	public LinkedList<Path> getPathList(String root, String end, int length) {
		LinkedList<String> vars = new LinkedList<>();
		String rootLabel = "AAAA";
		String endLabel = "BBBB";
		LinkedList<Path> pathList = new LinkedList<>();
		
		StringBuilder sparqlBuilder = new StringBuilder();
		sparqlBuilder.append("SELECT ");
		for(int i=0; i<length*2-1; i++) {
			String n = new String("?n"+i);
			vars.add(n);
			sparqlBuilder.append(n).append(" ");
		}
		vars.add(0, rootLabel);
		vars.add(vars.size(), endLabel);
		
		sparqlBuilder.append("WHERE { ");
		String lastNode = vars.get(0);
		for(int i=0; i<length; i++) {
			sparqlBuilder.append(lastNode).append(" ");
			sparqlBuilder.append(vars.get(i*2+1)).append(" ");
			sparqlBuilder.append(vars.get(i*2+1+1)).append(". ");
			lastNode = new String(vars.get(i*2+1+1));
		}
		sparqlBuilder.append("}");
		
		sparqlBuilder.replace(sparqlBuilder.indexOf(rootLabel), 
				sparqlBuilder.indexOf(rootLabel)+4, 
				"<"+root+">");
		sparqlBuilder.replace(sparqlBuilder.indexOf(endLabel), 
				sparqlBuilder.indexOf(endLabel)+4,
				"<"+end+">");
		
//		System.out.println(sparqlBuilder.toString());
		ResultSet rs = ClientManagement.query(sparqlBuilder.toString(), printQuery);
		while(rs.hasNext()) {
			Path path = new Path(root, end);
			QuerySolution qSolution = rs.next();
			LinkedList<RDFNode> list = new LinkedList<>();
			for(int i=0; i<length*2-1; i++) {
				String n = new String("n"+i);
				list.add(qSolution.get(n));
			}
			path.extend(list);
			pathList.add(path);
		}
		return pathList;
	}
	
	public static void main(String[] args) {
		XMLParser xmlParser = new XMLParser("./data/qald-5_train.xml");
		xmlParser.load();
		xmlParser.parse(DataSource.TRAIN);
		
//		ArrayList<Question> qList = parser.getQuestionsPart(0, 301, "resource");
//		Question q = qList.get(0);
//		
//		for (String answer : q.answers) {
//			System.out.println(answer);
//		}
		
		/****************************************/
		String wikiEntityFilePath = "./data/q-e/all-mark-wiki-entity.txt";
		String resultFilePath = "./data/path-result-mark-all.txt";
		BufferedOutputStream bw = null;
		try {
			bw = new BufferedOutputStream(new FileOutputStream(resultFilePath, true));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		PrintStream console = System.out;
		PrintStream out = new PrintStream(bw);
		System.setOut(out);
		
		PathFinder finder = new PathFinder();
		LinkedList<String> qeList = FileOps.LoadFilebyLine(wikiEntityFilePath);
		for (String qeLine : qeList) {
			String[] items = qeLine.split("\t");
			if(items.length < 5)
				continue;
			int pseudoId = Integer.parseInt(items[0]);
//			int beginOffset = Integer.parseInt(items[1]);
//			int endOffset = Integer.parseInt(items[2]);
//			String wikiUrl = items[3];
//			boolean correct = (Integer.parseInt(items[4]))==1?true:false;
			String entityUri = items[4];
			Question question = xmlParser.getQuestionWithPseudoId(pseudoId);
			if(!question.answerType.equals("resource"))
				continue;
			System.out.println(pseudoId+"\t"+question.question);
			System.out.println("\tForward:");
			for (String answer : question.answers) {
				System.out.println("\t"+"<"+answer+">");
				for(int length=1; length<=lengthThreshold; length++) {
					LinkedList<Path> pathList = finder.getPathList(entityUri, answer, length);
					if(pathList.size()>0) {
						System.out.println("\t\tLength = "+length);
						for (Path path : pathList) {
							System.out.println("\t\t"+path);
						}
					}
				}
			}
			System.out.println("\tBackward");
			for (String answer : question.answers) {
				for(int length=1; length<=lengthThreshold; length++) {
					LinkedList<Path> pathList = finder.getPathList(answer, entityUri, length);
					if(pathList.size()>0) {
						System.out.println("\t\tLength = "+length);
						for (Path path : pathList) {
							System.out.println("\t\t"+path);
						}
					}
				}
			}
		}
		
		out.close();
		System.setOut(console);
		
	}

}
