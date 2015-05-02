package pattern;

import java.util.LinkedList;

import paser.Question;
import paser.QuestionFrame;
import paser.QuestionSingle;
import pattern.QuestionClassifier.Category;
import finder.Pipeline;

public class FocusExtraction extends TypeExtraction {

	public void printQuestionListWithPOS() {
		Pipeline pipeline = new Pipeline();
		
		int notGetCount = 0;
		for(int i=1; i<=300; i++) {
			Question q = xmlParser.getQuestionWithPseudoId(i);
			QuestionSingle qs = q.toQuestionSingle();
			QuestionClassifier qc = new QuestionClassifier();
			LinkedList<String> wordList = qs.qWordList;
			LinkedList<String> posList = pipeline.getPOSTag(qs);
			
			if(qc.classify(qs) == Category.RESOURCE) { //&& !QuestionClassifier.judgeComparison(wordList, posList)) {
//				System.out.println(i+"\t"+wordList);
//				System.out.println("\t"+posList);
				
				LinkedList<String> focusList = focusExtraction(qs.question, wordList, posList);
				if(focusList.size()>0) {
					System.out.print(i);
					for (String focus : focusList) {
						System.out.print("\t"+focus);
					}
					System.out.println();
				}
				else {
//					System.out.println(i+"\t"+qs.question);
					notGetCount++;
				}
				
			}
		}
		System.out.println(notGetCount);
	}
	
	public LinkedList<String> focusExtraction(String text, LinkedList<String> oriWordList, LinkedList<String> posList) {
		LinkedList<String> focusList = new LinkedList<String>();
		
		// change to lower case
		String sentence = new String(text.toLowerCase());
		LinkedList<String> wordList = new LinkedList<String>();
		for (String string : oriWordList) {
			wordList.add(string.toLowerCase());
		}
		
		// extraction based on rule
		if(sentence.startsWith("give")) {
			if(sentence.startsWith("give me all")) {
				focusList.add(getFocusPhrase("all", wordList, posList));
			} else if(sentence.contains("give all")) {
				focusList.add(getFocusPhrase("all", wordList, posList));
			} else if(sentence.startsWith("give me")) {
				focusList.add(getFocusPhrase("me", wordList, posList));
			} else if(sentence.startsWith("give me a list of")) {
				focusList.add(getFocusPhrase("of", wordList, posList));
			}
		} else if(sentence.contains("list all")) {
			focusList.add(getFocusPhrase("all", wordList, posList));
		} else if(sentence.startsWith("list")) {
			focusList.add(getFocusPhrase("lsit", wordList, posList));
		} else if(sentence.startsWith("show me all")) {
			focusList.add(getFocusPhrase("all", wordList, posList));
		} else if(sentence.contains("show me")) {
			focusList.add(getFocusPhrase("me", wordList, posList));
		} else if(sentence.contains("show a list of")) {
			focusList.add(getFocusPhrase("of", wordList, posList));
		} else if(sentence.contains("which")) {
			focusList.add(getFocusPhrase("which", wordList, posList));
		} else if(sentence.contains("what")) {
			focusList.add(getFocusPhrase("what", wordList, posList));
		} else if(sentence.startsWith("who")) {
			focusList.add("person");
			focusList.add("organization");
		} else if(sentence.startsWith("where")) {
			focusList.add("place");
		} 
		
		return focusList;
	}
	
	public LinkedList<String> focusExtraction(QuestionFrame qf) {
		String text = qf.question;
		LinkedList<String> oriWordList = qf.wordList;
		LinkedList<String> posList = qf.wordList;
		LinkedList<String> focusList = new LinkedList<String>();
		
		// change to lower case
		String sentence = new String(text.toLowerCase());
		LinkedList<String> wordList = new LinkedList<String>();
		for (String string : oriWordList) {
			wordList.add(string.toLowerCase());
		}
		
		// extraction based on rule
		if(sentence.startsWith("give")) {
			if(sentence.startsWith("give me all")) {
				focusList.add(getFocusPhrase("all", wordList, posList));
			} else if(sentence.contains("give all")) {
				focusList.add(getFocusPhrase("all", wordList, posList));
			} else if(sentence.startsWith("give me")) {
				focusList.add(getFocusPhrase("me", wordList, posList));
			} else if(sentence.startsWith("give me a list of")) {
				focusList.add(getFocusPhrase("of", wordList, posList));
			}
		} else if(sentence.contains("list all")) {
			focusList.add(getFocusPhrase("all", wordList, posList));
		} else if(sentence.startsWith("list")) {
			focusList.add(getFocusPhrase("lsit", wordList, posList));
		} else if(sentence.startsWith("show me all")) {
			focusList.add(getFocusPhrase("all", wordList, posList));
		} else if(sentence.contains("show me")) {
			focusList.add(getFocusPhrase("me", wordList, posList));
		} else if(sentence.contains("show a list of")) {
			focusList.add(getFocusPhrase("of", wordList, posList));
		} else if(sentence.contains("which")) {
			focusList.add(getFocusPhrase("which", wordList, posList));
		} else if(sentence.contains("what")) {
			focusList.add(getFocusPhrase("what", wordList, posList));
		} else if(sentence.startsWith("who")) {
			focusList.add("person");
			focusList.add("organization");
		} else if(sentence.startsWith("where")) {
			focusList.add("place");
		} 
		
		return focusList;
	}
	
	
	private String getFocusPhrase(String startWord, LinkedList<String> wordList,
			LinkedList<String> posList) {
		StringBuilder focus = new StringBuilder();
		
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
					focus.append(focus.length()>0?" "+word:word);
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
		return focus.toString();
	}

	public static void main(String[] args) {
		FocusExtraction fe = new FocusExtraction();
		fe.printQuestionListWithPOS();
	}

}
