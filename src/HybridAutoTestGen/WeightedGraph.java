package HybridAutoTestGen;

import cfg.ICFG;
import cfg.object.ICfgNode;
import cfg.testpath.IFullTestpath;
import config.AbstractSetting;
import testdata.object.TestpathString_Marker;
import testdatagen.coverage.CFGUpdater_Mark;
import testdatagen.se.ISymbolicExecution;
import testdatagen.se.Parameter;
import testdatagen.se.SymbolicExecution;
import tree.object.FunctionNode;
import tree.object.IFunctionNode;
import tree.object.INode;

import java.io.FileWriter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WeightedGraph extends SourceGraph
{
    private List<WeightedTestPath> fullWeightedTestPaths;
    protected List<WeightedNode> nodes;

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

    public WeightedGraph(LocalDateTime createdDate, ICFG cfg, List<IFullTestpath> fullPossibleIFullTestpaths, IFunctionNode functionNode, String pathtoFile) {
        super(createdDate, cfg, fullPossibleIFullTestpaths, functionNode, pathtoFile);
        nodes = new ArrayList<WeightedNode>();

        this.fullWeightedTestPaths = new ArrayList<WeightedTestPath>();

        for (int pathNumber = 0; pathNumber < super.fullPossibleTestpaths.size(); pathNumber++)
        {
            List<ICfgNode> fullCfgNodes = (ArrayList<ICfgNode>) this.fullPossibleTestpaths.get(pathNumber).getAllCfgNodes();
            fullCfgNodes = new ArrayList<ICfgNode>(fullCfgNodes);
            fullCfgNodes.remove(0);
            fullCfgNodes.remove(fullCfgNodes.size() - 1);

            WeightedTestPath weightedTestPath = new WeightedTestPath((pathNumber));

            for (int i = 0; i < fullCfgNodes.size() - 1; i++)
            {
                WeightedEdge weightedEdge = new WeightedEdge(fullCfgNodes.get(i), fullCfgNodes.get(i + 1), pathNumber);
                weightedTestPath.addEdge(weightedEdge);
            }
            this.fullWeightedTestPaths.add(weightedTestPath);
        }
    }

    public void updateWeightForPath(int pathNumber, int weight)
    {
        WeightedTestPath testPath = this.fullWeightedTestPaths.get(pathNumber);
        testPath.setGenerated(true);
        for (ICfgNode cfgNode : testPath.getFullCfgNode())
        {
            cfgNode.setVisit(true);
        }
        for (WeightedEdge edge : testPath.getEdge())
        {
            edge.setIsVisited();
            for (WeightedTestPath testPath1 : this.fullWeightedTestPaths)
            {
                for (WeightedEdge edge1 : testPath1.getEdge())
                {
                    if (testPath1 != testPath && edge.getNode() == edge1.getNode() &&
                            edge.getNextNode() == edge1.getNextNode())
                    {
                        edge1.setIsVisited();
                    }
                }
            }

        }

        for (WeightedTestPath testPath2 : this.fullWeightedTestPaths)
        {
            for (WeightedEdge edge : testPath2.getEdge())
            {
                edge.setWeight(weight);
            }
        }

    }


    public float computeBranchCover() throws Exception
    {
        Set<WeightedEdge> setEdges = new HashSet<WeightedEdge>();
        Set<WeightedEdge> visitedEdges = new HashSet<WeightedEdge>();
        for (WeightedTestPath testPath : this.getFullWeightedTestPaths())
        {
            for (WeightedEdge edge : testPath.getEdge())
            {
                if (edge.isVisited())
                {
                    visitedEdges.add(edge);
                }
                setEdges.add(edge);
            }
        }

        if (visitedEdges.size() != 0)
        {
            this.branchCover = (float) visitedEdges.size() / setEdges.size();
        }
        return this.branchCover;
    }

    public float computeBranchCover(List<String> testDatas) throws Exception
    {
        for (WeightedTestPath path : this.getFullWeightedTestPaths())
        {
            testDatas.add(path.getTestCase());
        }
        WeightedFunctionExecution probFunction = new WeightedFunctionExecution(this, WeightedCFGTestGEn.pathToZ3,
                WeightedCFGTestGEn.pathToMingw32, WeightedCFGTestGEn.pathToGCC, WeightedCFGTestGEn.pathToGPlus);
        TestpathString_Marker testpath;
        for (String testData : testDatas)
        {
            testpath = probFunction.getEncodedPath(testData.replace(";;", ";"));
            CFGUpdater_Mark updater = new CFGUpdater_Mark(testpath, this.getCfg());
        }

        probFunction.deleteClone();
        return this.getCfg().computeBranchCoverage();
    }

    public float computeStatementCov()
    {
        int visitedNode = 0;
        for (ICfgNode cfgNode : this.cfg.getAllNodes())
        {
            if (cfgNode.isVisited())
            {
                visitedNode++;
            }
        }

        if (visitedNode != 0)
        {
            this.statementCover = (float) visitedNode / this.getAllCFGNode();

        }
        return this.statementCover;

    }


    public void computeProbabilityForAllPath(int version)
    {
        WeightedNode node;
        WeightedNode nextNode;
        for (WeightedTestPath probTestPath : this.fullWeightedTestPaths)
        {
            for (WeightedEdge edge : probTestPath.getEdge())
            {
                node = new WeightedNode(edge.getNode());
                nextNode = new WeightedNode(edge.getNextNode());
            }
        }
    }

    public void setVisitedPath(int pathNumber)
    {
        this.fullWeightedTestPaths.get(pathNumber).setGenerated(true);
    }

    public int countVisitedNode()
    {
        int count = 0;
        for (WeightedTestPath testPath : this.fullWeightedTestPaths)
        {
            if (testPath.isGenerated())
            {
                count++;
            }
        }
        return count;
    }

    public float getCoverage()
    {
        return (float) this.countVisitedNode() / (this.fullWeightedTestPaths.size());
    }

    public int getNewPath()
    {
        int weight = -1;
        int index = -1;
        for (WeightedTestPath testPath : this.fullWeightedTestPaths)
        {
            if (testPath.isGenerated() == false && testPath.getVisitedNumber() < this.epoches)
            {
                if (testPath.getWeight() >= weight)
                {
                    weight = testPath.getWeight();
                    index = this.fullWeightedTestPaths.indexOf(testPath);
                    break;
                }
            }
        }
        if (index != -1)
        {
            this.fullWeightedTestPaths.get(index).setVisitedNumber(1);
        }

        return index;
    }

    public void exportReport(LocalDateTime diff1, int coverage, float timeForLoop, String toolName) throws Exception
    {
        Duration duration = Duration.between(this.createdDate, diff1);

        float diff = Math.abs((float) duration.toMillis() / 1000);
        this.duration = diff;
        FileWriter csvWriter = new FileWriter(AbstractSetting.getValue("TEST_REPORT") + ".html", false);
        String valueString = "<!DOCTYPE html>\r\n" +
                "<html>\r\n" +
                "\r\n" +
                "<head> <link rel=\"stylesheet\" type=\"text/css\" href=\"hmm_report.css\">\r\n" +
                "\r\n" +
                "</head>\r\n" +
                "\r\n" +
                "<body>\r\n" +
                "    <h2>TEST REPORT</h2>\r\n" +

                "    <div class=\"table-wrapper\">\r\n" +
                "        <table class=\"fl-table\">\r\n" +
                "            <thead>\r\n" +
                "                <tr>\r\n" +
                "                    <th>PathNumber</th>\r\n" +
                "                    <th style=\"width: 800px\">Test path</th>\r\n" +
                "                    <th>Test Data</th>\r\n" +
                "                </tr>\r\n" +
                "            </thead>\r\n" +
                "            <tbody>";
        for (WeightedTestPath testPath : this.getFullWeightedTestPaths())
        {
            if (toolName == "WCFT4Cpp")
            {
                valueString += testPath.toString();
            }
            else
            {
                valueString += testPath.toStringForCFT4Cpp();
            }
        }

        String loopString = "";

        valueString += loopString;
        float stateCov = this.getCfg().computeStatementCoverage();
        float branchCov = this.getCfg().computeBranchCoverage();

        String coverInfo = "";
        try
        {
            coverInfo =
                    "        <div>C2 Coverage " + this.computeStatementCov() + "</div></div>\r\n"+
            "        <div>stateCov " + stateCov + "</div></div>\r\n"+
            "        <div>branchCov " + branchCov + "</div></div>\r\n";
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        valueString += coverInfo;
        valueString += "   <tbody>\r\n" +
                "        </table></div>\r\n" +
                "<div class=\"conlusion\">\n" +
                "<pre>" + this.functionNode.getAST().getRawSignature().toString() +

                "</pre>" +

                "        <div>Time For " + (coverage == 0 ? "C2 :" : "C3: ") + diff + "s</div>\r\n" +
//
//                coverInfo +
                "</body></html>";
        csvWriter.append(valueString);
        csvWriter.close();
    }

    public void addConstraint() throws Exception
    {
        Parameter paramaters = new Parameter();
        for (INode n : ((FunctionNode) this.functionNode).getArguments())
        {
            paramaters.add(n);
        }
        for (INode n : ((FunctionNode) this.functionNode).getReducedExternalVariables())
        {
            paramaters.add(n);
        }
        for (IFullTestpath fullTestpath : this.getFullPossibleFullTestpaths())
        {
            ISymbolicExecution se = new SymbolicExecution(fullTestpath, paramaters, this.functionNode);
            int path = this.getFullPossibleFullTestpaths().indexOf(fullTestpath);
            this.getFullWeightedTestPaths().get(path).setConstraints(se.getConstraints());
        }

    }

    public int getAllCFGNode()
    {
        Set<ICfgNode> nodes = new HashSet();
        for (WeightedTestPath testPath : this.getFullWeightedTestPaths())
        {
            for (ICfgNode node : testPath.getFullCfgNode())
            {
                nodes.add(node);
            }
        }
        return nodes.size();
    }

    public List<WeightedTestPath> getFullWeightedTestPaths()
    {
        return fullWeightedTestPaths;
    }
}
