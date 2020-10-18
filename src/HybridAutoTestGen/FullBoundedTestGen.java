package HybridAutoTestGen;

import cfg.CFG;
import cfg.CFGGenerationforSubConditionCoverage;
import cfg.ICFG;
import cfg.object.ICfgNode;
import cfg.testpath.*;
import config.*;
import coverage.FunctionCoverageComputation;
import normalizer.FunctionNormalizer;
import org.apache.log4j.Logger;
import parser.projectparser.ProjectParser;
import tree.object.IFunctionNode;
import tree.object.IProjectNode;
import tree.object.IVariableNode;
import utils.Utils;
import utils.search.FunctionNodeCondition;
import utils.search.Search;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class FullBoundedTestGen
{
    public static final String CONSTRAINTS_FILE = Settingv2.getValue(ISettingv2.SMT_LIB_FILE_PATH);
    public static final String Z3 = Settingv2.getValue(ISettingv2.SOLVER_Z3_PATH);
    final static Logger logger = Logger.getLogger(FullBoundedTestGen.class);
    /**
     * Represent control flow graph
     */
    private ICFG cfg;
    private IFunctionNode function;
    private int maxIterationsforEachLoop;
    private FullTestpaths possibleTestpaths = new FullTestpaths();
    private List<IVariableNode> variables;
    private String sourceFolder;
    private FunctionCoverageComputation functionCoverageComputation;
    private List<TestData> testCases;
    public IProjectNode projectNode;
    private float boundStep = 1;
    private int maxloop = 0;
    private String functionName;
    LocalDateTime beforeTestDataGenerationTime;
    LocalDateTime afterTestDataGenerationTime;

    public FullBoundedTestGen()
    {
        // TODO Auto-generated constructor stub
        this.cfg = cfg;
    }

    public FullBoundedTestGen(int _maxloop, String _functionName, String _sourceFolder) throws Exception
    {
        maxloop = _maxloop;
        sourceFolder = _sourceFolder;
        functionName = _functionName.replace(" ", "");
    }


    public static void main(String[] args) throws Exception
    {
        FullBoundedTestGen tpGen = new FullBoundedTestGen(1, "PDF(int,int,int)", Paths.TSDV_R1_2);

        tpGen.boundaryValueTestGen();

    }

    public void boundaryValueTestGen() throws Exception
    {
        ProjectParser parser = new ProjectParser(new File(sourceFolder));

        projectNode = parser.getRootTree();

        function = (IFunctionNode) Search.searchNodes(projectNode, new FunctionNodeCondition(), functionName).get(0);

        //Sinh dữ liệu test theo biên, cần phải sinh CFG theo sub condition thì mới có được các điều kiện
        FunctionConfig functionConfig = new FunctionConfig();
        functionConfig.setSolvingStrategy(ISettingv2.SUPPORT_SOLVING_STRATEGIES[0]);
        ((IFunctionNode) function).setFunctionConfig(functionConfig);
        FunctionNormalizer fnNorm = ((IFunctionNode) function).normalizedAST();
        String normalizedCoverage = fnNorm.getNormalizedSourcecode();
        ((IFunctionNode) function).setAST(fnNorm.getNormalizedAST());
        IFunctionNode clone = (IFunctionNode) function.clone();
        clone.setAST(Utils.getFunctionsinAST(normalizedCoverage.toCharArray()).get(0));
        CFGGenerationforSubConditionCoverage cfgGen = new CFGGenerationforSubConditionCoverage(clone);

        cfg = (CFG) cfgGen.generateCFG();
        cfg.setFunctionNode(clone);

        this.cfg.resetVisitedStateOfNodes();
        this.cfg.setIdforAllNodes();
        this.setTestCases(new ArrayList<TestData>());
        this.maxIterationsforEachLoop = maxloop;
        this.variables = function.getArguments();

        beforeTestDataGenerationTime = LocalDateTime.now();
        List<TestData> boundTestDataList = Utils.generateTestpathsForBoundaryTestGen(cfg, function.getPassingVariables(), 1);

        getTestCases().addAll(boundTestDataList);

        afterTestDataGenerationTime = LocalDateTime.now();

    }

    public void ExportReport() throws IOException
    {
        Duration duration = Duration.between(beforeTestDataGenerationTime, afterTestDataGenerationTime);

        float diff = Math.abs((float) duration.toMillis() / 1000);

        FileWriter csvWriter = new FileWriter(AbstractSetting.getValue("TEST_REPORT") + ".html", false);

        String valueString = "<!DOCTYPE html>\r\n" +
                "<html>\r\n" +
                "\r\n" +
                "<head> <link rel=\"stylesheet\" type=\"text/css\" href=\"hmm_report.css\">\r\n" +
                "\r\n" +
                "</head>\r\n" +
                "\r\n" +
                "<body>\r\n" +
                "    <h2>BVTG: TEST REPORT</h2>\r\n" +

                "    <div class=\"table-wrapper\">\r\n" +
                "        <table class=\"fl-table\">\r\n" +
                "            <thead>\r\n" +
                "                <tr>\r\n" +
                "                    <th>Boundary generated test data</th>\r\n" +
                "                </tr>\r\n" +
                "            </thead>\r\n" +
                "            <tbody>";
        for (TestData testcase : getTestCases())
        {
            valueString += "<tr><td>" + testcase.toString() + "</td></tr>";
        }

        valueString += "        <tr><td>Number of test data: " + getTestCases().size() + "; Elapsed time: " + diff + " s</td></tr>";

        valueString += "            </tbody></table></div>";

        valueString += "   </tbody>\r\n" +
                "        </table></div>\r\n" +
                "<div  class=\"table-wrapper\">" +
                "        <table class=\"fl-table\">\r\n" +
                "            <thead>\r\n" +
                "                <tr>\r\n" +
                "                    <th>Function raw signature</th>\r\n" +
                "                </tr>\r\n" +
                "            </thead>\r\n" +
                "            <tbody>" +
                "<tr><td><pre>" + this.function.getAST().getRawSignature().toString() +

                "</pre></tr></td>" +
                "            </tbody></table></div>"+

                "</body></html>";
        csvWriter.append(valueString);
        csvWriter.close();

    }

    protected IPartialTestpath createPartialTestpath(FullTestpath fullTp, boolean finalConditionType)
    {
        IPartialTestpath partialTp = new PartialTestpath();
		for (ICfgNode node : fullTp.getAllCfgNodes())
		{
			partialTp.getAllCfgNodes().add(node);
		}

        partialTp.setFinalConditionType(finalConditionType);
        return partialTp;
    }

    public ICFG getCfg()
    {
        return cfg;
    }

    public void setCfg(ICFG cfg)
    {
        this.cfg = cfg;
    }

    public int getMaxIterationsforEachLoop()
    {
        return maxIterationsforEachLoop;
    }

    public void setMaxIterationsforEachLoop(int maxIterationsforEachLoop)
    {
        this.maxIterationsforEachLoop = maxIterationsforEachLoop;
    }

    public FullTestpaths getPossibleTestpaths()
    {
        return possibleTestpaths;
    }

    public void setFunctionNode(IFunctionNode functionNode)
    {
        this.function = functionNode;
    }

    public IFunctionNode getFunctionNode()
    {
        return this.function;
    }

    public FunctionCoverageComputation getFunctionCoverageComputation()
    {
        return functionCoverageComputation;
    }

    public void setFunctionCoverageComputation(FunctionCoverageComputation functionCoverageComputation)
    {
        this.functionCoverageComputation = functionCoverageComputation;
    }

    public List<TestData> getTestCases()
    {
        return testCases;
    }

    public void setTestCases(List<TestData> testCases)
    {
        this.testCases = testCases;
    }
}
