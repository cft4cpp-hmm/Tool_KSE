package HybridAutoTestGen;

import cfg.CFG;
import cfg.CFGGenerationforSubConditionCoverage;
import cfg.ICFG;
import cfg.object.AbstractConditionLoopCfgNode;
import cfg.object.ConditionCfgNode;
import cfg.object.EndFlagCfgNode;
import cfg.object.ICfgNode;
import cfg.testpath.*;
import config.FunctionConfig;
import config.ISettingv2;
import config.ParameterBound;
import config.Settingv2;
import constraints.checker.RelatedConstraintsChecker;
import javafx.application.Application;
import javafx.stage.Stage;
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
import tree.object.IProjectNode;
import tree.object.IVariableNode;
import utils.ASTUtils;
import utils.SpecialCharacter;
import utils.Utils;
import utils.search.FunctionNodeCondition;
import utils.search.Search;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HybridAutoTestGen extends Application
{

    public static final String CONSTRAINTS_FILE = Settingv2.getValue(ISettingv2.SMT_LIB_FILE_PATH);
    public static final String Z3 = Settingv2.getValue(ISettingv2.SOLVER_Z3_PATH);
    final static Logger logger = Logger.getLogger(FullBoundedTestGen.class);
    /**
     * Represent control flow graph
     */
    private ICFG cfg;
    private int maxIterationsforEachLoop = ITestpathGeneration.DEFAULT_MAX_ITERATIONS_FOR_EACH_LOOP;
    private FullTestpaths possibleTestpaths = new FullTestpaths();
    public List<String> testCases;
    public IFunctionNode function;
    public IProjectNode projectNode;
    private int maxloop = 0;
    private String functionName;
    private String sourceFolder;
    private List<IVariableNode> variables;
    private float boundStep = 1;

    public static void main(String[] args)
    {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage)
    {

    }

    public HybridAutoTestGen(int _maxloop, String _functionName, String _sourceFolder, float _boundStep)
    {
        maxloop = _maxloop;
        functionName = _functionName;
        sourceFolder = _sourceFolder;
        boundStep = _boundStep;
    }

    //public void generateTestData(int maxloop, String functionName, String sourceFolder) throws Exception
    public void generateTestData() throws Exception
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
        this.testCases = new ArrayList<String>();
        this.maxIterationsforEachLoop = maxloop;
        this.variables = function.getArguments();

        LocalDateTime before1 = LocalDateTime.now();
        this.generateTestpathsForBoundaryTestGen();

        LocalDateTime after1 = LocalDateTime.now();

        Duration duration = Duration.between(before1, after1);

        float diff1 = Math.abs((float) duration.toMillis() / 1000);


        //Sinh dữ liệu test theo CFG có trọng số

        function.normalizedAST();
        FunctionConfig config = new FunctionConfig();
        config.setCharacterBound(new ParameterBound(32, 100));
        config.setIntegerBound(new ParameterBound(0, 100));
        config.setSizeOfArray(20);

        function.setFunctionConfig(config);

        cfg = function.generateCFG();

        cfg.setFunctionNode(function);
        this.cfg.resetVisitedStateOfNodes();
        this.cfg.setIdforAllNodes();
        this.maxIterationsforEachLoop = maxloop;
        this.variables = function.getArguments();

        LocalDateTime before = LocalDateTime.now();
        this.generateTestpaths();

        //create weighted test paths
        WeightedGraph graph = new WeightedGraph(before, cfg, this.getPossibleTestpaths(),
                this.function, sourceFolder);

        //Generate test data
        for (int i = 0; i < this.getPossibleTestpaths().size(); i++)
        {
            FullTestpath testpath = (FullTestpath) this.getPossibleTestpaths().get(i);
            FullTestpath tpclone = (FullTestpath) testpath.clone();
            tpclone.setTestCase(this.solveTestpath(function, testpath));

            String testcase = tpclone.getTestCase().replaceAll(";;", ";");

            if (!testCases.contains(testcase) && !testcase.equals(IStaticSolutionGeneration.NO_SOLUTION))
            {
                testCases.add(testcase);
            }

            if (!tpclone.getTestCase().equals(IStaticSolutionGeneration.NO_SOLUTION))
            {
                graph.updateWeightForPath(i, 1);
                graph.getFullWeightedTestPaths().get(i).setTestCase(tpclone.getTestCase());
            }
        }

        LocalDateTime after = LocalDateTime.now();

        Duration duration2 = Duration.between(before, after);

        float diff2 = Math.abs((float) duration2.toMillis() / 1000);


        float durationTotal = diff1 + diff2;

        graph.computeBranchCoverNew();
        graph.computeStatementCovNew();

        graph.exportReport(durationTotal, 0, 1, "Hybrid", testCases);

    }

    public void generateTestpaths()
    {
        FullTestpaths testpaths_ = new FullTestpaths();

        ICfgNode beginNode = cfg.getBeginNode();
        FullTestpath initialTestpath = new FullTestpath();
        initialTestpath.setFunctionNode(cfg.getFunctionNode());
        try
        {
            traverseCFG(beginNode, initialTestpath, testpaths_);
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

    public void generateTestpathsForBoundaryTestGen()
    {
        FullTestpaths testpaths_ = new FullTestpaths();

        ICfgNode beginNode = cfg.getBeginNode();
        FullTestpath initialTestpath = new FullTestpath();
        initialTestpath.setFunctionNode(cfg.getFunctionNode());
        try
        {
            traverseCFGForBoundaryTestGen(beginNode, initialTestpath, testpaths_);
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

    private void traverseCFG(ICfgNode stm, FullTestpath tp, FullTestpaths testpaths) throws Exception
    {
        tp.add(stm);
        if (stm instanceof EndFlagCfgNode)
        {
            testpaths.add((FullTestpath) tp.clone());
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
                        traverseCFG(falseNode, tp, testpaths);
                        traverseCFG(trueNode, tp, testpaths);
                    }
                    else
                    {
                        traverseCFG(falseNode, tp, testpaths);
                    }
                }
                else
                {
                    traverseCFG(falseNode, tp, testpaths);
                    traverseCFG(trueNode, tp, testpaths);
                }
            }
            else
            {
                traverseCFG(trueNode, tp, testpaths);
            }

            tp.remove(tp.size() - 1);
        }
    }

    private void traverseCFGForBoundaryTestGen(ICfgNode stm, FullTestpath tp, FullTestpaths testpaths) throws Exception
    {
        tp.add(stm);
        if (stm instanceof EndFlagCfgNode)
        {
            testpaths.add((FullTestpath) tp.clone());
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
                        traverseCFGForBoundaryTestGen(falseNode, tp, testpaths);
                        traverseCFGForBoundaryTestGen(trueNode, tp, testpaths);
                    }
                    else
                    {
                        traverseCFGForBoundaryTestGen(falseNode, tp, testpaths);
                    }
                }
                else
                {
                    Random rand = new Random();
                    for (float i = -boundStep; i <= boundStep; i += boundStep)
                    {
                        FullTestpath tp11 = (FullTestpath) tp.clone();
                        ConditionCfgNode stm1 = (ConditionCfgNode) stm.clone();

                        tp11.remove(tp.lastIndexOf(stm));
                        stm1.setContent(stm1.getContent().replaceAll("<=|>=|<|>|!=", "=="));
                        stm1.setAst(ASTUtils.convertToIAST(stm1.getContent() + "+" + i));
                        tp11.add(stm1);
                        tp11.add(trueNode);

                        String result = this.getSolution(tp11, true);
                        for (IVariableNode variable : this.variables)
                        {
                            if (!result.contains(variable.toString()) && !result.equals(IStaticSolutionGeneration.NO_SOLUTION))
                            {
                                result += variable.toString() + "=" + rand.nextInt(100) + ";";
                            }
                        }
                        result = result.replaceAll(";;", ";");
                        if (!testCases.contains(result) && !result.equals(IStaticSolutionGeneration.NO_SOLUTION))
                        {
                            testCases.add(result);
                        }
                    }
                    traverseCFGForBoundaryTestGen(falseNode, tp, testpaths);
                    traverseCFGForBoundaryTestGen(trueNode, tp, testpaths);
                }
            }
            else
            {
                traverseCFGForBoundaryTestGen(trueNode, tp, testpaths);
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

    protected String getSolution(FullTestpath tp, boolean finalConditionType) throws Exception
    {
        IPartialTestpath tp1 = createPartialTestpath(tp, finalConditionType);

        String solution = solveTestpath(cfg.getFunctionNode(), tp1);
        return solution;
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
}
