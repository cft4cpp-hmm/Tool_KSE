package HybridAutoTestGen;

import cfg.object.ICfgNode;

public class AlgNode
{
    private ICfgNode cfgNode;

    public AlgNode() {

    }

    public AlgNode(ICfgNode node) {
        this.cfgNode = node;
    }
    public ICfgNode getCfgNode() {
        return cfgNode;
    }

    public void setCfgNode(ICfgNode cfgNode) {
        this.cfgNode = cfgNode;
    }
}
