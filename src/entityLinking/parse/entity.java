package entityLinking.parse;

public class entity {
	
	private String name;
	private int start;
	private int end;
	private String mention;
	
	public entity(String name, int start, int end) {
		super();
		this.name = name;
		this.start = start;
		this.end = end;
	}
	
	public entity(String name, int start, int end, String mention) {
		super();
		this.name = name;
		this.start = start;
		this.end = end;
		this.mention = mention;
	}
	
	public String getName() {
		return name;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public String getMention() {
		return mention;
	}
}
