package HybridAutoTestGen;

import cfg.object.ICfgNode;

public class WeightedNode
{
    private ICfgNode cfgNode;

    public WeightedNode() {

    }

    public WeightedNode(ICfgNode node) {
        this.cfgNode = node;
    }
    public ICfgNode getCfgNode() {
        return cfgNode;
    }

    public void setCfgNode(ICfgNode cfgNode) {
        this.cfgNode = cfgNode;
    }
}
