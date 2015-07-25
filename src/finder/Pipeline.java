package finder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Properties;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFVisitor;
import com.hp.hpl.jena.rdf.model.Resource;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import entityLinking.client.entityClientConst.TOOLKIT;
import entityLinking.parse.responseParser;
import baseline.Answer;
import baseline.Classification;
import baseline.Entity;
import baseline.Main;
import baseline.Classification.CLASSIFICATION;
import baseline.PairPredicate;
import baseline.Predicate;
import basic.FileOps;
import paser.Question;
import paser.QuestionFrame;
import paser.QuestionSingle;
import paser.XMLParser;
import pattern.QuestionClassifier.Category;
import pattern.QuestionClassifier.Label;
import tool.OutputRedirector;
import knowledgebase.ClientManagement;

public class Pipeline {
	public enum DataSource {
		TRAIN, TEST
	}
	
	public static DataSource source = DataSource.TRAIN;
	public static String questionFile = "./data/qald-5_train.xml";
//	public static String testQuestionFile = "./data/qald-5_test_questions.xml";
	public static String testQuestionFile = "./data/qald3/dbpedia-test.xml";
//	public static String testQuestionFile = "./data/qald4/qald-4_multilingual_test_withanswers.xml";
//	public static String testQuestionFile = "./data/qald1/dbpedia-test.xml";
	
	public static int totalNumber = 300;
	
	public static String wikiEntityFilePath = "./data/q-e/all-mark-wiki-entity.txt";
	public static double matchThreshold = 0.5;
	public static int edThreshold = 2;
	
	public XMLParser xmlParser;
	public LinkedList<QuestionFrame> resource = new LinkedList<>();
	public LinkedList<QuestionFrame> who = new LinkedList<>();
	public LinkedList<QuestionFrame> where = new LinkedList<>();
	public LinkedList<QuestionFrame> number = new LinkedList<>();
	public LinkedList<QuestionFrame> date = new LinkedList<>();
	public LinkedList<QuestionFrame> bool = new LinkedList<>();
	public LinkedList<QuestionFrame> comparison = new LinkedList<>();
	
	public static StanfordCoreNLP pipeline;
	
	static {
		Properties props = new Properties();
		props.setProperty("annotators", "tokenize, ssplit, pos, lemma");
		pipeline = new StanfordCoreNLP(props);
	}
	
	/**
	 * To initialize AGraph model and XML parser.
	 */
	public Pipeline() {
		initialize();
	}
	
	public void initialize() {
		xmlParser = new XMLParser();
		xmlParser.setFilePath(questionFile);
		xmlParser.load();
		xmlParser.parse(source);
		for(int i=1; i<=totalNumber; i++) {
			QuestionFrame qf = xmlParser.getQuestionFrameWithPseudoId(i);
			
			if(qf.questionClassifier.label.get(Label.COMPARISON)) {
				comparison.add(qf); // comparison question
			} else if(qf.questionClassifier.category == Category.RESOURCE) {
				if( qf.questionClassifier.label.get(Label.WHO) ) {
					who.add(qf); // who
				} else if(qf.questionClassifier.label.get(Label.WHERE)) {
					where.add(qf); // where
				} else {
					resource.add(qf); // resource but not who/where
				} 
			} else if(qf.questionClassifier.category == Category.NUMBER) {
				number.add(qf);
			} else if(qf.questionClassifier.category == Category.DATE) {
				date.add(qf);
			} else if(qf.questionClassifier.category == Category.BOOLEAN) {
				bool.add(qf);
			}
//			System.err.println(qf.id+"\t"+qf.questionClassifier.category+"\t"+qf.questionClassifier.label.get(Label.COMPARISON));
		}
	}
	
	public Pipeline(DataSource s) {
		if(s == DataSource.TEST) {
			source = DataSource.TEST;
			questionFile = testQuestionFile;
			totalNumber = 99;
			initialize();
		} else {
			initialize();
		}
	}
	
	public String splitSentence(String sentence, int begin, int end) {
		String questionSplitted = sentence.substring(0, begin)+sentence.substring(end+1, sentence.length());
		return questionSplitted;
	}
	
