package HMM;

import java.util.ArrayList;
import java.util.List;

import cfg.ICFG;
import cfg.object.ICfgNode;

public class HMMGraph {
	private List<Node> nodes;
	private int version;
	public HMMGraph(int version) {
		nodes = new ArrayList<Node>();
		this.version = version;
	}
	public void addNode(Node node, Node nextNode, float weight) {
		for(Node node1: nodes) {
			if(node1.getCfgNode() == node.getCfgNode()) {
				node1.addProbability(nextNode,weight);
				return ;
			}
			
		}
		nodes.add(node);
		node.addProbability(nextNode,weight);
		
	}
	public void recomputeProbability() {
		if(this.version==1) {
			return ;
		}
		for(Node node: nodes) {
			node.recomputeProbabilities();
		}
	}
	
	public Node getNode(ICfgNode iCfgNode) {
		for(Node node: nodes) {
			if(node.getCfgNode()==iCfgNode) {
				return node;
			}
		}
		return null;
	}
}
