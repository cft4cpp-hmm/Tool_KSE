package HybridAutoTestGen;

import HMM.HMMGraph;
import HMM.Node;
import cfg.ICFG;
import cfg.object.AbstractConditionLoopCfgNode;
import cfg.object.ConditionCfgNode;
import cfg.object.EndFlagCfgNode;
import cfg.object.ICfgNode;
import cfg.testpath.*;
import config.*;
import constraints.checker.RelatedConstraintsChecker;
import coverage.FunctionCoverageComputation;
import normalizer.FunctionNormalizer;
import org.apache.log4j.Logger;
import parser.projectparser.ProjectParser;
import testdatagen.se.ISymbolicExecution;
import testdatagen.se.Parameter;
import testdatagen.se.PathConstraint;
import testdatagen.se.SymbolicExecution;
import testdatagen.se.solver.ISmtLibGeneration;
import testdatagen.se.solver.RunZ3OnCMD;
import testdatagen.se.solver.SmtLibGeneration;
import testdatagen.se.solver.Z3SolutionParser;
import tree.object.IFunctionNode;
import tree.object.IVariableNode;
import utils.SpecialCharacter;
import utils.Utils;
import utils.search.FunctionNodeCondition;
import utils.search.Search;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Generate all possible test paths
 *
 * @author DucAnh
 */
public class CFT4CPP
{
    public static final String CONSTRAINTS_FILE = Settingv2.getValue(ISettingv2.SMT_LIB_FILE_PATH);
    public static final String Z3 = Settingv2.getValue(ISettingv2.SOLVER_Z3_PATH);
    final static Logger logger = Logger.getLogger(CFT4CPP.class);
    /**
     * Represent control flow graph
     */
    private ICFG cfg;
    private int maxIterationsforEachLoop = ITestpathGeneration.DEFAULT_MAX_ITERATIONS_FOR_EACH_LOOP;
    private FullTestpaths possibleTestpaths = new FullTestpaths();
    public List<String> testCases;
    public IFunctionNode function;

    private List<ProbTestPath> fullProbTestPaths;
    private FunctionCoverageComputation coverageComputation;
    private LocalDateTime startDateTime;
    private float testDataGenerationTime;


    public CFT4CPP(ICFG cfg1, int maxloop, String projectPath, String functionName) throws Exception
    {
        this.cfg = cfg1;
        if (cfg1 == null)
        {
            //ProjectParser parser = new ProjectParser(new File(Paths.TSDV_R1_2));
            ProjectParser parser = new ProjectParser(new File(projectPath));
            IFunctionNode function;

            function = (IFunctionNode) Search.searchNodes(parser.getRootTree(), new FunctionNodeCondition(), functionName).get(0);
            FunctionNormalizer fnNorm = ((IFunctionNode) function).normalizedAST();


            FunctionConfig config = new FunctionConfig();
            config.setCharacterBound(new ParameterBound(32, 100));
            config.setIntegerBound(new ParameterBound(0, 100));
            config.setSizeOfArray(20);
            ((IFunctionNode) function).setFunctionConfig(config);

            ICFG cfg = ((IFunctionNode) function).generateCFG();

//			CFGGenerationforSubConditionCoverage cfgGen = new CFGGenerationforSubConditionCoverage(function);

//			CFGGenerationforBranchvsStatementCoverage cfgGen = new CFGGenerationforBranchvsStatementCoverage(function);
            int maxIterations = 0;
//			ICFG cfg = cfgGen.generateCFG();
            cfg.setFunctionNode(function);
            cfg.setIdforAllNodes();
            cfg.resetVisitedStateOfNodes();
            cfg.generateAllPossibleTestpaths(maxIterations);
            this.cfg = cfg;
            this.function = function;
            this.cfg.resetVisitedStateOfNodes();
            this.cfg.setIdforAllNodes();
            this.testCases = new ArrayList<String>();
            this.maxIterationsforEachLoop = maxloop;
        }
    }


