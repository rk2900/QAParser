package baseline;

import java.util.Comparator;

import knowledgebase.ClientManagement;

public class Entity implements Comparator<Entity>, Comparable<Entity> {
	String uri;
	String wiki;
	int start;
	int end;
	
	public Entity(String title, int start, int end){
		this.start = start;
		this.end = end;
		this.wiki = "http://en.wikipedia.org/wiki/"+title.replace(" ", "_");
//		this.uri = "http://dbpedia.org/resource/"+title.replace(" ", "_");
		this.uri = ClientManagement.getEntityOfWiki(wiki).toString();
	}
	
	public Entity(String uri, String wiki, int start, int end) {
		super();
		this.uri = uri;
		this.wiki = wiki;
		this.start = start;
		this.end = end;
	}
	
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public String getWiki() {
		return wiki;
	}
	public void setWiki(String wiki) {
		this.wiki = wiki;
	}
	public int getStart() {
		return start;
	}
	public void setStart(int start) {
		this.start = start;
	}
	public int getEnd() {
		return end;
	}
	public void setEnd(int end) {
		this.end = end;
	}
	
	public void print(){
		System.out.println("URI: "+uri+" Start: "+start+" End: "+end);
	}

	@Override
	public int compare(Entity o1, Entity o2) {
		// TODO Auto-generated method stub
		int result = o1.start - o2.start;
		if(result != 0){
			return result;
		}
		return o1.end - o2.end;
	}

	@Override
	public int compareTo(Entity o) {
		// TODO Auto-generated method stub
		int result = start - o.start;
		if(result != 0){
			return result;
		}
		return end - o.end;
	}
	
}
