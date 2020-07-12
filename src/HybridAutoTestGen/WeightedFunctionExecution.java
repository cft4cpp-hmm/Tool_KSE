package HybridAutoTestGen;

import cfg.ICFG;
import config.Paths;
import parser.projectparser.ProjectParser;
import testdata.object.TestpathString_Marker;
import testdatagen.FunctionExecution;
import tree.object.IFunctionNode;
import tree.object.INode;
import utils.Utils;
import utils.search.FunctionNodeCondition;
import utils.search.Search;

import java.io.File;
import java.io.IOException;

public class WeightedFunctionExecution extends FunctionExecution
{
    private SourceGraph graph;
    private File clone;

    public WeightedFunctionExecution(SourceGraph graph, String pathToZ3, String pathToMingw32, String pathToGCC,
                                     String pathToGPlus) throws Exception
    {
        super(pathToZ3, pathToMingw32, pathToGCC, pathToGPlus);
        this.graph = graph;
        String testedProject = graph.getPathToFile();
        clone = Utils.copy(testedProject);
        Paths.CURRENT_PROJECT.CLONE_PROJECT_PATH = clone.getAbsolutePath();
        ProjectParser parser = new ProjectParser(clone);
        INode function = Search
                .searchNodes(parser.getRootTree(), new FunctionNodeCondition(), graph.getFunctionNode().getName())
                .get(0);
        this.setTestedFunction((IFunctionNode) function);
        this.setClonedProject(clone.getCanonicalPath());
        this.setCFG(this.graph.getCfg());
    }

    public WeightedFunctionExecution(String PathToFile, String functionName, ICFG cfg) throws IOException
    {
        super(WeightedCFGTestGEn.pathToZ3, WeightedCFGTestGEn.pathToMingw32, WeightedCFGTestGEn.pathToGCC, WeightedCFGTestGEn.pathToGPlus);
        String testedProject = PathToFile;
        clone = Utils.copy(testedProject);
        Paths.CURRENT_PROJECT.CLONE_PROJECT_PATH = clone.getAbsolutePath();
        ProjectParser parser = new ProjectParser(clone);
        INode function = Search
                .searchNodes(parser.getRootTree(), new FunctionNodeCondition(), functionName)
                .get(0);
        this.setTestedFunction((IFunctionNode) function);
        this.setClonedProject(clone.getCanonicalPath());
        this.setCFG(cfg);
    }

    public TestpathString_Marker getEncodedPath(String preparedInput)
    {
        try
        {

            this.setPreparedInput(preparedInput);
            TestpathString_Marker testpathString_Marker = this.analyze(this.getTestedFunction(), this.getPreparedInput());

            return testpathString_Marker;

        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }

    }

    public void deleteClone()
    {
        Utils.deleteFileOrFolder(clone);
    }

}