    /**
     * @param cfg
     * @param maxloop
     * @param isResetVisitedState true if the visit stated is marked unvisited
     */
    public CFT4CPP(ICFG cfg, int maxloop, boolean isResetVisitedState)
    {
        maxIterationsforEachLoop = maxloop;
        this.cfg = cfg;

        if (isResetVisitedState)
        {
            this.cfg.resetVisitedStateOfNodes();
            this.cfg.setIdforAllNodes();
        }
    }


    public static void main(String[] args) throws Exception
    {
        CFT4CPP tpGen = new CFT4CPP(null, 1, Paths.TSDV_R1_2, "foo(int,int,int,int)");
        //foo(int,int,int,int)
        //sum(int,int)
        tpGen.run();

    }

    public void run() throws Exception
    {
        startDateTime = LocalDateTime.now();
        LocalDateTime before = LocalDateTime.now();
        this.generateTestpaths(this.function);
//		LocalDateTime after = LocalDateTime.now();
//		Duration duration = Duration.between(before,after);


        Graph graph = new Graph(before, cfg, this.getPossibleTestpaths(), this.function, Paths.TSDV_R1_2, 1);
        HMMGraph hmmGraph = new HMMGraph(1);
        Node node;
        Node nextNode;
        String solution;

        for (ProbTestPath testPath : graph.getFullProbTestPaths())
        {
            for (Edge edge : testPath.getEdge())
            {
                node = new Node(edge.getNode());
                nextNode = new Node(edge.getNextNode());
                hmmGraph.addNode(node, nextNode, (float) edge.getWeight());
            }
        }

        for (int i = 0; i < this.getPossibleTestpaths().size(); i++)
        {
            FullTestpath testpath = (FullTestpath) this.getPossibleTestpaths().get(i);
            if (!testpath.getTestCase().equals(IStaticSolutionGeneration.NO_SOLUTION))
            {
                graph.updateGraph(i, 1, hmmGraph, 1);
                graph.getFullProbTestPaths().get(i).setTestCase(testpath.getTestCase());
            }
        }

        graph.createProbabilityForTestPath(hmmGraph);

        LocalDateTime afterGenForC = LocalDateTime.now();

        Duration duration = Duration.between(startDateTime, afterGenForC);

        testDataGenerationTime =  ((float)duration.toMillis()/1000);

        fullProbTestPaths = graph.getFullProbTestPaths();
        function = graph.getFunctionNode();

//        graph.computeStatementCovNew();
//        graph.computeBranchCoverNew();
//
//        graph.toHtml(LocalDateTime.now(), 0, 1, "CFT4Cpp");

    }

