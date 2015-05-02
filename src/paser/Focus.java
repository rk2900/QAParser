package paser;

import java.util.LinkedList;

public class Focus {
	public int leftIndex;
	public int rightIndex;
	
	public Focus() {
		leftIndex = rightIndex = -1;
	}
	
	public boolean isEmpty() {
		return leftIndex<0 || rightIndex<0;
	}
	
	public void setFocus(LinkedList<Integer> focusWordList) {
		this.leftIndex = focusWordList.get(0);
		this.rightIndex = focusWordList.get(focusWordList.size()-1);
	}
	
	public String getFocusContent(LinkedList<String> wordList) {
		StringBuilder sb = new StringBuilder();
		if(isEmpty()) {
			return "";
		}
		for(int i=leftIndex; i<=rightIndex; i++) {
			sb.append(sb.length()>0?" ":"" + wordList.get(i));
		}
		
		return sb.toString();
	}
}
