package paser;

import java.util.ArrayList;
import java.util.LinkedList;

import pattern.FocusExtraction;
import pattern.QuestionClassifier;
import finder.PreProcess;
import baseline.Entity;

public class QuestionFrame extends Question {

	public LinkedList<String> wordList;
	public LinkedList<String> posList;
	public LinkedList<Entity> entityList;
	
	QuestionClassifier questionClassifier;
	Focus focus;
	
	public QuestionFrame(int id, boolean onlydbo, boolean aggregation,
			boolean hybrid, String answerType, ArrayList<String> keywords,
			String question, ArrayList<String> answers, String query) {
		
		super(id, onlydbo, aggregation, hybrid, answerType, keywords, question,
				new ArrayList<>(), new String());
		wordList = new LinkedList<>();
		posList = new LinkedList<>();
		entityList = new LinkedList<>();
		PreProcess.processWordAndPOS(this);
		questionClassifier = new QuestionClassifier();
		questionClassifier.classify(wordList, posList);
		focus = FocusExtraction.extract(this);
	}
	
	public boolean hasFocus() {
		return !focus.isEmpty();
	}
	
	public boolean isIntegralByWord(int loc) {
		for (Entity e:entityList) {
			if(loc>e.getStart()&&loc<=e.getEnd()) return true;
		}
		return false;
	}
	public boolean isIntegralByChar(int loc) {
		int tmp=0;
		for (int i=0;i<wordList.size();i++) {
			if(tmp==loc) return isIntegralByWord(i);
			tmp+=wordList.get(i).length();
		}
		return false;
	}
	public boolean setWordList(LinkedList<String> list) {
		if(list == null) {
			System.err.println("List of words is NULL!");
			return false;
		} else if(list.size() == 0) {
			System.err.println("List of words is empty!");
			return false;
		}
		else {
			this.wordList = list;
			return true;
		}
	}
	
	public boolean setPOSList(LinkedList<String> list) {
		if(list == null) {
			System.err.println("List of POS is NULL!");
			return false;
		} else if(list.size() == 0) {
			System.err.println("List of POS is empty!");
			return false;
		}
		else {
			this.posList = list;
			return true;
		}
	}
	
	public boolean setEntityList(LinkedList<Entity> list) {
		if(list == null) {
			System.err.println("List of entities is NULL!");
			return false;
		} else {
			this.entityList = list;
			return true;
		}
	}
	
	public LinkedList<String> getWordList() {
		return wordList;
	}
	
	public LinkedList<String> getPOSList() {
		return posList;
	}
	
	public LinkedList<Entity> getEntityList() {
		return entityList;
	}

	@Override
	public void print() {
		System.out.println("-------------------------");
		System.out.println("ID: "+"\t"+id);
		System.out.println("Category: "+"\t"+this.questionClassifier.category);
		System.out.println("Content: "+"\t"+this.question);
		System.out.println("Focus Index:"+"\t"+this.focus.leftIndex+"\t"+this.focus.rightIndex);
		System.out.println("Focus Content: "+"\t"+this.focus.getFocusContent(wordList));
		System.out.println("Word List: "+"\t"+this.wordList);
		System.out.println("POS List: "+"\t"+this.posList);
		System.out.println("Entity List: "+"\t"+this.entityList.size());
		System.out.println("-------------------------");
	}
}
