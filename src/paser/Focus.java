package paser;

import java.util.LinkedList;

import baseline.Entity;

public class Focus {
	public int leftIndex;
	public int rightIndex;
	
	public Focus() {
		leftIndex = rightIndex = -1;
	}
	
	public void setLeftIndex(int i) {
		leftIndex = i;
	}
	
	public void setRightIndex(int j) {
		rightIndex = j;
	}
	
	public int getLeftIndex() {
		return leftIndex;
	}
	
	public int getRightIndex() {
		return rightIndex;
	}
	
	public boolean isEmpty() {
		return leftIndex<0 || rightIndex<0;
	}
	
	public void setFocus(LinkedList<Integer> focusWordList) {
		this.leftIndex = focusWordList.get(0);
		this.rightIndex = focusWordList.get(focusWordList.size()-1);
	}
	
	public boolean hasEntity(LinkedList<Entity> entities) {
		boolean existFlag = false;
		for (Entity entity : entities) {
			if(leftIndex <= entity.getStart() && rightIndex >= entity.getEnd()) {
				// entity in focus
				existFlag = true;
				break;
			}
		}
		return existFlag;
	}
	
	public String getFocusContent(LinkedList<String> wordList) {
		StringBuilder sb = new StringBuilder();
		if(isEmpty()) {
			return "";
		}
		for(int i=leftIndex; i<=rightIndex; i++) {
			String w = wordList.get(i);
			sb.append(sb.length()>0?" ":"").append(w);
		}
		
		return sb.toString();
	}
	
}
