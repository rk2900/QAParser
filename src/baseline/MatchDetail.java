package baseline;

import syntacticParser.Constraint;
import syntacticParser.Node;

public class MatchDetail {

	Entity entity;
	Node node;
	Constraint constraint;
	int location;
	//0->node in the left of constraint 
	//1->node in the right of constraint
	
	public MatchDetail(Entity e, Node n, Constraint c, int l){
		entity = e;
		node = n;
		constraint = c;
		location = l;
	}
	
	public Entity getEntity() {
		return entity;
	}

	public void setEntity(Entity entity) {
		this.entity = entity;
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
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

}
