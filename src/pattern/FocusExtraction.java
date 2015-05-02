package pattern;

import java.util.LinkedList;

import paser.Focus;
import paser.Question;
import paser.QuestionFrame;
import paser.QuestionSingle;
import pattern.QuestionClassifier.Category;
import finder.Pipeline;

public class FocusExtraction extends TypeExtraction {

//	@Deprecated
//	public void printQuestionListWithPOS() {
//		Pipeline pipeline = new Pipeline();
//		
//		int notGetCount = 0;
//		for(int i=1; i<=300; i++) {
//			Question q = xmlParser.getQuestionWithPseudoId(i);
//			QuestionSingle qs = q.toQuestionSingle();
//			QuestionClassifier qc = new QuestionClassifier();
//			LinkedList<String> wordList = qs.qWordList;
//			LinkedList<String> posList = pipeline.getPOSTag(qs);
//			
//			if(qc.classify(qs.qWordList, qs.qPOSList) == Category.RESOURCE) { //&& !QuestionClassifier.judgeComparison(wordList, posList)) {
////				System.out.println(i+"\t"+wordList);
////				System.out.println("\t"+posList);
//				
//				LinkedList<String> focusList = focusExtraction(qs.question, wordList, posList);
//				if(focusList.size()>0) {
//					System.out.print(i);
//					for (String focus : focusList) {
//						System.out.print("\t"+focus);
//					}
//					System.out.println();
//				}
//				else {
////					System.out.println(i+"\t"+qs.question);
//					notGetCount++;
//				}
//				
//			}
//		}
//		System.out.println(notGetCount);
//	}
	
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
			} else if(sentence.startsWith("give me")) {
				focus.setFocus(getFocusPhrase("me", wordList, posList));
			} else if(sentence.startsWith("give me a list of")) {
				focus.setFocus(getFocusPhrase("of", wordList, posList));
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
		} else if(sentence.contains("which")) {
			focus.setFocus(getFocusPhrase("which", wordList, posList));
		} else if(sentence.contains("what")) {
			focus.setFocus(getFocusPhrase("what", wordList, posList));
		} else if(sentence.contains("how many")) {
			focus.setFocus(getFocusPhrase("many", wordList, posList));
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
				if(!mayendFlag && pos.startsWith("V")) {
					continue;
				}
				else if(mayendFlag && !pos.startsWith("NN")) {
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

	public static void main(String[] args) {
//		FocusExtraction fe = new FocusExtraction();
//		fe.printQuestionListWithPOS();
		Pipeline pipeline = new Pipeline();
	}

}