	@Deprecated
	public void proceed(int pseudoId) {
		// Read question text and entity inside
		LinkedList<String> qeLines = FileOps.LoadFilebyLine(wikiEntityFilePath);
		int lineCount = 0;
		String qe = "";
		while( lineCount < qeLines.size() && !(qe = qeLines.get(lineCount++)).startsWith(pseudoId+"\t") );
		String[] itemsOfQe = qe.split("\t");
		if( lineCount == qeLines.size() || itemsOfQe.length < 6)
			return;
		
		// Start to load question information
		Question question = xmlParser.getQuestionWithPseudoId(pseudoId);
		int beginOffset = Integer.parseInt(itemsOfQe[1]);
		int endOffset = Integer.parseInt(itemsOfQe[2]);
		
		// Get split question text
		String questionText = question.question;
		System.out.println(pseudoId+"\t"+questionText);
		String splitText = splitSentence(questionText, beginOffset, endOffset);
		
		// Get entity and surrounding predicates
		String entityUri = itemsOfQe[5];
		LinkedList<RDFNode> predicates = ClientManagement.getSurroundingPred(entityUri);
		
		// Insert matched predicates into new list
		HashSet<String> matchedPredicates = new HashSet<>();
		for (RDFNode predicate : predicates) {
			double matchScore = getMatchScore(splitText, predicate);
			if(matchScore > matchThreshold) {
				matchedPredicates.add(predicate.toString());
			}
		}
		
		// Print results
		System.out.println("\t"+matchedPredicates);
	}
	
	@Deprecated
	private double getMatchScore(String splitText, RDFNode predicate) {
		LinkedList<String> labels = ClientManagement.getLabel(predicate.toString());
		String[] wordsInText = splitText.split(" ");
		boolean matchedFlag = false;
		
		for (String label : labels) {
			String[] wordsInLabel = label.split(" ");
			int matchCount = 0;
			for(String wordL: wordsInLabel)
				for(String wordT: wordsInText)
					matchCount += editDistance(wordL, wordT, false)<edThreshold?1:0;
			if(matchedFlag = (matchCount==wordsInLabel.length?true:false) )
				break;
		}
		return matchedFlag?1:0;
	}

	@Deprecated
	private int editDistance(String wordL, String wordT, boolean visible) {
		int lenL = wordL.length();
		int lenT = wordT.length();
		int[][] distance = new int[lenL+1][lenT+1];
		for(int i=0; i<lenL+1; i++) {
			distance[i][0] = i;
		}
		for(int j=0; j<lenT+1; j++) {
			distance[0][j] = j;
		}
		
		for(int i=0; i<lenL; i++) {
			char L = wordL.charAt(i);
			for(int j=0; j<lenT; j++) {
				char T = wordT.charAt(j);
				if(L == T) {
					distance[i+1][j+1] = distance[i][j];
				} else {
					int replace = distance[i][j] + 1;
					int insert = distance[i][j+1] + 1;
					int delete = distance[i+1][j] + 1;
					
					int min = replace>insert?insert:replace;
					min = min>delete?delete:min;
					
					distance[i+1][j+1] = min;
				}
			}
		}
		
		if(visible)
			System.out.println("\t"+wordL+"\t"+wordT+"\t"+distance[lenL][lenT]);
		return distance[lenL][lenT];
	}

	@Deprecated
	public QuestionSingle preProcess(int pseudoId) {
		// Read question text and entity inside
		LinkedList<String> qeLines = FileOps.LoadFilebyLine("./data/q-e/all-mark-wiki-entity.txt");
		int lineCount = 0;
		String qe = "";
		while( lineCount < qeLines.size() && !(qe = qeLines.get(lineCount++)).startsWith(pseudoId+"\t") );
		String[] itemsOfQe = qe.split("\t");
		if( lineCount == qeLines.size() || itemsOfQe.length < 5)
			return null;
		
		// Start to load question information
		QuestionSingle question = xmlParser.getQuestionWithPseudoId(pseudoId).toQuestionSingle();
		int beginOffset = Integer.parseInt(itemsOfQe[1]);
		int endOffset = Integer.parseInt(itemsOfQe[2]);
		
		// Get entity mention
		String mention = question.question.substring(beginOffset, endOffset);
		question.mention = mention;
		
		// Get POS tags
		LinkedList<String> POSList = getPOSTag(question);
		// Eliminate punctuation
		if(question.qWordList.size() == 0)
			System.err.println("qWord list error: size is 0 !");
		else {
			question.qWordList.remove(question.qWordList.size()-1);
			POSList.remove(POSList.size()-1);
			question.qPOSList = POSList;
		}
		
		// Get mention words position
		for (String string : question.qWordList)
			if(mention.contains(string))
				question.entityPositions.add(question.qWordList.indexOf(string));
		
		// Get entity and surrounding predicates
		String entityUri = itemsOfQe[4];
		question.entityUri = entityUri;
		LinkedList<RDFNode> predicates = ClientManagement.getSurroundingPred(entityUri);
		for (RDFNode predNode : predicates)
			question.surPredicates.add(predNode.toString());
		
		return question;
	}
	
