package syntacticParser;

import java.util.List;

import paser.QuestionFrame;
import edu.stanford.nlp.trees.Tree;

public class constraintExtractor {
	public static boolean extract(QuestionFrame qf, Tree root, Tree t, Node target, ConstraintSet cs) {
		if(!t.isPhrasal()) return false;
		if(VP_VBNP(qf, root, t,target,cs)) return true;
		else if(VP_VBPP(qf, root, t,target,cs)) return true;
		else if(NP_NPPP(qf, root, t,target,cs)) return true;
		else if(SQ_VBNPVP(qf, root, t,target,cs)) return true;
		boolean flag=false;
		for (Tree ts:t.getChildrenAsList()) {
			if(ts.isPhrasal()&&extract(qf, root, ts,target,cs)) flag=true;
		}
		return flag;
	}
	public static boolean VP_VBNP(QuestionFrame qf, Tree root, Tree t, Node target, ConstraintSet cs) {
		List<Tree> u=t.getChildrenAsList();
		if(t.label().toString().equals("VP")&&u.size()==2&&u.get(0).isPreTerminal()&&u.get(0).label().toString().startsWith("VB")&&u.get(1).isPhrasal()&&u.get(1).label().toString().equals("NP")) {
			if(stringParser.getString(t).startsWith("List")||stringParser.getString(t).startsWith("Show")||stringParser.getString(t).startsWith("Give")) return false;
			Node tmp=new Node();
			if(extract(qf, root, u.get(1),tmp,cs)) {
				cs.add(new Constraint(target,u.get(0).getChild(0).label().toString(),tmp));
			} else {
				cs.add(new Constraint(target,u.get(0).getChild(0).label().toString(),new Node(stringParser.getString(u.get(1)),root.leftCharEdge(u.get(1)),root.rightCharEdge(u.get(1)))));
			}
			return true;
		}
		return false;
	}
	public static boolean VP_VBPP(QuestionFrame qf, Tree root, Tree t, Node target, ConstraintSet cs) {
		List<Tree> u=t.getChildrenAsList();
		if(t.label().toString().equals("VP")&&u.size()==2&&u.get(0).isPreTerminal()&&u.get(0).label().toString().startsWith("VB")&&u.get(1).isPhrasal()&&u.get(1).label().toString().equals("PP")) {
			Tree pp=u.get(1);
			List<Tree> ppu=pp.getChildrenAsList();
			if(ppu.size()==2&&ppu.get(0).isPreTerminal()&&ppu.get(1).isPhrasal()&&ppu.get(1).label().toString().equals("NP")) {
				Node tmp=new Node();
				if(extract(qf, root, ppu.get(1),tmp,cs)) {
					cs.add(new Constraint(target,u.get(0).getChild(0).label().toString()+" "+ppu.get(0).getChild(0).label().toString(),tmp));
				} else {
					cs.add(new Constraint(target,u.get(0).getChild(0).label().toString()+" "+ppu.get(0).getChild(0).label().toString(),new Node(stringParser.getString(ppu.get(1)),root.leftCharEdge(ppu.get(1)),root.rightCharEdge(ppu.get(1)))));
				}
				return true;
			}
		}
		return false;
	}
	public static boolean NP_NPPP(QuestionFrame qf, Tree root, Tree t, Node target, ConstraintSet cs) {
		List<Tree> u=t.getChildrenAsList();
		if(stringParser.getString(t).startsWith("a list")) return false;
		if(t.label().toString().equals("NP")&&u.size()==2&&u.get(0).isPhrasal()&&u.get(0).label().toString().equals("NP")&&u.get(1).isPhrasal()&&u.get(1).label().toString().equals("PP")) {
			if(qf.isIntegralByChar(root.leftCharEdge(u.get(1)))) return false;
			Tree pp=u.get(1);
			List<Tree> ppu=pp.getChildrenAsList();
			if(ppu.size()==2&&ppu.get(0).isPreTerminal()&&ppu.get(1).isPhrasal()&&ppu.get(1).label().toString().equals("NP")) {
				Node tmp=new Node();
				if(extract(qf, root, ppu.get(1),tmp,cs)) {
					cs.add(new Constraint(target,stringParser.getString(u.get(0)),tmp));
				} else {
					cs.add(new Constraint(target,stringParser.getString(u.get(0)),new Node(stringParser.getString(ppu.get(1)),root.leftCharEdge(ppu.get(1)),root.rightCharEdge(ppu.get(1)))));
				}
				return true;
			} 
		}
		return false;
	}
	public static boolean SQ_VBNPVP(QuestionFrame qf, Tree root, Tree t, Node target, ConstraintSet cs) {
		List<Tree> u=t.getChildrenAsList();
		if(t.label().toString().equals("SQ")&&u.size()==3&&u.get(0).isPreTerminal()&&u.get(0).label().toString().startsWith("VB")&&u.get(1).isPhrasal()&&u.get(1).label().toString().equals("NP")&&u.get(2).isPhrasal()&&u.get(2).label().toString().equals("VP")) {
			Node tmp=new Node();
			if(extract(qf, root, u.get(1),tmp,cs)) {
				cs.add(new Constraint(tmp,stringParser.getString(u.get(2)),target));
			} else {
				cs.add(new Constraint(new Node(stringParser.getString(u.get(1)),root.leftCharEdge(u.get(1)),root.rightCharEdge(u.get(1))),stringParser.getString(u.get(2)),target));
			}
			return true;
		}
		return false;
	}
}
