package HybridAutoTestGen;


import cfg.object.ICfgNode;

import java.util.ArrayList;
import java.util.List;

public class AlgorithmGraph
{
    private List<AlgNode> nodes;
    public AlgorithmGraph() {
        nodes = new ArrayList<AlgNode>();
    }
    public void addNode(AlgNode node) {
        for(AlgNode node1: nodes) {
            if(node1.getCfgNode() == node.getCfgNode()) {
                return ;
            }
        }
        nodes.add(node);

    }

    public AlgNode getNode(ICfgNode iCfgNode) {
        for(AlgNode node: nodes) {
            if(node.getCfgNode()==iCfgNode) {
                return node;
            }
        }
        return null;
    }
}