	public LinkedList<String> getPOSTag(QuestionSingle question) {
		LinkedList<String> tags = new LinkedList<String>();
		Annotation annotation = new Annotation(question.question);
		pipeline.annotate(annotation);
		CoreMap labeledSentence = annotation.get(SentencesAnnotation.class).get(0);
		for (CoreLabel label : labeledSentence.get(TokensAnnotation.class)) {
			question.qWordList.add(label.get(TextAnnotation.class));
			tags.add(label.get(PartOfSpeechAnnotation.class));
		}
		question.qPOSList = tags;
		return tags;
	}
	
	/**
	 * Get POS tag results of each word in sentence
	 * @param sentence
	 * @return list of POS tags
	 */
	public LinkedList<String> getPOSTag(String sentence) {
		LinkedList<String> tags = new LinkedList<String>();
		Annotation annotation = new Annotation(sentence);
		pipeline.annotate(annotation);
		CoreMap labeledSentence = annotation.get(SentencesAnnotation.class).get(0);
		for (CoreLabel label : labeledSentence.get(TokensAnnotation.class)) {
			tags.add(label.get(PartOfSpeechAnnotation.class));
		}
		return tags;
	}
	
	/**
	 * Get lemma form of words in sentence.
	 * @param sentence
	 * @return
	 */
	public LinkedList<String> getLemma(String sentence) {
		LinkedList<String> lemma = new LinkedList<>();
		Annotation annotation = new Annotation(sentence);
		pipeline.annotate(annotation);
		CoreMap labeledSentence = annotation.get(SentencesAnnotation.class).get(0);
		for (CoreLabel label : labeledSentence.get(TokensAnnotation.class)) {
			lemma.add(label.get(LemmaAnnotation.class));
		}
		return lemma;
	}
	
	public XMLParser getXMLParser() {
		return xmlParser;
	}
	
