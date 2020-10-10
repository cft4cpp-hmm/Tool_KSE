package cfg.object;

import org.eclipse.cdt.core.dom.ast.IASTNode;

public abstract class ConditionCfgNode extends NormalCfgNode {
	private boolean isVisitedTrueBranch = false;

	private boolean isVisitedFalseBranch = false;
	
	private boolean isGenForBound=false; // kha_ add for bounded test gen

	public ConditionCfgNode(IASTNode node) {
		super(node);
	}

	public boolean isVisitedTrueBranch() {
		return getTrueNode().isVisited();
	}

	public void setVisitedTrueBranch(boolean isVisitedTrueBranch) {
		this.isVisitedTrueBranch = isVisitedTrueBranch;
	}

	public boolean isVisitedFalseBranch() {
		return getFalseNode().isVisited();
	}

	public void setVisitedFalseBranch(boolean isVisitedFalseBranch) {
		this.isVisitedFalseBranch = isVisitedFalseBranch;
	}

	public boolean isGenForBound() {
		return isGenForBound;
	}

	public void setGenForBound(boolean isGenForBound) {
		this.isGenForBound = isGenForBound;
	}
	

}
