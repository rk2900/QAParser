package pattern;

import java.util.LinkedList;

import paser.Focus;
import paser.QuestionFrame;

public class FocusExtraction extends TypeExtraction {

	public static Focus extract(QuestionFrame qf) {
		String text = qf.question;
		LinkedList<String> oriWordList = qf.wordList;
		LinkedList<String> posList = qf.posList;
		
		Focus focus = new Focus();
		
		// change to lower case
		String sentence = new String(text.toLowerCase());
		LinkedList<String> wordList = new LinkedList<String>();
		for (String string : oriWordList) {
			wordList.add(string.toLowerCase());
		}
		
		// extraction based on rule
		if(sentence.startsWith("give")) {
			if(sentence.startsWith("give me all")) {
				focus.setFocus(getFocusPhrase("all", wordList, posList));
			} else if(sentence.contains("give all")) {
				focus.setFocus(getFocusPhrase("all", wordList, posList));
			} else if(sentence.startsWith("give me a list of")) {
				focus.setFocus(getFocusPhrase("of", wordList, posList));
			} else if(sentence.startsWith("give me")) {
				focus.setFocus(getFocusPhrase("me", wordList, posList));
			} 
		} else if(sentence.contains("list all")) {
			focus.setFocus(getFocusPhrase("all", wordList, posList));
		} else if(sentence.startsWith("list")) {
			focus.setFocus(getFocusPhrase("list", wordList, posList));
		} else if(sentence.startsWith("show me all")) {
			focus.setFocus(getFocusPhrase("all", wordList, posList));
		} else if(sentence.contains("show me")) {
			focus.setFocus(getFocusPhrase("me", wordList, posList));
		} else if(sentence.contains("show a list of")) {
			focus.setFocus(getFocusPhrase("of", wordList, posList));
		} else if(sentence.contains("which of")) {
			focus.setFocus(getFocusPhrase("of", wordList, posList));
		} else if(sentence.contains("which")) {
			focus.setFocus(getFocusPhrase("which", wordList, posList));
		} else if(sentence.contains("what")) {
			focus.setFocus(getFocusPhrase("what", wordList, posList));
		} else if(sentence.contains("how many")) {
			focus.setFocus(getFocusPhrase("many", wordList, posList));
		} else if(sentence.contains("how much")) {
			focus.setFocus(getFocusPhrase("much", wordList, posList));
		} else if(sentence.contains("amount of")) {
			focus.setFocus(getFocusPhrase("of", wordList, posList));
		}
		
		return focus;
	}
	
	
	private static LinkedList<Integer> getFocusPhrase(String startWord, LinkedList<String> wordList,
			LinkedList<String> posList) {
		LinkedList<Integer> focusWordList = new LinkedList<>();
		
//		System.err.println(startWord);
//		System.err.println(wordList);
		boolean startFlag = false;
		boolean mayendFlag = false;
		for(int i=0; i<wordList.size(); i++) {
			String word = wordList.get(i);
			String pos = posList.get(i);
			if(startFlag) {
				if(!mayendFlag && (pos.startsWith("V") || pos.startsWith("DT")) ) {
					continue;
				}
				else if(mayendFlag && !pos.startsWith("NN") && !pos.startsWith("CD")) {
					startFlag = false;
					break;
				}
				else {
					focusWordList.add(i);
					if(pos.startsWith("NN")) {
						mayendFlag = true;
					}
					continue;
				}
			}
			if(word.equals(startWord)) {
				startFlag = true;
			}
		}
		return focusWordList;
	}

}