	public static void main(String[] args) {
		Pipeline pipeline = new Pipeline(DataSource.TEST);
		responseParser parser = new responseParser();
		HashMap<QuestionFrame, LinkedList<RDFNode>> qaMap = new HashMap<QuestionFrame, LinkedList<RDFNode>>();
		
		OutputRedirector.openFileOutput("./data/output/debug.txt");
		for(QuestionFrame qf: pipeline.bool) {
			parser.setEntityList(qf, TOOLKIT.MINERDIS);
//			Answer answer = Classification.getAnswer(qf,CLASSIFICATION.BOOLEAN);
//			qf.print();
			
			Model model;
			RDFNode r = null;
			try {
				model = ClientManagement.getAgModel();
				r = model.createResource("true");
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			LinkedList<RDFNode> l = new LinkedList<RDFNode>();
			l.add(r);
			qaMap.put(qf, l );
//			if(answer!=null && answer.isException()) {
//				System.out.println("ANSWER = null / ANSWER has exception: " + answer.exceptionString );
//				continue;
//			} else {
//				if(answer.answerType == 0) {
//					System.out.println("ANSWER_TYPE = 0");
//					if(answer.predictList.size() > 0) {
//						Predicate p = answer.predictList.get(0);
//						System.out.println("Predicate: "+p.getUri());
//						LinkedList<RDFNode> nodeList = answer.resources.get(p);
//						for (RDFNode rdfNode : nodeList) {
//							System.out.print("\t"+rdfNode.toString());
//						}
//						qaMap.put(qf, nodeList);
//					}
//				} else {
//					if(answer.pairPredicates.size() > 0) {
//						PairPredicate pairPredicate = answer.pairPredicates.get(0);
//						System.out.println("Pair Predicate: "+pairPredicate.Predicate1.getUri() + "\t" + pairPredicate.Predicate2.getUri());
//						LinkedList<RDFNode> nodeList = answer.pairResources.get(pairPredicate);
//						for (RDFNode rdfNode : nodeList) {
//							System.out.print("\t"+rdfNode.toString());
//						}
//						qaMap.put(qf, nodeList);
//					}
//				}
//			}
			System.out.println("\n====================================================\n");
		}
		
		for(QuestionFrame qf: pipeline.comparison) {
			parser.setEntityList(qf, TOOLKIT.MINERDIS);
			Answer answer = Classification.getAnswer(qf,CLASSIFICATION.NORMAL);
			qf.print();
			if(answer!=null && answer.isException()) {
				System.out.println("ANSWER = null / ANSWER has exception: " + answer.exceptionString );
				continue;
			} else {
				if(answer.answerType == 0) {
					System.out.println("ANSWER_TYPE = 0");
					if(answer.predictList.size() > 0) {
						Predicate p = answer.predictList.get(0);
						System.out.println("Predicate: "+p.getUri());
						LinkedList<RDFNode> nodeList = answer.resources.get(p);
						for (RDFNode rdfNode : nodeList) {
							System.out.print("\t"+rdfNode.toString());
						}
						qaMap.put(qf, nodeList);
					}
				} else {
					if(answer.pairPredicates.size() > 0) {
						PairPredicate pairPredicate = answer.pairPredicates.get(0);
						System.out.println("Pair Predicate: "+pairPredicate.Predicate1.getUri() + "\t" + pairPredicate.Predicate2.getUri());
						LinkedList<RDFNode> nodeList = answer.pairResources.get(pairPredicate);
						for (RDFNode rdfNode : nodeList) {
							System.out.print("\t"+rdfNode.toString());
						}
						qaMap.put(qf, nodeList);
					}
				}
			}
			System.out.println("\n====================================================\n");
		}
	
		OntModel ontModel = ModelFactory.createOntologyModel();
		for(QuestionFrame qf: pipeline.number) {
			parser.setEntityList(qf, TOOLKIT.MINERDIS);
			Answer answer = Classification.getAnswer(qf,CLASSIFICATION.NUMBER);
			qf.print();
			if(answer!=null && answer.isException()) {
				System.out.println("ANSWER = null / ANSWER has exception: " + answer.exceptionString );
				continue;
			} else {
				if(answer.answerType == 0) {
					System.out.println("ANSWER_TYPE = 0");
					if(answer.predictList.size() > 0) {
						Predicate p = answer.predictList.get(0);
						System.out.println("Predicate: "+p.getUri());
						LinkedList<RDFNode> nodeList = answer.resources.get(p);
						
						for (RDFNode rdfNode : nodeList) {
							//目的是对添加需要count的情形
							if(rdfNode.isURIResource()){
								RDFNode tmpNode = ontModel.createLiteral(nodeList.size()+"");
								nodeList.clear();
								nodeList.add(tmpNode);
								break;
							}
							System.out.print("\t"+rdfNode.toString());
						}
						qaMap.put(qf, nodeList);
					}
				} else {
					if(answer.pairPredicates.size() > 0) {
						PairPredicate pairPredicate = answer.pairPredicates.get(0);
						System.out.println("Pair Predicate: "+pairPredicate.Predicate1.getUri() + "\t" + pairPredicate.Predicate2.getUri());
						LinkedList<RDFNode> nodeList = answer.pairResources.get(pairPredicate);
						for (RDFNode rdfNode : nodeList) {
							//目的是对添加需要count的情形
							if(rdfNode.isURIResource()){
								RDFNode tmpNode = ontModel.createLiteral(nodeList.size()+"");
								nodeList.clear();
								nodeList.add(tmpNode);
								break;
							}
							System.out.print("\t"+rdfNode.toString());
						}
						qaMap.put(qf, nodeList);
					}
				}
			}
			System.out.println("\n====================================================\n");
		}
		
		for(QuestionFrame qf: pipeline.date) {
			parser.setEntityList(qf, TOOLKIT.MINERDIS);
			Answer answer = Classification.getAnswer(qf,CLASSIFICATION.DATE);
			qf.print();
			if(answer!=null && answer.isException()) {
				System.out.println("ANSWER = null / ANSWER has exception: " + answer.exceptionString );
				continue;
			} else {
				if(answer.answerType == 0) {
					System.out.println("ANSWER_TYPE = 0");
					if(answer.predictList.size() > 0) {
						Predicate p = answer.predictList.get(0);
						System.out.println("Predicate: "+p.getUri());
						LinkedList<RDFNode> nodeList = answer.resources.get(p);
						for (RDFNode rdfNode : nodeList) {
							System.out.print("\t"+rdfNode.toString());
						}
						qaMap.put(qf, nodeList);
					}
				} else {
					if(answer.pairPredicates.size() > 0) {
						PairPredicate pairPredicate = answer.pairPredicates.get(0);
						System.out.println("Pair Predicate: "+pairPredicate.Predicate1.getUri() + "\t" + pairPredicate.Predicate2.getUri());
						LinkedList<RDFNode> nodeList = answer.pairResources.get(pairPredicate);
						for (RDFNode rdfNode : nodeList) {
							System.out.print("\t"+rdfNode.toString());
						}
						qaMap.put(qf, nodeList);
					}
				}
			}
			System.out.println("\n====================================================\n");
		}
		
		for(QuestionFrame qf: pipeline.where) {
			parser.setEntityList(qf, TOOLKIT.MINERDIS);
			Answer answer = Classification.getAnswer(qf,CLASSIFICATION.WHERE);
			qf.print();
			if(answer!=null && answer.isException()) {
				System.out.println("ANSWER = null / ANSWER has exception: " + answer.exceptionString );
				continue;
			} else {
				if(answer.answerType == 0) {
					System.out.println("ANSWER_TYPE = 0");
					if(answer.predictList.size() > 0) {
						Predicate p = answer.predictList.get(0);
						System.out.println("Predicate: "+p.getUri());
						LinkedList<RDFNode> nodeList = answer.resources.get(p);
						for (RDFNode rdfNode : nodeList) {
							System.out.print("\t"+rdfNode.toString());
						}
						qaMap.put(qf, nodeList);
					}
				} else {
					if(answer.pairPredicates.size() > 0) {
						PairPredicate pairPredicate = answer.pairPredicates.get(0);
						System.out.println("Pair Predicate: "+pairPredicate.Predicate1.getUri() + "\t" + pairPredicate.Predicate2.getUri());
						LinkedList<RDFNode> nodeList = answer.pairResources.get(pairPredicate);
						for (RDFNode rdfNode : nodeList) {
							System.out.print("\t"+rdfNode.toString());
						}
						qaMap.put(qf, nodeList);
					}
				}
			}
			System.out.println("\n====================================================\n");
		}
		
		for(QuestionFrame qf: pipeline.who) {
			parser.setEntityList(qf, TOOLKIT.MINERDIS);
			Answer answer = Classification.getAnswer(qf,CLASSIFICATION.WHO);
			qf.print();
			if(answer!=null && answer.isException()) {
				System.out.println("ANSWER = null / ANSWER has exception: " + answer.exceptionString );
				continue;
			} else {
				if(answer.answerType == 0) {
					System.out.println("ANSWER_TYPE = 0");
					if(answer.predictList.size() > 0) {
						Predicate p = answer.predictList.get(0);
						System.out.println("Predicate: "+p.getUri());
						LinkedList<RDFNode> nodeList = answer.resources.get(p);
						for (RDFNode rdfNode : nodeList) {
							System.out.print("\t"+rdfNode.toString());
						}
						qaMap.put(qf, nodeList);
					}
				} else {
					if(answer.pairPredicates.size() > 0) {
						PairPredicate pairPredicate = answer.pairPredicates.get(0);
						System.out.println("Pair Predicate: "+pairPredicate.Predicate1.getUri() + "\t" + pairPredicate.Predicate2.getUri());
						LinkedList<RDFNode> nodeList = answer.pairResources.get(pairPredicate);
						for (RDFNode rdfNode : nodeList) {
							System.out.print("\t"+rdfNode.toString());
						}
						qaMap.put(qf, nodeList);
					}
				}
			}
			System.out.println("\n====================================================\n");
		}
		
		for(QuestionFrame qf: pipeline.resource) {
			parser.setEntityList(qf, TOOLKIT.MINERDIS);
			Answer answer = Classification.getAnswer(qf,CLASSIFICATION.RESOURCE);
			qf.print();
			if(answer!=null && answer.isException()) {
				System.out.println("ANSWER = null / ANSWER has exception: " + answer.exceptionString );
				continue;
			} else {
				if(answer.answerType == 0) {
					System.out.println("ANSWER_TYPE = 0");
					if(answer.predictList.size() > 0) {
						Predicate p = answer.predictList.get(0);
						System.out.println("Predicate: "+p.getUri());
						LinkedList<RDFNode> nodeList = answer.resources.get(p);
						for (RDFNode rdfNode : nodeList) {
							System.out.print("\t"+rdfNode.toString());
						}
						qaMap.put(qf, nodeList);
					}
				} else {
					if(answer.pairPredicates.size() > 0) {
						PairPredicate pairPredicate = answer.pairPredicates.get(0);
						System.out.println("Pair Predicate: "+pairPredicate.Predicate1.getUri() + "\t" + pairPredicate.Predicate2.getUri());
						LinkedList<RDFNode> nodeList = answer.pairResources.get(pairPredicate);
						for (RDFNode rdfNode : nodeList) {
							System.out.print("\t"+rdfNode.toString());
						}
						qaMap.put(qf, nodeList);
					}
				}
			}
			System.out.println("\n====================================================\n");
		}
		
		pipeline.xmlParser.outputAnswer("./data/output/qald3-test-0.15.xml", qaMap);
		OutputRedirector.closeFileOutput();
		
	}

}
