package phrase;

public class WordDetail {
	String wordInLabel;
	String wordInQuestion;
	String posInQuestion;
	double score;
	public String getWordInLabel() {
		return wordInLabel;
	}
	public void setWordInLabel(String wordInLabel) {
		this.wordInLabel = wordInLabel;
	}
	public String getWordInQuestion() {
		return wordInQuestion;
	}
	public void setWordInQuestion(String wordInQuestion) {
		this.wordInQuestion = wordInQuestion;
	}
	public String getPosInQuestion() {
		return posInQuestion;
	}
	public void setPosInQuestion(String posInQuestion) {
		this.posInQuestion = posInQuestion;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
}
