package pattern;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import paser.QuestionSingle;
import paser.XMLParser;
import finder.Pipeline;

public class QuestionClassifier {
	
	public enum Category {
		RESOURCE, DATE, BOOLEAN, NUMBER
	}
	public enum Label {
		COMPARISON
	}
	public HashMap<Category, Boolean> cateMap;
	public HashMap<Label, Boolean> labelMap;
	public Category category;
	
	public QuestionClassifier() {
		cateMap = new HashMap<>();
		labelMap = new HashMap<>();
	}
	
	/**
	 * Get question category by classifier
	 * @param qs
	 */
	public Category classify(QuestionSingle qs) {
		LinkedList<String> wordList = qs.qWordList;
		LinkedList<String> POSList = qs.qPOSList;
		
		if(POSList == null || wordList == null) {
			System.err.println("POS List or Word List is null!");
			return null;
		}
		
		// category
		cateMap.put(Category.DATE, QuestionClassifier.judgeDate(wordList));
		cateMap.put(Category.BOOLEAN, QuestionClassifier.judgeBool(wordList, POSList));
		cateMap.put(Category.NUMBER, QuestionClassifier.judgeNumber(wordList));
		boolean resourceFlag = !(cateMap.get(Category.DATE) || cateMap.get(Category.BOOLEAN) || cateMap.get(Category.NUMBER));
		cateMap.put(Category.RESOURCE, resourceFlag);
		
		for (Category cat : cateMap.keySet()) {
			if(cateMap.get(cat)) {
				category = cat;
				break;
			}
		}
		
		return category;
//		// label
//		labelMap.put(Label.COMPARISON, QuestionClassifier.judgeComparison(wordList, POSList));
	}
	
	public HashMap<Category, Boolean> getCategoryResult() {
		if(cateMap.isEmpty())
			System.err.println("No category result!");
		return cateMap;
	}
	
	/**
	 * To judge if the question focuses on Date
	 * @param wordList
	 * @return
	 */
	public static boolean judgeDate(LinkedList<String> wordList) {
		if(wordList.size() == 0)
			return false;
		
		boolean judgeFlag = false;
		String firstWordL = wordList.get(0).toLowerCase();
		if(firstWordL.equals("when"))
			judgeFlag = true;
		else 
			judgeFlag = false;
		
		return judgeFlag;
	}
	
	/**
	 * To judge if the question focuses on Number
	 * @param wordList
	 * @return
	 */
	public static boolean judgeNumber(LinkedList<String> wordList) {
		if(wordList.size() == 0) 
			return false;
		
		boolean judgeFlag = false;
		String firstWord = wordList.get(0).toLowerCase();
		String secondWord = wordList.get(1).toLowerCase();
		if(firstWord.equals("how") && (secondWord.equals("many") || secondWord.equals("much") || secondWord.equals("often")))
			judgeFlag = true;
		
		for (String word : wordList) {
			if(word.equals("amount")) {
				judgeFlag = true;
				break;
			}
		}
		
		return judgeFlag;
	}
	
	
	/**
	 * To judge if the question focuses on Boolean
	 * @param wordList
	 * @param POSList
	 * @return
	 */
	public static boolean judgeBool(LinkedList<String> wordList, LinkedList<String> POSList) {
		if(wordList.size()*POSList.size() == 0)
			return false;
		
		boolean judgeFlag = false;
		String firstPOS = POSList.get(0);
		String firstWord = wordList.get(0);
		if(firstPOS.equals("VBZ") || firstPOS.equals("VBD"))
			judgeFlag = true;
		if(firstWord.equalsIgnoreCase("DO") || firstWord.equalsIgnoreCase("ARE"))
			judgeFlag = true;
		
		return judgeFlag;
	}
	
	/**
	 * To get the detailed type of boolean question
	 * @param wordList
	 * @param POSList
	 * @return 0: type check; 1: triple existence check; 
	 */
	public static Integer getBooleanType(LinkedList<String> wordList, LinkedList<String> POSList) {
		if(wordList.size()*POSList.size() == 0 || !judgeBool(wordList, POSList))
			return -1;
		
		int type = -1;
		String firstPOS = POSList.get(0);
		if(firstPOS.equals("VBZ"))
			type = 0;
		else if (firstPOS.equals("VBD"))
			type = 1;
		// TODO

		return type;
	}

	/**
	 * To judge if the question focuses on Comparison
	 * @param wordList
	 * @param POSList
	 * @return
	 */
	public static boolean judgeComparison(LinkedList<String> wordList, LinkedList<String> POSList) {
		if(wordList.size()*POSList.size() == 0) 
			return false;
		
		boolean judgeFlag = false;
		HashSet<String> compKeywords = new HashSet<>();
		compKeywords.add("JJS");
		compKeywords.add("RBS");
		compKeywords.add("JJR");
		for (String string : POSList) {
			if(compKeywords.contains(string)) {
				judgeFlag = true;
				break;
			}
		}

		return judgeFlag;
	}
	
	/**
	 * To get the detailed type of comparison target
	 * @param wordList
	 * @param POSList
	 * @return 0: (JJS) specific dimensional comparison; 
	 * 1: (RBS) comparison in the next noun; 
	 * 2: (JJR,IN,CD) comparison in the next noun with NUMBER
	 */
	public static Integer getComparisonType(LinkedList<String> wordList, LinkedList<String> POSList) {
		if(wordList.size()*POSList.size() == 0) 
			return -1;
		
		int type = -1;
		for(int i=0; i<POSList.size(); i++) {
			String curPOS = POSList.get(i);
			if(curPOS.equals("JJS")) {
				type = 0;
				break;
			} else if (curPOS.equals("RBS")) {
				type = 1;
				break;
			} else if (curPOS.equals("JJR")) {
				if(i<POSList.size()-2 && POSList.get(i+1).equals("IN") && POSList.get(i+2).equals("CD")) {
					type = 2;
				}
				break;
			}
		}
		
		return type;
	}
	
	public static void main(String[] args) {
		Pipeline pipeline = new Pipeline();
		XMLParser parser = pipeline.getXMLParser();
		
		LinkedList<QuestionSingle> resourceList = new LinkedList<>();
		LinkedList<QuestionSingle> dateList = new LinkedList<>();
		LinkedList<QuestionSingle> numberList = new LinkedList<>();
		LinkedList<QuestionSingle> booleanList = new LinkedList<>();
		HashMap<Category, LinkedList<QuestionSingle>> categoryMap = new HashMap<>();
		categoryMap.put(Category.RESOURCE, resourceList);
		categoryMap.put(Category.DATE, dateList);
		categoryMap.put(Category.NUMBER, numberList);
		categoryMap.put(Category.BOOLEAN, booleanList);
		
		int count = 0;
		for(int i=1; i<=300; i++) {
			QuestionSingle qs = parser.getQuestionWithPseudoId(i).toQuestionSingle();
			pipeline.getPOSTag(qs);
			
			QuestionClassifier qc = new QuestionClassifier();
			qc.classify(qs);
			String trueCate = qs.answerType;
			String predictCate = qc.category.toString();
			
			if(QuestionClassifier.judgeComparison(qs.qWordList, qs.qPOSList));
			else if(qc.category == Category.RESOURCE)
				System.out.println(qs.id+"\t"+qs.question);
		}
		
	}

}
