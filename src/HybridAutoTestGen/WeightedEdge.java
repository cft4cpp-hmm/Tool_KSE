package HybridAutoTestGen;

import cfg.object.ICfgNode;

public class WeightedEdge extends AlgEdge
{
    private float weight;

    public WeightedEdge(ICfgNode node, ICfgNode nextNode, int pathNumber)
    {
        super(node, nextNode, pathNumber);
        this.node = node;
        this.nextNode = nextNode;
        this.weight = 1;
        this.pathNumber = pathNumber;
    }

    public void addWeight(int unit)
    {
        this.weight += unit;
    }

    public float getWeight()
    {
        return weight;
    }

    public void setWeight(float weight)
    {
        this.weight = weight;
    }
}
