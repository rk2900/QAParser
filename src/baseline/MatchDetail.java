package baseline;

import syntacticParser.Constraint;

public class MatchDetail {

	Entity entity;
	Constraint constraint;
	int location;
	//0->node in the left of constraint 
	//1->node in the right of constraint
	String focusString;
	//
	
	public String getFocusString() {
		return focusString;
	}

	public void setFocusString(String focusString) {
		this.focusString = focusString;
	}

	public MatchDetail(){
		
	}
	public MatchDetail(Entity e,  Constraint c, int l, String f){
		entity = e;
		constraint = c;
		location = l;
		focusString = f;
	}
	
	public Entity getEntity() {
		return entity;
	}

	public void setEntity(Entity entity) {
		this.entity = entity;
	}

	public Constraint getConstraint() {
		return constraint;
	}

	public void setConstraint(Constraint constraint) {
		this.constraint = constraint;
	}

	public int getLocation() {
		return location;
	}

	public void setLocation(int location) {
		this.location = location;
	}

	public void print(){
		StringBuilder sb = new StringBuilder();
		sb.append(entity.uri);
		sb.append(" ");
		sb.append(constraint.edge);
		sb.append(" ");
		sb.append("X\n");
		System.out.println(sb.toString());
	}
}
