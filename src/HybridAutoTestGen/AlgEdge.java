package HybridAutoTestGen;

import cfg.object.ICfgNode;

public class AlgEdge
{
    protected ICfgNode node;
    protected ICfgNode nextNode;
    protected int pathNumber;
    protected boolean isVisited;

    public AlgEdge(ICfgNode node, ICfgNode nextNode, int pathNumber)
    {
        this.node = node;
        this.nextNode = nextNode;
        this.pathNumber = pathNumber;
    }

    public ICfgNode getNode()
    {
        return node;
    }

    public void setNode(ICfgNode node)
    {
        this.node = node;
    }

    public ICfgNode getNextNode()
    {
        return nextNode;
    }

    public void setNextNode(ICfgNode nextNode)
    {
        this.nextNode = nextNode;
    }

    public int getPathNumber()
    {
        return pathNumber;
    }

    public void setPathNumber(int pathNumber)
    {
        this.pathNumber = pathNumber;
    }

    public void setIsVisited()
    {
        this.isVisited = true;
    }

    public boolean isVisited()
    {
        return this.isVisited;
    }
}
