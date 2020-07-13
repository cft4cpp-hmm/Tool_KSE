package HybridAutoTestGen;


import cfg.object.ICfgNode;

import java.util.ArrayList;
import java.util.List;

public class AlgorithmGraph
{
    protected List<WeightedNode> nodes;
    public AlgorithmGraph() {
        nodes = new ArrayList<WeightedNode>();
    }
    public void addNode(WeightedNode node) {
        for(WeightedNode node1: nodes) {
            if(node1.getCfgNode() == node.getCfgNode()) {
                return ;
            }
        }
        nodes.add(node);

    }

    public WeightedNode getNode(ICfgNode iCfgNode) {
        for(WeightedNode node: nodes) {
            if(node.getCfgNode()==iCfgNode) {
                return node;
            }
        }
        return null;
    }
}