    public void ExportReport(String toolName) throws IOException
    {
        FileWriter csvWriter = new FileWriter(AbstractSetting.getValue("TEST_REPORT") + ".html", false);
        String valueString = "<!DOCTYPE html>\r\n" +
                "<html>\r\n" +
                "\r\n" +
                "<head> <link rel=\"stylesheet\" type=\"text/css\" href=\"hmm_report.css\">\r\n" +
                "\r\n" +
                "</head>\r\n" +
                "\r\n" +
                "<body>\r\n";

        if (toolName == "WCFT4Cpp")
        {
            valueString +=
                    "    <h2>STCFG: TEST REPORT</h2>\r\n";
        }
        else if (toolName == "CFT4Cpp")
        {
            valueString +=
                    "    <h2>STCFG: TEST REPORT</h2>\r\n";
        }
        else
        {
            valueString +=
                    "    <h2>Concolic: TEST REPORT</h2>\r\n";

        }

        valueString += "    <div class=\"table-wrapper\">\r\n" +
                "        <table class=\"fl-table\">\r\n" +
                "            <thead>\r\n" +
                "                <tr>\r\n" +
                "                    <th>PathNumber</th>\r\n" +
                "                    <th style=\"width: 800px\">Test path</th>\r\n" +
                "                    <th>Test Data</th>\r\n" +
                "                </tr>\r\n" +
                "            </thead>\r\n" +
                "            <tbody>";
        for (ProbTestPath testPath : this.fullProbTestPaths)
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
        valueString += "</tbody></table></div>";

        String loopString = "";

        valueString += "    <div class=\"table-wrapper\">\r\n" +
                "        <table class=\"fl-table\">\r\n" +
                "            <thead>\r\n" +
                "                <tr>\r\n" +
                "                    <th>Coverage information</th>\r\n" +
                "                </tr>\r\n" +
                "            </thead>\r\n" +
                "            <tbody>";

        float stateCov = ((float) coverageComputation.getNumberOfVisitedInstructions()) / ((float) coverageComputation.getNumberOfInstructions());
        float branchCov = ((float) coverageComputation.getNumberOfVisitedBranches()) / ((float) coverageComputation.getNumberOfBranches());


        String coverInfo = "";
        try
        {
            coverInfo =
                    "        <tr><td>stateCov: " + stateCov + "</td></tr>\r\n" +
                            "        <tr><td>branchCov: " + branchCov + "</td></tr>\r\n" +
                            "        <tr><td>Time: " + testDataGenerationTime + " s</td></tr>\r\n";
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        valueString += coverInfo;
        valueString += "   </tbody>\r\n" +
                "        </table></div>\r\n";


        valueString += "    <div class=\"table-wrapper\">\r\n" +
                "        <table class=\"fl-table\">\r\n" +
                "            <thead>\r\n" +
                "                <tr>\r\n" +
                "                    <th>Function raw signature</th>\r\n" +
                "                </tr>\r\n" +
                "            </thead>\r\n" +
                "            <tbody>" +
                "<tr><td><pre>" + this.function.getAST().getRawSignature().toString() +

                "</pre></td></tr></tbody></table></div>" +

                "</body></html>";
        csvWriter.append(valueString);
        csvWriter.close();

    }

    public List<TestData> getTestDataList()
    {
        List<TestData> testDataList = new ArrayList<>();
        for (ProbTestPath testPath : fullProbTestPaths)
        {
            String solution = testPath.getTestCase();
            if (!"".equals(solution) && Utils.isSolutionValid(this.function.getPassingVariables(), solution))
            {
                TestData testData = TestData.parseString(testPath.getTestCase());
                testDataList.add(testData);
            }
        }

        return testDataList;
    }

    public void generateTestpaths(IFunctionNode function)
    {
        // Date startTime = Calendar.getInstance().getTime();
        FullTestpaths testpaths_ = new FullTestpaths();

        ICfgNode beginNode = cfg.getBeginNode();
        FullTestpath initialTestpath = new FullTestpath();
        initialTestpath.setFunctionNode(cfg.getFunctionNode());
        try
        {
            traverseCFG(beginNode, initialTestpath, testpaths_, function);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        for (ITestpathInCFG tp : testpaths_)
        {
            tp.setFunctionNode(cfg.getFunctionNode());
        }

        possibleTestpaths = testpaths_;
    }

    private void traverseCFG(ICfgNode stm, FullTestpath tp, FullTestpaths testpaths, IFunctionNode function) throws Exception
    {

        tp.add(stm);
        FullTestpath tp1 = (FullTestpath) tp.clone();
        FullTestpath tp2 = (FullTestpath) tp.clone();
//		System.out.println(this.haveSolution(tp, finalConditionType)+tp.getFullPath());
//		System.out.println(stm.toString());
        if (stm instanceof EndFlagCfgNode)
        {
            FullTestpath tpclone = (FullTestpath) tp.clone();
            tpclone.setTestCase(this.solveTestpath(function, tp));
            testpaths.add(tpclone);
            testCases.add(tpclone.getTestCase());

            tp.remove(tp.size() - 1);
        }
        else
        {
            ICfgNode trueNode = stm.getTrueNode();
            ICfgNode falseNode = stm.getFalseNode();

            if (stm instanceof ConditionCfgNode)
            {

                if (stm instanceof AbstractConditionLoopCfgNode)
                {

                    int currentIterations = tp.count(trueNode);
                    if (currentIterations < maxIterationsforEachLoop)
                    {
                        tp1.add(falseNode);
//                        if (this.haveSolution(tp1, false))
//                        {
                            traverseCFG(falseNode, tp, testpaths, function);
//                        }
                        tp2.add(trueNode);
//                        if (this.haveSolution(tp2, true))
//                        {
                            traverseCFG(trueNode, tp, testpaths, function);
//                        }

//						traverseCFG(trueNode, tp, testpaths,function);
                    }
                    else
                    {
                        tp1.add(falseNode);
//                        if (this.haveSolution(tp1, false))
//                        {
                            traverseCFG(falseNode, tp, testpaths, function);
//                        }

                    }
                }
                else
                {
                    tp1.add(falseNode);

//                    if (this.haveSolution(tp1, false))
//                    {
                        traverseCFG(falseNode, tp, testpaths, function);
//                    }

                    tp2.add(trueNode);
//                    if (this.haveSolution(tp2, true))
//                    {
                        traverseCFG(trueNode, tp, testpaths, function);
//                    }
                }
            }
            else
            {
                traverseCFG(trueNode, tp, testpaths, function);
            }

            tp.remove(tp.size() - 1);
        }
    }

    protected boolean haveSolution(FullTestpath tp, boolean finalConditionType) throws Exception
    {
        IPartialTestpath tp1 = createPartialTestpath(tp, finalConditionType);

        String solution = solveTestpath(cfg.getFunctionNode(), tp1);

        if (!solution.equals(IStaticSolutionGeneration.NO_SOLUTION))
        {
            return true;
        }
        else
        {
            return false;
        }
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

    protected String solveTestpath(IFunctionNode function, ITestpathInCFG testpath) throws Exception
    {
        /*
         * Get the passing variables of the given function
         */
        Parameter paramaters = new Parameter();
        for (IVariableNode n : function.getArguments())
        {
            paramaters.add(n);
        }
        for (IVariableNode n : function.getReducedExternalVariables())
        {
            paramaters.add(n);
        }

        /*
         * Get the corresponding path constraints of the test path
         */
        ISymbolicExecution se = new SymbolicExecution(testpath, paramaters, function);

        // fast checking
        RelatedConstraintsChecker relatedConstraintsChecker = new RelatedConstraintsChecker(
                se.getConstraints().getNormalConstraints(), function);
        boolean isRelated = relatedConstraintsChecker.check();
        //
        if (isRelated)
        {
            if (se.getConstraints().getNormalConstraints().size()
                    + se.getConstraints().getNullorNotNullConstraints().size() > 0)
            {
                /*
                 * Solve the path constraints
                 */
                ISmtLibGeneration smtLibGen = new SmtLibGeneration(function.getPassingVariables(),
                        se.getConstraints().getNormalConstraints());
                smtLibGen.generate();

                Utils.writeContentToFile(smtLibGen.getSmtLibContent(), CONSTRAINTS_FILE);

                RunZ3OnCMD z3 = new RunZ3OnCMD(Z3, CONSTRAINTS_FILE);
                z3.execute();
                logger.debug("solving done");
                String staticSolution = new Z3SolutionParser().getSolution(z3.getSolution());

                if (staticSolution.equals(IStaticSolutionGeneration.NO_SOLUTION))
                {
                    return IStaticSolutionGeneration.NO_SOLUTION;
                }
                else
                {
                    if (se.getConstraints().getNullorNotNullConstraints().size() > 0)
                    {
                        for (PathConstraint nullConstraint : se.getConstraints().getNullorNotNullConstraints())
                        {
                            staticSolution += nullConstraint + SpecialCharacter.END_OF_STATEMENT;
                        }
                    }

                    if (se.getConstraints().getNullorNotNullConstraints().size() > 0)
                    {
                        return staticSolution + "; " + se.getConstraints().getNullorNotNullConstraints();
                    }
                    else
                    {
                        return staticSolution + ";";
                    }
                }
            }
            else
            {
                return IStaticSolutionGeneration.NO_SOLUTION;
            }
        }
        else
        {
            return IStaticSolutionGeneration.EVERY_SOLUTION;
        }
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

    public List<String> getTestCases()
    {
        return this.testCases;
    }

    public FunctionCoverageComputation getCoverageComputation()
    {
        return coverageComputation;
    }

    public void setCoverageComputation(FunctionCoverageComputation coverageComputation)
    {
        this.coverageComputation = coverageComputation;
    }
}
