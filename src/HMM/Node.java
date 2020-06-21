package HMM;

import java.util.HashMap;

import org.apache.log4j.varia.FallbackErrorHandler;

import cfg.object.ICfgNode;

public class Node {
	private ICfgNode cfgNode;
	private HashMap<Node, Float> probabilities;
	
	public Node() {
		this.probabilities = new HashMap<Node, Float>();
	}
	
	public Node(ICfgNode node) {
		this.cfgNode = node;
		this.probabilities = new HashMap<Node, Float>();
	}
	
	public void addProbability(Node node, float weight) {
		for(Node node2 :this.probabilities.keySet()) {
			if(node2.getCfgNode()==node.getCfgNode()) {
				return;
			}
		}
		if((!this.probabilities.containsKey(node)) && this.cfgNode !=node.getCfgNode()) {
			this.probabilities.put(node, (float)weight);
		}
	}
	public void updateProbability(ICfgNode cfgNode, int version) {
		Node node;
		float newValue = 0;
		for(Node node1: this.probabilities.keySet()) {
			if(node1.getCfgNode()==cfgNode) {
				newValue = this.probabilities.get(node1);
				if(version==0) {
					newValue +=0.1;
					this.probabilities.put(node1, this.probabilities.containsKey(node1)? newValue : (float)1);
					this.recomputeProbabilities();
				}
				else {
					newValue +=1;
					this.probabilities.put(node1, this.probabilities.containsKey(node1)? newValue : (float)1);
				}
				
//				this.recomputeProbabilities();
				return;
			}
		}
		
	}
	
	public void recomputeProbabilities() {
		float sum = this.getSum();
		if(sum ==0) throw new IllegalArgumentException("Did not init probability for each node!");
		this.probabilities.replaceAll((key,oldValue)->this.probabilities.get(key)/sum);
	}
	
	public float getProbability(ICfgNode cfgNode) {
		Node node = new Node();
		for(Node node1: this.probabilities.keySet()) {
			if(node1.getCfgNode()==cfgNode) {
				node = node1;
			}
		}
		return this.probabilities.get(node);
	}
	public float getSum() {
		float sum = 0;
		for(float val : this.probabilities.values()) {
			sum+=val;
		}
		
		return sum;
	}
	public ICfgNode getCfgNode() {
		return cfgNode;
	}

	public void setCfgNode(ICfgNode cfgNode) {
		this.cfgNode = cfgNode;
	}

	public HashMap<Node, Float> getProbabilities() {
		return probabilities;
	}
	

	public void setProbabilities(HashMap<Node, Float> probabilities) {
		this.probabilities = probabilities;
	}
	
}
