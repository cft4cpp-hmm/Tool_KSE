package HybridAutoTestGen;

import cfg.ICFG;
import cfg.object.AbstractConditionLoopCfgNode;
import cfg.object.ICfgNode;
import cfg.testpath.IFullTestpath;
import cfg.testpath.ITestpathInCFG;
import tree.object.IFunctionNode;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SourceGraph
{
    protected List<IFullTestpath> fullPossibleTestpaths;
    protected IFunctionNode functionNode;
    protected String pathToFile;
    protected ICFG cfg;
    protected LocalDateTime createdDate;
    protected int epoches;
    protected String loopSolution;
    protected int k;
    protected int RealLoppiterations;
    protected float statementCover;
    protected float branchCover;
    protected int realFor2loop;
    protected String _2LoopSolution;
    protected int loopCover;
    protected ITestpathInCFG pathFor2Loop;
    protected ITestpathInCFG pathForKLoop;
    protected float duration;

    public SourceGraph()
    {

    }

    public int getRealLoppiterations()
    {
        return RealLoppiterations;
    }

    public void setRealLoppiterations(int realLoppiterations)
    {
        RealLoppiterations = realLoppiterations;
    }

    public SourceGraph(LocalDateTime createdDate, ICFG cfg, List<IFullTestpath> fullPossibleIFullTestpaths, IFunctionNode functionNode, String pathtoFile)
    {

        List<IFullTestpath> fullTestpaths = fullPossibleIFullTestpaths;
        this.fullPossibleTestpaths = fullPossibleIFullTestpaths;
        this.functionNode = functionNode;
        this.pathToFile = pathtoFile;
        this.createdDate = createdDate;
        this.cfg = cfg;
        this.epoches = 1;
        this._2LoopSolution = null;
        this.loopSolution = null;

        for (int pathNumber = 0; pathNumber < this.fullPossibleTestpaths.size(); pathNumber++)
        {
            List<ICfgNode> fullCfgNodes = (ArrayList<ICfgNode>) this.fullPossibleTestpaths.get(pathNumber).getAllCfgNodes();
            fullCfgNodes = new ArrayList<ICfgNode>(fullCfgNodes);
            fullCfgNodes.remove(0);
            fullCfgNodes.remove(fullCfgNodes.size() - 1);

            for (int i = 0; i < fullCfgNodes.size(); i++)
            {
                if (fullCfgNodes.get(i).toString().contains("{") || fullCfgNodes.get(i).toString().contains("}")
                        || fullCfgNodes.get(i).toString().indexOf("[") == 0)
                {
                    fullCfgNodes.remove(i);
                    i = i - 1;
                }
            }
        }
    }

    public LocalDateTime getCreatedDate()
    {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate)
    {
        this.createdDate = createdDate;
    }

    public int getIntersection2Path(ProbTestPath path1, ProbTestPath path2)
    {
        int numOfNode = 0;
        for (ICfgNode node1_i : path1.getFullCfgNode())
        {
            for (ICfgNode node2_i : path2.getFullCfgNode())
            {
                if (node1_i == node2_i)
                {
                    numOfNode++;
                }
            }
        }
        return numOfNode - 1;
    }

    public float getDuration()
    {
        return this.duration;
    }


    public boolean hasLoop()
    {
        for (ICfgNode cfgNode : this.cfg.getAllNodes())
        {
            if (cfgNode instanceof AbstractConditionLoopCfgNode)
            {
                return true;

            }
        }
        return false;
    }

    public AbstractConditionLoopCfgNode getLastConditionNode(List<AbstractConditionLoopCfgNode> listCondition)
    {
        ICfgNode node = null;
        for (ICfgNode cfgNode : this.cfg.getAllNodes())
        {
            if (cfgNode instanceof AbstractConditionLoopCfgNode && !listCondition.contains(cfgNode))
            {
                node = cfgNode;
            }
        }
        return (AbstractConditionLoopCfgNode) node;
    }

    public ICFG getCfg()
    {
        return cfg;
    }

    public void setCfg(ICFG cfg)
    {
        this.cfg = cfg;
    }

    public String getPathToFile()
    {
        return pathToFile;
    }

    public void setPathToFile(String pathToFile)
    {
        this.pathToFile = pathToFile;
    }

    public IFunctionNode getFunctionNode()
    {
        return functionNode;
    }

    public void setFunctionNode(IFunctionNode functionNode)
    {
        this.functionNode = functionNode;
    }

    public List<IFullTestpath> getFullPossibleFullTestpaths()
    {
        return this.fullPossibleTestpaths;
    }

    public int getEpoches()
    {
        return epoches;
    }

    public void setEpoches(int epoches)
    {
        this.epoches = epoches;
    }

    public String getLoopSolution()
    {
        return loopSolution;
    }

    public void setLoopSolution(String loopSolution)
    {
        this.loopSolution = loopSolution;
    }

    public int getK()
    {
        return k;
    }

    public void setK(int k)
    {
        this.k = k;
    }

    public String get_2LoopSolution()
    {
        return _2LoopSolution;
    }

    public void set_2LoopSolution(String _2LoopSolution)
    {
        this._2LoopSolution = _2LoopSolution;
    }

    public int getRealFor2loop()
    {
        return realFor2loop;
    }

    public void setRealFor2loop(int realFor2loop)
    {
        this.realFor2loop = realFor2loop;
    }

    public int getLoopCover()
    {
        return loopCover;
    }

    public void setLoopCover(int loopCover)
    {
        this.loopCover = loopCover;
    }

    public ITestpathInCFG getPathFor2Loop()
    {
        return pathFor2Loop;
    }

    public void setPathFor2Loop(ITestpathInCFG pathFor2Loop)
    {
        this.pathFor2Loop = pathFor2Loop;
    }

    public ITestpathInCFG getPathForKLoop()
    {
        return pathForKLoop;
    }

    public void setPathForKLoop(ITestpathInCFG pathForKLoop)
    {
        this.pathForKLoop = pathForKLoop;
    }

}
